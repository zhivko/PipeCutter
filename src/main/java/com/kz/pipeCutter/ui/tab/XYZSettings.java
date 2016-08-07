package com.kz.pipeCutter.ui.tab;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.kz.pipeCutter.BBB.commands.Jog;
import com.kz.pipeCutter.ui.MyVerticalFlowLayout;
import com.kz.pipeCutter.ui.Positioner;
import com.kz.pipeCutter.ui.SavableSlider;
import com.kz.pipeCutter.ui.SavableText;
import com.kz.pipeCutter.ui.Settings;

@SuppressWarnings("serial")
public class XYZSettings extends JPanel {

	
	public XYZSettings() {
		super();
		Dimension panelPreferedDimension = new Dimension(220, 480);

		this.setPreferredSize(new Dimension(420, 450));
		FlowLayout flowLayout = (FlowLayout) this.getLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		
		String moveToText = "-10³,-10²,-10,-1,1,10,10²,10³";

		// ---------- X AXIS ---------------------------

		JPanel panelXAxis = new JPanel();
		panelXAxis.setPreferredSize(panelPreferedDimension);
		this.add(panelXAxis);
		panelXAxis.setLayout(new MyVerticalFlowLayout());
		JLabel lblNewLabel1 = new JLabel("X Axis");
		panelXAxis.add(lblNewLabel1);

		SavableText x_vel = new SavableText();
		x_vel.setParId("x_vel");
		x_vel.setLabelTxt("velocity [mm/min]:");
		panelXAxis.add(x_vel);

		SavableText x_acel = new SavableText();
		x_acel.setLabelTxt("acceleration:");
		panelXAxis.add(x_acel);
		x_acel.setParId("x_acc");

		SavableSlider sliderX = new SavableSlider();
		sliderX.setValues(moveToText);
		sliderX.setLabelTxt("Move for:");
		sliderX.setParId("x_step");
		panelXAxis.add(sliderX);

		JButton jog1 = new JButton("JOG");
		jog1.setPreferredSize(new Dimension(100, 50));
		panelXAxis.add(jog1);
		jog1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.getInstance().getSetting("x_vel"));
				Double distance = Double.valueOf(Settings.getInstance().getSetting("x_step"));
				new Jog(0, velocity/60, distance).start();
			}
		});
		SavableText positionX = new SavableText();
		positionX.setLabelTxt("Position:");
		panelXAxis.add(positionX);
		positionX.setParId("position_x");
		positionX.setNeedsSave(false);		
		

		// ---------- Y AXIS ---------------------------
		JPanel panelYAxis = new JPanel();
		panelYAxis.setPreferredSize(panelPreferedDimension);
		panelYAxis.setMinimumSize(new Dimension(250, 200));
		this.add(panelYAxis);
		panelYAxis.setLayout(new MyVerticalFlowLayout());
		JLabel lblNewLabel2 = new JLabel("Y Axis");
		panelYAxis.add(lblNewLabel2);

		SavableText y_vel = new SavableText();
		y_vel.setParId("y_vel");
		y_vel.setLabelTxt("velocity [mm/min]:");
		panelYAxis.add(y_vel);

		SavableText y_acc = new SavableText();
		y_acc.setLabelTxt("acceleration:");
		y_acc.setParId("y_acc");
		panelYAxis.add(y_acc);

		SavableSlider sliderY = new SavableSlider();
		sliderY.setValues(moveToText);
		sliderY.setLabelTxt("Move for:");
		sliderY.setParId("y_step");
		panelYAxis.add(sliderY);
		
		
		JButton jog2 = new JButton("JOG");
		jog2.setPreferredSize(new Dimension(100, 50));
		panelYAxis.add(jog2);
		jog2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.getInstance().getSetting("y_vel"));
				Double distance = Double.valueOf(Settings.getInstance().getSetting("y_step"));
				new Jog(1, velocity/60, distance).start();
			}
		});
		SavableText positionY = new SavableText();
		positionY.setLabelTxt("Position:");
		panelYAxis.add(positionY);
		positionY.setParId("position_y");
		positionY.setNeedsSave(false);
		
		
		// ---------- Z AXIS ---------------------------
		JPanel panelZAxis = new JPanel();
		panelZAxis.setPreferredSize(panelPreferedDimension);
		panelZAxis.setLayout(new MyVerticalFlowLayout());
		panelZAxis.setMinimumSize(new Dimension(250, 200));
		this.add(panelZAxis);
		JLabel lblNewLabel3 = new JLabel("Z Axis");
		panelZAxis.add(lblNewLabel3);

		SavableText z_vel = new SavableText();
		z_vel.setLabelTxt("velocity [mm/min]:");
		z_vel.setParId("z_vel");
		panelZAxis.add(z_vel);

		SavableText z_acc = new SavableText();
		z_acc.setLabelTxt("acceleration:");
		z_acc.setParId("z_acc");
		panelZAxis.add(z_acc);

		SavableSlider sliderZ = new SavableSlider();
		sliderZ.setValues(moveToText);
		sliderZ.setLabelTxt("Move for:");
		sliderZ.setParId("z_step");
		panelZAxis.add(sliderZ);
		
		JButton jog3 = new JButton("JOG");
		jog3.setPreferredSize(new Dimension(100, 50));
		panelZAxis.add(jog3);
		jog3.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Double velocity = Double.valueOf(Settings.getInstance().getSetting("z_vel"));
				Double distance = Double.valueOf(Settings.getInstance().getSetting("z_step"));
				new Jog(2, velocity/60, distance).start();
			}
		});			
		SavableText positionZ = new SavableText();
		positionZ.setLabelTxt("Position:");
		panelZAxis.add(positionZ);
		positionZ.setParId("position_z");
		positionZ.setNeedsSave(false);
	}
}
