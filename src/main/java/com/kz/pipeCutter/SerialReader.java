package com.kz.pipeCutter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/** */
public class SerialReader extends Thread {
	InputStream in;
	String lastResponse = "";
	List<ISerialListener> listeners;

	public SerialReader() {
		this.listeners = new ArrayList<ISerialListener>();
	}

	public SerialReader(InputStream in, ISerialListener listener) {
		this();
		this.in = in;
		this.listeners.add(listener);
	}

	public void run() {
		byte[] buffer = new byte[1024];
		int len = -1;
		try {

//			BufferedReader in = new BufferedReader(new InputStreamReader(this.in));
//			String line;
//
//			List<String> responseData = new ArrayList<String>();
//			while ((line = in.readLine()) != null) {
//				responseData.add(line);
//				for (ISerialListener listener : listeners) {
//					listener.justReadSomething(lastResponse);
//				}
//			}

			while ((len = this.in.read(buffer)) > -1) {
				// System.out.println("Received a signal.");
				lastResponse = new String(buffer, 0, len);
				if (!lastResponse.equals(""))
					for (ISerialListener listener : listeners) {
						listener.justReadSomething(lastResponse);
					}
				// System.out.print(lastResponse);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void registerListener(ISerialListener myListener) {
		listeners.add(myListener);
	}
}
