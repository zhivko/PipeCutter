package com.kz.pipeCutter.BBB.commands;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jmdns.ServiceInfo;
import javax.jmdns.impl.ServiceInfoImpl;

import org.apache.log4j.Logger;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.kz.pipeCutter.ui.CommandPanel;
import com.kz.pipeCutter.ui.NamedList;
import com.kz.pipeCutter.ui.Settings;
import com.sun.j3d.utils.geometry.GeometryInfo;

public class MachineKitStart extends SSH_Command implements Runnable {
	static MachineKitStart instance;

	public static MachineKitStart getInstance()
	{
		if(instance==null)
			instance = new MachineKitStart();
		return instance;
	}

	public MachineKitStart() {
	}

	private int noOfserviceStarted = 0;
	ArrayList<String> services;

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
			// Settings.getInstance().log("MK instance at host: " + host);
			session = jsch.getSession(user, ip, 22);
			session.setPassword(pass);

			session.setConfig("StrictHostKeyChecking", "no");
			session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");

			session.setServerAliveInterval(2000);
			session.setServerAliveCountMax(Integer.MAX_VALUE);

			session.setOutputStream(System.out);
			session.connect(25000); // making a connection with timeout.

			final ChannelShell channelShell = (ChannelShell) session.openChannel("shell");
			OutputStream ops = channelShell.getOutputStream();
			PrintStream ps = new PrintStream(ops, true);

			// channelShell.setAgentForwarding(true);
			// channelShell.setXForwarding(true);
			channelShell.connect(3 * 1000);

			String command;

			int config = 2;
			if (config == 0) {
				command = "source ~/git/machinekit/scripts/rip-environment";
				ps.println(command);
			} else if (config == 1) {
				command = "source ~/git/machinekitMulticore/scripts/rip-environment";
				ps.println(command);
			} else if (config == 2) {
				command = "source ~/git/machinekitOff/scripts/rip-environment";
				ps.println(command);
			}

			String rmCommand = "sudo rm -f /var/log/linuxcnc.log";
			ps.println(rmCommand);

			String createCommand = "sudo touch /var/log/linuxcnc.log";
			ps.println(createCommand);

			String modPerm = "sudo chmod 666 /var/log/linuxcnc.log";
			ps.println(modPerm);

			String emptyMessages = "sudo rm /var/log/messages";
			ps.println(emptyMessages);

			String createCommand2 = "sudo touch /var/log/messages";
			ps.println(createCommand2);

			String modPerm2 = "sudo chmod 666 /var/log/messages";
			ps.println(modPerm2);

			String restartLog = "sudo service rsyslog restart";
			ps.println(restartLog);

			String hostName = Settings.getInstance().getSetting("machinekit_host");

			if (hostName.equals("beaglebone.local"))
				command = "machinekit ~/git/machinekit/myini/CRAMPS.ini &";
			else
				command = "machinekit ~/git/machinekit/myini/sim/CRAMPS.ini &";

			ps.println(command);

			final Session session2 = jsch.getSession(user, hostName, 22);

			// UserInfo ui=new MyUserInfo(pass);
			// session.setUserInfo(ui);

			// session2.setPassword(pass);
			session2.setConfig("StrictHostKeyChecking", "no");
			session2.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");
			session2.setPassword(pass);
			session2.setServerAliveInterval(2000);
			session2.setServerAliveCountMax(Integer.MAX_VALUE);
			session2.connect(25000);

			Thread t1 = new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					readOutput(session2);
				}
			});

			Thread t2 = new Thread(new Runnable() {
				@Override
				public void run() {
					// TODO Auto-generated method stub
					readOutput(channelShell);
				}
			});
			services = new ArrayList<String>();
			t1.start();
			t2.start();
			noOfserviceStarted = 0;
			int maxNoOfserviceStarted = 10;
			while (services.size() < maxNoOfserviceStarted && !shouldStop) {
				System.out.print("Waiting for MK services, noOfserviceStarted = " + services.size() + "/" + maxNoOfserviceStarted + "... ");
				Thread.currentThread().sleep(2000);
				for (String servicename : services) {
					System.out.print(servicename + ", ");
				}
				System.out.println();
				
			}

