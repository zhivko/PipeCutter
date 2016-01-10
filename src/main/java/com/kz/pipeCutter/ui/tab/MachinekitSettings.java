package com.kz.pipeCutter.ui.tab;

import javax.swing.JPanel;

import com.kz.pipeCutter.ui.MyVerticalFlowLayout;
import com.kz.pipeCutter.ui.SavableText;

public class MachinekitSettings extends JPanel {
	public MachinekitSettings()
	{
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
	}
}
