package com.kz.pipeCutter.BBB.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.ui.Settings;

public class MachinekitRunPostgui {

	public void start()
	{
		
		TimerTask timerTask = new TimerTask() {
			
			@Override
			public void run() {
				try {
					MachinekitRunPostgui.this.runSshCmd();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		(new Timer()).schedule(timerTask, 15000);
	}
	
	public void runSshCmd() throws Exception {

		// this.SSH_Login();
		JSch jsch = new JSch();
		Session session;

		String ip = Settings.getInstance().getSetting("machinekit_ip");
		String user = Settings.getInstance().getSetting("machinekit_user");
		String pass = Settings.getInstance().getSetting("machinekit_password");
		Settings.getInstance().log("MK instance at IP: " + ip);
		// Settings.instance.log("MK instance at host: " + host);
		session = jsch.getSession(user, ip, 22);
		session.setPassword(pass);

		session.setConfig("StrictHostKeyChecking", "no");
		session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");

		session.setServerAliveInterval(2000);
		session.setServerAliveCountMax(Integer.MAX_VALUE);

		session.setOutputStream(System.out);
		session.connect(15000); // making a connection with timeout.

		ChannelShell channelShell = (ChannelShell) session.openChannel("shell");
		OutputStream ops = channelShell.getOutputStream();
		PrintStream ps = new PrintStream(ops, true);

		// channelShell.setAgentForwarding(true);
		// channelShell.setXForwarding(true);
		channelShell.connect(5 * 1000);

		// String command = "source ~/git/machinekit/scripts/rip-environment";
		// ps.println(command);
		try {
			Settings.instance.log("Running postgui hal....");
			String command = "halcmd -f /home/machinekit/machinekit/configs/ARM.BeagleBone.CRAMPS/3D.postgui.hal\n";
			ps.println(command);
			readOutput(channelShell);
		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			channelShell.disconnect();
		}
		Settings.instance.log("Running postgui hal. DONE.");
	}

	private void readOutput(ChannelShell channelShell) throws IOException, InterruptedException {
		InputStream in = channelShell.getInputStream();
		BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));

		int timeOutMs = 20 * 1000;

		long startMs = System.currentTimeMillis();
		String line;
		while (System.currentTimeMillis() < (startMs + timeOutMs)) {
			line = buffReader.readLine();
			if (line != null)
				Logger.getLogger(this.getClass()).info(line);
			Thread.sleep(100);
		}
		System.out.println("sf");
	}

}
