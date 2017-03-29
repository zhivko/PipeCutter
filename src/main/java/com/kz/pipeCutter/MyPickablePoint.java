package com.kz.pipeCutter;

import java.util.ArrayList;

import javax.vecmath.Point3d;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.pickable.PickablePoint;

public class MyPickablePoint extends PickablePoint {

	int id;
	public Point3d point;
	ArrayList<MyPickablePoint> neighbourPoints;
	public int inventorEdge;
	public Integer continuousEdgeNo;

	FirstOrLast firstOrLast = FirstOrLast.MIDDLE;

	public enum FirstOrLast {
		FIRST, LAST, MIDDLE
	}

	public MyPickablePoint() {
		super(new Coord3d((float) 0, (float) 0, (float) 0));
	}

	public MyPickablePoint(int id, Point3d xyz, Color rgb, float width, int inventorEdge) {
		super(new Coord3d((float) xyz.x, (float) xyz.y, (float) xyz.z), rgb, width);
		this.id = id;
		if (id == -1)
			System.out.println();
		this.setPickingId(id);
		this.inventorEdge = inventorEdge;
		this.continuousEdgeNo = -1;
		point = new Point3d(xyz.x, xyz.y, xyz.z);
	}

	public int getId() {
		return id;
	}

	public double getX() {
		return point.x;
	}

	public double getY() {
		return point.y;
	}

	public double getZ() {
		return point.z;
	}

	public String toString() {
		return " Id:" + this.getId() + " inventorEdgeNo:" + this.inventorEdge + " x:" + this.getX() + " y:" + this.getY() + " z:" + this.getZ()
				+ " ContEdge " + SurfaceDemo.instance.utils.continuousEdges.get(this.continuousEdgeNo).toString();
	}

	public boolean isOnSurface(MySurface surf) {
		for (MyEdge e : surf.edges) {
			for (Integer pointNo : e.points) {
				MyPickablePoint p = SurfaceDemo.instance.utils.points.get(pointNo);
				if (this.xyz.distance(p.xyz) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public double distance(Point3d p) {
		return this.point.distance(p);
	}

	public double distance(MyPickablePoint p) {
		return this.point.distance(p.point);
	}

	public boolean laysOnLeftSurface() {
		return firstOrLast.equals(FirstOrLast.LAST);
	}

	public boolean laysOnRightSurface() {
		// TODO Auto-generated method stub
		return firstOrLast.equals(FirstOrLast.FIRST);
	}

	public boolean equals(MyPickablePoint p2) {
		if (p2.getId() == this.id)
			return true;
		return false;
	}

	public FirstOrLast getFirstOrLast() {
		return firstOrLast;
	}

	public void setFirstOrLast(FirstOrLast firstOrLast) {
		this.firstOrLast = firstOrLast;
	}

	@Override
	public MyPickablePoint clone() {
		Point p1 = super.clone();
		MyPickablePoint p = new MyPickablePoint(this.id, this.point, p1.rgb.clone(), this.width, this.inventorEdge);
		return p;

	}

	public void setCoord(double x, double y, double z) {
		// TODO Auto-generated method stub
		point.x = x;
		point.y = y;
		point.z = z;
		this.getCoord().x = (float) x;
		this.getCoord().y = (float) y;
		this.getCoord().z = (float) z;
	}
}
