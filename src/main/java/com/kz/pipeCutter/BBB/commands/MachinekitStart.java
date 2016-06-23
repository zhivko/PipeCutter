package com.kz.pipeCutter.BBB.commands;

import java.util.concurrent.TimeUnit;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;

public class MachinekitStart extends SSH_Command {

	@Override
	public void runSshCmd() throws Exception {
		if (!SSH_CheckIfMachinekitRunning()) {
			String command = "machinekit /home/machinekit/machinekit/configs/ARM.BeagleBone.CRAMPS/CRAMPS.ini &";
			channelExec = (ChannelExec) session.openChannel("exec");
			MyOutputStreamReader myOut = new MyOutputStreamReader();
			channelExec.setOutputStream(myOut);
			channelExec.setCommand(command);
			channelExec.connect(3 * 1000);
			while (channelExec.getExitStatus() == -1) {
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			channelExec.disconnect();
		}
	}
	
	public boolean SSH_CheckIfMachinekitRunning() {
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
					return true;
				}
			}
			channelExec.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}	

}
