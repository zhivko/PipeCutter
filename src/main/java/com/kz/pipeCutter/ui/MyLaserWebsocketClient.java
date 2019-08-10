package com.kz.pipeCutter.ui;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

import com.kz.pipeCutter.BBB.predict.PolyTrendLine;
import com.kz.pipeCutter.ui.tab.XYZSettings;

@ClientEndpoint
public class MyLaserWebsocketClient {
	public URI uri;

	public float value;
	// public MyLaserWebsocketClient(URI myUri) {
	// this.uri = myUri;
	// }
	private static PolyTrendLine ptl;

	public void init() {
//		File f = new File("./capsense.csv");
//		Path p = Paths.get(f.toURI());
//		try {
//			List<String> lines = Files.readAllLines(p);
//
//			if (lines.size() > 0) {
//
//				double[] x = new double[lines.size()];
//				double[] y = new double[lines.size()];
//
//				int i = 0;
//				for (String line : lines) {
//					String[] xy = line.split(",");
//					x[i] = Double.valueOf(xy[1]);
//					y[i] = Double.valueOf(xy[0]);
//					i++;
//				}
//
//				ptl = new PolyTrendLine(4);
//				ptl.setValues(y, x);
//			}
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

	long lastPingMilis = 0;
	public boolean isOn = false;

	private static MyLaserWebsocketClient instance;

	public static synchronized MyLaserWebsocketClient getInstance() {
		if (instance == null)
			instance = new MyLaserWebsocketClient();
		return instance;
	}

	public MyLaserWebsocketClient() {
		init();
	}

	@OnOpen
	public void onOpen(Session session) {
		if (Settings.getInstance() != null)
			Settings.getInstance().log("\tConnected to laser distance at: " + session.getRequestURI());
		this.uri = session.getRequestURI();
		// try {
		// session.getBasicRemote().sendText("udpServerIP " + setUdpServerIP + " " +
		// setPort);
		// } catch (IOException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
	}

	@OnMessage
	public void onMessage(String message) {
		// Settings.getInstance().log("\tGot message from laser at: " + uri + "
		// message:"
		// + message);
		if (message.startsWith("distance")) {
			String valueStr = message.split(" ")[1];
			if (!valueStr.trim().equals("")) {
				value = Float.valueOf(valueStr);
				Settings.getInstance().setSetting("mymotion.laserHeight1", valueStr);
				Settings.getInstance().setSetting("mymotion.laserHeight1mm", value);
			}
		} else if (message.startsWith("Analogue")) {
			String valueStr = message.substring(message.lastIndexOf(" "), message.length());
			if (!valueStr.trim().equals("")) {
				value = Float.valueOf(valueStr);
				Settings.getInstance().setSetting("mymotion.laserHeight1", valueStr);
				DecimalFormat df = new DecimalFormat("#.0");
				double val = capToMM(Float.valueOf(valueStr));
				Settings.getInstance().setSetting("mymotion.laserHeight1mm", val);

				Thread.yield();

				/*
				 * if (((SavableText)
				 * Settings.controls.get("mymotion.laserHeight1")).jValue.getBackground( ) !=
				 * Color.GREEN) { Settings.getInstance(); ((SavableText)
				 * Settings.controls.get("mymotion.laserHeight1")).jValue.setBackground(
				 * Color.GREEN); } else ((SavableText)
				 * Settings.controls.get("mymotion.laserHeight1")).jValue.setBackground(
				 * Color.WHITE);
				 */

				if (System.currentTimeMillis() > lastPingMilis + 500) {
					isOn = false;
				} else {
					isOn = true;
				}
				lastPingMilis = System.currentTimeMillis();

			}
		}
	}

	private double capToMM(Float capSenseValue) {
		// TODO Auto-generated method stub
		// double ret = (9.57558 * Math.pow(10, -6)) / (Math.pow((8.008 -
		// capSenseValue), 1.721361908491850));
		// double ret = (6.92752* Math.pow(10,
		// -7))/(Math.pow(1-capSenseValue,2.163873198692411));

		// double ret = 1.010286660820*Math.pow(10, -3302) * Math.pow(Math.E,
		// 949.613*capSenseValue);
		if (ptl == null)
			return -1.0;
		return ptl.predict(capSenseValue);
	}

	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@OnClose
	public void onClose() {
		Settings.getInstance().log("\tClose ws connection to laser at: " + uri);
		Settings.getInstance().log("\tTrying to reconnect to: " + uri);
		XYZSettings.getInstance().makeWebsocketConnection();
	}

}
