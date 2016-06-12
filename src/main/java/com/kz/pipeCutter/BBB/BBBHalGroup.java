package com.kz.pipeCutter.BBB;

import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.zeromq.ZFrame;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Context;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMsg;

import com.kz.pipeCutter.BBB.commands.MachineTalkCommand;
import com.kz.pipeCutter.ui.Settings;

import pb.Message;
import pb.Message.Container;
import pb.Object.ParamOrBuilder;
import pb.Status.EmcStatusMotionAxis;
import pb.Types.ContainerType;

public class BBBHalGroup {
	static Socket halGrpSocket = null;
	static Socket halCmdSocket = null;
	static BBBHalGroup instance = null;
	public static ZMQ.Poller items = null;

	ByteArrayInputStream is;

	private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

	public BBBHalGroup() {
		getHalCmdSocket();

		// ping
		pb.Message.Container.Builder builder = Container.newBuilder();
		
		// motion.program-line
		
		pb.Object.Component component = pb.Object.Component.newBuilder().setName("motion").setNoCreate(false).build();
		pb.Object.Pin pin = pb.Object.Pin.newBuilder().setName("program-line").setType(pb.Types.ValueType.HAL_S32).setDir(pb.Types.HalPinDirection.HAL_OUT).build(); 
		//component.getParamOrBuilderList().set(0, (ParamOrBuilder)pin);
		
		
		builder.setType(ContainerType.MT_HALRCOMP_BIND);
		builder.setTicket(MachineTalkCommand.getNextTicket());

		Container container = builder.build();
		byte[] buff = container.toByteArray();
		String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
		System.out.println("Message: " + hexOutput);
		halCmdSocket.send(buff, 0);
		ZMsg receivedMessage = ZMsg.recvMsg(halCmdSocket);
		if (receivedMessage != null) {
			for (ZFrame f : receivedMessage) {
				byte[] returnedBytes = f.getData();
				try {
					Container contReturned = Message.Container.parseFrom(returnedBytes);
					Settings.instance.log(contReturned.toString());
					// System.out.println(contReturned.toString());
					// System.out.println(contReturned.getOperatorError().toString());
					// List<String> notes = contReturned.getNoteList();
					// for (String note : notes) {
					// System.out.println("\t" + note);
					// }
				} catch (Exception ex) {
					ex.printStackTrace();
				}

			}
		}

		getHalGrpSocket();
		Runnable errorReporter = new Runnable() {
			@Override
			public void run() {
				try {
					if (!Settings.instance.isVisible())
						return;

					Container contReturned;
					ZMsg receivedMessage = ZMsg.recvMsg(getHalGrpSocket());
					// System.out.println("loop: " + i);
					if (receivedMessage == null)
						return;
					for (ZFrame f : receivedMessage) {
						byte[] returnedBytes = f.getData();
						String messageType = new String(returnedBytes);
						// System.out.println("type: " + messageType);
						if (!messageType.equals("motion")) {
							contReturned = Message.Container.parseFrom(returnedBytes);
							if (contReturned.getType().equals(ContainerType.MT_EMCSTAT_FULL_UPDATE)
									|| contReturned.getType().equals(ContainerType.MT_EMCSTAT_INCREMENTAL_UPDATE)) {

								// System.out.println(contReturned.getInterpState().toString());
								// System.out.println(contReturned.getEmcStatusInterp().getInterpreterErrcode().getValueDescriptor());

								Iterator<EmcStatusMotionAxis> itAxis = contReturned.getEmcStatusMotion().getAxisList()
										.iterator();
								while (itAxis.hasNext()) {
									EmcStatusMotionAxis axis = itAxis.next();
									int index = axis.getIndex();
									switch (index) {
									case 0:
										final double x = contReturned.getEmcStatusMotion().getActualPosition().getX();
										Settings.instance.setSetting("position_x", x);
										break;
									case 1:
										final double y = contReturned.getEmcStatusMotion().getActualPosition().getY();
										Settings.instance.setSetting("position_y", y);
										break;
									case 2:
										final double z = contReturned.getEmcStatusMotion().getActualPosition().getZ();
										Settings.instance.setSetting("position_z", z);
										break;
									case 3:
										final double a = contReturned.getEmcStatusMotion().getActualPosition().getA();
										Settings.instance.setSetting("position_a", a);
										break;
									case 4:
										final double b = contReturned.getEmcStatusMotion().getActualPosition().getB();
										Settings.instance.setSetting("position_b", b);
										break;
									case 5:
										final double c = contReturned.getEmcStatusMotion().getActualPosition().getC();
										Settings.instance.setSetting("position_c", c);
										break;
									default:
										break;
									}

								}
								// SwingUtilities.invokeLater(new Runnable() {
								// @Override
								// public void run() {
								// Settings.instance.setSetting("position_y",
								// y);
								// Settings.instance.setSetting("position_z",
								// z);
								// Settings.instance.setSetting("position_a",
								// a);
								// Settings.instance.setSetting("position_b",
								// b);
								// Settings.instance.setSetting("position_c",
								// c);
								// }
								// });
							}
						}
					}
				} catch (Exception e) {
					if (!e.getMessage().equals("Unknown message type."))
						e.printStackTrace();
				}
			}
		};
		scheduler.scheduleAtFixedRate(errorReporter, 0, 10, TimeUnit.MILLISECONDS);
		// scheduler.schedule(errorReporter, 0, TimeUnit.SECONDS);
		halGrpSocket = null;
		instance = this;

	}

