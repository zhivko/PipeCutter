package com.kz.pipeCutter;

import java.util.Comparator;

import org.jzy3d.maths.Coord3d;

public class MyPickablePointMidXToEdgeCenterComparator implements Comparator<MyPickablePoint> {
	Coord3d center;

	public MyPickablePointMidXToEdgeCenterComparator(Coord3d center) {
		this.center = center;
	}

	@Override
	public int compare(MyPickablePoint p1, MyPickablePoint p2) {
		// TODO Auto-generated method stub
		if (Math.abs(p1.xyz.x - center.x) < Math.abs(p2.xyz.x - center.x))
			return -1;
		else if (Math.abs(p1.xyz.x - center.x) == Math.abs(p2.xyz.x - center.x))
			if (p1.xyz.y > p2.xyz.y)
				return -1;
			else if (p1.xyz.y < p2.xyz.y)
				return 1;
			else
				return 0;
		else
			return 1;
	}
}
