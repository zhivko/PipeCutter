package com.kz.pipeCutter.BBB;

import java.util.ArrayList;

import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.google.protobuf.InvalidProtocolBufferException;

import pb.Message;
import pb.Message.Container;
import pb.Status;
import pb.Status.EmcStatusTask;
import pb.Status.EmcStatusTask.Builder;
import pb.Status.EmcTaskStateType;
import pb.Task;
import pb.Types;
import pb.Types.ContainerType;

public class BBBCommand {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;
	static Socket socket = null;

	public static void main(String[] args) {
		BBBCommand comm = new BBBCommand();
		//comm.ping();
		comm.estopReset();
		comm.machineOn();
	}

	private Socket getSocket() {
		if (BBBCommand.socket != null)
			return socket;
		Discoverer discoverer = new Discoverer();
		ServiceInfo command = discoverer.getCommandService();
		String commandUrl = "tcp://beaglebone.local:" + command.getPort() + "/";

		Context con = ZMQ.context(1);
		socket = con.socket(ZMQ.DEALER);
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
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}

	}

	public void estopReset() {
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder()
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
	
	
	public void machineOn() {
		pb.Message.Container.Builder builder = Container.newBuilder();
		pb.Status.EmcCommandParameters emcCommandParameter = pb.Status.EmcCommandParameters
				.newBuilder()
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
}
