package com.kz.pipeCutter.BBB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.GcodeViewer;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;
import pb.Types.ValueType;

public class BBBHalCommand implements Runnable {
	Random rand = new Random(23424234);
	private String socketUri;
	public Socket socket = null;
	private ZContext ctx;

	public static BBBHalCommand instance;

	HashMap<String, String> halPin = new HashMap<>();

	private Thread readThread;

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

	protected void requestDescribe() {
		pb.Message.Container.Builder builder = Container.newBuilder();
		builder.setType(ContainerType.MT_HALRCOMMAND_DESCRIBE);
		Container container = builder.build();

		byte[] buff = container.toByteArray();
		// String hexOutput = javax.xml.bind.DatatypeConverter
		// .printHexBinary(buff);
		// System.out.println("Message: " + hexOutput);
		socket.send(buff);
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
		while (true) {
			try {
				ZMsg receivedMessage = ZMsg.recvMsg(socket, ZMQ.DONTWAIT);
				// System.out.println("loop: " + i);
				if (receivedMessage != null) {
					while (!receivedMessage.isEmpty()) {

						ZFrame frame = receivedMessage.poll();
						byte[] returnedBytes = frame.getData();
						Container contReturned = Message.Container.parseFrom(returnedBytes);
						if (contReturned.getType().equals(ContainerType.MT_HALRCOMMAND_DESCRIPTION)) {
							for (int i = 0; i < contReturned.getCompCount(); i++) {
								for (int j = 0; j < contReturned.getComp(i).getPinCount(); j++) {
									String value = null;
									if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_FLOAT) {
										value = Double.valueOf(contReturned.getComp(i).getPin(j).getHalfloat())
												.toString();
									} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_S32) {
										value = Integer.valueOf(contReturned.getComp(i).getPin(j).getHals32())
												.toString();
									} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_U32) {
										value = Integer.valueOf(contReturned.getComp(i).getPin(j).getHalu32())
												.toString();
									} else if (contReturned.getComp(i).getPin(j).getType() == ValueType.HAL_BIT) {
										value = Boolean.valueOf(contReturned.getComp(i).getPin(j).getHalbit())
												.toString();
									}
									halPin.put(contReturned.getComp(i).getPin(j).getName(), value);
								}
							}
						}
					}
					if (halPin.get("motion.program-line") != null)
						GcodeViewer.setLineNumber(Integer.valueOf(halPin.get("motion.program-line")).intValue());
					try {
						TimeUnit.MILLISECONDS.sleep(400);
						// requestDescribe();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}					
					requestDescribe();
				}
			} catch (Exception e) {
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
		if (readThread != null && readThread.isAlive())
			readThread.interrupt();

		ctx = new ZContext();
		// Set random identity to make tracing easier
		socket = ctx.createSocket(ZMQ.DEALER);
		socket.setIdentity(identity.getBytes(ZMQ.CHARSET));
		socket.setReceiveTimeOut(100);
		socket.connect(this.socketUri);

		readThread = new Thread(this);
		readThread.setName("BBBHalCommand");
		readThread.start();

		requestDescribe();
	}

	public Socket getSocket() {
		return socket;
	}
}