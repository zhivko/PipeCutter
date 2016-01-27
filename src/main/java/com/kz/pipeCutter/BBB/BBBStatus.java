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

public class BBBStatus {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;
	static Socket statusSocket = null;
	static BBBStatus instance = null;
	public static ZMQ.Poller items = null;

	ByteArrayInputStream is;
	private JSch jsch;
	public ChannelExec channelExec = null;
	private Session session;
	private PrintStream ps;

	private static int ticket = 5000;
	private final static ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public BBBStatus() {
		Runnable errorReporter = new Runnable() {
			@Override
			public void run() {
				try {
					Container contReturned;
					ZMsg receivedMessage = ZMsg.recvMsg(getStatusSocket());
					int i = 0;
					while (receivedMessage != null) {
						// System.out.println("loop: " + i);
						for (ZFrame f : receivedMessage) {
							byte[] returnedBytes = f.getData();
							String messageType = new String(returnedBytes);
							if (messageType != "status" && messageType != "preview") {
								contReturned = Message.Container.parseFrom(returnedBytes);
								if (contReturned.getType().equals(ContainerType.MT_INTERP_STAT) || contReturned.getType().equals(ContainerType.MT_PREVIEW)) {
									System.out.println("Status:");
									System.out.println("\t" + contReturned.toString());
//									List<String> notes = contReturned.getNoteList();
//									for (String note : notes) {
//										System.out.println("\t" + note);
//									}
								}
							}
						}
					}
					receivedMessage = ZMsg.recvMsg(getStatusSocket());
					i++;
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

		BBBStatus status = new BBBStatus();

	}

	private static Socket getStatusSocket() {
		if (statusSocket != null)
			return statusSocket;

		String statusUrl = Settings.getInstance().getSetting(
				"machinekit_previewstatusService_url");

		Context con = ZMQ.context(1);
		statusSocket = con.socket(ZMQ.SUB);
		statusSocket.setReceiveTimeOut(2000);
		statusSocket.setLinger(10);
		statusSocket.connect(statusUrl);

		statusSocket.subscribe("status".getBytes());
		statusSocket.subscribe("preview".getBytes());

		return statusSocket;
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
