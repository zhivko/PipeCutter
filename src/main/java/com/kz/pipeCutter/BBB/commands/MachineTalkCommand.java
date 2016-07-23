package com.kz.pipeCutter.BBB.commands;

import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.SwingWorker;

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
	private static Lock lock = new ReentrantLock();
	public Socket socket = null;
	ZContext ctx = null;
	public static int ticket = 0;
	private String uri;
	public static MachineTalkCommand instance;
	ExecutorService executor = Executors.newSingleThreadExecutor();

	public abstract Container prepareContainer() throws Exception;

	public MachineTalkCommand() {		
	}

	@Override
	public String call() {
		try {
			initSocket();
			Container cont = prepareContainer();
			if (cont != null) {
				byte[] buff = cont.toByteArray();
				String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
				System.out.println("Message: " + hexOutput);
				socket.send(buff, ZMQ.DONTWAIT);
				parseAndOutput(2);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			close();
		}
		return "ok";
	}

	// TODO Auto-generated method stub
	public void start() {
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					Future<String> future = MachineTalkCommand.this.executor.submit(MachineTalkCommand.this);
					try {
						System.out.println(Thread.currentThread().getName() + ": Lock");
						lock.lock();
						System.out.println(Thread.currentThread().getName() + ": Locked");
						future.get(12, TimeUnit.SECONDS);
					} catch (Exception e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				finally
				{
					System.out.println(Thread.currentThread().getName() + ": UNLock");
					lock.unlock();
					System.out.println(Thread.currentThread().getName() + ": UNLocked");					
				}

				return null;
			}

			@Override
			protected void process(List<Void> chunks) {
				// TODO Auto-generated method stub
				super.process(chunks);
			}

		};
		sw.execute();
	}

	public synchronized void initSocket() {
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
		socket.setReceiveTimeOut(1000);
		socket.setSendTimeOut(1000);
		socket.connect(this.uri);
	}

	protected synchronized void parseAndOutput() throws InvalidProtocolBufferException {
		parseAndOutput(1);
	}
	
	protected synchronized void parseAndOutput(int neededReceivedMessageCount) throws InvalidProtocolBufferException {
		System.out.println(Thread.currentThread().getName());
		int receivedMessageCount=0;
		while (true) {
			// System.out.println("loop: " + i);
			ZMsg receivedMessage = ZMsg.recvMsg(socket);
			if (receivedMessage != null) {
				ZFrame frame = receivedMessage.poll();
				byte[] returnedBytes = frame.getData();
				Container contReturned = Message.Container.parseFrom(returnedBytes);
				if (contReturned.equals(pb.Types.ContainerType.MT_PING)) {
					MachinekitSettings.instance.pingCommand();
				}
				contReturned.getReplyTicket();
				Settings.instance.log(contReturned.toString());
				receivedMessageCount++;
				if(receivedMessageCount==neededReceivedMessageCount)
					break;
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
