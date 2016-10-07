package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.zeromq.ZContext;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.MyPickablePoint;
import com.kz.pipeCutter.SurfaceDemo;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.GcodeViewer;
import com.kz.pipeCutter.ui.tab.MachinekitSettings;

import pb.Message;
import pb.Message.Container;
import pb.Status.EmcStatusMotionAxis;
import pb.Status.EmcTaskExecStateType;
import pb.Types.ContainerType;

public class BBBPreviewStatus implements Runnable {
	public static BBBPreviewStatus instance;
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
	private long lastPingMs;

	public BBBPreviewStatus() {
		initSocket();
		instance = this;
	}

	public static BBBPreviewStatus getInstance() {
		if (instance == null)
			instance = new BBBPreviewStatus();
		return instance;
	}

	public static void main(String[] args) {
		Settings sett = Settings.getInstance();
		sett.setVisible(true);
		BBBPreviewStatus status = new BBBPreviewStatus();
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

		Container contReturned;
		while (!readThread.isInterrupted()) {
			try {
				ZMsg receivedMessage = ZMsg.recvMsg(socket, ZMQ.DONTWAIT);
				// System.out.println("loop: " + i);
				if (receivedMessage != null) {
					while (!receivedMessage.isEmpty()) {

						ZFrame frame = receivedMessage.poll();
						byte[] returnedBytes = frame.getData();
						String messageType = new String(returnedBytes);
						// System.out.println("type: " + messageType);
						if (!messageType.equals("motion") && !messageType.equals("task") && !messageType.equals("io") && !messageType.equals("interp")) {

							contReturned = Message.Container.parseFrom(returnedBytes);
							if ((contReturned.getType().equals(ContainerType.MT_EMCSTAT_FULL_UPDATE)
									|| contReturned.getType().equals(ContainerType.MT_EMCSTAT_INCREMENTAL_UPDATE))) {

								if (contReturned.getEmcStatusMotion().hasActualPosition()) {
									if (contReturned.getEmcStatusMotion().getActualPosition().hasX()) {
										x = contReturned.getEmcStatusMotion().getActualPosition().getX();
										Settings.getInstance().setSetting("position_x", x);
									}
									if (contReturned.getEmcStatusMotion().getActualPosition().hasY()) {
										y = contReturned.getEmcStatusMotion().getActualPosition().getY();
										Settings.getInstance().setSetting("position_y", y);
									}
									if (contReturned.getEmcStatusMotion().getActualPosition().hasZ()) {
										z = contReturned.getEmcStatusMotion().getActualPosition().getZ();
										Settings.getInstance().setSetting("position_z", z);
									}
									if (contReturned.getEmcStatusMotion().getActualPosition().hasA()) {
										a = contReturned.getEmcStatusMotion().getActualPosition().getA();
										Settings.getInstance().setSetting("position_a", a);
									}
									if (contReturned.getEmcStatusMotion().getActualPosition().hasB()) {
										b = contReturned.getEmcStatusMotion().getActualPosition().getB();
										Settings.getInstance().setSetting("position_b", b);
									}
									if (contReturned.getEmcStatusMotion().getActualPosition().hasC()) {
										c = contReturned.getEmcStatusMotion().getActualPosition().getC();
										Settings.getInstance().setSetting("position_c", c);
									}
								}

								if (SurfaceDemo.getInstance() != null && SurfaceDemo.instance != null) {
									if (SurfaceDemo.getInstance().getChart() != null) {
										// System.out.println(String.format("%1$,.2f, %2$,.2f,
										// %3$,.2f",x,y,z));

										if (BBBPreviewStatus.instance != null) {
											Coord3d coord = new Coord3d(BBBPreviewStatus.instance.x, BBBPreviewStatus.instance.y, BBBPreviewStatus.instance.z);
											SurfaceDemo.getInstance().utils.rotatePoints(BBBPreviewStatus.instance.a, false, false);

											SurfaceDemo.getInstance().getPlasma().setPosition(coord);
											SurfaceDemo.getInstance().redrawPosition();
										}

										SurfaceDemo.getInstance().redrawPosition();
									}
								}
							} else if (contReturned.getType().equals(ContainerType.MT_PING)) {
								this.lastPingMs = System.currentTimeMillis();
								MachinekitSettings.instance.pingStatus();
							} else {
								System.out.println(contReturned.getType());
							}
						}
					}
					receivedMessage.destroy();
					receivedMessage = null;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			// try {
			// TimeUnit.MILLISECONDS.sleep(200);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
	}

	public void initSocket() {
		if (readThread != null && readThread.isAlive()) {
			readThread.interrupt();
			while (readThread.isAlive()) {
				try {
					TimeUnit.MILLISECONDS.sleep(500);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		if (ctx != null && socket != null) {
			ctx.destroySocket(socket);
			ctx.destroy();
		}

		uri = Settings.getInstance().getSetting("machinekit_previewstatusService_url");

		ctx = new ZContext();
		// Set random identity to make tracing easier
		socket = ctx.createSocket(ZMQ.SUB);

		Random rand = new Random(23424234);
		String identity = String.format("%04X-%04X", rand.nextInt(), rand.nextInt());

		socket.setIdentity(identity.getBytes());
		socket.subscribe("preview".getBytes());
		socket.subscribe("status".getBytes());
		// socket.setHWM(10000);
		socket.setReceiveTimeOut(5);
		socket.setSendTimeOut(1000);
		socket.connect(this.uri);

		readThread = new Thread(this);
		readThread.setName("BBBPreviewStatus");
		readThread.start();
				
		
	}

	public boolean isAlive() {
		if (this.lastPingMs != 0)
			return (System.currentTimeMillis() - this.lastPingMs > 1000);
		else
			return false;
	}

	public void reSubscribeMotion() {
		socket.unsubscribe("motion".getBytes());
		socket.subscribe("motion".getBytes());
	}

}
