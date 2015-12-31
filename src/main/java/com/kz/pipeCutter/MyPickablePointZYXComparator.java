package com.kz.pipeCutter;

import java.util.Comparator;

public class MyPickablePointZYXComparator implements Comparator<MyPickablePoint> {

	public MyPickablePointZYXComparator() {
	}

	@Override
	public int compare(MyPickablePoint p1, MyPickablePoint p2) {
		// TODO Auto-generated method stub

		if (p1.xyz.z > p2.xyz.z)
			return -1;
		else if (p1.xyz.z < p2.xyz.z)
			return 1;
		else {
			if (p1.xyz.y > p2.xyz.y)
				return -1;
			else if (p1.xyz.y < p2.xyz.y)
				return 1;
			else {
				if (p1.xyz.x > p2.xyz.x)
					return -1;
				else if (p1.xyz.x < p2.xyz.x)
					return 1;
				else
					return 0;
			}
		}
	}
}
