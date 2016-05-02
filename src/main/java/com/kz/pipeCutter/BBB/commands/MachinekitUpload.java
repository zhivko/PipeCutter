package com.kz.pipeCutter.BBB.commands;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.SftpATTRS;
import com.kz.pipeCutter.CutThread;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;
import com.kz.pipeCutter.ui.Settings;

public class MachinekitUpload extends SSH_Command {

	@Override
	public void runSshCmd() throws Exception {
		if (!SSH_CheckIfMachinekitRunning()) {
			String fileName ="prog.gcode";
			
			String localPath = Settings.instance.getSetting("gcode_folder");
			String remotePath = "/home/machinekit/machinekit/nc_files";
			SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.YYYY HH:mm.ss");

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
			String from = localPath + File.separatorChar + fileName;
			String to = remotePath + "/" + fileName;
			FileInputStream fis = new FileInputStream(new File(from));
			Settings.instance.log("Ftp put file to: " + to + "...\n");
			channelSFtp.put(fis, "prog.gcode");
			Settings.instance.log("Ftp put file to: " + to + "...DONE.\n");
			
			channelSFtp.setMtime(to, (int)((new Date().getTime())/1000));

			SftpATTRS attr = channelSFtp.stat(to);
			Date modifyDate = new Date(Long.valueOf(attr.getMTime() * 1000));
			sdf.format(modifyDate);
			Settings.instance.log(String.format(

			"File uploaded to %s modify date: %s", to, attr.getMtimeString()));

			channelSFtp.disconnect();
			channelExec.disconnect();
		}
	}

	public boolean SSH_CheckIfMachinekitRunning() {
		try {
			this.SSH_Login();
			// try to see if machinekit lready running
			// timedatectl set-time '2015-11-23 08:10:40'
			
			Date dt =new Date ();
			//GregorianCalendar cal = new GregorianCalendar(Locale.GERMANY);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			
			
		// @formatter:off
			String format1="sudo date +%%Y%%m%%d -s \"%s%s%s\"; sudo date +%%T -s \"%s:%s:%s\"";
			//String format2 = "sudo hwclock --set --date=\"%s-%s-%s %s:%s:%s\"  --localtime";
			String command = String.format(format1 + "; date", 
					String.format("%04d",cal.get(Calendar.YEAR)),
					String.format("%02d",cal.get(Calendar.MONTH)+1),
					String.format("%02d",cal.get(Calendar.DAY_OF_MONTH)),
					
					String.format("%02d",cal.get(Calendar.HOUR_OF_DAY)),
					String.format("%02d",cal.get(Calendar.MINUTE)),
					String.format("%02d",cal.get(Calendar.SECOND))
					);
		// @formatter:on
			MyOutputStreamReader myOut = new MyOutputStreamReader();
			channelExec = (ChannelExec) session.openChannel("exec");
			
			//set time
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

			command = "ps -aux | grep machinekit";
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
					Settings.instance.log("MachineKit already started..");
					channelExec.disconnect();
					return true;
				}
			}
			channelExec.disconnect();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return false;
	}}
