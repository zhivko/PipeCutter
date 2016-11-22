package com.kz.pipeCutter.BBB;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.kz.pipeCutter.BBB.commands.MachinekitRunPostgui;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;
import pb.Types.ValueType;

public class BBBHalCommand implements Runnable {
	Random rand = new Random(23424234);
	private String socketUri;
	public Socket socket = null;
	private ZContext ctx;
	boolean shouldRead;
	boolean pingThreadShouldEnd;

	public static BBBHalCommand instance;

	HashMap<String, String> halPin = new HashMap<>();

	private Thread readThread;
	private Thread pingThread;
	private long lastPingMs = 0;
	private boolean shouldPing = true;
	private long pingDelay = 1000;

	static String identity;
	static {
		Random rand = new Random(23424234);
		identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());
	}

	public BBBHalCommand() {
		this.socketUri = Settings.getInstance().getSetting("machinekit_halCmdService_url");
		initSocket();
		instance = this;
	}

	public static BBBHalCommand getInstance() {
		if (instance == null)
			instance = new BBBHalCommand();
		return instance;
	}

	public void requestDescribe() {
		pb.Message.Container.Builder builder = Container.newBuilder();
		builder.setType(ContainerType.MT_HALRCOMMAND_DESCRIBE);
		Container container = builder.build();

		byte[] buff = container.toByteArray();
		// String hexOutput = javax.xml.bind.DatatypeConverter
		// .printHexBinary(buff);
		// System.out.println("Message: " + hexOutput);
		socket.send(buff, 0);
	}

	public BBBHalCommand(String uri) {
		this.socketUri = uri;
		getSocket();
		// scheduler.scheduleAtFixedRate(this, 1000, 500,
		// TimeUnit.MILLISECONDS);
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
		halCmd.socketUri = halCmdUri;
		halCmd.getSocket();
		Thread myThread = new Thread(halCmd);
		myThread.start();

	}

	public void run() {
		// while (!Thread.currentThread().isInterrupted()) {
		// Tick once per second, pulling in arriving messages
		// for (int centitick = 0; centitick < 4; centitick++) {
		shouldRead = true;
		PollItem[] pollItems = new PollItem[] { new PollItem(socket, Poller.POLLIN) };
		while (shouldRead) {

			int rc = ZMQ.poll(pollItems,1, 100);
			// System.out.println("loop: " + i);
			for (int l = 0; l < rc; l++) {
				ZMsg msg = ZMsg.recvMsg(socket);
				try {
					ZFrame frame = null;
					while (pollItems[0].isReadable() && (frame = msg.poll()) != null) {

						byte[] returnedBytes = frame.getData();
						Container contReturned = Message.Container.parseFrom(returnedBytes);
						if (contReturned.getType().equals(ContainerType.MT_HALRCOMMAND_DESCRIPTION)) {
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
						} else if (contReturned.getType().equals(ContainerType.MT_PING_ACKNOWLEDGE)) {
							this.lastPingMs = System.currentTimeMillis();
							MachinekitSettings.instance.pingHalCommand();
							if (BBBStatus.getInstance().isAlive() && !BBBHalRComp.getInstance().isBinded && !BBBHalRComp.getInstance().isTryingToBind) {
								// if (BBBHalRComp.getInstance().isAlive())
								BBBHalRComp.getInstance().startBind();
							}
						} else if (contReturned.getType().equals(ContainerType.MT_HALRCOMP_BIND_CONFIRM)) {
							Settings.instance.log("MT_HALRCOMP_BIND_CONFIRM");
							BBBHalRComp.getInstance().isBinded = true;
							BBBHalRComp.getInstance().isTryingToBind = false;
							BBBHalRComp.getInstance().subcribe();

							for (int i = 0; i < contReturned.getCompCount(); i++) {
								for (int j = 0; j < contReturned.getComp(i).getPinCount(); j++) {
									BBBHalRComp.instance.pinsByHandle.put(contReturned.getComp(i).getPin(j).getHandle(),
											BBBHalRComp.instance.pinsByName.get(contReturned.getComp(i).getPin(j).getName()));
								}
							}
							new MachinekitRunPostgui().start();
							Settings.instance.updateHalValues();
							Settings.instance.setLaser1IP();
						} else {
							Settings.getInstance().log("Unknown message: " + contReturned.getType());
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				msg.destroy();
			}
		}

	}

	public void initSocket() {
		if (readThread != null && readThread.isAlive()) {
			shouldRead = false;
			while (readThread.isAlive()) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (pingThread != null && pingThread.isAlive()) {
			pingThreadShouldEnd = true;
			while (pingThread.isAlive()) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (ctx != null && socket != null) {
			ctx.destroySocket(socket);
			ctx.destroy();
		}

		ctx = new ZContext();
		// Set random identity to make tracing easier
		socket = ctx.createSocket(ZMQ.DEALER);
		socket.setIdentity(identity.getBytes());
		socket.setReceiveTimeOut(15);
		socket.setSendTimeOut(1000);
		socket.connect(this.socketUri);

		readThread = new Thread(this);
		readThread.setName("BBBHalCommand");
		readThread.start();

		startPingThread();
	}

	private void startPingThread() {
		pingThreadShouldEnd = false;
		pingThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (!pingThreadShouldEnd) {
					if (BBBHalCommand.this.shouldPing) {
						pb.Message.Container.Builder builder = Container.newBuilder();
						builder.setType(ContainerType.MT_PING);
						Container container = builder.build();
						byte[] buff = container.toByteArray();

						// String hexOutput =
						// javax.xml.bind.DatatypeConverter.printHexBinary(buff);
						// System.out.println("PING Message: " + hexOutput);

						socket.send(buff, 0);
					}
					try {
						Thread.sleep(pingDelay);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		});
		pingThread.start();
	}

	public Socket getSocket() {
		return socket;
	}

	public boolean isAlive() {
		if (this.lastPingMs != 0)
			if ((System.currentTimeMillis() - this.lastPingMs) < 3000)
				return true;
			else
				return false;
		else
			return false;
	}

	public void stopPing() {
		shouldPing = false;
	}

	public void startPing() {
		shouldPing = true;
	}

	public void stop() {
		shouldRead = false;
		pingThreadShouldEnd = true;
	}
}