package com.kz.pipeCutter.BBB.commands;

import java.util.concurrent.TimeUnit;

import javax.swing.SwingWorker;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;
import com.kz.pipeCutter.ui.Settings;

public abstract class SSH_Command {
	private static JSch jsch;
	protected static ChannelExec channelExec = null;
	protected static Session session;

	public abstract void runSshCmd() throws Exception;

	public void start() {
//		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
//			@Override
//			protected Void doInBackground() throws Exception {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							SSH_Login();
							runSshCmd();
						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
				});
				t.start();
//				return null;
//			};
//		};
//		sw.run();
	}

	public static void SSH_Login() throws Exception {
		if (jsch == null || session == null || !session.isConnected()) {
			jsch = new JSch();
			String host = Settings.getInstance().getSetting("machinekit_host");

			String ip = Settings.getInstance().getSetting("machinekit_ip");
			String user = Settings.getInstance().getSetting("machinekit_user");
			String pass = Settings.getInstance().getSetting("machinekit_password");
			Settings.getInstance().log("MK instance at IP: " + ip);
			// Settings.getInstance().log("MK instance at host: " + host);
			session = jsch.getSession(user, ip, 22);
			session.setPassword(pass);

			session.setConfig("StrictHostKeyChecking", "no");
			session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");

			session.setServerAliveInterval(2000);
			session.setServerAliveCountMax(Integer.MAX_VALUE);

			session.setOutputStream(System.out);
			session.connect(30000); // making a connection with timeout.

		}
	}
	
	public boolean SSH_CheckIfMachinekitRunning() {
		boolean ret = false;
		try {
			this.SSH_Login();
			// try to see if machinekit lready running
			String command = "ps -aux | grep machinekit";
			MyOutputStreamReader myOut = new MyOutputStreamReader();
			channelExec = (ChannelExec) session.openChannel("exec");
			channelExec.setCommand(command.getBytes());
			channelExec.setOutputStream(myOut);
			channelExec.connect();
			while (channelExec.getExitStatus() == -1) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			channelExec.disconnect();

			for (String line : myOut.getLines()) {
				if (line.matches("(.*)CRAMPS.ini")) {
					System.out.println("MachineKit already started");
					channelExec.disconnect();
					ret = true;
					break;
				}
			}
			channelExec.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}

	protected boolean shouldStop = false;
	public void stop()
	{
		this.shouldStop = true;
	}

}
