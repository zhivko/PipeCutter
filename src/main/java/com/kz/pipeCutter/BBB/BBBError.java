package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.omg.CORBA.ShortSeqHolder;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
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
	Socket socket = null;
	public static BBBError instance = null;

	ByteArrayInputStream is;
	public ChannelExec channelExec = null;
	ZContext ctx;
	private String uri;

	static String identity;

	private Thread readThread;
	private long lastPingMs = 0;
	private boolean shouldRead = false;

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
		while (shouldRead) {
			PollItem[] pollItems = new PollItem[] { new PollItem(socket, Poller.POLLIN) };
			int rc = ZMQ.poll(pollItems,1, 100);
			// System.out.println("loop: " + i);
			for (int l = 0; l < rc; l++) {
				ZMsg msg = ZMsg.recvMsg(socket,ZMQ.DONTWAIT);
				try {
					ZFrame frame = null;
					while (pollItems[0].isReadable() && (frame = msg.poll()) != null) {
						byte[] returnedBytes = frame.getData();
						String messageType = new String(returnedBytes);
						// System.out.println(messageType);
						if (!messageType.equals("error") && !messageType.equals("text") && !messageType.equals("display") && !messageType.equals("status")) {
							// System.out.println(messageType);
							contReturned = Message.Container.parseFrom(returnedBytes);
							if (contReturned.getType().equals(ContainerType.MT_PING)) {
								this.lastPingMs = System.currentTimeMillis();
								MachinekitSettings.instance.pingError();
							} else {
								Settings.getInstance().log(contReturned.getType() + " " + contReturned.getTopic());
								List<String> notes = contReturned.getNoteList();
								for (String note : notes) {
									Settings.getInstance().log("\t" + note);
								}
							}
						}
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				msg.destroy();
			}
//			try {
//				Thread.sleep(100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
		}

	}

	public static void main(String[] args) {
		Settings sett = Settings.getInstance();
		sett.setVisible(true);
		BBBError error = new BBBError();
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
		if (ctx != null && socket != null) {
			ctx.destroy();
			// socket.close();
		}

		Random rand = new Random(23424234);
		String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());

		uri = Settings.getInstance().getSetting("machinekit_errorService_url");
		ctx = new ZContext();
		// Set random identity to make tracing easier
		System.out.println("PID: " + ManagementFactory.getRuntimeMXBean().getName());
		socket = ctx.createSocket(ZMQ.SUB);
		socket.subscribe("display".getBytes());
		socket.subscribe("status".getBytes());
		socket.subscribe("text".getBytes());
		socket.subscribe("error".getBytes());
		socket.subscribe("config".getBytes());
		socket.setReceiveTimeOut(5);
		socket.setSendTimeOut(1000);
		socket.setIdentity(identity.getBytes());
		socket.connect(uri);

		shouldRead = true;
		readThread = new Thread(this);
		readThread.setName("BBBError");
		readThread.start();
	}

	public org.zeromq.ZMQ.Socket getSocket() {
		return socket;
	}

	public boolean isAlive() {
		if (this.lastPingMs != 0)
			return (System.currentTimeMillis() - this.lastPingMs > 1000);
		else
			return false;
	}

	public void stop() {
		// TODO Auto-generated method stub
		shouldRead = false;
	}
}
