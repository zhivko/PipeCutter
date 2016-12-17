package com.kz.pipeCutter;

import org.apache.commons.math3.geometry.euclidean.threed.Plane;
import org.jzy3d.plot3d.primitives.pickable.PickablePoint;

public class PointAndPlane {
	public org.jzy3d.plot3d.primitives.Point point;
	public org.jzy3d.plot3d.primitives.Point prevPoint;
	public org.jzy3d.plot3d.primitives.Point nextPoint;
	public Plane plane;
	
	
	public PointAndPlane()
	{
		this.point = new PickablePoint();
		this.plane = null;
		this.prevPoint = null;
		this.nextPoint = null;
	}
	
	
}
