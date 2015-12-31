package com.kz.pipeCutter;

import java.util.Comparator;

public class MyPickablePointYComparator implements Comparator<MyPickablePoint> {

	public MyPickablePointYComparator() {
	}

	@Override
	public int compare(MyPickablePoint p1, MyPickablePoint p2) {
		// TODO Auto-generated method stub
		if (p1.xyz.y < p2.xyz.y)
			return -1;
		else if (p1.xyz.y == p2.xyz.y)
			return 0;
		else
			return 1;
	}
}

