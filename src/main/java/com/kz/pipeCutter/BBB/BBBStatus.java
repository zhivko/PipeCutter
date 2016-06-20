package com.kz.pipeCutter.BBB;

import java.awt.Frame;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;

import pb.Message;
import pb.Message.Container;
import pb.Status.EmcStatusMotionAxis;
import pb.Types.ContainerType;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.ui.Settings;

public class BBBStatus implements Runnable {
	private static BBBStatus instance;
	private org.zeromq.ZMQ.Socket socket = null;
	ByteArrayInputStream is;
	public ChannelExec channelExec = null;
	ZContext ctx;
	private String uri;

	static String identity;
	static {
		Random rand = new Random(23424234);
		identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());
	}

	private final static ScheduledExecutorService scheduler = Executors
			.newScheduledThreadPool(1);

	public BBBStatus() {
		initSocket();
		scheduler.scheduleAtFixedRate(this, 2000, 100, TimeUnit.MILLISECONDS);
		instance = this;
	}

	public static BBBStatus getInstance() {
		if (instance == null)
			instance = new BBBStatus();
		return instance;
	}

	public static void main(String[] args) {
		Settings sett = new Settings();
		sett.setVisible(true);
		BBBStatus status = new BBBStatus();
	}

	public Socket getSocket() {
		return this.socket;
	}

	@Override
	public void run() {
		try {
			if (!Settings.instance.isVisible())
				return;

			Container contReturned;
			ZMsg receivedMessage = ZMsg.recvMsg(socket);
			while (true) {
				// System.out.println("loop: " + i);
				ZFrame frame = receivedMessage.poll();
				if (frame == null) {
					break;
				}
				byte[] returnedBytes = frame.getData();
				String messageType = new String(returnedBytes);
				// System.out.println("type: " + messageType);
				if (!messageType.equals("motion")) {
					contReturned = Message.Container.parseFrom(returnedBytes);
					if (contReturned.getType().equals(
							ContainerType.MT_EMCSTAT_FULL_UPDATE)
							|| contReturned.getType().equals(
									ContainerType.MT_EMCSTAT_INCREMENTAL_UPDATE)) {

						// System.out.println(contReturned.getInterpState().toString());
						// System.out.println(contReturned.getEmcStatusInterp().getInterpreterErrcode().getValueDescriptor());

						Iterator<EmcStatusMotionAxis> itAxis = contReturned
								.getEmcStatusMotion().getAxisList().iterator();
						while (itAxis.hasNext()) {
							EmcStatusMotionAxis axis = itAxis.next();
							int index = axis.getIndex();
							switch (index) {
							case 0:
								final double x = contReturned.getEmcStatusMotion()
										.getActualPosition().getX();
								Settings.instance.setSetting("position_x", x);
								break;
							case 1:
								final double y = contReturned.getEmcStatusMotion()
										.getActualPosition().getY();
								Settings.instance.setSetting("position_y", y);
								break;
							case 2:
								final double z = contReturned.getEmcStatusMotion()
										.getActualPosition().getZ();
								Settings.instance.setSetting("position_z", z);
								break;
							case 3:
								final double a = contReturned.getEmcStatusMotion()
										.getActualPosition().getA();
								Settings.instance.setSetting("position_a", a);
								break;
							case 4:
								final double b = contReturned.getEmcStatusMotion()
										.getActualPosition().getB();
								Settings.instance.setSetting("position_b", b);
								break;
							case 5:
								final double c = contReturned.getEmcStatusMotion()
										.getActualPosition().getC();
								Settings.instance.setSetting("position_c", c);
								break;
							default:
								break;
							}

						}
					}
				}
			}

		} catch (Exception e) {
			if (!e.getMessage().equals("Unknown message type."))
				e.printStackTrace();
		}
	}
	
	public void initSocket() {
		if (ctx != null && socket != null) {
			socket.close();
			ctx.close();
		}

		uri = Settings.getInstance().getSetting("machinekit_statusService_url");

		ctx = new ZContext();
		// Set random identity to make tracing easier
		socket = ctx.createSocket(ZMQ.SUB);
		socket.setIdentity(identity.getBytes(ZMQ.CHARSET));
		socket.subscribe("motion".getBytes(ZMQ.CHARSET));
		socket.setReceiveTimeOut(100);
		socket.connect(this.uri);
	}	
	
}
