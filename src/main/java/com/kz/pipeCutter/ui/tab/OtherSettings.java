package com.kz.pipeCutter.ui.tab;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;

import com.kz.pipeCutter.SurfaceDemo;
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
		parseFile.jValue.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyChar() == '\n') {
					SurfaceDemo.instance.init();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});
		this.add(parseFile);
		
		
		//rotations of input file
		SavableText rotations = new SavableText();
		rotations.setLabelTxt("Rotations:");
		rotations.setParId("rotations");
		rotations.jValue.setToolTipText("Add coordinate and separate with space example: x+90 y-90 z+270");
		rotations.jValue.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e.getKeyChar() == '\n') {
					SurfaceDemo.instance.init();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub

			}
		});
		this.add(rotations);		
		

		SavableText screenshotFolder = new SavableText();
		screenshotFolder.setLabelTxt("Screenshot folder:");
		screenshotFolder.setParId("screenshot_folder");
		this.add(screenshotFolder);

		SavableText radius = new SavableText();
		radius.setLabelTxt("Pipe radius:");
		radius.setParId("pipe_radius");
		this.add(radius);

		SavableCheckBox cutKerfOffset = new SavableCheckBox();
		cutKerfOffset.setLabelTxt("Kerf offset edge wile cuttting");
		cutKerfOffset.setParId("cut_kerf_offset");
		cutKerfOffset.setNeedsSave(true);
		this.add(cutKerfOffset);

		SavableText pipeDimX = new SavableText();
		pipeDimX.setNeedsSave(true);
		pipeDimX.setLabelTxt("Pipe X dimension:");
		pipeDimX.setParId("pipe_dim_x");
		this.add(pipeDimX);

		SavableText pipeDimZ = new SavableText();
		pipeDimZ.setNeedsSave(true);
		pipeDimZ.setLabelTxt("Pipe Z dimension:");
		pipeDimZ.setParId("pipe_dim_z");
		this.add(pipeDimZ);

		SavableText pipeDimMaxY = new SavableText();
		pipeDimMaxY.setNeedsSave(true);
		pipeDimMaxY.setLabelTxt("Pipe Y max dimension:");
		pipeDimMaxY.setParId("pipe_dim_max_y");
		this.add(pipeDimMaxY);

		SavableText pipeDimMinY = new SavableText();
		pipeDimMinY.setNeedsSave(true);
		pipeDimMinY.setLabelTxt("Pipe Y min dimension:");
		pipeDimMinY.setParId("pipe_dim_min_y");
		this.add(pipeDimMinY);

		SavableText zoomCenter = new SavableText();
		zoomCenter.setLabelTxt("Zoom center:");
		zoomCenter.setParId("ui_zoom_center");
		zoomCenter.setNeedsSave(true);
		this.add(zoomCenter);

		SavableText zoomRadius = new SavableText();
		zoomRadius.setLabelTxt("Zoom radius:");
		zoomRadius.setParId("ui_zoom_radius");
		zoomRadius.setNeedsSave(true);
		this.add(zoomRadius);

		SavableCheckBox numberEdges = new SavableCheckBox();
		numberEdges.setLabelTxt("Number edges");
		numberEdges.setParId("ui_number_edges");
		numberEdges.setNeedsSave(true);
		this.add(numberEdges);

		SavableCheckBox numberPoints = new SavableCheckBox();
		numberPoints.setLabelTxt("Number points");
		numberPoints.setParId("ui_number_points");
		numberPoints.setNeedsSave(true);
		this.add(numberPoints);

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

		SavableCheckBox uiZoomPlasma = new SavableCheckBox();
		uiZoomPlasma.setLabelTxt("Zoom plasma");
		uiZoomPlasma.setParId("ui_zoom_plasma");
		uiZoomPlasma.setNeedsSave(true);
		this.add(uiZoomPlasma);

		SavableCheckBox uiZoomPoint = new SavableCheckBox();
		uiZoomPoint.setLabelTxt("Zoom point");
		uiZoomPoint.setParId("ui_zoom_point");
		uiZoomPoint.setNeedsSave(true);
		this.add(uiZoomPoint);

		SavableText uiZoom = new SavableText();
		uiZoom.setLabelTxt("Zoom");
		uiZoom.setParId("ui_zoom");
		uiZoom.setNeedsSave(true);
		this.add(uiZoom);

		SavableText laser1IP = new SavableText();
		laser1IP.setLabelTxt("Laser 1 IP");
		laser1IP.setParId("laser_1_ip");
		laser1IP.setNeedsSave(true);
		this.add(laser1IP);

		SavableText positioner1reassign = new SavableText();
		positioner1reassign.setLabelTxt("Rotator 1 reassign: ");
		positioner1reassign.setParId("rotator_1_reassign");
		positioner1reassign.setNeedsSave(true);
		this.add(positioner1reassign);

		SavableText positioner2reassign = new SavableText();
		positioner2reassign.setLabelTxt("Rotator 2 reassign: ");
		positioner2reassign.setParId("rotator_2_reassign");
		positioner2reassign.setNeedsSave(true);
		this.add(positioner2reassign);

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
