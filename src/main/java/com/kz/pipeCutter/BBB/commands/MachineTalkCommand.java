package com.kz.pipeCutter.BBB.commands;

import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;

import pb.Message;
import pb.Message.Container;

public abstract class MachineTalkCommand implements Callable<String> {
	public Socket socket = null;
	ZContext ctx = null;
	public static int ticket = 0;
	private String uri;
	public static MachineTalkCommand instance;
	ExecutorService executor = Executors.newSingleThreadExecutor();

	public abstract Container prepareContainer() throws Exception;

	public MachineTalkCommand() {
		initSocket();
	}

	@Override
	public String call() {
		try {
			byte[] buff = prepareContainer().toByteArray();
			String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
			System.out.println("Message: " + hexOutput);
			socket.send(buff);
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

	// private void initSocket() {
	// this.commandUri = Settings.getInstance().getSetting(
	// "machinekit_commandService_url");
	// Random rand = new Random(23424234);
	//
	// ctx = ContextFactory.createContext(1);
	// //ctx = ZMQ.context(1);
	// // Set random identity to make tracing easier
	// String identity = String
	// .format("%04X-%04X", rand.nextInt(), rand.nextInt());
	// commandSocket = ctx.buildSocket(SocketType.DEALER)
	// .withIdentity(identity.getBytes()).connect(this.commandUri);
	// }

	public void initSocket() {
		if (ctx != null && socket != null) {
			socket.close();
			ctx.close();
		}
		this.uri = Settings.getInstance().getSetting("machinekit_commandService_url");

		Random rand = new Random(23424234);
		String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());

		ctx = new ZContext();
		// Set random identity to make tracing easier
		socket = ctx.createSocket(ZMQ.DEALER);
		socket.setIdentity(identity.getBytes(ZMQ.CHARSET));
		socket.setReceiveTimeOut(100);
		socket.connect(this.uri);
	}

	protected void parseAndOutput() throws InvalidProtocolBufferException {
		System.out.println(Thread.currentThread().getName());
		while (true) {
			// System.out.println("loop: " + i);
			ZMsg receivedMessage = ZMsg.recvMsg(socket,ZMQ.DONTWAIT);
			if (receivedMessage != null) {
				ZFrame frame = receivedMessage.poll();
				byte[] returnedBytes = frame.getData();
				Container contReturned = Message.Container.parseFrom(returnedBytes);
				if (contReturned.equals(pb.Types.ContainerType.MT_PING)) {
					MachinekitSettings.instance.pingCommand();
				}
				Settings.instance.log(contReturned.toString());
				break;
			}
			try {
				TimeUnit.MILLISECONDS.sleep(100);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public synchronized static int getNextTicket() {
		ticket++;
		return ticket;
	}

	public Socket getCommandSocket() {
		return this.socket;
	}

	public void close() {
		if (socket != null)
			socket.close();
		if (ctx != null)
			ctx.close();
	}
}
