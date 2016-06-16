package com.kz.pipeCutter.BBB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kz.pipeCutter.ui.SavableControl;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.GcodeViewer;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;
import pb.Types.ValueType;

public class BBBHalCommand implements Runnable {
	Random rand = new Random(23424234);
	private String halCmdUri;
	private static Socket client = null;
	private ZContext ctx;

	HashMap<String, String> halPin = new HashMap<>();

	private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public BBBHalCommand() {
		this.halCmdUri = Settings.getInstance().getSetting("machinekit_halCmdService_url");
		getClient();
		//scheduler.scheduleAtFixedRate(this, 1000, 500, TimeUnit.MILLISECONDS);
	}

	public BBBHalCommand(String uri) {
		this.halCmdUri = uri;
		getClient();
		//scheduler.scheduleAtFixedRate(this, 1000, 500, TimeUnit.MILLISECONDS);
	}	
	
	public static void main(String[] args) {
		String halCmdUri = "";
		Discoverer disc = new Discoverer();
		disc.discover();
		ArrayList<ServiceInfo> al = disc.getDiscoveredServices();
		while (halCmdUri.equals("")) {
			for (ServiceInfo si : al) {
				if (si.getName().matches("HAL Rcommand.*")) {
					halCmdUri = "tcp://" + si.getServer() + ":" + si.getPort() + "/";
					break;
				}
			}
			if (!halCmdUri.equals(""))
				break;
			try {
				System.out.println("Still looking for halcmd service.");
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// String halCmdUri = "tcp://beaglebone.local.:49155/";
		BBBHalCommand halGroup = new BBBHalCommand();
		halGroup.halCmdUri = halCmdUri;
		halGroup.getClient();
		Thread myThread = new Thread(halGroup);
		myThread.start();

	}

	public void run() {
		pb.Message.Container.Builder builder = Container.newBuilder();

		builder.setType(ContainerType.MT_HALRCOMMAND_DESCRIBE);
		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		//System.out.println("Message: " + hexOutput);
		client.send(buff, 0);

		PollItem[] items = new PollItem[] { new PollItem(client, Poller.POLLIN) };

		// while (!Thread.currentThread().isInterrupted()) {
		// Tick once per second, pulling in arriving messages
		// for (int centitick = 0; centitick < 4; centitick++) {
		ZMQ.poll(items, 10);
		if (items[0].isReadable()) {
			ZMsg msg = ZMsg.recvMsg(client);

			Container contReturned;
			try {
				contReturned = Message.Container.parseFrom(msg.getFirst().getData());
				for (int i = 0; i < contReturned.getCompCount(); i++) {
					for (int j = 0; j < contReturned.getComp(i).getPinCount(); j++) {
						String value = null;
						if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_FLOAT) {
							value = Double.valueOf(contReturned.getComp(i).getPin(j).getHalfloat()).toString();
						} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_S32) {
							value = Integer.valueOf(contReturned.getComp(i).getPin(j).getHals32()).toString();
						} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_U32) {
							value = Integer.valueOf(contReturned.getComp(i).getPin(j).getHalu32()).toString();
						} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_BIT) {
							value = Boolean.valueOf(contReturned.getComp(i).getPin(j).getHalbit()).toString();
						}
						halPin.put(contReturned.getComp(i).getPin(j).getName(), value);
					}
				}
				//System.out.println();
				GcodeViewer.setLineNumber(Integer.valueOf(halPin.get("motion.program-line")).intValue());
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			msg.destroy();
			//Thread.currentThread().interrupt();
		}
		// }
	}

	private void getClient() {
		ctx = new ZContext();
		client = ctx.createSocket(ZMQ.DEALER);

		// Set random identity to make tracing easier
		String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());
		client.setIdentity(identity.getBytes(ZMQ.CHARSET));
		client.connect(this.halCmdUri);
	}
}