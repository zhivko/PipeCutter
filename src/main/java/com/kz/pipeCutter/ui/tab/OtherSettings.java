package com.kz.pipeCutter.ui.tab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import com.kz.pipeCutter.BBB.BBBHalCommand;
import com.kz.pipeCutter.BBB.BBBHalRComp;
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

		SavableText screenshotFolder = new SavableText();
		screenshotFolder.setLabelTxt("Screenshot folder:");
		screenshotFolder.setParId("screenshot_folder");
		this.add(screenshotFolder);

		SavableText zoomBounds = new SavableText();
		zoomBounds.setLabelTxt("Zoom bounds:");
		zoomBounds.setParId("zoom_bounds");
		this.add(zoomBounds);

		SavableCheckBox pingBBBHalCmd = new SavableCheckBox() {
			@Override
			public void valueChangedFromUI() {
				// TODO Auto-generated method stub
				super.valueChangedFromUI();
				if (Settings.instance != null) {
					if (Settings.instance.isVisible() && this.getParValue().equals("0"))
						BBBHalCommand.getInstance().stopPing();
					else
						BBBHalCommand.getInstance().startPing();
				}
			}
		};
		pingBBBHalCmd.setLabelTxt("Ping bbb halcmd");
		pingBBBHalCmd.setParId("ping_halcmd");
		this.add(pingBBBHalCmd);

		
		SavableCheckBox g93mode = new SavableCheckBox();
		g93mode.setLabelTxt("G93 mode");
		g93mode.setParId("gcode_g93");
		this.add(g93mode);
		
		
		
		SavableText hal_vx = new SavableText();
		hal_vx.setLabelTxt("HAL vx:");
		hal_vx.setParId("mymotion.vx");
		hal_vx.setNeedsSave(false);
		hal_vx.setPin(new PinDef("mymotion.vx", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		this.add(hal_vx);

		SavableText hal_dvx = new SavableText();
		hal_dvx.setLabelTxt("HAL dvx:");
		hal_dvx.setParId("mymotion.dvx");
		hal_dvx.setNeedsSave(false);
		hal_dvx.setPin(new PinDef("mymotion.dvx", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		this.add(hal_dvx);
		
		SavableText hal_vy = new SavableText();
		hal_vy.setLabelTxt("HAL vy:");
		hal_vy.setParId("mymotion.vy");
		hal_vy.setNeedsSave(false);
		hal_vy.setPin(new PinDef("mymotion.vy", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		this.add(hal_vy);
		
		SavableText hal_vz = new SavableText();
		hal_vz.setLabelTxt("HAL vz:");
		hal_vz.setParId("mymotion.vz");
		hal_vz.setNeedsSave(false);
		hal_vz.setPin(new PinDef("mymotion.vz", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		this.add(hal_vz);

		SavableText hal_dvz = new SavableText();
		hal_dvz.setLabelTxt("HAL dvz:");
		hal_dvz.setParId("mymotion.dvz");
		hal_dvz.setNeedsSave(false);
		hal_dvz.setPin(new PinDef("mymotion.dvz", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		this.add(hal_dvz);
		
		SavableText hal_curr_radius = new SavableText();
		hal_curr_radius.setLabelTxt("HAL radius:");
		hal_curr_radius.setParId("mymotion.current-radius");
		hal_curr_radius.setNeedsSave(false);
		hal_curr_radius.setPin(new PinDef("mymotion.current-radius", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		this.add(hal_curr_radius);

		SavableText hal_v = new SavableText();
		hal_v.setLabelTxt("HAL v:");
		hal_v.setParId("mymotion.v");
		hal_v.setNeedsSave(false);
		hal_v.setPin(new PinDef("mymotion.v", HalPinDirection.HAL_IN, ValueType.HAL_FLOAT));
		this.add(hal_v);
		
		
	}
}
