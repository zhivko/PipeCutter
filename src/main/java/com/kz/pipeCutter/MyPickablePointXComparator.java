package com.kz.pipeCutter;

import java.util.Comparator;

public class MyPickablePointXComparator implements Comparator<MyPickablePoint> {

	public MyPickablePointXComparator() {
	}

	@Override
	public int compare(MyPickablePoint p1, MyPickablePoint p2) {
		// TODO Auto-generated method stub
		if (p1.xyz.x < p2.xyz.x)
			return -1;
		else if (p1.xyz.x == p2.xyz.x)
			return 0;
		else
			return 1;
	}
}

