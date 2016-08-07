package com.kz.pipeCutter.BBB;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.kz.pipeCutter.ui.IHasPinDef;
import com.kz.pipeCutter.ui.PinDef;
import com.kz.pipeCutter.ui.SavableControl;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.GcodeViewer;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;
import pb.Types.ValueType;

public class BBBHalRComp implements Runnable {
	private String halRCompUri;
	private Socket socket = null;
	private static BBBHalRComp instance;
	private ZContext ctx;
	private pb.Message.Container.Builder builder;
	private Thread readThread;
	HashMap<String, pb.Object.Component.Builder> components = new HashMap<>();
	HashMap<String, pb.Object.Pin.Builder> pins = new HashMap<>();
	HashMap<Integer, PinDef> pinsByHandle = new HashMap<>();
	HashMap<String, PinDef> pinsByName = new HashMap<>();

	HashMap<String, String> halPins = new HashMap<>();

	public boolean isBinded = false;
	public boolean isTryingToBind = false;
	private long lastPingMs;

	public BBBHalRComp() {
		prepareBindContainer();
		initSocket();
		// scheduler.scheduleAtFixedRate(new Thread(this), 1000, 1000,
		// TimeUnit.MILLISECONDS);
		instance = this;
	}

	public static BBBHalRComp getInstance() {
		if (instance == null)
			instance = new BBBHalRComp();
		return instance;
	}

	public BBBHalRComp(String uri) {
		this.halRCompUri = uri;
		prepareBindContainer();
		initSocket();
		readThread = new Thread(this);
		readThread.start();
	}

	private void prepareBindContainer() {
		this.builder = Container.newBuilder();
		builder.setType(ContainerType.MT_HALRCOMP_BIND);

		List<IHasPinDef> savableControls = Settings.getInstance().getAllPinControls();
		for (IHasPinDef savableControl : savableControls) {
			if (savableControl.getPin() != null) {
				String pinName = savableControl.getPin().getPinName();
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
					pin.setDir(savableControl.getPin().getPinDir());
					pin.setType(savableControl.getPin().getPinType());
					pin.setName(savableControl.getPin().getPinName());
					pins.put(pin.getName(), pin);
					pinsByName.put(pin.getName(), savableControl.getPin());
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
		// System.out.println(Thread.currentThread().getName());
		while (!readThread.isInterrupted()) {
			ZMsg receivedMessage = ZMsg.recvMsg(socket, ZMQ.DONTWAIT);
			// System.out.println("loop: " + i);
			if (receivedMessage != null) {
				while (!receivedMessage.isEmpty()) {
					ZFrame frame = receivedMessage.poll();
					Container contReturned;
					try {
						String data = new String(frame.getData());
						if (!components.keySet().contains(data)) {
							contReturned = Message.Container.parseFrom(frame.getData());
							if (contReturned.getType().equals(ContainerType.MT_HALRCOMP_FULL_UPDATE)) {
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
										halPins.put(contReturned.getComp(i).getPin(j).getName(), value);
										pinsByHandle.put(contReturned.getComp(i).getPin(j).getHandle(),
												pinsByName.get(contReturned.getComp(i).getPin(j).getName()));
									}
								}
								GcodeViewer.instance.setLineNumber(Integer.valueOf(halPins.get("mymotion.program-line")).intValue());
								GcodeViewer.instance.setPlasmaOn(Boolean.valueOf(halPins.get("mymotion.spindle-on")).booleanValue());
							} else if (contReturned.getType().equals(ContainerType.MT_PING)) {
								this.lastPingMs = System.currentTimeMillis();
								MachinekitSettings.instance.pingHalRcomp();
							} else if (contReturned.getType().equals(ContainerType.MT_HALRCOMP_INCREMENTAL_UPDATE)) {
								for (int j = 0; j < contReturned.getPinCount(); j++) {
									String value = null;
									PinDef pinDef = pinsByHandle.get(contReturned.getPin(j).getHandle());

									if (pinDef.getPinType() == ValueType.HAL_FLOAT) {
										value = Double.valueOf(contReturned.getPin(j).getHalfloat()).toString();
									} else if (pinDef.getPinType() == ValueType.HAL_S32) {
										value = Integer.valueOf(contReturned.getPin(j).getHals32()).toString();
									} else if (pinDef.getPinType() == ValueType.HAL_U32) {
										value = Integer.valueOf(contReturned.getPin(j).getHalu32()).toString();
									} else if (pinDef.getPinType() == ValueType.HAL_BIT) {
										value = Boolean.valueOf(contReturned.getPin(j).getHalbit()).toString();
									}
									halPins.put(pinDef.getPinName(), value);
								}
								GcodeViewer.instance.setLineNumber(Integer.valueOf(halPins.get("mymotion.program-line")).intValue());
								GcodeViewer.instance.setPlasmaOn(Boolean.valueOf(halPins.get("mymotion.spindle-on")).booleanValue());
							} else {
								System.out.println(contReturned.getType().toString());
							}
						}
						// System.out.println();

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					// try {
					// TimeUnit.MILLISECONDS.sleep(100);
					// } catch (InterruptedException e) {
					// // TODO Auto-generated catch block
					// e.printStackTrace();
					// }
				}
				receivedMessage.destroy();
				receivedMessage = null;
			}
		}
	}

	// Thread.currentThread().interrupt();

	public void initSocket() {
		if (ctx != null && socket != null) {
			socket.close();
			ctx.close();
		}
		if (readThread != null && readThread.isAlive()) {
			readThread.interrupt();
			while (readThread.isAlive()) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		this.halRCompUri = Settings.getInstance().getSetting("machinekit_halRCompService_url");
		ctx = new ZContext(2);
		// Set random identity to make tracing easier
		socket = ctx.createSocket(ZMQ.XSUB);

		Random rand = new Random(23424234);
		String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());

		socket.setIdentity(identity.getBytes(ZMQ.CHARSET));
		// socket.setReceiveTimeOut(1000);
		// socket.setRcvHWM(10000);
		socket.connect(this.halRCompUri);

		readThread = new Thread(this);
		readThread.setName("BBBHalRComp");
		readThread.start();
		
		isBinded = false;
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

	}

	public void startBind() {
		this.isTryingToBind = true;
		Container container = this.builder.build();
		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message:  " + hexOutput);
		BBBHalCommand.instance.socket.send(buff);
	}

	public void subcribe() {
		List<IHasPinDef> savableControls = Settings.getInstance().getAllPinControls();
		for (IHasPinDef savableControl : savableControls) {
			if (savableControl.getPin() != null) {
				String compName = savableControl.getPin().getPinName().split("\\.")[0];
				String subscription = Character.toString((char) 1) + compName;
				socket.send(subscription.getBytes());
			}
		}
	}

	public boolean isAlive() {
		if (this.lastPingMs != 0)
			return (System.currentTimeMillis() - this.lastPingMs > 1000);
		else
			return false;
	}

	private void prepareUpdateContainer() {
		this.builder = Container.newBuilder();
		builder.setType(ContainerType.MT_HALRCOMP_SET);

		List<IHasPinDef> savableControls = Settings.getInstance().getAllPinControls();
		for (IHasPinDef savableControl : savableControls) {
			if (savableControl.getPin() != null) {
				String pinName = savableControl.getPin().getPinName();
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
					pin.setDir(savableControl.getPin().getPinDir());
					pin.setType(savableControl.getPin().getPinType());
					pin.setName(savableControl.getPin().getPinName());
					pins.put(pin.getName(), pin);
					pinsByName.put(pin.getName(), savableControl.getPin());
				}
				comp.addPin(pin);
			}
		}
	}
	

}
