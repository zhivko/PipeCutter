package com.kz.pipeCutter.ui.tab;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.kz.pipeCutter.BBB.commands.ExecuteMdi;
import com.kz.pipeCutter.BBB.commands.Jog;
import com.kz.pipeCutter.ui.MyVerticalFlowLayout;
import com.kz.pipeCutter.ui.PinDef;
import com.kz.pipeCutter.ui.Positioner;
import com.kz.pipeCutter.ui.SavableSlider;
import com.kz.pipeCutter.ui.SavableText;
import com.kz.pipeCutter.ui.Settings;

import pb.Types.HalPinDirection;
import pb.Types.ValueType;

@SuppressWarnings("serial")
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
		rotator1_vel.setPin(new PinDef("myini.maxvel_3", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		rotator1_vel.requiresHalRCompSet = true;
		rotator1_vel.setParId("rotator1_vel");
		rotator1_vel.setLabelTxt("max velocity [°/sec]:");
		panelRotator1.add(rotator1_vel);

		SavableText rotator1_acc = new SavableText();
		rotator1_acc.setLabelTxt("max acceleration:");
		panelRotator1.add(rotator1_acc);
		rotator1_acc.setParId("rotator1_acc");
		rotator1_acc.setPin(new PinDef("myini.maxacc_3", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		rotator1_acc.requiresHalRCompSet = true;


		SavableSlider sliderRot1 = new SavableSlider();
		sliderRot1.setValues(moveToText);
		sliderRot1.setLabelTxt("Move for:");
		sliderRot1.setParId("rotator1_step");
		panelRotator1.add(sliderRot1);

		SavableText rot1_jog_speed = new SavableText();
		rot1_jog_speed.setLabelTxt("jog velocity [mm/min]:");
		rot1_jog_speed.setParId("r1_jog_vel");
		panelRotator1.add(rot1_jog_speed);		
		
		
		JButton jog1 = new JButton("JOG");
		jog1.setPreferredSize(new Dimension(100, 50));
		panelRotator1.add(jog1);
		jog1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.getInstance()
						.getSetting("r1_jog_vel"));
				Double distance = Double.valueOf(Settings.getInstance()
						.getSetting("rotator1_step"));
			  //new Jog(3, velocity/60, distance).start();
				RotatorSettings.this.jog(3, velocity, distance);
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

		SavableText rotator2_vel = new SavableText();
		rotator2_vel.setPin(new PinDef("myini.maxvel_4", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		rotator2_vel.requiresHalRCompSet = true;
		rotator2_vel.setLabelTxt("max velocity [°/sec]:");
		panelRotator2.add(rotator2_vel);
		rotator2_vel.setParId("rotator2_vel");

		SavableText rotator2_acc = new SavableText();
		rotator2_acc.setLabelTxt("max acceleration:");
		rotator2_acc.setParId("rotator2_acc");
		rotator2_acc.setPin(new PinDef("myini.maxacc_4", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		rotator2_acc.requiresHalRCompSet = true;
		
		panelRotator2.add(rotator2_acc);

		SavableSlider sliderRot2 = new SavableSlider();
		sliderRot2.setValues(moveToText);
		sliderRot2.setLabelTxt("Move for:");
		sliderRot2.setParId("rotator2_step");
		panelRotator2.add(sliderRot2);

		SavableText rot2_jog_speed = new SavableText();
		rot2_jog_speed.setLabelTxt("jog velocity [mm/min]:");
		rot2_jog_speed.setParId("r2_jog_vel");
		panelRotator2.add(rot2_jog_speed);				
		
		JButton jog2 = new JButton("JOG");
		jog2.setPreferredSize(new Dimension(100, 50));
		panelRotator2.add(jog2);
		jog2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.getInstance()
						.getSetting("r2_jog_vel"));
				Double distance = Double.valueOf(Settings.getInstance()
						.getSetting("rotator2_step"));
				RotatorSettings.this.jog(4, velocity, distance);
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
		rotator3Speed.setLabelTxt("max velocity [°/sec]:");
		rotator3Speed.setParId("rotator3_vel");
		panelRotator3.add(rotator3Speed);

		SavableText savableSetting_1 = new SavableText();
		savableSetting_1.setLabelTxt("max acceleration:");
		savableSetting_1.setParId("rotator3_acc");
		panelRotator3.add(savableSetting_1);

		SavableSlider sliderRot3 = new SavableSlider();
		sliderRot3.setValues(moveToText);
		sliderRot3.setLabelTxt("Move for:");
		sliderRot3.setParId("rotator3_step");
		panelRotator3.add(sliderRot3);

		SavableText rot3_jog_speed = new SavableText();
		rot3_jog_speed.setLabelTxt("jog velocity [mm/min]:");
		rot3_jog_speed.setParId("r3_jog_vel");
		panelRotator3.add(rot3_jog_speed);		
		
		JButton jog3 = new JButton("JOG");
		jog3.setPreferredSize(new Dimension(100, 50));
		panelRotator3.add(jog3);
		jog3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Long velocity = Long.valueOf(Settings.getInstance()
						.getSetting("r3_jog_vel"));
				Double distance = Double.valueOf(Settings.getInstance()
						.getSetting("rotator3_step"));
				RotatorSettings.this.jog(5, velocity, distance);
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

	protected void jog(int axisId, double speed, Double distance) {

		// cycle through linked jog rotator settings to establish weather we need to
		// do synchronized rotations
		String settingRot1 = "rotator1_linkedJog_enable";
		String settingRot2 = "rotator2_linkedJog_enable";
		String settingRot3 = "rotator3_linkedJog_enable";

		if (Settings.getInstance().getParameter(settingRot1).getParValue().equals("1")
				|| Settings.getInstance().getParameter(settingRot2).getParValue().equals("1")
				|| Settings.getInstance().getParameter(settingRot3).getParValue().equals("1")) {
			
			String mdiCommand = "G91";
			new ExecuteMdi(mdiCommand).start();
			
			mdiCommand = "G01";
			if(Settings.getInstance().getParameter(settingRot1).getParValue().equals("1"))
				mdiCommand += " A" + distance;
			if(Settings.getInstance().getParameter(settingRot2).getParValue().equals("1"))
				mdiCommand += " B" + distance;
			if(Settings.getInstance().getParameter(settingRot3).getParValue().equals("1"))
				mdiCommand += " C" + distance;
			
			mdiCommand += " F" + String.valueOf(speed);
			new ExecuteMdi(mdiCommand).start();;
			
		} else {
			new Jog(axisId, speed, distance).start();
		}
	}
}
