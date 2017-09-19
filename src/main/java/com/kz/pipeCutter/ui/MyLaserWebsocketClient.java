package com.kz.pipeCutter.ui;

import java.net.URI;

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

	@OnOpen
	public void onOpen(Session session) {
		if (Settings.instance != null)
			Settings.instance.log("\tConnected to laser distance at: " + session.getRequestURI());
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
		// Settings.instance.log("\tGot message from laser at: " + uri + " message:"
		// + message);
		if (message.startsWith("Analogue")) {
			String value = message.substring(message.lastIndexOf(" "), message.length());
			
			Settings.instance.setSetting("mymotion.laserHeight1",value);
			//System.out.println(value);
		}
	}

	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@OnClose
	public void onClose() {
		Settings.instance.log("\tClose ws connection to laser at: " + uri);
	}

}
