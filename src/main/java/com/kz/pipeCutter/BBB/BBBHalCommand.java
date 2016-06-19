package com.kz.pipeCutter.BBB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;

import org.zeromq.ContextFactory;
import org.zeromq.api.MessageFlag;
import org.zeromq.api.Socket;
import org.zeromq.api.SocketType;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;
import pb.Types.ValueType;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.GcodeViewer;

public class BBBHalCommand implements Runnable {
	Random rand = new Random(23424234);
	private String halCmdUri;
	public Socket socket = null;
	private org.zeromq.api.Context ctx;

	public static BBBHalCommand instance;

	HashMap<String, String> halPin = new HashMap<>();

	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(2);

	static String identity;
	static {
		Random rand = new Random(23424234);
		identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());
	}

	public BBBHalCommand() {
		this.halCmdUri = Settings.getInstance().getSetting(
				"machinekit_halCmdService_url");
		initSocket();
		// socket reader
		scheduler.scheduleAtFixedRate(this, 2000, 500, TimeUnit.MILLISECONDS);
		//requestDescribe();
		//scheduleHalDescribe();
		instance = this;
	}

	public static BBBHalCommand getInstance() {
		if (instance == null)
			instance = new BBBHalCommand();
		return instance;
	}

	private void scheduleHalDescribe() {
		// socket hal describe sender
		scheduler.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				requestDescribe();

			}
		}, 5000, 2000, TimeUnit.MILLISECONDS);
	}

	protected void requestDescribe() {
		pb.Message.Container.Builder builder = Container.newBuilder();
		builder.setType(ContainerType.MT_HALRCOMMAND_DESCRIBE);
		Container container = builder.build();

		byte[] buff = container.toByteArray();
		//String hexOutput = javax.xml.bind.DatatypeConverter
		//		.printHexBinary(buff);
		// System.out.println("Message: " + hexOutput);
		socket.send(buff);
	}

	public BBBHalCommand(String uri) {
		this.halCmdUri = uri;
		getSocket();
		// scheduler.scheduleAtFixedRate(this, 1000, 500, TimeUnit.MILLISECONDS);
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
		BBBHalCommand halCmd = new BBBHalCommand(halCmdUri);
		halCmd.halCmdUri = halCmdUri;
		halCmd.getSocket();
		Thread myThread = new Thread(halCmd);
		myThread.start();

	}

	public void run() {
		// while (!Thread.currentThread().isInterrupted()) {
		// Tick once per second, pulling in arriving messages
		// for (int centitick = 0; centitick < 4; centitick++) {
		byte[] received = socket.receive();
		if (received != null) {
			Container contReturned;
			try {
				contReturned = Message.Container.parseFrom(received);
				if (contReturned.getType().equals(
						ContainerType.MT_HALRCOMMAND_DESCRIPTION)) {
					for (int i = 0; i < contReturned.getCompCount(); i++) {
						for (int j = 0; j < contReturned.getComp(i).getPinCount(); j++) {
							String value = null;
							if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_FLOAT) {
								value = Double.valueOf(
										contReturned.getComp(i).getPin(j).getHalfloat()).toString();
							} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_S32) {
								value = Integer.valueOf(
										contReturned.getComp(i).getPin(j).getHals32()).toString();
							} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_U32) {
								value = Integer.valueOf(
										contReturned.getComp(i).getPin(j).getHalu32()).toString();
							} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_BIT) {
								value = Boolean.valueOf(
										contReturned.getComp(i).getPin(j).getHalbit()).toString();
							}
							halPin.put(contReturned.getComp(i).getPin(j).getName(), value);
						}
					}
					//GcodeViewer.setLineNumber(Integer.valueOf(
					//		halPin.get("motion.program-line")).intValue());
					requestDescribe();
				} else if (contReturned.getType().equals(
						ContainerType.MT_HALRCOMP_BIND_CONFIRM)) {
					Settings.instance.log(contReturned.getType().toString() + "\n");
					BBBHalRComp.getInstance().subcribe();
				} else {
					Settings.instance.log(contReturned.getType().toString() + "\n");
				}
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void initSocket() {
		if (ctx != null && socket != null) {
			socket.close();
			ctx.close();
		}
		ctx = ContextFactory.createContext(1);
		// Set random identity to make tracing easier
		socket = ctx.buildSocket(SocketType.DEALER)
				.withIdentity(identity.getBytes()).withReceiveTimeout(100).connect(this.halCmdUri);
	}

	public Socket getSocket() {
		return socket;
	}
}