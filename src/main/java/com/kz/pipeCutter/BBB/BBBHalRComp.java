package com.kz.pipeCutter.BBB;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;
import pb.Types.ValueType;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kz.pipeCutter.ui.SavableControl;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.GcodeViewer;

public class BBBHalRComp implements Runnable {
	Random rand = new Random(23424234);
	private String halRCompUri;
	private static Socket client = null;
	private ZContext ctx;
	private pb.Message.Container.Builder builder;
	private Thread readThread;

	HashMap<String, String> halPins = new HashMap<>();

	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public BBBHalRComp() {
		this.halRCompUri = Settings.getInstance().getSetting(
				"machinekit_halRCompService_url");
		getClient();
		enumerateRemotePins();
		new Thread(this).start();
	}

	public BBBHalRComp(String uri) {
		this.halRCompUri = uri;
		getClient();
		enumerateRemotePins();
		readThread = new Thread(this);
		readThread.start();
	}

	private void enumerateRemotePins() {
		this.builder = Container.newBuilder();
		builder.setType(ContainerType.MT_HALGROUP_BIND);

		HashMap<String, pb.Object.Component.Builder> components = new HashMap<>();
		HashMap<String, pb.Object.Pin.Builder> pins = new HashMap<>();

		List<SavableControl> savableControls = Settings.instance.getAllControls();
		for (SavableControl savableControl : savableControls) {
			String pinName = savableControl.getPinName();
			if (pinName != null) {
				String compName = pinName.split("\\.")[0];

				pb.Object.Component.Builder comp = null;
				if (components.containsKey(compName)) {
					comp = components.get(compName);
				} else {
					comp = pb.Object.Component.newBuilder().setName(compName);
					components.put(compName, comp);
				}

//				pb.Object.Pin.Builder pin = null;
//				if (pins.containsKey(pinName)) {
//					pin = pins.get(pinName);
//				} else {
//					pin = pb.Object.Pin.newBuilder().setName(pinName);
//				}
//
//				comp.addPin(pin);
			}
		}

		for (pb.Object.Component.Builder comp : components.values()) {
			builder.addComp(comp);
		}

	}

	public void run() {
		Container container = this.builder.build();
		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		client.send(buff, 0);

		PollItem[] items = new PollItem[] { new PollItem(client, Poller.POLLIN) };

		while (!Thread.currentThread().isInterrupted()) {
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
							halPins.put(contReturned.getComp(i).getPin(j).getName(), value);
						}
					}
					// System.out.println();
					GcodeViewer.setLineNumber(Integer.valueOf(
							halPins.get("motion.program-line")).intValue());
					
				} catch (InvalidProtocolBufferException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				msg.destroy();
				// Thread.currentThread().interrupt();
			}
		}
	}

	private void getClient() {
		ctx = new ZContext();
		client = ctx.createSocket(ZMQ.XSUB);
		// Set random identity to make tracing easier
		String identity = String
				.format("%04X-%04X", rand.nextInt(), rand.nextInt());
		client.setIdentity(identity.getBytes(ZMQ.CHARSET));
		client.setLinger(0);
		//client.subscribe("motion".getBytes(ZMQ.CHARSET));
		client.connect(this.halRCompUri);
	}

	public void interrupt() {
		readThread.interrupt();
	}
}