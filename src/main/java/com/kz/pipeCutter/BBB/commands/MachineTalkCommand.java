package com.kz.pipeCutter.BBB.commands;

import java.util.List;
import java.util.concurrent.Callable;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kz.pipeCutter.ui.Settings;

import pb.Message;
import pb.Message.Container;

public abstract class MachineTalkCommand {
	public static Socket commandSocket = null;
	public int ticket = 0;

	public abstract Container prepareContainer() throws Exception;

		// TODO Auto-generated method stub
	public void start() {
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					byte[] buff = prepareContainer().toByteArray();
					String hexOutput = javax.xml.bind.DatatypeConverter
							.printHexBinary(buff);
					System.out.println("Message: " + hexOutput);
					getCommandSocket().send(buff, 0);
					parseAndOutput();
					// parseAndOutput();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		t.run();
	}

	public static Socket getCommandSocket() {
		if (MachineTalkCommand.commandSocket != null)
			return commandSocket;
		String commandUrl = Settings.getInstance().getSetting(
				"machinekit_commandService_url");
		System.out.println("commandUrl: " + commandUrl);
		Context con = ZMQ.context(1);
		commandSocket = con.socket(ZMQ.DEALER);
		commandSocket.setReceiveTimeOut(2000);
		commandSocket.setSendTimeOut(2000);
		commandSocket.setSendBufferSize(2000);
		commandSocket.connect(commandUrl);

		return commandSocket;
	}

	protected void parseAndOutput() throws InvalidProtocolBufferException {
		ZMsg receivedMessage = ZMsg.recvMsg(getCommandSocket());
		if (receivedMessage != null) {
			for (ZFrame f : receivedMessage) {
				byte[] returnedBytes = f.getData();
				Container contReturned = Message.Container.parseFrom(returnedBytes);
				Settings.instance.log(contReturned.toString());
				// System.out.println(contReturned.toString());
				// System.out.println(contReturned.getOperatorError().toString());
				// List<String> notes = contReturned.getNoteList();
				// for (String note : notes) {
				// System.out.println("\t" + note);
				// }

			}
		}
	}

	protected void parseAndOutput1() {
		byte[] received = null;
		long nowMilis = System.currentTimeMillis();
		long currentMilis = System.currentTimeMillis();
		while (received == null && (currentMilis - nowMilis) < 1000) {
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
