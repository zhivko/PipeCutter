package com.kz.pipeCutter;

import java.util.ArrayList;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.pickable.PickablePoint;

public class MyPickablePoint extends PickablePoint {

	int id;
	ArrayList<MyPickablePoint> neighbourPoints;
	public int inventorEdge;
	public Integer continuousEdgeNo;

	public MyPickablePoint(int id, Coord3d xyz, Color rgb, float width, int inventorEdge) {
		super(xyz, rgb, width);
		this.id = id;
		if (id == -1)
			System.out.println();
		this.setPickingId(id);
		this.inventorEdge = inventorEdge;
		this.continuousEdgeNo = -1;
	}

	public int getId() {
		return id;
	}

	public float getX() {
		return this.xyz.x;
	}

	public float getY() {
		return this.xyz.y;
	}

	public float getZ() {
		return this.xyz.z;
	}

	public String toString() {
		return " Id:" + this.getId() + " inventorEdgeNo:" + this.inventorEdge + " continuousEdgeNo:"
				+ this.continuousEdgeNo + " x:" + this.getX() + " y:" + this.getY() + " z:" + this.getZ();
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

	public double distance(MyPickablePoint p) {
		return this.xyz.distance(p.xyz);
	}

	public boolean laysOnLeftSurface() {
		// TODO Auto-generated method stub

		String key = SurfaceDemo.instance.utils.getMinYMaxYKeyFor(this);
		Utils.MinYMaxY minmax = SurfaceDemo.instance.utils.minAndMaxY.get(key);

		double distance = this.xyz.distance(new Coord3d(this.xyz.x, minmax.minY, this.xyz.z));

		if (distance == 0)
			return true;

		return false;
	}

	public boolean laysOnRightSurface() {
		// TODO Auto-generated method stub

		String key = SurfaceDemo.instance.utils.getMinYMaxYKeyFor(this);
		Utils.MinYMaxY minmax = SurfaceDemo.instance.utils.minAndMaxY.get(key);

		if (this.xyz.y == minmax.maxY)
			return true;

		return false;
	}

	public boolean equals(MyPickablePoint p2) {
		if (p2.getId() == this.id)
			return true;
		return false;
	}

	@Override
	public MyPickablePoint clone() {
		Point p1 = super.clone();
		MyPickablePoint p = new MyPickablePoint(this.id, p1.xyz, p1.rgb.clone(), this.width,
				this.inventorEdge);
		return p;

	}
}
