package com.kz.pipeCutter.BBB.commands;

import java.io.File;

import org.apache.log4j.Logger;

import com.kz.pipeCutter.SurfaceDemo;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.GcodeViewer;
import com.kz.pipeCutter.ui.tab.RotatorSettings;

public class CenterXOnPipeProcedure implements Runnable {

	@Override
	public void run() {

		SurfaceDemo.instance.utils.calculateMaxAndMins();
		String folder = Settings.getInstance().getSetting("gcode_folder");
		File f = new File(folder + File.separatorChar + "prog.gcode");

		Settings.getInstance().log(String.format("Delete file %s %b", f.getAbsolutePath(), f.delete()));

		float dimPipeX = Float.valueOf(Settings.instance.getSetting("pipe_dim_x"));
		float dimPipeZ = Float.valueOf(Settings.instance.getSetting("pipe_dim_z"));
		float dimMaxY = Float.valueOf(Settings.instance.getSetting("pipe_dim_max_y"));
		float dimMinY = Float.valueOf(Settings.instance.getSetting("pipe_dim_min_y"));

		float plasmaAboveOff = Float.valueOf(Settings.instance.getSetting("plasma_cut_offset_mm"));
		float plasmaAbovePierceOff = Float.valueOf(Settings.instance.getSetting("plasma_pierce_offset_mm"));
		float plasmaPierceTimeS = Float.valueOf(Settings.instance.getSetting("plasma_pierce_time_s"));

		float g0Speed = Float.valueOf(Settings.instance.getSetting("gcode_feedrate_g0"));
		float g1Speed = Float.valueOf(Settings.instance.getSetting("gcode_feedrate_g1"));

		double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0f));

		SurfaceDemo.instance.writeToGcodeFile(String.format(java.util.Locale.US, "G00 X0 Y%.2f Z%.2f", dimMaxY, dimPipeZ / 2 + plasmaAbovePierceOff));

		// turn plasma on
		SurfaceDemo.instance.writeToGcodeFile(String.format(java.util.Locale.US, "M3 S400"));

		// wait for pierce
		SurfaceDemo.instance.writeToGcodeFile(String.format(java.util.Locale.US, "G04 P%.2f", plasmaPierceTimeS));

		// go to first cut point
		SurfaceDemo.instance.writeToGcodeFile(String.format(java.util.Locale.US, "G00 X0 Y%.2f Z%.2f", dimMaxY, dimPipeZ / 2 + plasmaAboveOff, g0Speed));

		// cut 10mm
		SurfaceDemo.instance
				.writeToGcodeFile(String.format(java.util.Locale.US, "G01 X0 Y%.2f Z%.2f F%.2f", dimMaxY - 10.0f, dimPipeZ / 2 + plasmaAboveOff, g1Speed));

		// turn off plasma
		SurfaceDemo.instance.writeToGcodeFile(String.format(java.util.Locale.US, "M5"));

		// move to safe pos
		SurfaceDemo.instance.writeToGcodeFile(String.format(java.util.Locale.US, "G00 X0 Y%.2f Z%.2f", dimMaxY - 10.0f, diagonal / 2 + 10.0f));

		// End program
		SurfaceDemo.instance.writeToGcodeFile(String.format(java.util.Locale.US, "M02"));
		
		
		Settings.instance.log("GCode procedure created. Upload to MK run and enter position of xCut.");
	}

}
