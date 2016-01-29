package com.kz.pipeCutter.ui.tab;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.kz.pipeCutter.BBB.BBBStatus;
import com.kz.pipeCutter.BBB.commands.Jog;
import com.kz.pipeCutter.ui.MyVerticalFlowLayout;
import com.kz.pipeCutter.ui.Positioner;
import com.kz.pipeCutter.ui.SavableSlider;
import com.kz.pipeCutter.ui.SavableText;
import com.kz.pipeCutter.ui.Settings;

public class RotatorSettings extends JPanel {

	
	public RotatorSettings() {
		super();
		Dimension panelPreferedDimension = new Dimension(220, 480);

		this.setPreferredSize(new Dimension(420, 450));
		FlowLayout flowLayout = (FlowLayout) this.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		
		String moveToText = "-10³,-10²,-10,-1,1,10,10²,10³";

		// ----------ROTATOR 1---------------------------

		JPanel panelRotator1 = new JPanel();
		panelRotator1.setPreferredSize(panelPreferedDimension);
		this.add(panelRotator1);
		panelRotator1.setLayout(new MyVerticalFlowLayout());
		JLabel lblNewLabel1 = new JLabel("Rotator1");
		panelRotator1.add(lblNewLabel1);

		SavableText rotator1_vel = new SavableText();
		rotator1_vel.setParId("rotator1_vel");
		rotator1_vel.setLabelTxt("velocity [°/min]:");
		panelRotator1.add(rotator1_vel);

		SavableText rotator1_acel = new SavableText();
		rotator1_acel.setLabelTxt("acceleration:");
		panelRotator1.add(rotator1_acel);
		rotator1_acel.setParId("rotator1_acc");

		SavableSlider sliderRot1 = new SavableSlider();
		sliderRot1.setValues(moveToText);
		sliderRot1.setLabelTxt("Move for:");
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
				Settings.parVelocity = velocity/60;
				new Jog().start();
			}
		});
		
		SavableText positionA = new SavableText();
		positionA.setLabelTxt("Position:");
		panelRotator1.add(positionA);
		positionA.setParId("position_a");
		positionA.setNeedsSave(false);
		
		Positioner pos1 = new Positioner(1);
		panelRotator1.add(pos1);
		

		// ----------ROTATOR 2---------------------------
		JPanel panelRotator2 = new JPanel();
		panelRotator2.setPreferredSize(panelPreferedDimension);
		panelRotator2.setMinimumSize(new Dimension(250, 200));
		this.add(panelRotator2);
		panelRotator2.setLayout(new MyVerticalFlowLayout());
		JLabel lblNewLabel2 = new JLabel("Rotator2");
		panelRotator2.add(lblNewLabel2);

		SavableText rotator2_vel1 = new SavableText();
		rotator2_vel1.setLabelTxt("velocity [°/min]:");
		panelRotator2.add(rotator2_vel1);
		rotator2_vel1.setParId("rotator2_vel");

		SavableText savableSetting = new SavableText();
		savableSetting.setLabelTxt("acceleration:");
		savableSetting.setParId("rotator2_acc");
		panelRotator2.add(savableSetting);

		SavableSlider sliderRot2 = new SavableSlider();
		sliderRot2.setValues(moveToText);
		sliderRot2.setLabelTxt("Move for:");
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
				Settings.parVelocity = velocity/60;
				new Jog().start();
			}
		});		
		
		SavableText positionB = new SavableText();
		positionB.setLabelTxt("Position:");
		panelRotator2.add(positionB);
		positionB.setParId("position_b");			
		positionB.setNeedsSave(false);
		
		Positioner pos2 = new Positioner(2);
		panelRotator2.add(pos2);		


		// ----------ROTATOR 3---------------------------
		JPanel panelRotator3 = new JPanel();
		panelRotator3.setPreferredSize(panelPreferedDimension);
		panelRotator3.setLayout(new MyVerticalFlowLayout());
		panelRotator3.setMinimumSize(new Dimension(250, 200));
		this.add(panelRotator3);
		JLabel lblNewLabel3 = new JLabel("Rotator3");
		panelRotator3.add(lblNewLabel3);

		SavableText rotator3Speed = new SavableText();
		rotator3Speed.setLabelTxt("velocity [°/min]:");
		rotator3Speed.setParId("rotator3_vel");
		panelRotator3.add(rotator3Speed);

		SavableText savableSetting_1 = new SavableText();
		savableSetting_1.setLabelTxt("acceleration:");
		savableSetting_1.setParId("rotator3_acc");
		panelRotator3.add(savableSetting_1);

		SavableSlider sliderRot3 = new SavableSlider();
		sliderRot3.setValues(moveToText);
		sliderRot3.setLabelTxt("Move for:");
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
				Settings.parVelocity = velocity/60;
				new Jog().start();
				
				
			}
		});			
		SavableText positionC = new SavableText();
		positionC.setLabelTxt("Position:");
		panelRotator3.add(positionC);
		positionC.setParId("position_c");
		positionC.setNeedsSave(false);

		Positioner pos3 = new Positioner(3);
		panelRotator3.add(pos3);		

	}
}
