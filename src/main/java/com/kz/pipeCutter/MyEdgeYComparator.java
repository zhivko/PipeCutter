package com.kz.pipeCutter;

import java.util.Comparator;

public class MyEdgeYComparator implements Comparator<MyEdge> {

	public MyEdgeYComparator() {
	}

	@Override
	public int compare(MyEdge e1, MyEdge e2) {
		// TODO Auto-generated method stub
		if (e1.center.y < e2.center.y)
			return -1;
		else if (e1.center.y == e2.center.y)
			return 0;
		else
			return 1;
	}
}

