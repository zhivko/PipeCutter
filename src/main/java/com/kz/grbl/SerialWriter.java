package com.kz.grbl;

import java.io.IOException;
import java.io.OutputStream;

/** */
public class SerialWriter extends Thread {
	OutputStream out;

	public SerialWriter(OutputStream out) {
		this.out = out;
	}

	public void run() {
		while(true)
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public void send(String command) {
		try {

			this.out.write((command + "\n").getBytes());
			this.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}