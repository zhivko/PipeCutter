package com.kz.pipeCutter.BBB.commands;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.XYDataItem;

import com.kz.pipeCutter.ui.MyLaserWebsocketClient;
import com.kz.pipeCutter.ui.Settings;

public class CenterXOnPipe implements Runnable {

	boolean shouldStop = false;
	private static CenterXOnPipe instance;
	public static float offsetX = 10; // we need to be in center x within 10mm
																		// before starting center procedure

	public static CenterXOnPipe getInstance() {
		if (instance == null)
			instance = new CenterXOnPipe();
		return instance;
	}

	@Override
	public void run() {
		shouldStop = false;
		Thread.currentThread().setName("CenterXOnPipe");
		new ExecuteMdi("G90").start();
		if (!MyLaserWebsocketClient.getInstance().isOn) {
			Settings.getInstance().log("Connect capacitive sensor!");
			return;
		}

		Settings.getInstance().xyzSettings.seriesXZ.clear();
		Settings.getInstance().xyzSettings.updateChartRange();

		Float dimX = Float.valueOf(Settings.getInstance().getSetting("pipe_dim_x"));
		Float dimZ = Float.valueOf(Settings.getInstance().getSetting("pipe_dim_z"));

		float diagonal = (float) Math.sqrt(Math.pow(dimX / 2, 2) + Math.pow(dimZ / 2, 2));
		float highZPos = (diagonal + 20);
		CenterPipe.getInstance().executeMdiAndWaitFor("G00 Z" + highZPos, "position_z", highZPos);
		CenterPipe.getInstance().executeMdiAndWaitFor("G00  X0", "position_x", 0);

		CenterPipe.getInstance().moveProbeTo4mmOffset();

		int waitPositionMs = 30;

		// we assume we are on X center of pipe. First we move to half pipe x
		// dimension
		float xPos = Float.valueOf(Settings.getInstance().getSetting("position_x"));
		float xPosStart = xPos;
		float pipe_dim_x = Float.valueOf(Settings.getInstance().getSetting("pipe_dim_x"));

		float newXPos = -(pipe_dim_x / 2 + offsetX);

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
				// Settings.getInstance().log("Waiting for Xpos...");
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		if (shouldStop)
			return;

		String speed = " F1000";
		float xmin, xmax;
		float z;
		xPos = Float.valueOf(Settings.getInstance().getSetting("position_x"));
		z = Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1"));
		float startZ = z;
		float startX = xPos;
		float deltaX = 1.1f;
		while (true && !shouldStop) {
			newXPos = Math.round((xPos + deltaX) * 10) / 10.0f;
			new ExecuteMdi("G01 X" + newXPos + speed).start();
			while (true) {
				String xVal = Settings.getInstance().getNonEmptySetting("position_x");

				if (xVal != null && !xVal.equals("")) {
					xPos =  Math.round((Float.valueOf(xVal)) * 10) / 10.0f;
					if (xPos == newXPos)
						break;
				}
				try {
					Thread.sleep(waitPositionMs);
					Settings.getInstance().log("Waiting for Xpos... " + xPos + " != " + newXPos);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			String value = Settings.getInstance().getNonEmptySetting("mymotion.laserHeight1mm");
			if (!value.equals("")) {
				z = Float.valueOf(value);
				Settings.getInstance().xyzSettings.seriesXZ.add(xPos, z);
				// if (z > startZ + 1.5f)
				// deltaX = 0.1f;
				if (z > 1000) {
					xmin = xPos;
					break;
				}
			}
			if (xPos >= (pipe_dim_x / 2 + offsetX))
				break;
		}
		if (shouldStop)
			return;

		float sumX = 0;
		for (int i = 0; i < Settings.getInstance().xyzSettings.seriesXZ.getItems().size(); i++) {
			XYDataItem item = (XYDataItem) Settings.getInstance().xyzSettings.seriesXZ.getItems().get(i);
			if (item.getY().floatValue() < 5) {
				sumX = sumX + item.getX().floatValue();
			}
		}
		float avgX = sumX / Settings.getInstance().xyzSettings.seriesXZ.getItems().size();

		CenterPipe.getInstance().executeMdiAndWaitFor("G00 X" + avgX, "position_x", avgX);
		new ExecuteMdi("G92 X0").start();

		//
		//
		// while (true && !shouldStop) {
		// String xVal = Settings.getInstance().getSetting("position_x");
		// if (xVal != null && !xVal.equals("")) {
		// xPos = Float.valueOf(xVal);
		// if (xPos == startX)
		// break;
		// }
		// try {
		// Thread.sleep(waitPositionMs);
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
		// }
		// if(shouldStop)
		// return;
		//
		// deltaX = -1.5f;
		// while (true && !shouldStop) {
		// newXPos = Math.round((xPos + deltaX) * 10) / 10.0f;
		// new ExecuteMdi("G01 X" + newXPos + speed).start();
		// while (true) {
		// String xVal = Settings.getInstance().getSetting("position_x");
		// if (xVal != null && !xVal.equals("")) {
		// xPos = Float.valueOf(xVal);
		// if (xPos == newXPos)
		// break;
		// }
		// try {
		// Thread.sleep(waitPositionMs);
		// } catch (Exception ex) {
		// ex.printStackTrace();
		// }
		// }
		// z =
		// Float.valueOf(Settings.getInstance().getSetting("mymotion.laserHeight1"));
		// Settings.getInstance().xyzSettings.seriesXZ.add(xPos, z);
		//
		// if (z > startZ + 1.5f)
		// deltaX = -0.1f;
		// if (z > 1000) {
		// xmax = xPos;
		// break;
		// }
		//
		// }
		// if(shouldStop)
		// return;
		//
		//
		ValueAxis domainAxis = Settings.getInstance().xyzSettings.chart.getXYPlot().getDomainAxis();
		ValueAxis rangeAxis = Settings.getInstance().xyzSettings.chart.getXYPlot().getRangeAxis();
		//
		double xMeasures = Settings.getInstance().xyzSettings.seriesXZ.getMaxX() - Settings.getInstance().xyzSettings.seriesXZ.getMinX();
		double yMeasures = Settings.getInstance().xyzSettings.seriesXZ.getMaxY() - Settings.getInstance().xyzSettings.seriesXZ.getMinY();
		//
		// Float posX =
		// Float.valueOf(Settings.getInstance().getSetting("position_x"));
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

		// if (!shouldStop) {
		// new ExecuteMdi(String.format("G01 X%5.3f " + speed, (xmin + xmax) /
		// 2.0f)).start();
		//
		// Settings.getInstance().log(String.format("xmin: %5.3f xmax: %5.3f
		// x_center: %5.3f", xmin, xmax, (xmin + xmax) / 2.0f));
		// }
	}

	public void stop() {
		shouldStop = true;
	}

}
