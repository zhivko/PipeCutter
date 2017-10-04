package com.kz.pipeCutter;

import java.util.Comparator;

import org.apache.commons.math3.geometry.spherical.twod.Edge;

public class RoundPointComparator2 implements Comparator<MyPickablePoint> {

	@Override
	public int compare(MyPickablePoint o1, MyPickablePoint o2) {
		// TODO Auto-generated method stub
		
		if(o1.continuousEdgeNo == 15)
		{
			System.out.println("Comparing " + o1.id + " to " + o2.id);
		}
		
		for (MyEdge edg : SurfaceDemo.getInstance().utils.edges.values()) {
			if (edg.points.get(0) == o1.id && edg.points.get(1) == o2.id) {
				return 1;
			}
		}
		
		return -1;
	}
}
