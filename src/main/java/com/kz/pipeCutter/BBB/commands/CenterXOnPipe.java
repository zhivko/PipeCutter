package com.kz.pipeCutter.BBB.commands;

import org.jfree.chart.axis.ValueAxis;

import com.kz.pipeCutter.ui.Settings;

public class CenterXOnPipe implements Runnable {

	boolean shouldStop = false;
	public static float offsetX = 10; // we need to be in center x within 10mm before starting center procedure

	@Override
	public void run() {
		Thread.currentThread().setName("CenterXOnPipe");
		
		Settings.getInstance().xyzSettings.seriesXZ.clear();
		Settings.getInstance().xyzSettings.updateChartRange();

		int waitPositionMs = 30;
		new ExecuteMdi("G90").start();
		
		// we assume we are on X center of pipe. First we move to half pipe x dimension
		float xPos = Float.valueOf(Settings.getInstance().getSetting("position_x"));
		float xPosStart = xPos;
		float pipe_dim_x = Float.valueOf(Settings.getInstance().getSetting("pipe_dim_x"));
		
		float newXPos = -(pipe_dim_x/2 + offsetX);
		
		new ExecuteMdi("G00 X" + newXPos).start();
		while (true && !shouldStop) {
			// we need to check for position x to reach new x pos
			String xVal = Settings.getInstance().getSetting("position_x");
			if (xVal != null && !xVal.equals("")) {
				xPos = Float.valueOf(xVal);
				if (xPos == newXPos)
					break;
			}
			try {
				Thread.sleep(waitPositionMs);
				Settings.getInstance().log("Waiting for Xpos...");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			
			
		}
		if(shouldStop)
			return;

		
		String speed = " F1000";
		float xmin, xmax;
		float z;
		xPos = Float.valueOf(Settings.getInstance().getSetting("position_x"));
		z = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1"));
		float startZ = z;
		float startX = xPos;
		float deltaX = 1.0f;
		while (true && !shouldStop) {
			newXPos = Math.round((xPos + deltaX) * 10) / 10.0f;
			new ExecuteMdi("G01 X" + newXPos + speed).start();
			while (true) {
				String xVal = Settings.getInstance().getSetting("position_x");
				
				if (xVal != null && !xVal.equals("")) {
					xPos = Float.valueOf(xVal);
					if (xPos == newXPos)
						break;
				}
				try {
					Thread.sleep(waitPositionMs);
					Settings.getInstance().log("Waiting for Xpos...");

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			String value = Settings.getInstance().getSetting("mymotion.laserHeight1");
			if (!value.equals("")) {
				z = Float.valueOf(value);
				Settings.getInstance().xyzSettings.seriesXZ.add(xPos, z);
				if (z > startZ + 1.5f)
					deltaX = 0.1f;
				if (z > 1000) {
					xmin = xPos;
					break;
				}
			}
			if(xPos >= (pipe_dim_x/2 + offsetX))
				break;
		}
		if(shouldStop)
			return;


		new ExecuteMdi("G01 X" + xPosStart + speed).start();
		
		
//		
//
//		while (true && !shouldStop) {
//			String xVal = Settings.getInstance().getSetting("position_x");
//			if (xVal != null && !xVal.equals("")) {
//				xPos = Float.valueOf(xVal);
//				if (xPos == startX)
//					break;
//			}
//			try {
//				Thread.sleep(waitPositionMs);
//			} catch (Exception ex) {
//				ex.printStackTrace();
//			}
//		}
//		if(shouldStop)
//			return;
//
//		deltaX = -1.5f;
//		while (true && !shouldStop) {
//			newXPos = Math.round((xPos + deltaX) * 10) / 10.0f;
//			new ExecuteMdi("G01 X" + newXPos + speed).start();
//			while (true) {
//				String xVal = Settings.getInstance().getSetting("position_x");
//				if (xVal != null && !xVal.equals("")) {
//					xPos = Float.valueOf(xVal);
//					if (xPos == newXPos)
//						break;
//				}
//				try {
//					Thread.sleep(waitPositionMs);
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//			}
//			z = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1"));
//			Settings.getInstance().xyzSettings.seriesXZ.add(xPos, z);
//
//			if (z > startZ + 1.5f)
//				deltaX = -0.1f;
//			if (z > 1000) {
//				xmax = xPos;
//				break;
//			}
//
//		}
//		if(shouldStop)
//			return;
//
//
		ValueAxis domainAxis = Settings.getInstance().xyzSettings.chart.getXYPlot().getDomainAxis();
		ValueAxis rangeAxis = Settings.getInstance().xyzSettings.chart.getXYPlot().getRangeAxis();
//
		double xMeasures = Settings.getInstance().xyzSettings.seriesXZ.getMaxX() - Settings.getInstance().xyzSettings.seriesXZ.getMinX();
		double yMeasures = Settings.getInstance().xyzSettings.seriesXZ.getMaxY() - Settings.getInstance().xyzSettings.seriesXZ.getMinY();
//
//		Float posX = Float.valueOf(Settings.getInstance().getSetting("position_x"));
//		Float pipeDimZ = Float.valueOf(Settings.getInstance().getSetting("pipe_dim_z"));
//
//		// current las measurement
//		Float lasHeight = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1"));

		domainAxis.setRange(Settings.getInstance().xyzSettings.seriesXZ.getMinX() - 0.1 * xMeasures,
				Settings.getInstance().xyzSettings.seriesXZ.getMaxX() + 0.1 * xMeasures);
		rangeAxis.setRange(Settings.getInstance().xyzSettings.seriesXZ.getMinY() - 0.1 * yMeasures,
				Settings.getInstance().xyzSettings.seriesXZ.getMaxY() + 0.1 * yMeasures);

//		if (!shouldStop) {
//			new ExecuteMdi(String.format("G01 X%5.3f " + speed, (xmin + xmax) / 2.0f)).start();
//
//			Settings.getInstance().log(String.format("xmin: %5.3f xmax: %5.3f x_center: %5.3f", xmin, xmax, (xmin + xmax) / 2.0f));
//		}
	}

	public void stop() {
		shouldStop = true;
	}

}
