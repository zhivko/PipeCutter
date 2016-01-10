package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import pb.Message.Container;
import pb.Status;
import pb.Types;
import pb.Types.ContainerType;

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

	private static int ticket=0;
	
	public static void main(String[] args) {
		BBBCommand comm = new BBBCommand();
		//comm.SSH_Login();
		//comm.SSH_StartMachinekit();
		comm.ping();
		comm.estopReset();
		comm.machineOnMachineTalk();
		comm.jogAxis(1,200.0, 100.0);
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		comm.jogAxis(0,40.0, 20.0);
		comm.jogAxis(0,500.0, 20.0);
		comm.executeMdi("G0 X100.0");
		//comm.machineOnSSH();
	}

	private void executeMdi(String command) {
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder()
				.setCommand(command)
				.build();

		builder.setType(ContainerType.MT_EMC_TASK_PLAN_EXECUTE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getSocket().send(buff);
		byte[] received = getSocket().recv();
		try {
			Container contReturned = Container.parseFrom(received);
			contReturned.getType().toString();
			System.out.println(contReturned.toString());
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void jogAxis(int axis, double velocity, double distance) {
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder()
				.setIndex(axis)
				.setVelocity(velocity)
				.setDistance(distance)
				.build();

		builder.setType(ContainerType.MT_EMC_AXIS_INCR_JOG);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setTicket(ticket++);
		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getSocket().send(buff);
		byte[] received = getSocket().recv();
		try {
			Container contReturned = Container.parseFrom(received);
			contReturned.getType().toString();
			System.out.println(contReturned.toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Socket getSocket() {
		if (BBBCommand.socket != null)
			return socket;
		Discoverer discoverer = new Discoverer();
		ServiceInfo command = null;
		while (command == null) {
			command = discoverer.getCommandService();
			if (command == null)
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}

		String commandUrl = "tcp://beaglebone.local:" + command.getPort() + "/";

		Context con = ZMQ.context(1);
		socket = con.socket(ZMQ.DEALER);
		socket.setReceiveTimeOut(3000);
		socket.connect(commandUrl);
		return socket;
	}

	public void ping() {

		byte[] buff;
		Container container = Container.newBuilder()
				.setType(Types.ContainerType.MT_PING).build();
		buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Mesage: " + hexOutput);
		getSocket().send(buff);

		byte[] received = getSocket().recv();
		try {
			Container contReturned = Container.parseFrom(received);
			contReturned.getType().toString();
			System.out.println(contReturned.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void estopReset() {
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
		byte[] received = getSocket().recv();
		try {
			Container contReturned = Container.parseFrom(received);
			contReturned.getType().toString();
			System.out.println(contReturned.toString());
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void machineOnMachineTalk() {
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
		byte[] received = getSocket().recv();
		try {
			Container contReturned = Container.parseFrom(received);
			contReturned.getType().toString();
			System.out.println(contReturned.toString());
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
