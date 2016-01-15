package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import pb.Message.Container;
import pb.Status;
import pb.Status.EmcTaskModeType;
import pb.Types;
import pb.Types.ContainerType;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.ui.Settings;

public class BBBCommand {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;
	static Socket socket = null;

	ByteArrayInputStream is;
	private JSch jsch;
	private Channel channel;
	private Session session;
	private PrintStream ps;

	private static int ticket = 0;

	public static void main(String[] args) {
		BBBCommand comm = new BBBCommand();

		try {
			// comm.SSH_Login();
			// comm.SSH_StartMachinekit();
			comm.ping();
			Thread.sleep(100);
			comm.estopReset();
			Thread.currentThread();
			Thread.sleep(100);
			comm.machineOnMachineTalk();
			Thread.sleep(100);
			comm.jogAxis(0, 50.0, 120.0);
			Thread.sleep(100);
			comm.jogAxis(1, 50.0, 120.0);
			Thread.sleep(100);
			comm.jogAxis(2, 50.0, 120.0);
			Thread.sleep(100);
			comm.toMdiMode();
			Thread.sleep(100);
			comm.executeMdi("G0 X800.0");
			Thread.sleep(100);
			comm.toManualMode();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// comm.machineOnSSH();
	}

	private void executeMdi(String command) throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());
		pb.Message.Container.Builder builder = Container.newBuilder();

		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder().setCommandBytes(ByteString.copyFromUtf8(command)).build();

		builder.setType(ContainerType.MT_EMC_TASK_PLAN_EXECUTE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getSocket().send(buff);
		parseAndOutput();
	}

	private void parseAndOutput() throws Exception {
		byte[] received = null;
		long nowMilis = System.currentTimeMillis();
		long currentMilis = System.currentTimeMillis();
		while (received == null && (currentMilis - nowMilis) < 5000) {
			received = getSocket().recv();
			currentMilis = System.currentTimeMillis();
		}
		if (received == null)
			throw new Exception("No response!");
		try {
			if (received != null) {
				Container contReturned = Container.parseFrom(received);
				contReturned.getType().toString();
				System.out.println(contReturned.toString());
			} else {
				System.out.println("Received is NULL!");
			}
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void toMdiMode() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder().setTaskMode(EmcTaskModeType.EMC_TASK_MODE_MDI).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_MODE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getSocket().send(buff);
		parseAndOutput();
	}

	private void toManualMode() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder().setTaskMode(EmcTaskModeType.EMC_TASK_MODE_MANUAL).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_MODE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getSocket().send(buff);
		parseAndOutput();
	}

	private void jogAxis(int axis, double velocity, double distance)
			throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder().setIndex(axis).setVelocity(velocity)
				.setDistance(distance).build();

		builder.setType(ContainerType.MT_EMC_AXIS_INCR_JOG);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setTicket(ticket++);
		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getSocket().send(buff);
		parseAndOutput();
	}

	private Socket getSocket() {
		if (BBBCommand.socket != null)
			return socket;

		String commandUrl = Settings.instance.getSetting("machinekit_command_url");
		if (commandUrl == null) {
			Discoverer discoverer = new Discoverer();
			ServiceInfo command = null;
			while (command == null) {
				command = discoverer.getCommandService();
				if (command == null)
					try {
						System.out
								.println("Still looking for command servvice with mdns...");
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			commandUrl = "tcp://beaglebone.local:" + command.getPort() + "/";
			Settings.instance.setSetting("machinekit_command_url", commandUrl);
		}

		Context con = ZMQ.context(1);
		socket = con.socket(ZMQ.DEALER);
		socket.setReceiveTimeOut(3000);
		socket.connect(commandUrl);
		return socket;
	}

	public void ping() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		byte[] buff;
		Container container = Container.newBuilder()
				.setType(Types.ContainerType.MT_PING).build();
		buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Mesage: " + hexOutput);
		getSocket().send(buff);

		parseAndOutput();
	}

	public void estopReset() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder()
				.setTaskState(Status.EmcTaskStateType.EMC_TASK_STATE_ESTOP_RESET)
				.build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_STATE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getSocket().send(buff);
		parseAndOutput();
	}

	public void machineOnMachineTalk() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder().setTaskState(Status.EmcTaskStateType.EMC_TASK_STATE_ON)
				.build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_STATE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getSocket().send(buff);
		parseAndOutput();
	}

	public void SSH_Login() {
		try {
			this.jsch = new JSch();

			// jsch.setKnownHosts("/home/foo/.ssh/known_hosts");

			String host = Settings.instance.getSetting("machinekit_host");
			String user = Settings.instance.getSetting("machinekit_user");
			String pass = Settings.instance.getSetting("machinekit_password");

			this.session = jsch.getSession(user, host, 22);
			session.setPassword(pass);

			session.setConfig("StrictHostKeyChecking", "no");

			session.connect(5000); // making a connection with timeout.
			session.setServerAliveInterval(2000);
			session.setServerAliveCountMax(Integer.MAX_VALUE);
			this.channel = session.openChannel("shell");
			this.channel.setOutputStream(System.out);

			OutputStream ops = this.channel.getOutputStream();
			this.ps = new PrintStream(ops, true);

			this.channel.connect();
			// System.out.println("Exit status: " + channel.getExitStatus());

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void SSH_StartMachinekit() {
		try {
			String command = "machinekit /home/machinekit/machinekit/configs/ARM.BeagleBone.CRAMPS/CRAMPS.ini &";
			this.ps.println(command);
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void SSH_SendGcode(String gcode) {
		try {
			String command = "axis-remote --mdi '" + gcode + "'";
			this.ps.println(command);
		} catch (Exception e) {
			System.out.println(e);
		}
	}
}
