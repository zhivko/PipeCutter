package com.kz.pipeCutter.ui;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;

@ClientEndpoint
public class MyWebsocketClient {
	public Positioner positioner;

	public MyWebsocketClient(Positioner positioner) {
		// TODO Auto-generated constructor stub
		this.positioner = positioner;
	}

	@OnOpen
	public void onOpen(Session session) {
		if(Settings.instance!=null)
			Settings.instance.log("Connected to: " + session.getRequestURI() + "\n");
		this.positioner.isConnected = true;
	}

	@OnMessage
	public void onMessage(String message) {
		String res[] = message.split(" ");
		if (message.substring(0, 1).equals("X") && res.length == 5) {
			positioner.x = Double.valueOf(res[0].substring(1, res[0].length()));
			positioner.y = Double.valueOf(res[1].substring(1, res[1].length()));
			positioner.z = Double.valueOf(res[2].substring(1, res[2].length()));
			positioner.e = Double.valueOf(res[3].substring(1, res[3].length()));
			if (res[4].endsWith("1"))
				positioner.m = true;
			else
				positioner.m = false;
		}
	}

	@OnError
	public void onError(Throwable t) {
		t.printStackTrace();
	}

	@OnClose
	public void onClose() {
		positioner.isConnected = false;
	}

}