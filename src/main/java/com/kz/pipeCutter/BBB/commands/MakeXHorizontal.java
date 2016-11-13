package com.kz.pipeCutter.BBB.commands;

import java.util.HashMap;

import org.apache.commons.math3.stat.regression.SimpleRegression;

import com.kz.pipeCutter.ui.Settings;

public class MakeXHorizontal implements Runnable {

	@Override
	public void run() {
		SimpleRegression regression = new SimpleRegression();

		HashMap<Float, Float> xz = new HashMap<Float, Float>();

		int waitPositionMs = 200;
		float maxDx = (Float.valueOf(Settings.instance.getSetting("pipe_dim_x")) - 2.0f * Float.valueOf(Settings.instance.getSetting("radius"))) / 2.0f;

		int speed = 1000;
		float xmin, xmax;
		float z;
		float xPos = Float.valueOf(Settings.getInstance().getSetting("position_x"));
		z = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));
		float startZ = z;
		float startX = xPos;
		float deltaX = 1.5f;
		while (true) {
			float newXPos = Math.round((xPos + deltaX) * 10) / 10.0f;
			new ExecuteMdi(String.format("G01 X%5.3f F%d", newXPos, speed)).start();

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
			try {
				Thread.sleep(waitPositionMs);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			z = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));
			xz.put(Float.valueOf(xPos), Float.valueOf(z));
			regression.addData(xPos, z);

			if (z > 1000 || Math.abs(startX - newXPos) > maxDx) {
				xmin = xPos;
				break;
			}
		}

		new ExecuteMdi(String.format("G01 X%5.3f F%d", startX, speed)).start();

		while (true) {
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
			new ExecuteMdi(String.format("G01 X%5.3f F%d", newXPos, speed)).start();
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
			try {
				Thread.sleep(waitPositionMs);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			z = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));
			xz.put(Float.valueOf(xPos), Float.valueOf(z));
			regression.addData(xPos, z);
			if (z > 1000 || Math.abs(startX - newXPos) > maxDx) {
				xmax = xPos;
				break;
			}
		}
		new ExecuteMdi(String.format("G01 X%5.3f F%d", startX, speed)).start();

		System.out.println(regression.getIntercept());
		// displays intercept of regression line
		System.out.println(regression.getSlope());
		// displays slope of regression line
		System.out.println(regression.getSlopeStdErr());
		// displays slope standard error

		double angle = Math.atan2(regression.getSlope(), 1.0d);
		double angleDeg = angle * 180.0f / Math.PI;

		Settings.instance.log(String.format("xmin: %5.3f xmax: %5.3f x_center: %5.3f", xmin, xmax, (xmin + xmax) / 2.0f));
		Settings.instance.log(String.format("angle: %5.3f", angleDeg));
		double angleCorr = -angleDeg;
		Settings.instance.log(String.format("Rotating for angle: %5.3f", angleCorr));

		Float maxRotSpeed = Float.valueOf(Settings.instance.getSetting("myini.maxvel_3"));
		new ExecuteMdi(String.format("G91\nG01 A%5.3f B%5.3f F%5.3f\nG90", angleCorr, angleCorr, maxRotSpeed)).start();
	}

}
