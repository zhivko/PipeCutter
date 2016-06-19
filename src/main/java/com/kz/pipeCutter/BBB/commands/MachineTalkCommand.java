package com.kz.pipeCutter.BBB.commands;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.zeromq.ContextFactory;
import org.zeromq.api.Context;
import org.zeromq.api.Socket;
import org.zeromq.api.SocketType;

import pb.Message;
import pb.Message.Container;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kz.pipeCutter.ui.Settings;

public abstract class MachineTalkCommand implements Callable<String> {
	public Socket commandSocket = null;
	Context ctx = null;
	public static int ticket = 0;
	private String commandUri;
	public static MachineTalkCommand instance;
	ExecutorService executor = Executors.newSingleThreadExecutor();

	public abstract Container prepareContainer() throws Exception;

	public MachineTalkCommand() {
		getSocket();
	}

	@Override
	public String call() {
		try {
			byte[] buff = prepareContainer().toByteArray();
			String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
			System.out.println("Message: " + hexOutput);
			commandSocket.send(buff);
			parseAndOutput();
			close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "ok";
	}

	// TODO Auto-generated method stub
	public void start() {
		Future<String> future = executor.submit(this);
		try {
			future.get(10, TimeUnit.SECONDS);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private void getSocket() {
		this.commandUri = Settings.getInstance().getSetting(
				"machinekit_commandService_url");
		Random rand = new Random(23424234);

		ctx = ContextFactory.createContext(1);
		//ctx = ZMQ.context(1);
		// Set random identity to make tracing easier
		String identity = String
				.format("%04X-%04X", rand.nextInt(), rand.nextInt());
		commandSocket = ctx.buildSocket(SocketType.DEALER)
				.withIdentity(identity.getBytes()).connect(this.commandUri);
	}

	protected void parseAndOutput() throws InvalidProtocolBufferException {
		System.out.println(Thread.currentThread().getName());
		org.zeromq.api.Message receivedMessage = commandSocket.receiveMessage();
		if (receivedMessage != null) {
			for (org.zeromq.api.Message.Frame f : receivedMessage.getFrames()) {
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
			// received = getCommandSocket().recv();
			org.zeromq.api.Message msg = commandSocket.receiveMessage();
			received = msg.getFirstFrame().getData();
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

	public synchronized static int getNextTicket() {
		ticket++;
		return ticket;
	}

	public Socket getCommandSocket() {
		return this.commandSocket;
	}

	public void close() {
		if (commandSocket != null)
			commandSocket.close();
		if (ctx != null)
			ctx.close();
	}
}
