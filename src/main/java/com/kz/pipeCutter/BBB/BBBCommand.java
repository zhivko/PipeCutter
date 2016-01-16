package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.ui.Settings;

import pb.Message.Container;
import pb.Status;
import pb.Status.EmcTaskModeType;
import pb.Types;
import pb.Types.ContainerType;

public class BBBCommand {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;
	static Socket commandSocket = null;
	static Socket errorSocket = null;
	static Socket reportSocket = null;
	static BBBCommand instance = null;

	ByteArrayInputStream is;
	private JSch jsch;
	public ChannelExec channelExec = null;
	private Session session;
	private PrintStream ps;

	private static int ticket = 0;
	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public BBBCommand() {
		Runnable errorReporter = new Runnable() {
			@Override
			public void run() {
				Container contReturned;
				try {
					contReturned = Container.parseFrom(getErrorSocket().recv());
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
					e.printStackTrace();
				}
			}
		};
		scheduler.scheduleAtFixedRate(errorReporter, 0, 1, TimeUnit.SECONDS);
		instance = this;
	}

	public static void main(String[] args) {

		Settings sett = new Settings();
		sett.setVisible(true);
		getCommandSocket();
		getErrorSocket();

		BBBCommand comm = new BBBCommand();

		try {

			comm.SSH_StopMachinekit();
			comm.SSH_StartMachinekit();
			System.exit(0);

			comm.ping();
			Thread.sleep(100);
			comm.estopReset();
			Thread.sleep(100);
			comm.machineOnMachineTalk();
			Thread.sleep(100);

			comm.jogAxis(0, 1.0, 50.0);
			Thread.sleep(100);
			comm.jogAxis(1, 1.0, 50.0);
			Thread.sleep(100);
			comm.jogAxis(2, 1.0, 50.0);
			Thread.sleep(100);

			comm.toManualMode();
			Thread.sleep(100);

			comm.homeAxis(0);
			Thread.sleep(100);
			comm.homeAxis(1);
			Thread.sleep(100);
			comm.homeAxis(2);
			Thread.sleep(100);

			Thread.sleep(100);
			comm.executeMdi("G0 X80.0 Y20.0 Z10.0");
			Thread.sleep(100);
			comm.toManualMode();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		// comm.machineOnSSH();
	}

	public void executeMdi(String command) throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder().setCommand(command)
				.build();

		builder.setType(ContainerType.MT_EMC_TASK_PLAN_EXECUTE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getCommandSocket().send(buff);
		parseAndOutput();
	}

	private void parseAndOutput() {
		byte[] received = null;
		long nowMilis = System.currentTimeMillis();
		long currentMilis = System.currentTimeMillis();
		while (received == null && (currentMilis - nowMilis) < 10000) {
			received = getCommandSocket().recv();
			currentMilis = System.currentTimeMillis();
		}
		try {
			if (received == null)
				System.out.println("No response!");
			else {
				Container contReturned = Container.parseFrom(received);
				contReturned.getType().toString();
				System.out.println(contReturned.toString());
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void toMdiMode() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setTaskMode(EmcTaskModeType.EMC_TASK_MODE_MDI).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_MODE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getCommandSocket().send(buff);
		parseAndOutput();
	}

	public void toManualMode() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setTaskMode(EmcTaskModeType.EMC_TASK_MODE_MANUAL).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_MODE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getCommandSocket().send(buff);
		parseAndOutput();
	}

	public void homeAxis(int axis) throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder().setIndex(axis)
				.build();

		builder.setType(ContainerType.MT_EMC_AXIS_HOME);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setTicket(ticket++);
		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getCommandSocket().send(buff);
		parseAndOutput();
	}

	public void jogAxis(int axis, double velocity, double distance) {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder().setIndex(axis)
				.setVelocity(velocity).setDistance(distance).build();

		builder.setType(ContainerType.MT_EMC_AXIS_INCR_JOG);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setTicket(ticket++);
		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getCommandSocket().send(buff);
		parseAndOutput();
	}

	private static Socket getCommandSocket() {
		if (BBBCommand.commandSocket != null)
			return commandSocket;

		String commandUrl = Settings.getInstance().getSetting("machinekit_commandService_url");
		System.out.println("commandUrl: " + commandUrl);
		Context con = ZMQ.context(1);
		commandSocket = con.socket(ZMQ.DEALER);
		commandSocket.setDelayAttachOnConnect(true);
		commandSocket.setReceiveTimeOut(4000);
		commandSocket.setSendBufferSize(4000);
		commandSocket.connect(commandUrl);
		return commandSocket;
	}

	private static Socket getReportSocket() {
		if (BBBCommand.reportSocket != null)
			return reportSocket;

		String commandUrl = Settings.getInstance().getSetting("machinekit_commandService_url");

		Context con = ZMQ.context(1);
		reportSocket = con.socket(ZMQ.SUB);

		reportSocket.setReceiveTimeOut(3000);

		reportSocket.connect(commandUrl);
		reportSocket.subscribe("task".getBytes());
		reportSocket.subscribe("motion".getBytes());
		reportSocket.subscribe("io".getBytes());
		reportSocket.subscribe("interp".getBytes());
		reportSocket.subscribe("config".getBytes());

		return reportSocket;
	}

	private static Socket getErrorSocket() {
		if (BBBCommand.errorSocket != null)
			return errorSocket;

		String errorUrl = Settings.getInstance().getSetting("machinekit_errorService_url");

		Context con = ZMQ.context(1);
		errorSocket = con.socket(ZMQ.SUB);
		errorSocket.setReceiveTimeOut(3000);
		errorSocket.connect(errorUrl);
		errorSocket.subscribe("error".getBytes());
		return errorSocket;
	}

	public void ping() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		byte[] buff;
		Container container = Container.newBuilder().setType(Types.ContainerType.MT_PING).setTicket(ticket++).build();
		buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Mesage: " + hexOutput);
		getCommandSocket().send(buff);

		parseAndOutput();
	}

