package com.kz.pipeCutter.ui.tab;

import java.awt.Color;
import java.awt.TextField;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import com.kz.pipeCutter.ui.MyVerticalFlowLayout;
import com.kz.pipeCutter.ui.NamedList;
import com.kz.pipeCutter.ui.SavableText;

@SuppressWarnings("serial")
public class MachinekitSettings extends JPanel {
	public NamedList machinekitServices;
	public static MachinekitSettings instance = null;
	SavableText errorUrl;
	SavableText statusUrl;
	SavableText commandUrl;
	SavableText halCmdUrl;
	SavableText halGroupUrl;
	
	public static void main(String[] args) {

		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				MachinekitSettings sett = new MachinekitSettings();
				JFrame my = new JFrame();
				my.getContentPane().add(sett, null);
				my.setVisible(true);
			}
		});

	}

	public MachinekitSettings() {
		this.setLayout(new MyVerticalFlowLayout());

		SavableText machineHost = new SavableText();
		machineHost.setLabelTxt("Host:");
		machineHost.setParId("machinekit_host");
		add(machineHost);

		SavableText machineIP = new SavableText();
		machineIP.setLabelTxt("IP:");
		machineIP.setParId("machinekit_ip");
		add(machineIP);

		SavableText machineUser = new SavableText();
		machineUser.setLabelTxt("User:");
		machineUser.setParId("machinekit_user");
		add(machineUser);

		SavableText machinePass = new SavableText();
		machinePass.setLabelTxt("Password:");
		machinePass.setParId("machinekit_password");
		add(machinePass);

		commandUrl = new SavableText();
		commandUrl.setLabelTxt("Command Service url:");
		commandUrl.setParId("machinekit_commandService_url");
		add(commandUrl);

		errorUrl = new SavableText();
		errorUrl.setLabelTxt("Error service url:");
		errorUrl.setParId("machinekit_errorService_url");
		add(errorUrl);

		statusUrl = new SavableText();
		statusUrl.setLabelTxt("Status service url:");
		statusUrl.setParId("machinekit_statusService_url");
		add(statusUrl);

		SavableText previewUrl = new SavableText();
		previewUrl.setLabelTxt("Preview service url:");
		previewUrl.setParId("machinekit_previewstatusService_url");
		add(previewUrl);

		halGroupUrl = new SavableText();
		halGroupUrl.setLabelTxt("HalRemoteComp service url:");
		halGroupUrl.setParId("machinekit_halRCompService_url");
		add(halGroupUrl);

		halCmdUrl = new SavableText();
		halCmdUrl.setLabelTxt("HalCmd service url:");
		halCmdUrl.setParId("machinekit_halCmdService_url");
		add(halCmdUrl);

		machinekitServices = new NamedList();
		machinekitServices.setParId("machinekit_services");
		machinekitServices.setLabelTxt("MachineTalk services");
		add(machinekitServices);

		MachinekitSettings.instance = this;
	}

	public void pingError() {
		flash(errorUrl.jValue);
	}

	public void pingStatus() throws InvocationTargetException, InterruptedException {
		flash(statusUrl.jValue);
	}

	private void flash(JTextField jValue) {
		final JTextField control = jValue; 
		SwingWorker<Void, Void> sw = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				// TODO Auto-generated method stub
				control.setBackground(Color.GREEN);
				Thread.sleep(150);
				control.setBackground(Color.WHITE);
				Thread.sleep(150);
				return null;
			}
		};
		sw.execute();
	}

	public void pingCommand() {
		flash(commandUrl.jValue);
	}
	
	public void pingHalCommand() {
		flash(halCmdUrl.jValue);
	}
	
	public void pingHalGroupCommand() {
		flash(halGroupUrl.jValue);
	}	
}
