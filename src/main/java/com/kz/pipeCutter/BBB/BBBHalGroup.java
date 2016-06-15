package com.kz.pipeCutter.BBB;

import java.util.ArrayList;
import java.util.Random;

import javax.jmdns.ServiceInfo;

import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.google.protobuf.InvalidProtocolBufferException;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;

public class BBBHalGroup implements Runnable {
	Random rand = new Random(23424234);
	private String halCmdUri;
	private static Socket client = null;
	private ZContext ctx;

	public static void main(String[] args) {
		String halCmdUri = "";
		Discoverer disc = new Discoverer();
		disc.discover();
		ArrayList<ServiceInfo> al = disc.getDiscoveredServices();
		while (halCmdUri.equals("")) {
			for (ServiceInfo si : al) {
				if (si.getName().matches("HAL Rcommand.*")) {
					halCmdUri = "tcp://" + si.getServer() + ":" + si.getPort() + "/";
					break;
				}
			}
			if (!halCmdUri.equals(""))
				break;
			try {
				System.out.println("Still looking for halcmd service.");
				Thread.currentThread().sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		BBBHalGroup halGroup = new BBBHalGroup();
		halGroup.halCmdUri = halCmdUri;
		halGroup.getClient();
		Thread myThread = new Thread(halGroup);
		myThread.start();

		pb.Message.Container.Builder builder = Container.newBuilder();

		builder.setType(ContainerType.MT_HALRCOMMAND_DESCRIBE);
		// builder.setType(ContainerType.MT_PING);
		// builder.setTicket(MachineTalkCommand.getNextTicket());

		Container container = builder.build();

		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		client.send(buff, 0);
	}

	public void run() {
		getClient();

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
						contReturned = Message.Container.parseFrom(msg.getFirst().getData());
						System.out.println(contReturned.toString());
					} catch (InvalidProtocolBufferException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					msg.destroy();
				}
			}

		}
		ctx.destroy();
	}

	private void getClient() {
		ctx = new ZContext();
		client = ctx.createSocket(ZMQ.DEALER);

		// Set random identity to make tracing easier
		String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());
		client.setIdentity(identity.getBytes(ZMQ.CHARSET));
		client.connect(this.halCmdUri);
	}
}