	public void estopReset() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setTaskState(Status.EmcTaskStateType.EMC_TASK_STATE_ESTOP_RESET).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_STATE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		getCommandSocket().send(buff);
		parseAndOutput();
	}

	public void machineOnMachineTalk() throws Exception {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setTaskState(Status.EmcTaskStateType.EMC_TASK_STATE_ON).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_STATE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		if (getCommandSocket().send(buff))
			parseAndOutput();
	}

	public void SSH_Login() {
		try {
			if (this.jsch == null || this.session == null || !this.session.isConnected()) {
				this.jsch = new JSch();

				// jsch.setKnownHosts("/home/foo/.ssh/known_hosts");

				String host = Settings.getInstance().getSetting("machinekit_host");
				String ip = Settings.getInstance().getSetting("machinekit_ip");
				String user = Settings.getInstance().getSetting("machinekit_user");
				String pass = Settings.getInstance().getSetting("machinekit_password");

				this.session = jsch.getSession(user, ip, 22);
				session.setPassword(pass);

				session.setConfig("StrictHostKeyChecking", "no");

				session.setServerAliveInterval(2000);
				session.setServerAliveCountMax(Integer.MAX_VALUE);

				session.setOutputStream(System.out);
				session.connect(3000); // making a connection with timeout.

				System.out.println("Is ssh session connected: " + session.isConnected());
				MyOutputStreamReader myOut = new MyOutputStreamReader();

				channelExec = (ChannelExec) session.openChannel("exec");
				channelExec.setOutputStream(myOut);
				String command = "pwd\nexit\n";
				channelExec.setCommand(command);
				channelExec.setErrStream(System.err);
				channelExec.connect();
				while (channelExec.getExitStatus() == -1) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						System.out.println(e);
					}
				}
				channelExec.disconnect();

			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	private String readFromResult(Channel channel) throws IOException {
		StringBuffer buff = new StringBuffer();
		byte[] tmp = new byte[1024];
		while (true) {
			while (channel.getInputStream().available() > 0) {
				int i = channel.getInputStream().read(tmp, 0, 1024);
				if (i < 0)
					break;
				String result = new String(tmp, 0, i);
				buff.append(result);
			}
			if (channel.isClosed()) {
				System.out.println("exit-status: " + channel.getExitStatus());
				break;
			}
			try {
				Thread.sleep(500);
			} catch (Exception ee) {
			}
		}
		return buff.toString();
	}

	public boolean SSH_CheckIfMachinekitRunning() {
		try {
			this.SSH_Login();
			// try to see if machinekit lready running
			String command = "ps -aux | grep machinekit\nexit\n";
			MyOutputStreamReader myOut = new MyOutputStreamReader();
			channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(command.getBytes());
			channelExec.setOutputStream(myOut);
			channelExec.connect();
			while (channelExec.getExitStatus() == -1) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			channelExec.disconnect();

			for (String line : myOut.getLines()) {
				if (line.matches("(.*)CRAMPS.ini")) {
					System.out.println("MachineKit already started");
					channelExec.disconnect();
					return true;
				}
			}
			channelExec.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}

	public void SSH_StartMachinekit() {
		try {
			this.SSH_Login();
			if (!SSH_CheckIfMachinekitRunning()) {
				String command = "machinekit /home/machinekit/machinekit/configs/ARM.BeagleBone.CRAMPS/CRAMPS.ini &";
				channelExec = (ChannelExec) session.openChannel("exec");
				MyOutputStreamReader myOut = new MyOutputStreamReader();
				channelExec.setOutputStream(myOut);
				channelExec.setCommand(command);
				channelExec.connect(3 * 1000);
				while (channelExec.getExitStatus() == -1) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						System.out.println(e);
					}
				}
				channelExec.disconnect();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void SSH_SendGcode(String gcode) {
		try {
			this.SSH_Login();
			String command = "axis-remote --mdi '" + gcode + "'";
			channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(command);
			MyOutputStreamReader myOut = new MyOutputStreamReader();
			channelExec.setOutputStream(myOut);
			channelExec.connect();
			String result = readFromResult(channelExec);
			System.out.println(result);
			channelExec.disconnect();

		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void SSH_StopMachinekit() {
		try {
			this.SSH_Login();
			String command = "ps -aux | grep CRAMPS.ini";
			channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(command);
			MyOutputStreamReader myOut = new MyOutputStreamReader();
			channelExec.setOutputStream(myOut);
			channelExec.connect(3 * 1000);
			while (channelExec.getExitStatus() == -1) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			channelExec.disconnect();

			String killCommands = "";
			for (String line : myOut.getLines()) {
				if (!line.contains("grep")) {
					String splittedLine[] = line.replaceAll("\\s+", " ").split(" ");
					String pid = splittedLine[1];
					if (!pid.equals(""))
						killCommands += "kill -9 " + pid + "\n";
				}
			}
			System.out.println(killCommands);

			if (!killCommands.equals("")) {
				channelExec = (ChannelExec) session.openChannel("exec");
				myOut = new MyOutputStreamReader();
				channelExec.setOutputStream(myOut);
				channelExec.setCommand(killCommands);
				channelExec.connect(3 * 1000);
				while (channelExec.getExitStatus() == -1) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
						System.out.println(e);
					}
				}
				channelExec.disconnect();
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void SSH_ListMachinekitProcesses() {
		try {
			this.SSH_Login();
			String command = "ps -aux | grep CRAMPS.ini";
			channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(command);
			MyOutputStreamReader myOut = new MyOutputStreamReader();
			channelExec.setOutputStream(myOut);
			channelExec.connect(3 * 1000);
			while (channelExec.getExitStatus() == -1) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			channelExec.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static BBBCommand getInstance() {
		if (instance == null)
			instance = new BBBCommand();

		return instance;
	}

	public void machineOffMachineTalk() {
		System.out.println(new Object() {
		}.getClass().getEnclosingMethod().getName());

		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setTaskState(Status.EmcTaskStateType.EMC_TASK_STATE_OFF).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_STATE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(ticket++);

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		if (getCommandSocket().send(buff))
			parseAndOutput();
	}

}
