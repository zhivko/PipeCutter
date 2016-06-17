package com.kz.pipeCutter.BBB.commands;

import com.jcraft.jsch.ChannelExec;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;
import com.kz.pipeCutter.ui.Settings;

public class MachinekitStop extends SSH_Command {

	@Override
	public void runSshCmd() throws Exception {
		String command = "ps -aux | grep 'CRAMPS.ini\\|rtapi\\|msgd\\|haltalk\\|halcmd'";
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

		String killCommands = "";
		for (String line : myOut.getLines()) {
			if (!line.contains("grep")) {
				String splittedLine[] = line.replaceAll("\\s+", " ").split(" ");
				String pid = splittedLine[1];
				if (!pid.equals(""))
				{
					killCommands += "kill -9 " + pid + "\n";
					Settings.instance.log(killCommands);
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
					Thread.sleep(1000);
				} catch (Exception e) {
					System.out.println(e);
				}
			}
			channelExec.disconnect();
		}

	}
}
