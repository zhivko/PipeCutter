package com.kz.pipeCutter.BBB.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;
import javax.jmdns.impl.ServiceInfoImpl;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.BBB.Discoverer;
import com.kz.pipeCutter.BBB.MyOutputStreamReader;
import com.kz.pipeCutter.ui.NamedList;
import com.kz.pipeCutter.ui.Settings;

public class MachineKitStart extends SSH_Command {

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

			// channelShell.setAgentForwarding(true);
			// channelShell.setXForwarding(true);
			channelShell.connect(3 * 1000);

			// String command = "source ~/git/machinekit/scripts/rip-environment";
			// ps.println(command);
			String command = "machinekit ~/git/machinekit/myini/CRAMPS.ini &";
			ps.println(command);
			readOutput(channelShell);

		}

	}

	private void readOutput(ChannelShell channelShell) throws IOException, InterruptedException {
		InputStream in = channelShell.getInputStream();
		BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));

		int noOfserviceStarted = 0;
		Pattern p = Pattern.compile("^.*port\\s=\\s(.*)\\stxtrec\\s.*name\\s=\\s(.*)\\son.*");

		int timeOutMs = 300 * 1000;

		long startMs = System.currentTimeMillis();

		// && (noOfserviceStarted < 6) && System.currentTimeMillis() < (startMs +
		// timeOutMs)

		while ((noOfserviceStarted < 6)) {
			String line = null;
			Thread.sleep(1000);
			while ((line = buffReader.readLine()) != null) {
				Logger.getLogger(this.getClass()).info(line);
				if (noOfserviceStarted < 6) {
					Matcher m = p.matcher(line);
					if (m.find()) {
						Settings.instance.log("Service '" + m.group(2) + "' on port: " + m.group(1) + " on BBB is on.");
						ServiceInfo serviceInfo = ServiceInfoImpl.create(m.group(2), m.group(2), Integer.valueOf(m.group(1)), "beaglebone.local");
						NamedList.getCommandServiceUrl(serviceInfo);
						NamedList.getErrorServiceUrl(serviceInfo);
						NamedList.getStatusServiceUrl(serviceInfo);
						NamedList.getPreviewStatusServiceUrl(serviceInfo);
						NamedList.getHalCmdServiceUrl(serviceInfo);

						noOfserviceStarted++;
					}
				}

				if (noOfserviceStarted == 5) {
					//if all 5 services are started from mkwrapper
					//start also halcmd and halrcomp
					//Settings.instance.log("Starting discoverer...");
					//Discoverer.getInstance().discover();
					Settings.instance.log("Init HalCmd dealer");
					Settings.instance.initHalCmdService();
					Settings.instance.log("Init HalRComp XSUB subscriber");
					Settings.instance.initHalRcompService();
				}
			}
		}
	}


}
