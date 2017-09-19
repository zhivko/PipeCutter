package com.kz.pipeCutter.BBB.commands;

import org.jfree.chart.axis.ValueAxis;

import com.kz.pipeCutter.ui.Settings;

public class CenterXOnPipe implements Runnable {

	boolean shouldStop = false;

	@Override
	public void run() {
		Settings.instance.xyzSettings.seriesXZ.clear();
		Settings.instance.xyzSettings.updateChartRange();

		int waitPositionMs = 200;

		String speed = " F1000";
		float xmin, xmax;
		float z;
		float xPos = Float.valueOf(Settings.getInstance().getSetting("position_x"));
		z = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));
		float startZ = z;
		float startX = xPos;
		float deltaX = 1.5f;
		while (true && !shouldStop) {
			float newXPos = Math.round((xPos + deltaX) * 10) / 10.0f;
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
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			String value = Settings.instance.getSetting("mymotion.laserHeight1");
			if (!value.equals("")) {
				z = Float.valueOf(value);
				Settings.instance.xyzSettings.seriesXZ.add(xPos, z);
				if (z > startZ + 1.5f)
					deltaX = 0.1f;
				if (z > 1000) {
					xmin = xPos;
					break;
				}
			}

			if (shouldStop)
				return;
		}

		new ExecuteMdi("G01 X" + startX + speed).start();

		while (true && !shouldStop) {
			String xVal = Settings.getInstance().getSetting("position_x");
			if (xVal != null && !xVal.equals("")) {
				xPos = Float.valueOf(xVal);
				if (xPos == startX)
					break;
			}
			try {
				Thread.sleep(waitPositionMs);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		deltaX = -1.5f;
		while (true) {
			float newXPos = Math.round((xPos + deltaX) * 10) / 10.0f;
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
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			z = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));
			Settings.instance.xyzSettings.seriesXZ.add(xPos, z);

			if (z > startZ + 1.5f)
				deltaX = -0.1f;
			if (z > 1000) {
				xmax = xPos;
				break;
			}

		}

		ValueAxis domainAxis = Settings.instance.xyzSettings.chart.getXYPlot().getDomainAxis();
		ValueAxis rangeAxis = Settings.instance.xyzSettings.chart.getXYPlot().getRangeAxis();

		double xMeasures = Settings.instance.xyzSettings.seriesXZ.getMaxX() - Settings.instance.xyzSettings.seriesXZ.getMinX();
		double yMeasures = Settings.instance.xyzSettings.seriesXZ.getMaxY() - Settings.instance.xyzSettings.seriesXZ.getMinY();

		Float posX = Float.valueOf(Settings.instance.getSetting("position_x"));
		Float pipeDimZ = Float.valueOf(Settings.instance.getSetting("pipe_dim_z"));

		// current las measurement
		Float lasHeight = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));

		domainAxis.setRange(Settings.instance.xyzSettings.seriesXZ.getMinX() - 0.1 * xMeasures,
				Settings.instance.xyzSettings.seriesXZ.getMaxX() + 0.1 * xMeasures);
		rangeAxis.setRange(Settings.instance.xyzSettings.seriesXZ.getMinY() - 0.1 * yMeasures,
				Settings.instance.xyzSettings.seriesXZ.getMaxY() + 0.1 * yMeasures);

//		if (!shouldStop) {
//			new ExecuteMdi(String.format("G01 X%5.3f " + speed, (xmin + xmax) / 2.0f)).start();
//
//			Settings.instance.log(String.format("xmin: %5.3f xmax: %5.3f x_center: %5.3f", xmin, xmax, (xmin + xmax) / 2.0f));
//		}
	}

	public void stop() {
		shouldStop = true;
	}

}
