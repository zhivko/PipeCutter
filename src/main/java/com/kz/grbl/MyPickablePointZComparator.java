package com.kz.grbl;

import java.util.Comparator;

public class MyPickablePointZComparator implements Comparator<MyPickablePoint> {

	public MyPickablePointZComparator() {
	}

	@Override
	public int compare(MyPickablePoint p1, MyPickablePoint p2) {
		// TODO Auto-generated method stub
		if (p1.xyz.z < p2.xyz.z)
			return -1;
		else if (p1.xyz.z == p2.xyz.z)
			return 0;
		else
			return 1;
	}
}

