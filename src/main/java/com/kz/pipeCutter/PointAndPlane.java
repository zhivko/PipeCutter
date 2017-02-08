package com.kz.pipeCutter;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.jzy3d.plot3d.primitives.pickable.PickablePoint;

public class PointAndPlane {
	public MyPickablePoint point;
	public MyPickablePoint prevPoint;
	public MyPickablePoint nextPoint;
	public Plane plane;
	public boolean direction;
	
	
	public PointAndPlane()
	{
		this.point = new MyPickablePoint();
		this.plane = null;
		this.prevPoint = null;
		this.nextPoint = null;
		this.direction = false;
	}
	
	
}
