package com.kz.pipeCutter.BBB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;

import org.zeromq.ContextFactory;
import org.zeromq.api.Context;
import org.zeromq.api.Message.Frame;
import org.zeromq.api.MessageFlag;
import org.zeromq.api.Socket;
import org.zeromq.api.SocketType;
import org.zeromq.jzmq.sockets.SocketBuilder;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;
import pb.Types.ValueType;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kz.pipeCutter.ui.PinDef;
import com.kz.pipeCutter.ui.SavableControl;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.GcodeViewer;

public class BBBHalRComp implements Runnable {
	private String halRCompUri;
	private Socket socket = null;
	private static BBBHalRComp instance;
	private Context ctx;
	private pb.Message.Container.Builder builder;
	private Thread readThread;
	HashMap<String, pb.Object.Component.Builder> components = new HashMap<>();
	HashMap<String, pb.Object.Pin.Builder> pins = new HashMap<>();
	HashMap<Integer, PinDef> pinsByHandle = new HashMap<>();
	HashMap<String, PinDef> pinsByName = new HashMap<>();
	
	HashMap<String, String> halPins = new HashMap<>();
	public boolean isAlive = true;

	static String identity;
	static {
		Random rand = new Random(23424234);
		identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());
	}

	private final ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public BBBHalRComp() {
		initSocket();
		prepareBindContainer();
		scheduler.scheduleAtFixedRate(this, 1000, 500, TimeUnit.MILLISECONDS);
		// new Thread(this).start();
		instance = this;
	}

	public static BBBHalRComp getInstance() {
		if (instance == null)
			instance = new BBBHalRComp();
		return instance;
	}

	public BBBHalRComp(String uri) {
		this.halRCompUri = uri;
		initSocket();
		prepareBindContainer();
		readThread = new Thread(this);
		readThread.start();
	}

	private void prepareBindContainer() {
		this.builder = Container.newBuilder();
		builder.setType(ContainerType.MT_HALRCOMP_BIND);

		List<SavableControl> savableControls = Settings.instance.getAllControls();
		for (SavableControl savableControl : savableControls) {
			if (savableControl.pinDef != null) {
				String pinName = savableControl.pinDef.getPinName();
				String compName = pinName.split("\\.")[0];

				pb.Object.Component.Builder comp = null;
				if (components.containsKey(compName)) {
					comp = components.get(compName);
				} else {
					comp = pb.Object.Component.newBuilder().setName(compName);
					components.put(compName, comp);
				}
				pb.Object.Pin.Builder pin = null;
				if (pins.containsKey(pinName)) {
					pin = pins.get(pinName);
				} else {
					pin = pb.Object.Pin.newBuilder().setName(pinName);
					pin.setDir(savableControl.pinDef.getPinDir());
					pin.setType(savableControl.pinDef.getPinType());
					pin.setName(savableControl.pinDef.getPinName());
					pins.put(pin.getName(), pin);
					pinsByName.put(pin.getName(), savableControl.pinDef);
				}
				comp.addPin(pin);
			}
		}

		for (pb.Object.Component.Builder comp : components.values()) {
			builder.addComp(comp);
		}

	}

	public void run() {
		// Tick once per second, pulling in arriving messages
		// for (int centitick = 0; centitick < 4; centitick++) {
		org.zeromq.api.Message msg = socket.receiveMessage();
		if (msg != null) {
			for (Frame frame : msg.getFrames()) {
				Container contReturned;
				try {
					String data = new String(frame.getData());
					if (!components.keySet().contains(data)) {
						contReturned = Message.Container.parseFrom(frame.getData());
						if (contReturned.getType().equals(
								ContainerType.MT_HALRCOMP_FULL_UPDATE)) {
							for (int i = 0; i < contReturned.getCompCount(); i++) {
								for (int j = 0; j < contReturned.getComp(i).getPinCount(); j++) {
									String value = null;
									if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_FLOAT) {
										value = Double.valueOf(
												contReturned.getComp(i).getPin(j).getHalfloat())
												.toString();
									} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_S32) {
										value = Integer.valueOf(
												contReturned.getComp(i).getPin(j).getHals32())
												.toString();
									} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_U32) {
										value = Integer.valueOf(
												contReturned.getComp(i).getPin(j).getHalu32())
												.toString();
									} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_BIT) {
										value = Boolean.valueOf(
												contReturned.getComp(i).getPin(j).getHalbit())
												.toString();
									}
									halPins.put(contReturned.getComp(i).getPin(j).getName(),
											value);
									pinsByHandle.put(contReturned.getComp(i).getPin(j).getHandle(), pinsByName.get(contReturned.getComp(i).getPin(j).getName()));
								}
							}
							GcodeViewer.setLineNumber(Integer.valueOf(
									halPins.get("mymotion.program-line")).intValue());

						} else if (contReturned.getType().equals(ContainerType.MT_PING)) {
							this.isAlive = true;
						} else if (contReturned.getType().equals(
								ContainerType.MT_HALRCOMP_INCREMENTAL_UPDATE)) {
							for (int j = 0; j < contReturned.getPinCount(); j++) {
								String value = null;
								PinDef pinDef = pinsByHandle.get(contReturned.getPin(j).getHandle());
								
								if (pinDef.getPinType() == ValueType.HAL_FLOAT) {
									value = Double.valueOf(
											contReturned.getPin(j).getHalfloat())
											.toString();
								} else if (pinDef.getPinType() == ValueType.HAL_S32) {
									value = Integer.valueOf(
											contReturned.getPin(j).getHals32())
											.toString();
								} else if (pinDef.getPinType() == ValueType.HAL_U32) {
									value = Integer.valueOf(
											contReturned.getPin(j).getHalu32())
											.toString();
								} else if (pinDef.getPinType() == ValueType.HAL_BIT) {
									value = Boolean.valueOf(
											contReturned.getPin(j).getHalbit())
											.toString();
								}
								halPins.put(pinDef.getPinName(), value);
							}
							GcodeViewer.setLineNumber(Integer.valueOf(
									halPins.get("mymotion.program-line")).intValue());
						} else {
							System.out.println(contReturned.getType().toString());
						}
					}
					// System.out.println();

				} catch (InvalidProtocolBufferException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		// Thread.currentThread().interrupt();
	}

	public void initSocket() {
		if (ctx != null && socket != null) {
			socket.close();
			ctx.close();
		}

		this.halRCompUri = Settings.getInstance().getSetting(
				"machinekit_halRCompService_url");
		ctx = ContextFactory.createContext(1);
		// Set random identity to make tracing easier

		SocketBuilder builder = ctx.buildSocket(SocketType.XSUB)
				.withIdentity(identity.getBytes()).withReceiveTimeout(200);
		socket = builder.connect(this.halRCompUri);

		// this.builder = Container.newBuilder();
		// Container cont = Container.newBuilder().setType(ContainerType.MT_PING)
		// .build();
		// byte[] buff = cont.toByteArray();
		// socket.send(buff);

	}

	public Socket getSocket() {
		return this.socket;
	}

	public void interrupt() {
		readThread.interrupt();
	}

	public static void main1(String[] args) {
		String halCmdUri = "";
		Discoverer disc = new Discoverer();
		disc.discover();
		ArrayList<ServiceInfo> al = disc.getDiscoveredServices();
		while (halCmdUri.equals("")) {
			for (ServiceInfo si : al) {
				if (si.getName().matches("HAL Rcomp.*")) {
					halCmdUri = "tcp://" + si.getServer() + ":" + si.getPort();
					break;
				}
			}
			if (!halCmdUri.equals(""))
				break;
			try {
				System.out.println("Still looking for halrcomp service.");
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// String halCmdUri = "tcp://beaglebone.local.:49155/";
		BBBHalRComp halRComp = new BBBHalRComp(halCmdUri);
		halRComp.halRCompUri = halCmdUri;
		halRComp.initSocket();
		Thread myThread = new Thread(halRComp);
		myThread.start();

	}

	public void startBind() {
		Container container = BBBHalRComp.instance.builder.build();
		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message:  " + hexOutput);
		BBBHalCommand.instance.socket.send(buff);
	}

	public void subcribe() {
		List<SavableControl> savableControls = Settings.instance.getAllControls();
		for (SavableControl savableControl : savableControls) {
			if (savableControl.pinDef != null) {
				String compName = savableControl.pinDef.getPinName().split("\\.")[0];
				String subscription = Character.toString((char) 1) + compName;
				socket.send(subscription.getBytes());
			}
		}
	}

}