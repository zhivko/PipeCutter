package com.kz.pipeCutter;

import java.util.Comparator;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;

public class PointComparator implements Comparator<Point> {

	private Point point;

	public PointComparator(Point p) {
		this.point = p;
	}

	@Override
	public int compare(Point mp1, Point mp2) {
		// TODO Auto-generated method stub
		double distance1 = mp1.getCoord().distance(point.xyz);
		double distance2 = mp2.getCoord().distance(point.xyz);

		if (distance1 < distance2)
			return -1;
		else if (distance1 == distance2)
			return 0;
		else
			return 1;

	}

	/**
	 * @param args
	 */

}
