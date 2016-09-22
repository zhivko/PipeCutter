package com.kz.pipeCutter.BBB.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;
import com.kz.pipeCutter.ui.Settings;

public class MachinekitStart extends SSH_Command {

	@Override
	public void runSshCmd() throws Exception {
		if (!SSH_CheckIfMachinekitRunning()) {
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

			//channelShell.setAgentForwarding(true);
			//channelShell.setXForwarding(true);
			channelShell.connect(3*1000);

//			String command = "source ~/git/machinekit/scripts/rip-environment";
//			ps.println(command);
			String command = "machinekit ~/machinekit/configs/ARM.BeagleBone.CRAMPS/CRAMPS.ini &";
			ps.println(command);
			readOutput(channelShell);
			
			
		}

	}

	private void readOutput(ChannelShell channelShell) throws IOException, InterruptedException {
		InputStream in = channelShell.getInputStream();
		byte[] bt = new byte[1024];

		while (true) {
			String str = null;
			Thread.sleep(1000);
			while (in.available() > 0) {
				int i = in.read(bt, 0, 1024);
				if (i < 0)
					break;
				str = new String(bt, 0, i);
				// displays the output of the command executed.
				System.out.print(str);
			}
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

}
