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

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.BBB.commands.MachineTalkCommand;
import com.kz.pipeCutter.ui.Settings;

import pb.Message.Container;
import pb.Types.ContainerType;

public class BBBError {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;
	static Socket commandSocket = null;
	static Socket errorSocket = null;
	static Socket reportSocket = null;
	static BBBError instance = null;

	ByteArrayInputStream is;
	private JSch jsch;
	public ChannelExec channelExec = null;
	private Session session;
	private PrintStream ps;

	private static int ticket = 0;
	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public BBBError() {
		Runnable errorReporter = new Runnable() {
			@Override
			public void run() {
				Container contReturned;
				try {
					
					byte[] buff;
					Container container =
					Container.newBuilder().setType(ContainerType.MT_PING).setTicket(ticket++).build();
					buff = container.toByteArray();
					String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
					System.out.println("Mesage: " + hexOutput);
					MachineTalkCommand.commandSocket.send(buff);					
					
					
					contReturned = Container.parseFrom(getErrorSocket().recv());
					System.out.println(contReturned.toString());
					if (contReturned.getType().toString().equals("MT_EMC_OPERATOR_ERROR")) {
						List<String> notes = contReturned.getNoteList();
						System.out.println("Operator error:");
						for (String note : notes) {
							System.out.println("\t" + note);
						}
						System.out.println("");
					} else if (!contReturned.getType().toString().equals("MT_PING")) {
						System.out.println(contReturned.toString());
					}
				} catch (InvalidProtocolBufferException e) {
					System.out.println("Unknown message type.");
					e.printStackTrace();
				}
			}
		};
		scheduler.scheduleAtFixedRate(errorReporter, 0, 5, TimeUnit.SECONDS);
		instance = this;
	}

	public static void main(String[] args) {

		Settings sett = new Settings();
		sett.setVisible(true);
		getErrorSocket();

		BBBError error = new BBBError();

	}

	private static Socket getErrorSocket() {
		if (BBBError.errorSocket != null)
			return errorSocket;

		String errorUrl = Settings.getInstance().getSetting("machinekit_errorService_url");

		Context con = ZMQ.context(1);
		errorSocket = con.socket(ZMQ.SUB);
		errorSocket.setReceiveTimeOut(3000);
		errorSocket.connect(errorUrl);
		errorSocket.subscribe("error".getBytes());
		errorSocket.subscribe("text".getBytes());
		errorSocket.subscribe("display".getBytes());
		errorSocket.setLinger(0);
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
