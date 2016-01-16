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

		machinekitServices = new NamedList();
		machinekitServices.setParId("machinekit_services");
		machinekitServices.setLabelTxt("MachineTalk services");
		machinekitServices.setListPrefferedSize(350, 160);
		add(machinekitServices);

		JButton startMachineKit = new JButton("Start MachineKit");
		startMachineKit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new MachinekitStart().start();
			}
		});
		add(startMachineKit);

		JButton discoverMachineKit = new JButton("Discover MachineKit");
		discoverMachineKit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Discoverer.getInstance().discover();
			}
		});
		add(discoverMachineKit);

		JButton listMachineKit = new JButton("List MachineKit");
		listMachineKit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new MachinekitListProcesses().start();
			}
		});
		add(listMachineKit);

		JButton MachineKitStop = new JButton("Stop MachineKit");
		MachineKitStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new MachinekitStop().start();
			}
		});

		add(MachineKitStop);

		JButton EstopReset = new JButton("EStop reset");
		EstopReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new EstopReset().start();
			}
		});
		add(EstopReset);

		JButton PowerOn = new JButton("Power ON");
		PowerOn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PowerOn().start();
			}
		});
		add(PowerOn);

		JButton PowerOff = new JButton("Power OFF");
		PowerOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PowerOff().start();
			}
		});
		add(PowerOff);

		JButton ModeManual = new JButton("Mode: MANUAL");
		ModeManual.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Settings.parMode = EmcTaskModeType.EMC_TASK_MODE_MANUAL;
				new ChangeMode().start();
			}
		});
		add(ModeManual);		
		
		JButton HomeAll = new JButton("Home ALL");
		HomeAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Settings.parMode = EmcTaskModeType.EMC_TASK_MODE_MANUAL;
					new ChangeMode().start();
					Thread.sleep(1000);

					for (int i = 0; i < 4; i++) {
						Settings.parAxisNo = i;
						new HomeAxis().start();
						Thread.sleep(1000);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		add(HomeAll);

		SavableText mdiCommand = new SavableText();
		mdiCommand.setLabelTxt("MDI:");
		mdiCommand.setParId("machinekit_mdi");
		add(mdiCommand);
		mdiCommand.jValue.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == 10) {
					try {
						Settings.parMode = EmcTaskModeType.EMC_TASK_MODE_MDI;
						new ChangeMode().start();
						Thread.sleep(1000);
						
						Settings.parMdiCommand = mdiCommand.getParValue();
						new ExecuteMdi().start();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					System.out.println(e.getKeyCode());
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

		MachinekitSettings.instance = this;
	}
}
