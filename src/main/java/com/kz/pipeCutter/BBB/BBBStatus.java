package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.jzy3d.maths.Coord3d;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.PollItem;
import org.zeromq.ZMQ.Poller;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.SurfaceDemo;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;

import pb.Message;
import pb.Message.Container;
import pb.Types.ContainerType;

public class BBBStatus implements Runnable {
	private final static int REQUEST_TIMEOUT = 2500;
	public static BBBStatus instance;
	private org.zeromq.ZMQ.Socket socket = null;
	ByteArrayInputStream is;
	public ChannelExec channelExec = null;
	ZContext ctx;
	private String uri;
	private Thread readThread;

	public double x = 0;
	public double y = 0;
	public double z = 0;
	public double a = 0;
	public double b = 0;
	public double c = 0;

	public double base_x = 0;
	public double base_y = 0;
	public double base_z = 0;
	public double base_a = 0;
	public double base_b = 0;
	public double base_c = 0;

	public double g92_x = 0;
	public double g92_y = 0;
	public double g92_z = 0;
	public double g92_a = 0;
	public double g92_b = 0;
	public double g92_c = 0;

	public double g5x_x = 0;
	public double g5x_y = 0;
	public double g5x_z = 0;
	public double g5x_a = 0;
	public double g5x_b = 0;
	public double g5x_c = 0;

	public double toolOff_x = 0;
	public double toolOff_y = 0;
	public double toolOff_z = 0;
	public double toolOff_a = 0;
	public double toolOff_b = 0;
	public double toolOff_c = 0;

	private long lastPingMs;
	private boolean shouldRead;
	private double pingDelay = 1500;

	public BBBStatus() {
		initSocket();
		instance = this;
	}

	public static BBBStatus getInstance() {
		if (instance == null)
			instance = new BBBStatus();
		return instance;
	}

	public static void main(String[] args) {
		Settings sett = Settings.getInstance();
		sett.setVisible(true);
		BBBStatus status = new BBBStatus();
	}

	public Socket getSocket() {
		return this.socket;
	}

