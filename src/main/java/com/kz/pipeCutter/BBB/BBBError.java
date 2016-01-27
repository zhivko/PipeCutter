package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.google.protobuf.ByteString;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.ui.CommandPanel;
import com.kz.pipeCutter.ui.Settings;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;

public class BBBError {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;
	static Socket errorSocket = null;
	static BBBError instance = null;
	public static ZMQ.Poller items = null;

	ByteArrayInputStream is;
	private JSch jsch;
	public ChannelExec channelExec = null;
	private Session session;
	private PrintStream ps;

	private static int ticket = 5000;
	private final static ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public BBBError() {
		Runnable errorReporter = new Runnable() {
			@Override
			public void run() {
				try {
					Container contReturned;
					ZMsg receivedMessage = ZMsg.recvMsg(getErrorSocket());
					int i = 0;
					while (receivedMessage != null) {
						// System.out.println("loop: " + i);
						for (ZFrame f : receivedMessage) {
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
						receivedMessage = ZMsg.recvMsg(getErrorSocket());
						i++;
					}

				} catch (Exception e) {
					if (!e.getMessage().equals("Unknown message type."))
						e.printStackTrace();
				}
			}
		};
		scheduler.scheduleAtFixedRate(errorReporter, 0, 5, TimeUnit.SECONDS);
		// scheduler.schedule(errorReporter, 0, TimeUnit.SECONDS);
		instance = this;

	}

	public static void main(String[] args) {

		Settings sett = new Settings();
		sett.setVisible(true);
		getErrorSocket();

		BBBStatus error = new BBBStatus();

	}

	private static Socket getErrorSocket() {
		if (errorSocket != null)
			return errorSocket;

		String errorUrl = Settings.getInstance().getSetting(
				"machinekit_errorService_url");

		Context con = ZMQ.context(2);
		errorSocket = con.socket(ZMQ.SUB);
		errorSocket.setReceiveTimeOut(2000);
		errorSocket.setLinger(20);
		errorSocket.setHWM(0);
		errorSocket.connect(errorUrl);

		errorSocket.subscribe("error".getBytes());
		errorSocket.subscribe("text".getBytes());
		errorSocket.subscribe("display".getBytes());
		errorSocket.setLinger(10);
		
		return errorSocket;
	}

	// public void ping() throws Exception {
	// System.out.println(new Object() {
	// }.getClass().getEnclosingMethod().getName());
	//
	// byte[] buff;
	// Container container =
	// Container.newBuilder().setType(Types.ContainerType.MT_PING).setTicket(ticket++).build();
	// buff = container.toByteArray();
	// String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
	// System.out.println("Mesage: " + hexOutput);
	// getCommandSocket().send(buff);
	//
	// parseAndOutput();
	// }

}
