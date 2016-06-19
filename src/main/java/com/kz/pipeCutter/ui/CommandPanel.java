package com.kz.pipeCutter.ui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.kz.pipeCutter.BBB.Discoverer;
import com.kz.pipeCutter.BBB.commands.AbortGCode;
import com.kz.pipeCutter.BBB.commands.ChangeMode;
import com.kz.pipeCutter.BBB.commands.EstopReset;
import com.kz.pipeCutter.BBB.commands.ExecuteMdi;
import com.kz.pipeCutter.BBB.commands.HomeAxis;
import com.kz.pipeCutter.BBB.commands.MachinekitListProcesses;
import com.kz.pipeCutter.BBB.commands.MachinekitStart;
import com.kz.pipeCutter.BBB.commands.MachinekitStop;
import com.kz.pipeCutter.BBB.commands.MachinekitUpload;
import com.kz.pipeCutter.BBB.commands.OpenGCode;
import com.kz.pipeCutter.BBB.commands.PlayGCode;
import com.kz.pipeCutter.BBB.commands.PowerOff;
import com.kz.pipeCutter.BBB.commands.PowerOn;
import com.kz.pipeCutter.BBB.commands.UnHomeAxis;

import pb.Status.EmcTaskModeType;

public class CommandPanel extends JPanel {
	public JTextArea log;

	public CommandPanel() {
		super();

		//this.setPreferredSize(new Dimension(420, 332));
		FlowLayout flowLayout = (FlowLayout) this.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		// ----------machineKitPanel---------------------------

		JPanel machineKitPanel = new JPanel();
		machineKitPanel.setPreferredSize(new Dimension(150, 350));
		this.add(machineKitPanel);

		JButton startMachineKit = new JButton("Start MK");
		startMachineKit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new MachinekitStart().start();
			}
		});
		machineKitPanel.add(startMachineKit);

		JButton discoverMachineKit = new JButton("Discover MK");
		discoverMachineKit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						Discoverer.getInstance().discover();
					}
				});
			}
		});
		machineKitPanel.add(discoverMachineKit);

		JButton listMachineKit = new JButton("List MK");
		listMachineKit.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						new MachinekitListProcesses().start();
					}
				});
			}
		});
		machineKitPanel.add(listMachineKit);

		JButton MachineKitStop = new JButton("Kill MK");
		MachineKitStop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new MachinekitStop().start();
			}
		});

		machineKitPanel.add(MachineKitStop);

		// ----------machineTalkPanel---------------------------
		JPanel machineTalkPanel = new JPanel();
		machineTalkPanel.setPreferredSize(new Dimension(130, 350));

		this.add(machineTalkPanel);

		JButton estopReset = new JButton("EStop reset");
		estopReset.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new EstopReset().start();
			}
		});
		machineTalkPanel.add(estopReset);

		JButton powerOn = new JButton("Power ON");
		powerOn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PowerOn().start();
			}
		});
		machineTalkPanel.add(powerOn);

		JButton PowerOff = new JButton("Power OFF");
		PowerOff.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PowerOff().start();
			}
		});
		machineTalkPanel.add(PowerOff);

		JButton modeManual = new JButton("Mode: MANUAL");
		modeManual.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ChangeMode(EmcTaskModeType.EMC_TASK_MODE_MANUAL).start();
			}
		});
		machineTalkPanel.add(modeManual);

		JButton modeMDI = new JButton("Mode: MDI");
		modeMDI.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ChangeMode(EmcTaskModeType.EMC_TASK_MODE_MDI).start();
			}
		});
		machineTalkPanel.add(modeMDI);

		JButton modeAutomatic = new JButton("Mode: AUTOMATIC");
		modeAutomatic.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new ChangeMode(EmcTaskModeType.EMC_TASK_MODE_AUTO).start();
			}
		});
		machineTalkPanel.add(modeAutomatic);

		JButton homeAll = new JButton("Home ALL");
		homeAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new ChangeMode(EmcTaskModeType.EMC_TASK_MODE_MANUAL).start();
					for (int i = 0; i <= 4; i++) {
						new HomeAxis(i).start();
						Thread.sleep(500);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		machineTalkPanel.add(homeAll);

		JButton unHomeAll = new JButton("UnHome ALL");
		unHomeAll.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new ChangeMode(EmcTaskModeType.EMC_TASK_MODE_MANUAL).start();
					for (int i = 0; i < 4; i++) {
						new UnHomeAxis(i).start();
						Thread.sleep(10);
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
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

		JButton uploadGCode = new JButton("Upload GC");
		uploadGCode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new MachinekitUpload().start();

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		machineTalkPanel.add(uploadGCode);
		
		JButton openGCode = new JButton("Open GC");
		openGCode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new OpenGCode().start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		machineTalkPanel.add(openGCode);		

		JButton playGCode = new JButton("Play GC");
		playGCode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new PlayGCode().start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		machineTalkPanel.add(playGCode);		
		
		
		JButton abortGCode = new JButton("Abort GCODE");
		abortGCode.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					new AbortGCode().start();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		machineTalkPanel.add(abortGCode);			
		
		
		log = new JTextArea();

		JScrollPane sp = new JScrollPane(log);
		sp.setAutoscrolls(true);
		sp.setPreferredSize(new Dimension(530, 80));
		machineTalkPanel.add(sp);

		machineTalkPanel.setPreferredSize(new Dimension(650, 400));
	}
}