	@Override
	public void run() {
		// ON BBB updates depends on [DISPLAY]CYCLE_TIME in the Ini file of
		// linuxcnc.

		if (!Settings.getInstance().isVisible())
			return;

		shouldRead = true;
		Container contReturned;
		while (shouldRead) {
			PollItem[] pollItems = new PollItem[] { new PollItem(socket, Poller.POLLIN) };
			int rc = ZMQ.poll(pollItems, 1, 100);
			if (rc == -1)
				break; // Interrupted
			for (int l = 0; l < rc; l++) {
				ZMsg msg = ZMsg.recvMsg(socket, REQUEST_TIMEOUT);
				ZFrame frame = null;
				while (pollItems[0].isReadable() && (frame = msg.poll()) != null) {
					byte[] returnedBytes = frame.getData(); // frame.getData();
					String messageType = new String(returnedBytes);
					if (!messageType.equals("motion") && !messageType.equals("task") && !messageType.equals("io")
							&& !messageType.equals("interp")) {
						try {
							contReturned = Message.Container.parseFrom(returnedBytes);
							if ((contReturned.getType().equals(ContainerType.MT_EMCSTAT_FULL_UPDATE)
									|| contReturned.getType().equals(ContainerType.MT_EMCSTAT_INCREMENTAL_UPDATE))) {

								if (contReturned.getEmcStatusMotion().hasActualPosition()) {
									if (contReturned.getEmcStatusMotion().getActualPosition().hasX()) {
										base_x = contReturned.getEmcStatusMotion().getActualPosition().getX();
									}
									if (contReturned.getEmcStatusMotion().getActualPosition().hasY()) {
										base_y = contReturned.getEmcStatusMotion().getActualPosition().getY();
									}
									if (contReturned.getEmcStatusMotion().getActualPosition().hasZ()) {
										base_z = contReturned.getEmcStatusMotion().getActualPosition().getZ();
									}
									// base_z =
									// Float.valueOf(BBBHalRComp.instance.halPins.get("myini.thc-z-pos"));

									if (contReturned.getEmcStatusMotion().getActualPosition().hasA()) {
										base_a = contReturned.getEmcStatusMotion().getActualPosition().getA();
									}
									if (contReturned.getEmcStatusMotion().getActualPosition().hasB()) {
										base_b = contReturned.getEmcStatusMotion().getActualPosition().getB();
									}
									if (contReturned.getEmcStatusMotion().getActualPosition().hasC()) {
										base_c = contReturned.getEmcStatusMotion().getActualPosition().getC();
									}
								}

								getG92Offset(contReturned);
								getG5XOffset(contReturned);
								getToolOffset(contReturned);

								// basePosition[axisName] -= g5xOffset[axisName] +
								// g92Offset[axisName] + toolOffset[axisName];
								x = base_x - (g5x_x + g92_x + toolOff_x);
								y = base_y - (g5x_y + g92_y + toolOff_y);
								z = base_z - (g5x_z + g92_z + toolOff_z);
								a = base_a - (g5x_a + g92_a + toolOff_a);
								b = base_b - (g5x_b + g92_b + toolOff_b);
								c = base_c - (g5x_c + g92_c + toolOff_c);

								Settings.getInstance().setSetting("position_x", x);
								Settings.getInstance().setSetting("position_y", y);
								Settings.getInstance().setSetting("position_z", z);
								Settings.getInstance().setSetting("position_a", a);
								Settings.getInstance().setSetting("position_b", b);
								// Settings.getInstance().setSetting("position_c", c);

								if (SurfaceDemo.getInstance() != null && SurfaceDemo.instance != null) {
									if (SurfaceDemo.getInstance().getChart() != null) {
										// System.out.println(String.format("%1$,.2f, %2$,.2f,
										// %3$,.2f",x,y,z));

										if (BBBStatus.instance != null) {
											float zOffset = 0;
											if (!Settings.instance.getSetting("myini.offset-value").equals(""))
												zOffset = Float.valueOf(Settings.instance.getSetting("myini.offset-value"));
											Coord3d coord = new Coord3d(BBBStatus.instance.x, BBBStatus.instance.y, BBBStatus.instance.z + zOffset);
											SurfaceDemo.getInstance().utils.rotatePoints(BBBStatus.instance.a, false, false);
											SurfaceDemo.getInstance().getPlasma().setPosition(coord);
										}

										try {
											SurfaceDemo.getInstance().redrawPosition();
										} catch (Exception ex) {
											ex.getSuppressed();
										}
									}
								}
							} else if (contReturned.getType().equals(ContainerType.MT_PING)) {
								this.lastPingMs = System.currentTimeMillis();
								MachinekitSettings.instance.pingStatus();
							} else {
								System.out.println(contReturned.getType());
							}

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		}

	}

	private void getToolOffset(Container contReturned) {
		if (contReturned.getEmcStatusIo().hasToolOffset()) {
			if (contReturned.getEmcStatusIo().getToolOffset().hasX())
				this.toolOff_x = contReturned.getEmcStatusIo().getToolOffset().getX();
			if (contReturned.getEmcStatusIo().getToolOffset().hasY())
				this.toolOff_y = contReturned.getEmcStatusIo().getToolOffset().getY();
			if (contReturned.getEmcStatusIo().getToolOffset().hasZ())
				this.toolOff_z = contReturned.getEmcStatusIo().getToolOffset().getZ();
			if (contReturned.getEmcStatusIo().getToolOffset().hasA())
				this.toolOff_a = contReturned.getEmcStatusIo().getToolOffset().getA();
			if (contReturned.getEmcStatusIo().getToolOffset().hasB())
				this.toolOff_b = contReturned.getEmcStatusIo().getToolOffset().getB();
			if (contReturned.getEmcStatusIo().getToolOffset().hasC())
				this.toolOff_c = contReturned.getEmcStatusIo().getToolOffset().getC();
		}
	}

	private void getG92Offset(Container contReturned) {
		if (contReturned.getEmcStatusMotion().hasG92Offset()) {
			if (contReturned.getEmcStatusMotion().getG92Offset().hasX())
				this.g92_x = contReturned.getEmcStatusMotion().getG92Offset().getX();
			if (contReturned.getEmcStatusMotion().getG92Offset().hasY())
				this.g92_y = contReturned.getEmcStatusMotion().getG92Offset().getY();
			if (contReturned.getEmcStatusMotion().getG92Offset().hasZ())
				this.g92_z = contReturned.getEmcStatusMotion().getG92Offset().getZ();
			if (contReturned.getEmcStatusMotion().getG92Offset().hasA())
				this.g92_a = contReturned.getEmcStatusMotion().getG92Offset().getA();
			if (contReturned.getEmcStatusMotion().getG92Offset().hasB())
				this.g92_b = contReturned.getEmcStatusMotion().getG92Offset().getB();
			if (contReturned.getEmcStatusMotion().getG92Offset().hasC())
				this.g92_c = contReturned.getEmcStatusMotion().getG92Offset().getC();
		}
	}

	private void getG5XOffset(Container contReturned) {
		if (contReturned.getEmcStatusMotion().hasG5XOffset()) {
			if (contReturned.getEmcStatusMotion().getG5XOffset().hasX())
				this.g5x_x = contReturned.getEmcStatusMotion().getG5XOffset().getX();
			if (contReturned.getEmcStatusMotion().getG5XOffset().hasY())
				this.g5x_y = contReturned.getEmcStatusMotion().getG5XOffset().getY();
			if (contReturned.getEmcStatusMotion().getG5XOffset().hasZ())
				this.g5x_z = contReturned.getEmcStatusMotion().getG5XOffset().getZ();
			if (contReturned.getEmcStatusMotion().getG5XOffset().hasA())
				this.g5x_a = contReturned.getEmcStatusMotion().getG5XOffset().getA();
			if (contReturned.getEmcStatusMotion().getG5XOffset().hasB())
				this.g5x_b = contReturned.getEmcStatusMotion().getG5XOffset().getB();
			if (contReturned.getEmcStatusMotion().getG5XOffset().hasC())
				this.g5x_c = contReturned.getEmcStatusMotion().getG5XOffset().getC();
		}
	}

	public void initSocket() {
		if (readThread != null) {
			shouldRead = false;
			while (readThread.isAlive()) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
					readThread.interrupt();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (ctx != null && socket != null) {
			ctx.destroy();
		}

		uri = Settings.getInstance().getSetting("machinekit_statusService_url");

		ctx = new ZContext();
		// Set random identity to make tracing easier
		socket = ctx.createSocket(ZMQ.SUB);

		Random rand = new Random(23424234);
		String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());

		socket.setIdentity(identity.getBytes());

		socket.setHWM(10000);
		socket.setReceiveTimeOut(5);
		socket.setSendTimeOut(1000);
		socket.connect(this.uri);

		reSubscribeMotion();

		readThread = new Thread(this);
		readThread.setName("BBBStatus");
		readThread.start();
	}

	public boolean isAlive() {
		if (this.lastPingMs != 0)
			if ((System.currentTimeMillis() - this.lastPingMs) < 3000)
				return true;
			else
				return false;
		else
			return false;
	}

	public void reSubscribeMotion() {
		initOffsets();

		socket.unsubscribe("motion".getBytes());
		socket.unsubscribe("task".getBytes());
		socket.unsubscribe("io".getBytes());
		socket.unsubscribe("interp".getBytes());

		socket.subscribe("motion".getBytes());
		socket.subscribe("task".getBytes());
		socket.subscribe("io".getBytes());
		socket.subscribe("interp".getBytes());
	}

	public void initOffsets() {
		x = 0;
		y = 0;
		z = 0;
		a = 0;
		b = 0;
		c = 0;

		base_x = 0;
		base_y = 0;
		base_z = 0;
		base_a = 0;
		base_b = 0;
		base_c = 0;

		g92_x = 0;
		g92_y = 0;
		g92_z = 0;
		g92_a = 0;
		g92_b = 0;
		g92_c = 0;

		g5x_x = 0;
		g5x_y = 0;
		g5x_z = 0;
		g5x_a = 0;
		g5x_b = 0;
		g5x_c = 0;

		toolOff_x = 0;
		toolOff_y = 0;
		toolOff_z = 0;
		toolOff_a = 0;
		toolOff_b = 0;
		toolOff_c = 0;
	}

	public void stop() {
		this.shouldRead = false;
	}

}