//			t1.interrupt();
//			t2.interrupt();
			if (services.size() == maxNoOfserviceStarted) {
				// if all 5 services are started from mkwrapper
				// start also halcmd and halrcomp
				// Settings.getInstance().log("Starting discoverer...");
				// Discoverer.getInstance().discover();
				Settings.getInstance().log("Init HalCmd dealer");
				Settings.getInstance().initHalCmdService();
				Settings.getInstance().log("Init HalRComp XSUB subscriber");
				Settings.getInstance().initHalRcompService();
			}
			
			CommandPanel.getInstance().startMachineKitButton.setSelected(false);

		}
	}

	private void readOutput(Session session) {
		InputStream stream = null;
		try {
			Pattern p = Pattern.compile("\"(.*)=(.*)\" \"dsn=(tcp://(.*):(.*))\"");

			int timeOutMs = 300 * 1000;

			long startMs = System.currentTimeMillis();

			// && (noOfserviceStarted < 6) && System.currentTimeMillis() < (startMs +
			// timeOutMs)

			Thread.sleep(5000);

			Channel channel = session.openChannel("sftp");
			ChannelSftp channelSftp = (ChannelSftp) channel;
			channel.connect();

			while (!channelSftp.isConnected())
				Thread.currentThread().sleep(100);

			channelSftp.cd("/var/log");
			stream = channelSftp.get("messages");
			BufferedReader buffReader = new BufferedReader(new InputStreamReader(stream));

			while (!shouldStop && services.size() < 10) {
				String line = null;
				while ((line = buffReader.readLine()) != null) {
					Logger.getLogger(this.getClass()).info(line);
					if (line.contains("zeroconf: registered"))
						System.out.println("");
					Matcher m = p.matcher(line);
					if (m.find()) {
						Settings.getInstance().log("Service '" + m.group(2) + "' on port: " + m.group(5) + " on BBB is on.");
						String hostName = Settings.getInstance().getSetting("machinekit_host");
						ServiceInfo serviceInfo = ServiceInfoImpl.create(m.group(2), m.group(2), Integer.valueOf(m.group(5)), hostName);
						NamedList.getCommandServiceUrl(serviceInfo);
						NamedList.getErrorServiceUrl(serviceInfo);
						NamedList.getStatusServiceUrl(serviceInfo);
						NamedList.getPreviewStatusServiceUrl(serviceInfo);
						NamedList.getHalCmdServiceUrl(serviceInfo);
						services.add(m.group(2));
						noOfserviceStarted++;
					}

				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}

	private void readOutput(ChannelShell channelShell) {

		try {
			InputStream in = channelShell.getInputStream();
			BufferedReader buffReader = new BufferedReader(new InputStreamReader(in));

			Pattern p = Pattern.compile("^.*port\\s=\\s(.*)\\stxtrec\\s.*name\\s=\\s(.*)\\son.*");

			int timeOutMs = 300 * 1000;

			long startMs = System.currentTimeMillis();

			// && (noOfserviceStarted < 6) && System.currentTimeMillis() < (startMs +
			// timeOutMs)

			while (!shouldStop && services.size() < 10) {
				String line = null;
				Thread.sleep(1000);
				while ((line = buffReader.readLine()) != null) {
					Logger.getLogger(this.getClass()).info(line);
					if (line.contains("service: dsname ="))
						System.out.println("");
					Matcher m = p.matcher(line);
					if (m.find()) {
						Settings.getInstance().log("Service '" + m.group(2) + "' on port: " + m.group(1) + " on BBB is on.");
						String hostName = Settings.getInstance().getSetting("machinekit_host");
						ServiceInfo serviceInfo = ServiceInfoImpl.create(m.group(2), m.group(2), Integer.valueOf(m.group(1)), hostName);
						NamedList.getCommandServiceUrl(serviceInfo);
						NamedList.getErrorServiceUrl(serviceInfo);
						NamedList.getStatusServiceUrl(serviceInfo);
						NamedList.getPreviewStatusServiceUrl(serviceInfo);
						NamedList.getHalCmdServiceUrl(serviceInfo);
						services.add(m.group(2));
						noOfserviceStarted++;
					}

				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		} finally {

		}
	}

	@Override
	public void run() {
		try {
			runSshCmd();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
