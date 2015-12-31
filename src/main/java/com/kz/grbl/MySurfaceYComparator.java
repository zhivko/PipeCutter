package com.kz.grbl;

import java.util.Comparator;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Vector3d;

public class MySurfaceYComparator implements Comparator<MySurface> {

	public MySurfaceYComparator() {
	}

	@Override
	public int compare(MySurface surf1, MySurface surf2) {
		// TODO Auto-generated method stub
		if (surf1.center.y < surf2.center.y)
			return -1;
		else if (surf1.center.y < surf2.center.y)
			return 0;
		else
			return 1;
	}
}

