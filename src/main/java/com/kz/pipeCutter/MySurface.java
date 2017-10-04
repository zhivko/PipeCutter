package com.kz.pipeCutter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeMap;

import org.jzy3d.maths.Coord3d;

public class MySurface {

	/**
	 * @param args
	 */
	Coord3d center;
	ArrayList<MyEdge> edges = null;
	Integer surfaceNo;

	public MySurface(Integer surface) {
		this.edges = new ArrayList<MyEdge>();
		this.surfaceNo = surface;
	}

	public void addEdge(MyEdge e) {
		edges.add(e);
		float centerX=0;
		float centerY=0;
		float centerZ=0;
		for (MyEdge edge : edges) {
			centerX += edge.center.x;
			centerY += edge.center.y;
			centerZ += edge.center.z;
		}
		this.center = new Coord3d(centerX/edges.size(), centerY/edges.size(), centerZ/edges.size());
	}
	
	public MyEdge getEdge(Integer edgeNo)
	{
		return SurfaceDemo.getInstance().utils.edges.get(edgeNo);
	}
	
	public Collection<MyEdge> getEdges() {
		return this.edges;
	}

	public void reorderEdges() {
//		System.out.println("center: " + this.center);
//		for (MyPickablePoint point : points) {
//			double a = MyEdge
//					.calculateAngle(point.xyz, center, points.get(0).xyz, points.get(0).xyz, points.get(1).xyz);
//			System.out.println(a + " " + point.toString());
//		}
		Collections.sort(edges, new MyEdgeComparator(this.center, this.edges.get(0).center, this.edges.get(0).center,
				this.edges.get(1).center));
//		for (MyPickablePoint point : points) {
//
//			double a = MyEdge
//					.calculateAngle(point.xyz, center, points.get(0).xyz, points.get(0).xyz, points.get(1).xyz);
//			System.out.println(a + " " + point.toString());
//		}

	}
	
}
