package com.kz.pipeCutter.BBB.commands;

import java.util.concurrent.Callable;

import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.kz.pipeCutter.ui.Settings;

import pb.Message.Container;

public abstract class MachineTalkCommand {
	public static Socket commandSocket = null;
	public int ticket=0;

	public abstract Container prepareContainer();	
	
	
	public void start() {
		// TODO Auto-generated method stub
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] buff = prepareContainer().toByteArray();
					String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
					System.out.println("Message: " + hexOutput);
					getCommandSocket().send(buff);
					parseAndOutput();					
					//parseAndOutput();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		t.run();
	}

	protected static Socket getCommandSocket() {
		if (MachineTalkCommand.commandSocket != null)
			return commandSocket;
		String commandUrl = Settings.getInstance().getSetting("machinekit_commandService_url");
		System.out.println("commandUrl: " + commandUrl);
		Context con = ZMQ.context(1);
		commandSocket = con.socket(ZMQ.DEALER);
		commandSocket.setDelayAttachOnConnect(true);
		commandSocket.setReceiveTimeOut(4000);
		commandSocket.setSendTimeOut(4000);
		commandSocket.setSendBufferSize(4000);
		commandSocket.connect(commandUrl);
		return commandSocket;
	}

	protected void parseAndOutput() {
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
}
