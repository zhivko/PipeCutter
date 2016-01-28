package com.kz.pipeCutter.ui.tab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.kz.pipeCutter.BBB.Discoverer;
import com.kz.pipeCutter.BBB.commands.ChangeMode;
import com.kz.pipeCutter.BBB.commands.EstopReset;
import com.kz.pipeCutter.BBB.commands.ExecuteMdi;
import com.kz.pipeCutter.BBB.commands.HomeAxis;
import com.kz.pipeCutter.BBB.commands.MachinekitListProcesses;
import com.kz.pipeCutter.BBB.commands.MachinekitStart;
import com.kz.pipeCutter.BBB.commands.MachinekitStop;
import com.kz.pipeCutter.BBB.commands.PowerOff;
import com.kz.pipeCutter.BBB.commands.PowerOn;
import com.kz.pipeCutter.ui.MyVerticalFlowLayout;
import com.kz.pipeCutter.ui.NamedList;
import com.kz.pipeCutter.ui.SavableText;
import com.kz.pipeCutter.ui.Settings;

import pb.Status.EmcTaskModeType;

@SuppressWarnings("serial")
public class MachinekitSettings extends JPanel {
	public NamedList machinekitServices;
	public static MachinekitSettings instance = null;

	public static void main(String[] args) {
		MachinekitSettings sett = new MachinekitSettings();
		JFrame my = new JFrame();
		my.getContentPane().add(sett, null);
		my.setVisible(true);
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

		SavableText commandUrl = new SavableText();
		commandUrl.setLabelTxt("Command Service url:");
		commandUrl.setParId("machinekit_commandService_url");
		add(commandUrl);

		SavableText errorUrl = new SavableText();
		errorUrl.setLabelTxt("Error service url:");
		errorUrl.setParId("machinekit_errorService_url");
		add(errorUrl);

		SavableText statusUrl = new SavableText();
		statusUrl.setLabelTxt("Status service url:");
		statusUrl.setParId("machinekit_statusService_url");
		add(statusUrl);		

		SavableText previewUrl = new SavableText();
		previewUrl.setLabelTxt("Preview service url:");
		previewUrl.setParId("machinekit_previewstatusService_url");
		add(previewUrl);				

		machinekitServices = new NamedList();
		machinekitServices.setParId("machinekit_services");
		machinekitServices.setLabelTxt("MachineTalk services");
		machinekitServices.setListPrefferedSize(350, 160);
		add(machinekitServices);




		MachinekitSettings.instance = this;
	}
}