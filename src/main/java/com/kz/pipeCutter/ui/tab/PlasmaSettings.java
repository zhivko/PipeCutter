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
import com.kz.pipeCutter.ui.PinDef;
import com.kz.pipeCutter.ui.Positioner;
import com.kz.pipeCutter.ui.SavableCheckBox;
import com.kz.pipeCutter.ui.SavableSlider;
import com.kz.pipeCutter.ui.SavableText;
import com.kz.pipeCutter.ui.Settings;

import pb.Types.HalPinDirection;
import pb.Types.ValueType;

public class PlasmaSettings extends JPanel {

	
	public PlasmaSettings() {
		super();
		this.setPreferredSize(new Dimension(420, 450));
		MyVerticalFlowLayout flowLayout = new MyVerticalFlowLayout();
		this.setLayout(flowLayout);

		SavableText plasmaPierceOffsetMm = new SavableText();
		plasmaPierceOffsetMm.setLabelTxt("Plasma pierce offset [mm]:");
		plasmaPierceOffsetMm.setParId("plasma_pierce_offset_mm");
		this.add(plasmaPierceOffsetMm);

		SavableText plasmaPierceTimeS = new SavableText();
		plasmaPierceTimeS.setLabelTxt("Plasma pierce time [s]:");
		plasmaPierceTimeS.setParId("plasma_pierce_time_s");
		this.add(plasmaPierceTimeS);
		
		SavableText plasmaKerfOffsetMm = new SavableText();
		plasmaKerfOffsetMm.setLabelTxt("Plasma kerf offset [mm]:");
		plasmaKerfOffsetMm.setParId("plasma_kerf_offset_mm");
		this.add(plasmaKerfOffsetMm);					

		SavableText plasmaCutOffsetMm = new SavableText();
		plasmaCutOffsetMm.setLabelTxt("Plasma above surface offset [mm]:");
		plasmaCutOffsetMm.setParId("plasma_cut_offset_mm");
		this.add(plasmaCutOffsetMm);					
		
		SavableCheckBox thcEnablel = new SavableCheckBox();
		thcEnablel.setPin(new PinDef("myini.thc-enable", HalPinDirection.HAL_OUT, ValueType.HAL_BIT));
		thcEnablel.setNeedsSave(true);
		thcEnablel.requiresHalRCompSet = true;
		thcEnablel.setParId("myini.thc-enable");
		thcEnablel.setLabelTxt("Thc enable:");
		this.add(thcEnablel);		

		SavableText actualVolts = new SavableText();
		actualVolts.setPin(new PinDef("myini.actual-volts", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		actualVolts.requiresHalRCompSet = false;
		actualVolts.setParId("myini.actual-volts");
		actualVolts.setLabelTxt("Actual volts [V]:");
		this.add(actualVolts);				

		SavableText voltsRequested = new SavableText();
		voltsRequested.setPin(new PinDef("myini.volts-requested", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		voltsRequested.requiresHalRCompSet = true;
		voltsRequested.setNeedsSave(true);
		voltsRequested.setParId("myini.volts-requested");
		voltsRequested.setLabelTxt("VoltsRequested [V]:");
		voltsRequested.setToolTipText("Tip Volts current_vel >= min_velocity requested");
		this.add(voltsRequested);		

		SavableText velStatus = new SavableText();
		velStatus.setPin(new PinDef("myini.vel-status", HalPinDirection.HAL_IN, ValueType.HAL_BIT));
		velStatus.requiresHalRCompSet = false;
		velStatus.setParId("myini.vel-status");
		velStatus.setLabelTxt("Velocity status:");
		velStatus.setToolTipText("When the THC thinks we are at requested speed");
		this.add(velStatus);				
		
		
		SavableText velTol = new SavableText();
		velTol.setPin(new PinDef("myini.vel-tol", HalPinDirection.HAL_OUT, ValueType.HAL_FLOAT));
		velTol.requiresHalRCompSet = true;
		velTol.setNeedsSave(true);
		velTol.setParId("myini.vel-tol");
		velTol.setLabelTxt("Velocity Tolerance (Corner Lock):");
		velTol.setToolTipText("Velocity Tolerance (Corner Lock)s");
		this.add(velTol);			
		
	}
}
