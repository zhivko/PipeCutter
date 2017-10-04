package com.kz.pipeCutter;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.net.telnet.EchoOptionHandler;
import org.apache.commons.net.telnet.InvalidTelnetOptionException;
import org.apache.commons.net.telnet.SuppressGAOptionHandler;
import org.apache.commons.net.telnet.TelnetClient;
import org.apache.commons.net.telnet.TelnetNotificationHandler;
import org.apache.commons.net.telnet.TerminalTypeOptionHandler;

/***
 * This is a simple example of use of TelnetClient. An external option handler
 * (SimpleTelnetOptionHandler) is used. Initial configuration requested by
 * TelnetClient will be: WILL ECHO, WILL SUPPRESS-GA, DO SUPPRESS-GA. VT100
 * terminal type will be subnegotiated.
 * <p>
 * Also, use of the sendAYT(), getLocalOptionState(), getRemoteOptionState() is
 * demonstrated. When connected, type AYT to send an AYT command to the server
 * and see the result. Type OPT to see a report of the state of the first 25
 * options.
 * <p>
 * 
 * @author Bruno D'Avanzo
 ***/
public class MyTelnetClient implements TelnetNotificationHandler {
	private TelnetClient tc = null;
	private String lastRead = "";
	private String ipAddress;
	private int remotePort;

	/***
	 * Main for the TelnetClientExample.
	 ***/

	public MyTelnetClient(String ipAddress, int remotePort) {
		this.lastRead = "";
		this.ipAddress = ipAddress;
		this.remotePort = remotePort;
	}

	public static void main(String[] args) throws Exception {
	}

	/***
	 * Callback method called when TelnetClient receives an option negotiation
	 * command.
	 * <p>
	 * 
	 * @param negotiation_code
	 *          - type of negotiation command received (RECEIVED_DO,
	 *          RECEIVED_DONT, RECEIVED_WILL, RECEIVED_WONT)
	 *          <p>
	 * @param option_code
	 *          - code of the option negotiated
	 *          <p>
	 ***/
	// @Override
	public void receivedNegotiation(int negotiation_code, int option_code) {
		String command = null;
		if (negotiation_code == TelnetNotificationHandler.RECEIVED_DO) {
			command = "DO";
		} else if (negotiation_code == TelnetNotificationHandler.RECEIVED_DONT) {
			command = "DONT";
		} else if (negotiation_code == TelnetNotificationHandler.RECEIVED_WILL) {
			command = "WILL";
		} else if (negotiation_code == TelnetNotificationHandler.RECEIVED_WONT) {
			command = "WONT";
		}
		System.out.println("Received " + command + " for option code " + option_code);
	}

	public void send(String txt) {
		try {

			String txt1 = txt.replace(",", ".");
			if (getTelnetClient() != null && getTelnetClient().getOutputStream() != null) {
				System.out.println("\t" + txt1);
				this.lastRead = "";
				getTelnetClient().getOutputStream().write((txt1 + "\n").getBytes());
				getTelnetClient().getOutputStream().flush();

				String response = readResponse();
				System.out.println(response);

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void disconnect() {
		try {
			getTelnetClient().disconnect();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String readResponse() {
		InputStream instr = getTelnetClient().getInputStream();
		StringBuilder lastRead = new StringBuilder();
		try {
			byte[] buff = new byte[1024];
			int ret_read = 0;

			do {
				ret_read = instr.read(buff);
				if (ret_read > 0) {
					String returnedString = new String(buff, 0, ret_read);
					lastRead.append(returnedString);
				}
			} while (!lastRead.toString().endsWith("> "));
		} catch (IOException e) {
			System.err.println("Exception while reading socket:" + e.getMessage());
		}

		return lastRead.toString();

	}

	private class ReaderThread extends Thread {
		/***
		 * Reader thread. Reads lines from the TelnetClient and echoes them on the
		 * screen.
		 ***/
		// @Override
		public void run() {
			InputStream instr = getTelnetClient().getInputStream();
			StringBuilder lastRead = new StringBuilder();
			try {
				byte[] buff = new byte[1024];
				int ret_read = 0;

				do {
					ret_read = instr.read(buff);
					if (ret_read > 0) {
						final String returnedString = new String(buff, 0, ret_read);
						lastRead.append(returnedString);
						if (lastRead.toString().endsWith("> ")) {
							SurfaceDemo.getInstance().smoothie.lastRead = lastRead.toString();
							lastRead.delete(0, lastRead.toString().length() - 1);
						}
						// System.out.print(returnedString);
					}
				} while (ret_read >= 0);
			} catch (IOException e) {
				System.err.println("Exception while reading socket:" + e.getMessage());
			}

			try {
				getTelnetClient().disconnect();
			} catch (IOException e) {
				System.err.println("Exception while closing telnet:" + e.getMessage());
			}
		}
	}

	public OutputStream getOutputStream() {
		// TODO Auto-generated method stub
		return getTelnetClient().getOutputStream();
	}

	public InputStream getInputStream() {
		// TODO Auto-generated method stub
		return getTelnetClient().getInputStream();
	}

	public TelnetClient getTelnetClient() {

		if (tc == null) {
			TelnetClient tc1 = new TelnetClient();

			TerminalTypeOptionHandler ttopt = new TerminalTypeOptionHandler("VT100", false, false, true, false);
			EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
			SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);

			try {
				tc1.addOptionHandler(ttopt);
				tc1.addOptionHandler(echoopt);
				tc1.addOptionHandler(gaopt);
			} catch (InvalidTelnetOptionException e) {
				System.err.println("Error registering option handlers: " + e.getMessage());
			}

			boolean end_loop = false;
			try {
				//tc1.setDefaultTimeout(4);
				tc1.connect(SmoothieUploader.smoothieIP, this.remotePort);
				tc1.registerNotifHandler(this);
				tc = tc1;
			} catch (IOException e) {
				System.err.println("Exception while connecting to: " + this.ipAddress + ":" + this.remotePort + "\n"
						+ e.getMessage());
			}

		}
		return tc;
	}

}
