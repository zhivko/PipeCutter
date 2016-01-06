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
import pb.Task;
import pb.Types;

public class BBBCommand {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		byte[] buff;
		Context con = ZMQ.context(1);
		Socket socket = con.socket(ZMQ.DEALER);
		socket.connect("tcp://beaglebone.local:57668/");
		Container container = Container.newBuilder()
				.setType(Types.ContainerType.MT_PING).build();
		
		buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter
				.printHexBinary(buff);
		System.out.println(hexOutput);
		socket.send(buff);

		byte[] received = socket.recv();
		try {
			Container contReturned = Container.parseFrom(received);
			contReturned.getType().toString();
			System.out.println(contReturned.toString());
		} catch (InvalidProtocolBufferException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		container = Container.newBuilder()
				.setType(Types.ContainerType.MT_EMC_TASK_SET_STATE).build();
		container.getAllFields().put(container.getOneofFieldDescriptor(getField(Status.EmcTaskStateType.EMC_TASK_STATE_ESTOP.getValueDescriptor());// .getAllFields().put(Status.EmcTaskStateType.EMC_TASK_STATE_ESTOP_VALUE,  "emc_command_params","task_state");
		buff = container.toByteArray();
		hexOutput = javax.xml.bind.DatatypeConverter
				.printHexBinary(buff);
		System.out.println(hexOutput);
		socket.send(buff);
		received = socket.recv();
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
