package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.kz.pipeCutter.BBB.BBBHalCommand;
import com.kz.pipeCutter.BBB.Discoverer;
import com.kz.pipeCutter.BBB.commands.AbortGCode;
import com.kz.pipeCutter.BBB.commands.ChangeMode;
import com.kz.pipeCutter.BBB.commands.EstopReset;
import com.kz.pipeCutter.BBB.commands.ExecuteMdi;
import com.kz.pipeCutter.BBB.commands.HomeAllAxis;
import com.kz.pipeCutter.BBB.commands.MachinekitListProcesses;
import com.kz.pipeCutter.BBB.commands.MachineKitStart;
import com.kz.pipeCutter.BBB.commands.MachinekitKill;
import com.kz.pipeCutter.BBB.commands.MachinekitUpload;
import com.kz.pipeCutter.BBB.commands.OpenGCode;
import com.kz.pipeCutter.BBB.commands.PlayGCode;
import com.kz.pipeCutter.BBB.commands.PowerOff;
import com.kz.pipeCutter.BBB.commands.PowerOn;
import com.kz.pipeCutter.BBB.commands.MachineKitShutDown;
import com.kz.pipeCutter.BBB.commands.UnHomeAllAxis;

import pb.Status.EmcTaskModeType;

public class CommandPanel extends JPanel {
	public JTextArea log;

	public CommandPanel() {
		super();

		// this.setPreferredSize(new Dimension(420, 332));
		FlowLayout flowLayout = (FlowLayout) this.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		// ----------machineKitPanel---------------------------

		JPanel machineKitPanel = new JPanel();
		machineKitPanel.setPreferredSize(new Dimension(150, 350));
		this.add(machineKitPanel);

		MyButton startMachineKit = new MyButton("Start MK") {
			@Override
			public void doIt() {
				new MachineKitStart().start();
			}
		};
		machineKitPanel.add(startMachineKit);

		MyButton discoverMachineKit = new MyButton("Discover MK") {
			@Override
			public void doIt() {
				Discoverer.getInstance().discover();
			}
		};
		machineKitPanel.add(discoverMachineKit);

		MyButton listMachineKit = new MyButton("List MK") {
			@Override
			public void doIt() {
				new MachinekitListProcesses().start();
			}
		};
		machineKitPanel.add(listMachineKit);

		MyButton MachineKitShutdown = new MyButton("ShutDown MK") {
			@Override
			public void doIt() {
				new MachineKitShutDown().start();
			}
		};
		machineKitPanel.add(MachineKitShutdown);

		MyButton MachineKitKill = new MyButton("Kill MK") {
			@Override
			public void doIt() {
				new MachinekitKill().start();
			}
		};
		machineKitPanel.add(MachineKitKill);		
		
		
		// ----------machineTalkPanel---------------------------
		JPanel machineTalkPanel = new JPanel();
		machineTalkPanel.setPreferredSize(new Dimension(130, 350));

		this.add(machineTalkPanel);

		MyButton estopReset = new MyButton("EStop reset") {
			@Override
			public void doIt() {
				new EstopReset().start();
			}
		};
		machineTalkPanel.add(estopReset);

		MyButton powerOn = new MyButton("Power ON") {
			@Override
			public void doIt() {
				new PowerOn().start();
			}
		};
		machineTalkPanel.add(powerOn);

		MyButton PowerOff = new MyButton("Power OFF") {
			@Override
			public void doIt() {
				new PowerOff().start();
			}
		};
		machineTalkPanel.add(PowerOff);

		MyButton modeManual = new MyButton("Mode: MANUAL") {
			@Override
			public void doIt() {
				new ChangeMode(EmcTaskModeType.EMC_TASK_MODE_MANUAL).start();
			}
		};
		machineTalkPanel.add(modeManual);

		MyButton modeMDI = new MyButton("Mode: MDI") {
			@Override
			public void doIt() {
				new ChangeMode(EmcTaskModeType.EMC_TASK_MODE_MDI).start();
			}
		};
		machineTalkPanel.add(modeMDI);

		MyButton modeAutomatic = new MyButton("Mode: AUTOMATIC") {
			@Override
			public void doIt() {
				new ChangeMode(EmcTaskModeType.EMC_TASK_MODE_AUTO).start();
			}
		};
		machineTalkPanel.add(modeAutomatic);

		MyButton homeAll = new MyButton("Home ALL") {
			@Override
			public void doIt() {
				new HomeAllAxis().start();
			}
		};
		machineTalkPanel.add(homeAll);

		MyButton unHomeAll = new MyButton("UnHome ALL") {
			@Override
			public void doIt() {
				new UnHomeAllAxis().start();
			}
		};
		machineTalkPanel.add(unHomeAll);

		final SavableText mdiCommand1 = new SavableText();
		mdiCommand1.setLabelTxt("MDI1:");
		mdiCommand1.setParId("machinekit_mdi1");
		machineTalkPanel.add(mdiCommand1);
		mdiCommand1.jValue.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == 10) {
					try {
						new ExecuteMdi(mdiCommand1.getParValue()).start();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					// System.out.println(e.getKeyCode());
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

		final SavableText mdiCommand2 = new SavableText();
		mdiCommand2.setLabelTxt("MDI2:");
		mdiCommand2.setParId("machinekit_mdi2");
		machineTalkPanel.add(mdiCommand2);
		mdiCommand2.jValue.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyCode() == 10) {
					try {
						new ExecuteMdi(mdiCommand2.getParValue()).start();
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					// System.out.println(e.getKeyCode());
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});

		MyButton uploadGCode = new MyButton("Upload GC") {
			@Override
			public void doIt() {
				new MachinekitUpload().start();
			}
		};
		machineTalkPanel.add(uploadGCode);

		MyButton openGCode = new MyButton("Open GC"){
			@Override
			public void doIt() {
					new OpenGCode().start();
			}
		};
		machineTalkPanel.add(openGCode);

		MyButton playGCode = new MyButton("Play GC"){
			@Override
			public void doIt() {
					new PlayGCode().start();
					//BBBHalCommand.getInstance().requestDescribe();
			}
		};
		machineTalkPanel.add(playGCode);

		MyButton abortGCode = new MyButton("Abort GCODE"){
			@Override
			public void doIt() {
					new AbortGCode().start();
			}
		};
		machineTalkPanel.add(abortGCode);

		log = new JTextArea();

		JScrollPane sp = new JScrollPane(log);
		sp.setAutoscrolls(true);
		sp.setPreferredSize(new Dimension(530, 80));
		machineTalkPanel.add(sp);

		machineTalkPanel.setPreferredSize(new Dimension(650, 400));
	}
}
