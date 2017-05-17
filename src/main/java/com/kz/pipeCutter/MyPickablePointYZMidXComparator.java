package com.kz.pipeCutter;

import java.util.Comparator;

public class MyPickablePointYZMidXComparator implements Comparator<MyPickablePoint> {

	public MyPickablePointYZMidXComparator() {
	}

	@Override
	public int compare(MyPickablePoint p1, MyPickablePoint p2) {
		// TODO Auto-generated method stub

		if (p1.xyz.y > p2.xyz.y)
			return -1;
		else if (p1.xyz.y < p2.xyz.y)
			return 1;
		else {
			if (Math.abs(p1.xyz.z) > Math.abs(p2.xyz.z))
				return 1;
			else if (Math.abs(p1.xyz.z) < Math.abs(p2.xyz.z))
				return -1;
			else
				if (Math.abs(p1.xyz.x) > Math.abs(p2.xyz.x))
					return 1;
				else if (Math.abs(p1.xyz.x) < Math.abs(p2.xyz.x))
					return -1;
				else
					if (p1.xyz.y > p2.xyz.y)
						return -1;
					else if (p1.xyz.y < p2.xyz.y)
						return 1;
		}		
		return 0;
	}
}
