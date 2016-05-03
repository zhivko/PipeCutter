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

public class OtherSettings extends JPanel {

	
	public OtherSettings() {
		super();
		this.setPreferredSize(new Dimension(420, 450));
		MyVerticalFlowLayout flowLayout = new MyVerticalFlowLayout();
		this.setLayout(flowLayout);

		SavableText gcodeFolder = new SavableText();
		gcodeFolder.setLabelTxt("Gcode Folder:");
		gcodeFolder.setParId("gcode_folder");
		this.add(gcodeFolder);
		
		SavableText G0feedRate = new SavableText();
		G0feedRate.setLabelTxt("g0 feedrate:");
		G0feedRate.setParId("gcode_feedrate_g0");
		this.add(G0feedRate);		

		SavableText G1feedRate = new SavableText();
		G1feedRate.setLabelTxt("g1 feedrate:");
		G1feedRate.setParId("gcode_feedrate_g1");
		this.add(G1feedRate);			

		SavableText parseFile = new SavableText();
		parseFile.setLabelTxt("File to parse:");
		parseFile.setParId("gcode_input_file");
		this.add(parseFile);			
		
	}
}