	public static void main(String[] args) {

		Settings sett = new Settings();
		sett.setVisible(true);

		BBBHalGroup halGroup = new BBBHalGroup();

	}

	private static Socket getHalGrpSocket() {

		if (halGrpSocket != null)
			return halGrpSocket;

		String halGrpUrl = Settings.getInstance().getSetting("machinekit_halGroupService_url");

		Context con = ZMQ.context(1);
		halGrpSocket = con.socket(ZMQ.SUB);
		halGrpSocket.setReceiveTimeOut(10000);
		halGrpSocket.setLinger(0);
		halGrpSocket.setIdentity("id654645".getBytes());
		halGrpSocket.subscribe("default".getBytes());

		halGrpSocket.connect(halGrpUrl);

		// ZMQ.Poller poller = new ZMQ.Poller(1);
		// poller.register(halGrpSocket);
		// poller.poll();
		// PollItem item = poller.getItem(0);

		return halGrpSocket;
	}

	private static Socket getHalCmdSocket() {

		if (halCmdSocket != null)
			return halCmdSocket;

		String halGrpUrl = Settings.getInstance().getSetting("machinekit_halCmdService_url");

		Context con = ZMQ.context(1);
		halCmdSocket = con.socket(ZMQ.DEALER);
		halCmdSocket.setReceiveTimeOut(10000);
		halCmdSocket.setLinger(0);
		halCmdSocket.setIdentity("id654645".getBytes());

		halCmdSocket.connect(halGrpUrl);

		// ZMQ.Poller poller = new ZMQ.Poller(1);
		// poller.register(halGrpSocket);
		// poller.poll();
		// PollItem item = poller.getItem(0);

		return halCmdSocket;
	}

	// public void ping() throws Exception {
	// System.out.println(new Object() {
	// }.getClass().getEnclosingMethod().getName());
	//
	// byte[] buff;
	// Container container =
	// Container.newBuilder().setType(Types.ContainerType.MT_PING).setTicket(ticket++).build();
	// buff = container.toByteArray();
	// String hexOutput = javax.xml.bind.DatatypeConverter.printHexBinary(buff);
	// System.out.println("Mesage: " + hexOutput);
	// getCommandSocket().send(buff);
	//
	// parseAndOutput();
	// }

}
