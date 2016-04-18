package com.kz.pipeCutter.BBB.commands;

import java.io.File;
import java.io.FileInputStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;

public class MachinekitUpload extends SSH_Command {

	@Override
	public void runSshCmd() throws Exception {
		if (!SSH_CheckIfMachinekitRunning()) {
			String command = "machinekit /home/machinekit/machinekit/configs/ARM.BeagleBone.CRAMPS/CRAMPS.ini &";
			ChannelSftp channelSFtp = (ChannelSftp) session.openChannel("sftp");
			MyOutputStreamReader myOut = new MyOutputStreamReader();
			channelSFtp.setOutputStream(myOut);
			channelSFtp.connect(3 * 1000);
			while (channelExec.getExitStatus() == -1) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			channelSFtp.cd("/home/machinekit/machinekit/nc_files");
			String from = "/home/kz/git/PipeCutter/prog.gcode";
			FileInputStream fis = new FileInputStream(new File(from));
			channelSFtp.put(fis,"prog.gcode");
			channelSFtp.disconnect();
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
					Thread.sleep(1000);
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
