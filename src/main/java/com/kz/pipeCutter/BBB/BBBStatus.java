package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.ContextFactory;
import org.zeromq.api.Context;
import org.zeromq.api.Message.Frame;
import org.zeromq.api.MessageFlag;
import org.zeromq.api.Socket;
import org.zeromq.api.SocketType;
import org.zeromq.jzmq.sockets.SocketBuilder;

import pb.Message;
import pb.Message.Container;
import pb.Status.EmcStatusMotionAxis;
import pb.Types.ContainerType;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.ui.Settings;

public class BBBStatus implements Runnable {
	private static BBBStatus instance;
	private Socket socket = null;
	ByteArrayInputStream is;
	public ChannelExec channelExec = null;
	Context ctx;

	static String identity;
	static
	{
		Random rand = new Random(23424234);
		identity = String
				.format("%04X-%04X", rand.nextInt(), rand.nextInt());
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

	public void initSocket() {
		if(ctx!=null && socket!=null)
		{
			socket.close();
			ctx.close();
		}
		String statusUrl = Settings.getInstance().getSetting(
				"machinekit_statusService_url");
		ctx = ContextFactory.createContext(1);
		// Set random identity to make tracing easier
		SocketBuilder builder = ctx.buildSocket(SocketType.SUB).asSubscribable()
				.subscribe("motion".getBytes());
		builder = builder.withIdentity(identity.getBytes());
		builder = builder.withReceiveTimeout(1000);
		socket = builder.connect(statusUrl);
	}

	public Socket getSocket()
	{
		return this.socket;
	}
	
	@Override
	public void run() {
		try {
			if (!Settings.instance.isVisible())
				return;

			Container contReturned;
			org.zeromq.api.Message receivedMessage = socket.receiveMessage();
			// System.out.println("loop: " + i);
			if (receivedMessage != null) {
				for (Frame f : receivedMessage.getFrames()) {
					byte[] returnedBytes = f.getData();
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
			}
		} catch (Exception e) {
			if (!e.getMessage().equals("Unknown message type."))
				e.printStackTrace();
		}
	}
}
