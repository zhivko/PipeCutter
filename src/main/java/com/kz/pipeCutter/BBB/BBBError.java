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

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;

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

	private Thread readThread;
	private long lastPingMs=0;

	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public BBBError() {
		initSocket();
		// scheduler.scheduleAtFixedRate(this, 2000, 500,
		// TimeUnit.MILLISECONDS);
		instance = this;
	}

	public static BBBError getInstance() {
		if (instance == null)
			instance = new BBBError();
		return instance;
	}

	public void run() {
		Container contReturned;
		while (!readThread.isInterrupted()) {
			try {
				ZMsg receivedMessage = ZMsg.recvMsg(socket, ZMQ.DONTWAIT);
				// System.out.println("loop: " + i);
				if (receivedMessage != null) {
					while (!receivedMessage.isEmpty()) {
						ZFrame frame = receivedMessage.poll();
						byte[] returnedBytes = frame.getData();
						String messageType = new String(returnedBytes);
						if (!messageType.equals("error") && !messageType.equals("text") && !messageType.equals("display")
								&& !messageType.equals("status")) {
							// System.out.println(messageType);

							contReturned = Message.Container.parseFrom(returnedBytes);
							if (contReturned.getType().equals(ContainerType.MT_PING)) {
								this.lastPingMs = System.currentTimeMillis();
								MachinekitSettings.instance.pingError();
							} else {
								Settings.instance.log(contReturned.getType() + " " + contReturned.getTopic());
								List<String> notes = contReturned.getNoteList();
								for (String note : notes) {
									Settings.instance.log("\t" + note);
								}
							}

						}
					}
					receivedMessage.destroy();
					receivedMessage = null;
				}
			} catch (Exception e) {
				if (!e.getMessage().equals("Error:")) {
					e.printStackTrace();
					Settings.instance.log("Error: " + e.getMessage());
				}

			}
			// try {
			// TimeUnit.MILLISECONDS.sleep(100);
			// } catch (Exception ex) {
			// ex.printStackTrace();
			// }
		}
	}

	public static void main(String[] args) {
		Settings sett = new Settings();
		sett.setVisible(true);
		BBBError error = new BBBError();
	}

	public void initSocket() {
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
		if (ctx != null && socket != null) {
			socket.close();
			ctx.close();
		}

		Random rand = new Random(23424234);
		String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());

		uri = Settings.getInstance().getSetting("machinekit_errorService_url");
		ctx = new ZContext(2);
		// Set random identity to make tracing easier
		socket = ctx.createSocket(ZMQ.SUB);
		socket.subscribe("display".getBytes(ZMQ.CHARSET));
		socket.subscribe("status".getBytes(ZMQ.CHARSET));
		socket.subscribe("text".getBytes(ZMQ.CHARSET));
		socket.subscribe("error".getBytes(ZMQ.CHARSET));
		socket.subscribe("config".getBytes(ZMQ.CHARSET));
		socket.setReceiveTimeOut(200);
		socket.setSendTimeOut(200);
		socket.setIdentity(identity.getBytes(ZMQ.CHARSET));
		socket.connect(uri);

		readThread = new Thread(this);
		readThread.setName("BBBError");
		readThread.start();
	}

	public org.zeromq.ZMQ.Socket getSocket() {
		return socket;
	}
	
	public boolean isAlive()
	{
		if(this.lastPingMs!=0)
			return (System.currentTimeMillis()-this.lastPingMs > 1000);
		else
			return false;
	}

}
