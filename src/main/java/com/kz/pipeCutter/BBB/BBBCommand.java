package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jogamp.common.util.Bitstream.ByteInputStream;
import com.kz.pipeCutter.ui.Settings;

import pb.Message.Container;
import pb.Status;
import pb.Types;
import pb.Types.ContainerType;

public class BBBCommand {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;
	static Socket socket = null;
	
	ByteArrayInputStream is;

	public static void main(String[] args) {
		BBBCommand comm = new BBBCommand();
		comm.SSH_Login();
		comm.SSH_StartMachinekit();
		
		//comm.ping();
		comm.estopReset();
		//comm.machineOnSSH();
	}

	private Socket getSocket() {
		if (BBBCommand.socket != null)
			return socket;
		Discoverer discoverer = new Discoverer();
		ServiceInfo command = discoverer.getCommandService();
		String commandUrl = "tcp://beaglebone.local:" + command.getPort() + "/";

		Context con = ZMQ.context(1);
		socket = con.socket(ZMQ.DEALER);
		socket.setReceiveTimeOut(3000);
		socket.connect(commandUrl);
 		return socket;
	}

	public void ping() {

		byte[] buff;
		Container container = Container.newBuilder().setType(Types.ContainerType.MT_PING).build();
		buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Mesage: " + hexOutput);
		getSocket().send(buff);

		byte[] received = getSocket().recv();
		try {
			Container contReturned = Container.parseFrom(received);
			contReturned.getType().toString();
			System.out.println(contReturned.toString());
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

	}

	public void estopReset() {
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setTaskState(Status.EmcTaskStateType.EMC_TASK_STATE_ESTOP_RESET).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_STATE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(1);

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
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters.newBuilder()
				.setTaskState(Status.EmcTaskStateType.EMC_TASK_STATE_ON).build();

		builder.setType(ContainerType.MT_EMC_TASK_SET_STATE);
		builder.setEmcCommandParams(emcCommandParameter);
		builder.setInterpName("execute");
		builder.setTicket(1);

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
			JSch jsch = new JSch();

			// jsch.setKnownHosts("/home/foo/.ssh/known_hosts");

			String host = Settings.instance.getSetting("machinekit_host");
			String user = Settings.instance.getSetting("machinekit_user");
			String pass = Settings.instance.getSetting("machinekit_password");

			Session session = jsch.getSession(user, host, 22);
			session.setPassword(pass);

			//session.setUserInfo(userinfo);

			// It must not be recommended, but if you want to skip host-key check,
			// invoke following,
			session.setConfig("StrictHostKeyChecking", "no");

			// session.connect();
			session.connect(5000); // making a connection with timeout.
			Channel channel = session.openChannel("shell");

			// Enable agent-forwarding.
			// ((ChannelShell)channel).setAgentForwarding(true);

			/*
			 * // a hack for MS-DOS prompt on Windows. channel.setInputStream(new
			 * FilterInputStream(System.in){ public int read(byte[] b, int off, int
			 * len)throws IOException{ return in.read(b, off, (len>1024?1024:len)); }
			 * });
			 */
			InputStream is = new ByteArrayInputStream("\n".getBytes());
			//InputStream is = new ByteArrayInputStream("ls -all\n".getBytes());
			channel.setInputStream(is);
			channel.setOutputStream(System.out);

			/*
			 * // Choose the pty-type "vt102".
			 * ((ChannelShell)channel).setPtyType("vt102");
			 */

			/*
			 * // Set environment variable "LANG" as "ja_JP.eucJP".
			 * ((ChannelShell)channel).setEnv("LANG", "ja_JP.eucJP");
			 */

			// channel.connect();
			channel.connect(3 * 1000);
			System.out.println("Exit status: " + channel.getExitStatus());
			
		} catch (Exception e) {
			System.out.println(e);
		}
	}

	public void SSH_StartMachinekit() {
		try {
			String command = "machinekit /home/machinekit/machinekit/configs/ARM.BeagleBone.CRAMPS/CRAMPS.ini &\n";
			this.is.read(command.getBytes());
		} catch (Exception e) {
			System.out.println(e);
		}
	}


}
