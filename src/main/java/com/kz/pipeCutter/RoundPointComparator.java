package com.kz.pipeCutter;

import java.util.Comparator;

import org.jzy3d.maths.Coord3d;

public class RoundPointComparator implements Comparator<Integer> {

	@Override
	public int compare(Integer o1, Integer o2) {
		// TODO Auto-generated method stub
		MyPickablePoint p1 = SurfaceDemo.instance.utils.points.get(o1);
		MyContinuousEdge contEdge = SurfaceDemo.instance.utils.continuousEdges.get(p1.continuousEdgeNo);
		
		for (MyEdge edge : contEdge.connectedEdges) {
			if (edge.points.get(0).equals(o1) && edge.points.get(1).equals(o2)) {
				return -1;
			}
		}
		return 1;
	}
}
