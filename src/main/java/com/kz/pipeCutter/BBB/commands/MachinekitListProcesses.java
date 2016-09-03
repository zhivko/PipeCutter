package com.kz.pipeCutter.BBB.commands;

import java.util.concurrent.TimeUnit;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;

public class MachinekitListProcesses extends SSH_Command {

	@Override
	public void runSshCmd() throws Exception {
		// ps -aux | grep 'CRAMPS.ini\|rtapi\|msgd\|haltalk\|halcmd\|hal_temp_bbb'
		String command = "ps -aux | grep 'CRAMPS.ini\\|rtapi\\|msgd\\|haltalk\\|halcmd\\|hal_temp_bbb'";
		channelExec = (ChannelExec) session.openChannel("exec");
		channelExec.setCommand(command);
		MyOutputStreamReader myOut = new MyOutputStreamReader();
		channelExec.setOutputStream(myOut);
		channelExec.connect(3 * 1000);
		while (channelExec.getExitStatus() == -1) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		channelExec.disconnect();
		session.disconnect();
	}

}
