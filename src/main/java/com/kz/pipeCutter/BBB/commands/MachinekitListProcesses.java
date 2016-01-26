package com.kz.pipeCutter.BBB.commands;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;

public class MachinekitListProcesses extends SSH_Command {

	@Override
	public void runSshCmd() throws Exception {
		String command = "ps -aux | grep CRAMPS.ini";
		channelExec = (ChannelExec) session.openChannel("exec");
		channelExec.setCommand(command);
		MyOutputStreamReader myOut = new MyOutputStreamReader();
		channelExec.setOutputStream(myOut);
		channelExec.connect(3 * 1000);
		while (channelExec.getExitStatus() == -1) {
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		channelExec.disconnect();
		session.disconnect();
	}

}
