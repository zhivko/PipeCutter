package com.kz.pipeCutter.BBB;

import java.math.BigInteger;
import java.util.ArrayList;

import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import pb.Message;
import pb.Motcmds.MotionCommand;
import pb.Status.EmcStatusTask;
import pb.Status.EmcTaskModeType;

public class BBBCommand {
	ServiceListener bonjourServiceListener;
	ArrayList<ServiceInfo> services;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		String s="088952d2010765786563757465";
		byte[] buff = new BigInteger(s,16).toByteArray();
		
		Context con = ZMQ.context(1);
		
		Socket socket = con.socket(ZMQ.DEALER);
		socket.connect("tcp://192.168.7.2:55236/");
		
		Message.Container msgContainer = Message.Container.getDefaultInstance();
		MotionCommand motionCmd = Message.Container.getDefaultInstance().getMotcmd();
		EmcStatusTask task = msgContainer.getEmcStatusTask().getDefaultInstanceForType().
		task.getTaskMode().values() = EmcTaskModeType.EMC_TASK_MODE_AUTO;
		task.getTaskMode().values().length
		System.out.println(javax.xml.bind.DatatypeConverter.printHexBinary(task.toByteArray()));
		
		socket.send(buff);		
	}

}
