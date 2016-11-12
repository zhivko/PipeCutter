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

		String speed = " F1000";
		float xmin, xmax;
		float z;
		float xPos = Float.valueOf(Settings.getInstance().getSetting("position_x"));
		z = Float.valueOf(Settings.instance.getSetting("mymotion.laserHeight1"));
		float startZ = z;
		float startX = xPos;
		float deltaX = 1.5f;
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
			xz.put(Float.valueOf(xPos), Float.valueOf(z));
			regression.addData(xPos, z);

			if (z > startZ + 1.5f)
				deltaX = 0.1f;
			if (z > 1000) {
				xmin = xPos;
				break;
			}
		}

		new ExecuteMdi("G01 X" + startX + speed).start();

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
			xz.put(Float.valueOf(xPos), Float.valueOf(z));
			regression.addData(xPos, z);
			if (z > startZ + 1.5f)
				deltaX = -0.1f;
			if (z > 1000) {
				xmax = xPos;
				break;
			}
		}
		new ExecuteMdi(String.format("G01 X%5.3f F%d", (xmin + xmax) / 2.0f, speed)).start();

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
		Settings.instance.log(String.format("Rotating for half angle: %5.3f", angle / 2.0f));
		
		new ExecuteMdi(String.format("G91\nG01 A%5.3f B%5.3f %d\nG90", angle / 2.0f, angle / 2.0f, speed)).start();
	}

}
