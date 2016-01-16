package com.kz.pipeCutter.ui.tab;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.kz.pipeCutter.BBB.BBBCommand;
import com.kz.pipeCutter.BBB.commands.Jog;
import com.kz.pipeCutter.ui.MyVerticalFlowLayout;
import com.kz.pipeCutter.ui.SavableSlider;
import com.kz.pipeCutter.ui.SavableText;
import com.kz.pipeCutter.ui.Settings;

public class RotatorSettings extends JPanel {
	public RotatorSettings() {
		super();

		this.setPreferredSize(new Dimension(420, 332));
		FlowLayout flowLayout = (FlowLayout) this.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);

		// ----------ROTATOR 1---------------------------

		JPanel panelRotator1 = new JPanel();
		panelRotator1.setPreferredSize(new Dimension(220, 450));
		this.add(panelRotator1);
		panelRotator1.setLayout(new MyVerticalFlowLayout());
		JLabel lblNewLabel1 = new JLabel("Rotator1");
		panelRotator1.add(lblNewLabel1);

		SavableText rotator1_vel = new SavableText();
		rotator1_vel.setParId("rotator1_vel");
		rotator1_vel.setLabelTxt("velocity:");
		panelRotator1.add(rotator1_vel);

		SavableText rotator1_acel = new SavableText();
		rotator1_acel.setLabelTxt("acceleration:");
		panelRotator1.add(rotator1_acel);
		rotator1_acel.setParId("rotator1_acc");

		SavableSlider sliderRot1 = new SavableSlider();
		sliderRot1.setValues("1,10,100,1000");
		sliderRot1.setLabelTxt("Move for:");
		sliderRot1.setMinValue(0);
		sliderRot1.setMaxValue(3);
		sliderRot1.setStepValue(1);
		sliderRot1.setParId("rotator1_step");
		panelRotator1.add(sliderRot1);

		JButton jog1 = new JButton("JOG");
		jog1.setPreferredSize(new Dimension(100, 50));
		panelRotator1.add(jog1);
		jog1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.instance.getSetting("rotator1_vel"));
				Double distance = Double.valueOf(Settings.instance.getSetting("rotator1_step"));
				Settings.parAxisNo = 3;
				Settings.parDistance = distance;
				Settings.parVelocity = velocity;
				new Jog().start();
			}
		});

		// ----------ROTATOR 2---------------------------
		JPanel panelRotator2 = new JPanel();
		panelRotator2.setPreferredSize(new Dimension(220, 450));
		panelRotator2.setMinimumSize(new Dimension(250, 200));
		this.add(panelRotator2);
		panelRotator2.setLayout(new MyVerticalFlowLayout());
		JLabel lblNewLabel2 = new JLabel("Rotator2");
		panelRotator2.add(lblNewLabel2);

		SavableText rotator2_vel1 = new SavableText();
		rotator2_vel1.setLabelTxt("velocity");
		panelRotator2.add(rotator2_vel1);
		rotator2_vel1.setParId("rotator2_vel");

		SavableText savableSetting = new SavableText();
		savableSetting.setLabelTxt("acceleration:");
		savableSetting.setParId("rotator2_acc");
		panelRotator2.add(savableSetting);

		SavableSlider sliderRot2 = new SavableSlider();
		sliderRot2.setValues("1,10,100,1000");
		sliderRot2.setLabelTxt("Move for:");
		sliderRot2.setMinValue(0);
		sliderRot2.setMaxValue(3);
		sliderRot2.setStepValue(1);
		sliderRot2.setParId("rotator2_step");
		panelRotator2.add(sliderRot2);
		
		
		JButton jog2 = new JButton("JOG");
		jog2.setPreferredSize(new Dimension(100, 50));
		panelRotator2.add(jog2);
		jog2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.instance.getSetting("rotator2_vel"));
				Double distance = Double.valueOf(Settings.instance.getSetting("rotator2_step"));
				
				Settings.parAxisNo = 4;
				Settings.parDistance = distance;
				Settings.parVelocity = velocity;
				new Jog().start();
			}
		});		

		// ----------ROTATOR 3---------------------------
		JPanel panelRotator3 = new JPanel();
		panelRotator3.setPreferredSize(new Dimension(220, 450));
		panelRotator3.setLayout(new MyVerticalFlowLayout());
		panelRotator3.setMinimumSize(new Dimension(250, 200));
		this.add(panelRotator3);
		JLabel lblNewLabel3 = new JLabel("Rotator3");
		panelRotator3.add(lblNewLabel3);

		SavableText rotator3Speed = new SavableText();
		rotator3Speed.setLabelTxt("velocity:");
		rotator3Speed.setParId("rotator3_vel");
		panelRotator3.add(rotator3Speed);

		SavableText savableSetting_1 = new SavableText();
		savableSetting_1.setLabelTxt("acceleration:");
		savableSetting_1.setParId("rotator3_acc");
		panelRotator3.add(savableSetting_1);

		SavableSlider sliderRot3 = new SavableSlider();
		sliderRot3.setValues("1,10,100,1000");
		sliderRot3.setLabelTxt("Move for:");
		sliderRot3.setMinValue(0);
		sliderRot3.setMaxValue(3);
		sliderRot3.setStepValue(1);
		sliderRot3.setParId("rotator3_step");
		panelRotator3.add(sliderRot3);
		
		JButton jog3 = new JButton("JOG");
		jog3.setPreferredSize(new Dimension(100, 50));
		panelRotator3.add(jog3);
		jog1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.instance.getSetting("rotator3_vel"));
				Double distance = Double.valueOf(Settings.instance.getSetting("rotator3_step"));
				
				Settings.parAxisNo = 5;
				Settings.parDistance = distance;
				Settings.parVelocity = velocity;
				new Jog().start();
				
				
			}
		});				

	}
}
