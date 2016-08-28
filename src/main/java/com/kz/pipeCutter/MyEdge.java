package com.kz.pipeCutter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;

import com.kz.pipeCutter.MyContinuousEdge.EdgeType;

public class MyEdge {

	/**
	 * @param args
	 */
	Coord3d center = null;
	ArrayList<Integer> points = null;
	Integer edgeNo;
	Integer surfaceNo;
	ArrayList<MyEdge> connectedEdges = null;
	float length;
	
	EdgeType edgeType;
	
	public enum EdgeType {
    ONRADIUS, NORMAL
	}	
	
	// length distribution
	public static HashMap<Float, Integer> hmLengthDistrib = new HashMap<Float, Integer>();  
	

	LineStrip lineStrip;

	public MyEdge(Integer edgeNo, Integer surfaceNo) {
		this.edgeNo = edgeNo;
		this.points = new ArrayList<Integer>();
		this.center = new Coord3d();
		this.surfaceNo = surfaceNo;
		this.connectedEdges = new ArrayList<MyEdge>();
	}

	public MyEdge() {
		this.edgeNo = -1;
		this.points = new ArrayList<Integer>();
		this.center = new Coord3d();
		this.surfaceNo = -1;
		this.connectedEdges = new ArrayList<MyEdge>();
	}

	public void setLineStrip(LineStrip ls) {
		this.lineStrip = ls;
	}

	public void addPoint(Integer pointNo) {
		// boolean alreadyAdded = false;
		// for (MyPickablePoint p1 : this.points) {
		// if (p1.xyz.distance(p.xyz) == 0) {
		// alreadyAdded = true;
		// break;
		// }
		// }
		// if (!alreadyAdded) {
		points.add(pointNo);
		calculateCenter();
		if (points.size() >= 2)
		{
			calculateLength();
			if(!hmLengthDistrib.containsKey(this.length))
					hmLengthDistrib.put(this.length, 0);

			hmLengthDistrib.put(this.length, hmLengthDistrib.get(this.length)+1);
		}
		//System.out.println("Edge: " + this.edgeNo + " points:" + points.size());
	}

	private void calculateLength() {
		// TODO Auto-generated method stub
		// @formatter:off
		length = (float) Math.sqrt(Math.pow(SurfaceDemo.instance.utils.points.get(this.points.get(0))
				.getX() - SurfaceDemo.instance.utils.points.get(this.points.get(1)).getX(), 2.0d)
				+ Math.pow(SurfaceDemo.instance.utils.points.get(this.points.get(0)).getY()
						- SurfaceDemo.instance.utils.points.get(this.points.get(1)).getY(), 2.0d)
				+ Math.pow(SurfaceDemo.instance.utils.points.get(this.points.get(0)).getZ()
						- SurfaceDemo.instance.utils.points.get(this.points.get(1)).getZ(), 2.0d));
		// @formatter:on
	}

	public void calculateCenter() {
		float sumx = 0;
		float sumy = 0;
		float sumz = 0;
		for (Integer pointNo : points) {
			MyPickablePoint p = SurfaceDemo.instance.utils.points.get(pointNo);
			sumx += p.getX();
			sumy += p.getY();
			sumz += p.getZ();
		}
		center.x = sumx / (points.size());
		center.y = sumy / (points.size());
		center.z = sumz / (points.size());
	}

	/**
	 * Calculate the angle at the vertex between two rays formed by three 3d
	 * points.
	 * 
	 * @param a
	 *          A 3d point.
	 * @param vertex
	 *          The vertex point.
	 * @param b
	 *          A 3d point.
	 * 
	 * @return The angle, from 0 to 2 * PI, in radians.
	 */

	public static MyEdge getEdge(int edgeNo) {
		MyEdge ret = null;
		for (MySurface surface : SurfaceDemo.instance.utils.surfaces.values()) {
			for (MyEdge edge : surface.getEdges()) {
				if (edge.edgeNo == edgeNo) {
					ret = edge;
					break;
				}
			}
			if (ret != null)
				break;
		}
		return ret;
	}

	public static double dotProd(double[] a, double[] b) {
		if (a.length != b.length) {
			throw new IllegalArgumentException("The dimensions have to be equal!");
		}
		double sum = 0;
		for (int i = 0; i < a.length; i++) {
			sum += a[i] * b[i];
		}
		return sum;
	}

	public MyPickablePoint getPointByIndex(int index) {
		Integer pointNo = this.points.get(index);
		return SurfaceDemo.instance.utils.points.get(pointNo);
	}
	
	public String toString()
	{
		return String.valueOf(this.edgeNo);
	}

}
