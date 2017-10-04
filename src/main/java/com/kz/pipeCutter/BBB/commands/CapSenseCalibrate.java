package com.kz.pipeCutter.BBB.commands;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.jfree.chart.axis.ValueAxis;

import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.XYZSettings;

public class CapSenseCalibrate implements Runnable {

	boolean shouldStop = false;
	public static float offsetX = 5; // we assume probe is in center x and arround
																		// 5mm above pipe

	@Override
	public void run() {
		Thread.currentThread().setName("CapSenseCalibrate");
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(".\\capsense.cvs", "UTF-8");
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		Settings.getInstance().xyzSettings.seriesXZ.clear();
		Settings.getInstance().xyzSettings.updateChartRange();

		int waitPositionMs = 30;
		new ExecuteMdi("G90").start();

		// we assume we are on X center of pipe. First we move to touch the pipe
		float deltaZ=-0.1f;
		while (true && !shouldStop) {
			float lasDist = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1"));
			float zPos = Float.valueOf(Settings.getInstance().getSetting("position_z"));
			if (lasDist > 8.0025)
				deltaZ = -0.1f;
			else
				break;
			float newZPos = zPos + deltaZ;

			new ExecuteMdi("G00 Z" + newZPos).start();

			while (true) {
				float tempZPos = Float.valueOf(Settings.getInstance().getSetting("position_z"));
				if (Math.round(tempZPos * 10.0) / 10.0 <= Math.round(newZPos * 10.0) / 10.0)
					break;

				try {
					Thread.sleep(waitPositionMs);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		Settings.getInstance().log("Probe touched pipe.");

		float xPos0 = Float.valueOf(Settings.getInstance().getSetting("position_x"));
		float zPos0 = Float.valueOf(Settings.getInstance().getSetting("position_z"));

		deltaZ = 0.1f;
		float newZPos = zPos0;
		while (true) {
			newZPos = newZPos + deltaZ;
			new ExecuteMdi("G01 Z" + newZPos + " F150").start();
			
			float tempZPos = Float.valueOf(Settings.getInstance().getSetting("position_z"));
			while(Math.round(tempZPos * 10.0) / 10.0 < Math.round(newZPos * 10.0) / 10.0)
			{
				tempZPos = Float.valueOf(Settings.getInstance().getSetting("position_z"));
				try {
					Thread.sleep(waitPositionMs);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(waitPositionMs);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			tempZPos = Float.valueOf(Settings.getInstance().getSetting("position_z"));
			float zLas = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1"));

			Settings.getInstance().xyzSettings.seriesXZ.add((tempZPos-zPos0), zLas);
			writer.println((tempZPos-zPos0) + ";" + zLas);

			if(Math.round(newZPos * 10.0) / 10.0 >= Math.round((zPos0+5) * 10.0) / 10.0)
				break;
			
			try {
				Thread.sleep(waitPositionMs);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		writer.close();

		ValueAxis domainAxis = Settings.getInstance().xyzSettings.chart.getXYPlot().getDomainAxis();
		ValueAxis rangeAxis = Settings.getInstance().xyzSettings.chart.getXYPlot().getRangeAxis();
		//
		double xMeasures = Settings.getInstance().xyzSettings.seriesXZ.getMaxX() - Settings.getInstance().xyzSettings.seriesXZ.getMinX();
		double yMeasures = Settings.getInstance().xyzSettings.seriesXZ.getMaxY() - Settings.getInstance().xyzSettings.seriesXZ.getMinY();
		//
		// Float posX = Float.valueOf(Settings.getInstance().getSetting("position_x"));
		// Float pipeDimZ =
		// Float.valueOf(Settings.getInstance().getSetting("pipe_dim_z"));
		//
		// // current las measurement
		// Float lasHeight =
		// Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1"));

		domainAxis.setRange(Settings.getInstance().xyzSettings.seriesXZ.getMinX() - 0.1 * xMeasures,
				Settings.getInstance().xyzSettings.seriesXZ.getMaxX() + 0.1 * xMeasures);
		rangeAxis.setRange(Settings.getInstance().xyzSettings.seriesXZ.getMinY() - 0.1 * yMeasures,
				Settings.getInstance().xyzSettings.seriesXZ.getMaxY() + 0.1 * yMeasures);

		Settings.getInstance().xyzSettings.chart.fireChartChanged();
		XYZSettings.getInstance().repaint();
		
		new ExecuteMdi("G00 X" + xPos0).start();

		// if (!shouldStop) {
		// new ExecuteMdi(String.format("G01 X%5.3f " + speed, (xmin + xmax) /
		// 2.0f)).start();
		//
		// Settings.getInstance().log(String.format("xmin: %5.3f xmax: %5.3f x_center:
		// %5.3f", xmin, xmax, (xmin + xmax) / 2.0f));
		// }
	}

	public void stop() {
		shouldStop = true;
	}

}
