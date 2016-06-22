package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.ui.Settings;

public class BBBError implements Runnable {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;
	org.zeromq.ZMQ.Socket socket = null;
	static BBBError instance = null;

	ByteArrayInputStream is;
	public ChannelExec channelExec = null;
	ZContext ctx;
	private String uri;

	static String identity;
	static {
		Random rand = new Random(23424234);
		identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());
	}

	private final static ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public BBBError() {
		initSocket();
		scheduler.scheduleAtFixedRate(this, 2000, 500, TimeUnit.MILLISECONDS);
		instance = this;
	}

	public static BBBError getInstance() {
		if (instance == null)
			instance = new BBBError();
		return instance;
	}

	public void run() {
		Container contReturned;
		while (true) {
			ZMsg receivedMessage = ZMsg.recvMsg(socket,ZMQ.DONTWAIT);
			// System.out.println("loop: " + i);
			if (receivedMessage != null) {
				while (!receivedMessage.isEmpty()) {
					ZFrame frame = receivedMessage.poll();
					byte[] returnedBytes = frame.getData();
					String messageType = new String(returnedBytes);
					if (!messageType.equals("error") && !messageType.equals("text")
							&& !messageType.equals("display")
							&& !messageType.equals("status")) {
						// System.out.println(messageType);
						try {
							contReturned = Message.Container.parseFrom(returnedBytes);
							if (!contReturned.getType().equals(ContainerType.MT_PING)) {
								Settings.instance.log(contReturned.getType() + " " + contReturned.getTopic());
								List<String> notes = contReturned.getNoteList();
								for (String note : notes) {
									Settings.instance.log("\t" + note);
								}
							}
						} catch (Exception e) {
							if (!e.getMessage().equals("Unknown message type.")) {
								e.printStackTrace();
								Settings.instance.log("Error: " + e.getMessage());
							}

						}
					}
				}
			}
			try {
				Thread.sleep(200);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		Settings sett = new Settings();
		sett.setVisible(true);
		BBBError error = new BBBError();
	}

	public void initSocket() {
		if (ctx != null && socket != null) {
			socket.close();
			ctx.close();
		}
		uri = Settings.getInstance().getSetting("machinekit_errorService_url");
		ctx = new ZContext();
		// Set random identity to make tracing easier
		socket = ctx.createSocket(ZMQ.SUB);
		socket.subscribe("display".getBytes(ZMQ.CHARSET));
		socket.subscribe("status".getBytes(ZMQ.CHARSET));
		socket.subscribe("text".getBytes(ZMQ.CHARSET));
		socket.subscribe("error".getBytes(ZMQ.CHARSET));
		socket.setReceiveTimeOut(100);
		socket.setIdentity(identity.getBytes(ZMQ.CHARSET));
		socket.connect(uri);
	}

	public org.zeromq.ZMQ.Socket getSocket() {
		return socket;
	}

}
