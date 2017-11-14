package com.kz.pipeCutter.BBB.commands;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.jfree.chart.axis.ValueAxis;

import com.kz.pipeCutter.ui.MyLaserWebsocketClient;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.XYZSettings;

public class CapSenseCalibrate implements Runnable {

	static CapSenseCalibrate instance;
	boolean shouldStop = false;
	public static float offsetX = 5; // we assume probe is in center x and arround
																		// 5mm above pipe

	public static CapSenseCalibrate getInstance() {
		if (instance == null)
			instance = new CapSenseCalibrate();
		return instance;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("CapSenseCalibrate");
		shouldStop = false;

		Thread.currentThread().setName("CapSenseCalibrate");
		
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(".\\capsense.csv", "UTF-8");

			Settings.getInstance().xyzSettings.seriesXZ.clear();
			Settings.getInstance().xyzSettings.updateChartRangeCap();

			int waitPositionMs = 30;
			new ExecuteMdi("G90").start();

			// we assume we are on X center of pipe. First we move to touch the pipe
			float deltaZ = -0.1f;
			while (true && !shouldStop) {
				float lasDist = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1"));

				String position_z = "";
				while (position_z.equals("")) {
					position_z = Settings.getInstance().getSetting("position_z");
					try {
						Thread.sleep(waitPositionMs);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				float zPos = Float.valueOf(position_z);

				if (lasDist > 8.0025)
					deltaZ = -0.1f;
				else
					break;
				float newZPos = zPos + deltaZ;

				new ExecuteMdi("G00 Z" + newZPos).start();

				while (true) {

					String tempZPos = "";
					while (tempZPos.equals("")) {
						tempZPos = Settings.getInstance().getSetting("position_z");
						try {
							Thread.sleep(waitPositionMs);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					if (Math.round(Float.valueOf(tempZPos) * 10.0) / 10.0 <= Math.round(newZPos * 10.0) / 10.0)
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
			while (true && !shouldStop) {
				newZPos = newZPos + deltaZ;
				new ExecuteMdi("G01 Z" + newZPos + " F150").start();

				float tempZPos = Float.valueOf(Settings.getInstance().getSetting("position_z"));
				while (Math.round(tempZPos * 10.0) / 10.0 < Math.round(newZPos * 10.0) / 10.0) {
					String position_z =Settings.getInstance().getSetting("position_z");
					tempZPos = Float.valueOf(position_z);
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

				Settings.getInstance().xyzSettings.seriesXZ.add((tempZPos - zPos0), zLas);
				writer.println((tempZPos - zPos0) + "," + zLas);

				if (Math.round(newZPos * 10.0) / 10.0 >= Math.round((zPos0 + 5) * 10.0) / 10.0)
					break;

				try {
					Thread.sleep(waitPositionMs);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			writer.close();
			if(shouldStop)
				return;

			ValueAxis domainAxis = Settings.getInstance().xyzSettings.chart.getXYPlot().getDomainAxis();
			ValueAxis rangeAxis = Settings.getInstance().xyzSettings.chart.getXYPlot().getRangeAxis();
			//
			double xMeasures = Settings.getInstance().xyzSettings.seriesXZ.getMaxX() - Settings.getInstance().xyzSettings.seriesXZ.getMinX();
			double yMeasures = Settings.getInstance().xyzSettings.seriesXZ.getMaxY() - Settings.getInstance().xyzSettings.seriesXZ.getMinY();

			domainAxis.setRange(Settings.getInstance().xyzSettings.seriesXZ.getMinX() - 0.1 * xMeasures,
					Settings.getInstance().xyzSettings.seriesXZ.getMaxX() + 0.1 * xMeasures);
			rangeAxis.setRange(Settings.getInstance().xyzSettings.seriesXZ.getMinY() - 0.1 * yMeasures,
					Settings.getInstance().xyzSettings.seriesXZ.getMaxY() + 0.1 * yMeasures);

			Settings.getInstance().xyzSettings.chart.fireChartChanged();
			XYZSettings.getInstance().repaint();

			new ExecuteMdi("G00 X" + xPos0).start();

			MyLaserWebsocketClient.getInstance().init();
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void stop() {
		shouldStop = true;
	}

}
