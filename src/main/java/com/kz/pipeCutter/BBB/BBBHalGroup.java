package com.kz.pipeCutter.BBB;

import java.util.Random;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;

import com.google.protobuf.InvalidProtocolBufferException;
import com.kz.pipeCutter.ui.Settings;

public class BBBHalGroup implements Runnable {
	Random rand = new Random(23424234);
	static Socket client = null;

	public static void main(String[] args) {

		Settings sett = new Settings();
		sett.setVisible(true);

		BBBHalGroup halGroup = new BBBHalGroup();
		Thread myThread = new Thread(halGroup);
		myThread.start();

		pb.Message.Container.Builder builder = Container.newBuilder();

		//builder.setType(ContainerType.MT_HALRCOMMAND_DESCRIBE);
		builder.setType(ContainerType.MT_PING);
		//builder.setTicket(MachineTalkCommand.getNextTicket());

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		client.send(buff, 0);
	}

	public void run() {
		ZContext ctx = new ZContext();
		client = ctx.createSocket(ZMQ.DEALER);

		// Set random identity to make tracing easier
		String identity = String
				.format("%04X-%04X", rand.nextInt(), rand.nextInt());
		client.setIdentity(identity.getBytes(ZMQ.CHARSET));
		String halCmdUrl = Settings.getInstance().getSetting(
				"machinekit_halCmdService_url");
		client.connect(halCmdUrl);

		PollItem[] items = new PollItem[] { new PollItem(client, Poller.POLLIN) };

		int requestNbr = 0;
		while (!Thread.currentThread().isInterrupted()) {
			// Tick once per second, pulling in arriving messages
			for (int centitick = 0; centitick < 4; centitick++) {
				ZMQ.poll(items, 10);
				if (items[0].isReadable()) {
					ZMsg msg = ZMsg.recvMsg(client);

					Container contReturned;
					try {
						contReturned = Message.Container
								.parseFrom(msg.getFirst().getData());
						Settings.instance.log(contReturned.toString());
					} catch (InvalidProtocolBufferException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					msg.getLast().print(identity);
					msg.destroy();
				}
			}

		}
		ctx.destroy();
	}
}