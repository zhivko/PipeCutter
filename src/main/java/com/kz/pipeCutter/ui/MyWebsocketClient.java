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
			Settings.instance.log("\tConnected to: " + session.getRequestURI() + "\n");
		this.positioner.isConnected = true;
	}

	@OnMessage
	public void onMessage(String message) {
		String res[] = message.split(" ");
		if (message.substring(0, 1).equals("X") && res.length == 5) {
			positioner.x = Long.valueOf(res[0].substring(1, res[0].length()));
			positioner.y = Long.valueOf(res[1].substring(1, res[1].length()));
			positioner.z = Long.valueOf(res[2].substring(1, res[2].length()));
			positioner.e = Long.valueOf(res[3].substring(1, res[3].length()));
			if (res[4].endsWith("1"))
			{
				positioner.m = true;
				positioner.linkedJogEnableCheckBox.setParValue("1");
			}
			else
			{
				positioner.m = false;
				positioner.linkedJogEnableCheckBox.setParValue("0");
			}
			positioner.initToolTips();
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