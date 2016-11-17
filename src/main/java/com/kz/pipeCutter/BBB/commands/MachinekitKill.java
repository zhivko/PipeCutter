package com.kz.pipeCutter.BBB.commands;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.BBB.BBBError;
import com.kz.pipeCutter.BBB.BBBHalCommand;
import com.kz.pipeCutter.BBB.BBBHalRComp;
import com.kz.pipeCutter.BBB.BBBStatus;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;
import com.kz.pipeCutter.ui.Settings;

public class MachinekitKill extends SSH_Command {

	@Override
	public void runSshCmd() throws Exception {

		if (BBBHalRComp.instance != null)
			BBBHalRComp.instance.stop();
		if (BBBStatus.instance != null)
			BBBStatus.instance.stop();
		if (BBBError.instance != null)
			BBBError.instance.stop();
		if (BBBHalCommand.instance != null)
			BBBHalCommand.instance.stop();
		

		MyOutputStreamReader myOut = new MyOutputStreamReader();
		channelExec = (ChannelExec) session.openChannel("exec");
		channelExec.setOutputStream(myOut);

		String avahiRestart = "sudo service avahi-daemon restart";

		// channelExec.setCommand(avahiRestart);
		// channelExec.connect(3 * 1000);
		// while (channelExec.getExitStatus() == -1) {
		// try {
		// TimeUnit.SECONDS.sleep(1);
		// } catch (Exception e) {
		// System.out.println(e);
		// }
		// }

		String command = avahiRestart + "\nps -aux | grep 'CRAMPS.ini\\|rtapi\\|msgd\\|haltalk\\|halcmd\\|hal_temp_bbb'";
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

		String killCommands = "";
		for (String line : myOut.getLines()) {
			if (!line.contains("grep")) {
				String splittedLine[] = line.replaceAll("\\s+", " ").split(" ");
				String pid = splittedLine[1];
				if (!pid.equals("") && StringUtils.isNumeric(pid)) {
					killCommands += "kill -9 " + pid + "\n";
					Settings.getInstance().log(killCommands);
				}
			}
		}
		System.out.println(killCommands);

		if (!killCommands.equals("")) {
			channelExec = (ChannelExec) session.openChannel("exec");
			myOut = new MyOutputStreamReader();
			channelExec.setOutputStream(myOut);
			channelExec.setCommand(killCommands);
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
}
