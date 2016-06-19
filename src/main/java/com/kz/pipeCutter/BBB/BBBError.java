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

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.ui.Settings;

public class BBBError implements Runnable {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;
	Socket socket = null;
	static BBBError instance = null;

	ByteArrayInputStream is;
	public ChannelExec channelExec = null;
	Context ctx;
	
	static String identity;
	static
	{
		Random rand = new Random(23424234);
		identity = String
				.format("%04X-%04X", rand.nextInt(), rand.nextInt());
	}
	

	
	
	private final static ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public BBBError()
	{
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
		try {
			Container contReturned;
			org.zeromq.api.Message receivedMessage = socket
					.receiveMessage(MessageFlag.DONT_WAIT);
			int i = 0;
			while (receivedMessage != null) {
				// System.out.println("loop: " + i);
				for (Frame f : receivedMessage.getFrames()) {
					byte[] returnedBytes = f.getData();
					String messageType = new String(returnedBytes);
					if (!messageType.equals("error") && !messageType.equals("text")
							&& !messageType.equals("display")
							&& !messageType.equals("status")) {
						// System.out.println(messageType);
						contReturned = Message.Container.parseFrom(returnedBytes);
						if (!contReturned.getType().equals(ContainerType.MT_PING)) {
							Settings.instance.log(contReturned.toString());
							List<String> notes = contReturned.getNoteList();
							for (String note : notes) {
								System.out.println("\t" + note);
							}
						}
					}
				}
				receivedMessage = socket.receiveMessage();
				i++;
			}

		} catch (Exception e) {
			if (!e.getMessage().equals("Unknown message type.")) {
				e.printStackTrace();
				Settings.instance.log("Error: " + e.getMessage());
			}
		}
	}

	public static void main(String[] args) {

		Settings sett = new Settings();
		sett.setVisible(true);
		BBBError error = new BBBError();
	}

	public void initSocket() {
		if(ctx!=null && socket!=null)
		{
			socket.close();
			ctx.close();
		}
		String errorUrl = Settings.getInstance().getSetting(
				"machinekit_errorService_url");
		ctx = ContextFactory.createContext(1);
		// Set random identity to make tracing easier
		
		SocketBuilder builder = ctx.buildSocket(SocketType.SUB).asSubscribable()
				.subscribe("error".getBytes());
		builder.asSubscribable().subscribe("display".getBytes());
		builder.asSubscribable().subscribe("status".getBytes());
		builder.asSubscribable().subscribe("text".getBytes());
		builder.withIdentity(identity.getBytes());
		builder.withReceiveTimeout(3000);
		socket = builder.connect(errorUrl);
	}

	public Socket getSocket()
	{
		return socket;
	}
	
}
