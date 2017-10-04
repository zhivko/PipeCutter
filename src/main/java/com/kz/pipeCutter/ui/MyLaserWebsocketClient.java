package com.kz.pipeCutter.ui;

import java.awt.Color;
import java.net.URI;
import java.text.DecimalFormat;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class MyLaserWebsocketClient {
	public URI uri;

	// public MyLaserWebsocketClient(URI myUri) {
	// this.uri = myUri;
	// }

	long lastPingMilis = 0;
	public boolean isOn = false;

	private static MyLaserWebsocketClient instance;

	public static synchronized MyLaserWebsocketClient getInstance() {
		if (instance == null)
			instance = new MyLaserWebsocketClient();
		return instance;
	}

	public MyLaserWebsocketClient() {
		instance = this;
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
		if (message.startsWith("Analogue")) {
			String value = message.substring(message.lastIndexOf(" "), message.length());
			if (!value.trim().equals("")) {
				Settings.getInstance().setSetting("mymotion.laserHeight1", value);
				DecimalFormat df = new DecimalFormat("#.0");

				Settings.getInstance().setSetting("mymotion.laserHeight1mm", df.format(capToMM(Float.valueOf(value))));

				/*
				 * if (((SavableText)
				 * Settings.controls.get("mymotion.laserHeight1")).jValue.getBackground(
				 * ) != Color.GREEN) { Settings.getInstance(); ((SavableText)
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
		double ret = (9.57558 * Math.pow(10, -6)) / (Math.pow((8.008 - capSenseValue), 1.721361908491850));

		return ret;
	}

	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@OnClose
	public void onClose() {
		Settings.getInstance().log("\tClose ws connection to laser at: " + uri);
	}

}
