package com.kz.pipeCutter;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.jzy3d.colors.Color;
import org.jzy3d.plot3d.primitives.Point;

public class BlinkThread extends Thread {

	Point myPoint = null;
	Color origColor = null;
	private int count = 0;
	static ArrayList<Point> alPoints = new ArrayList();

	public BlinkThread(Point myPoint) {
		this.myPoint = myPoint;
		this.origColor = myPoint.getColor();
		if (!alPoints.contains(myPoint)) {
			alPoints.add(myPoint);
		}
	}

	/**
	 * @param args
	 */

	@Override
	public void run() {
		while (count < 10) {
			System.out.println(count);
			if (myPoint.getColor() == Color.WHITE) {
				myPoint.setColor(BlinkThread.this.origColor);
				System.out.println("BARVA");
			} else {
				myPoint.setColor(Color.WHITE);
				System.out.println("BELA");
			}
			try {
				TimeUnit.MILLISECONDS.sleep(300);
				SurfaceDemo.instance.getChart().render();
				count++;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		BlinkThread.alPoints.remove(myPoint);

	}
}
