package com.kz.pipeCutter;

import java.util.Comparator;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Vector3d;

public class MyEdgeComparator implements Comparator<MyEdge> {

	private Coord3d startPoint;
	private Coord3d centerPoint;
	private Coord3d normalPoint0;
	private Coord3d normalPoint1;

	public MyEdgeComparator(Coord3d centerPoint, Coord3d startPoint, Coord3d normalPoint0, Coord3d normalPoint1) {
		this.startPoint = startPoint;
		this.centerPoint = centerPoint;
		this.normalPoint0 = normalPoint0;
		this.normalPoint1 = normalPoint1;
	}

	@Override
	public int compare(MyEdge edge1, MyEdge edge2) {
		// TODO Auto-generated method stub
		double angle1 = calculateAngle(edge1.center, this.centerPoint, this.startPoint, normalPoint0, normalPoint1);
		double angle2 = calculateAngle(edge2.center, this.centerPoint, this.startPoint, normalPoint0, normalPoint1);

		if (angle1 < angle2)
			return -1;
		else if (angle1 == angle2)
			return 0;
		else
			return 1;

	}


	
	public static double calculateAngle(Coord3d a, Coord3d vertex, Coord3d b, Coord3d point1, Coord3d point2) {
		Coord3d Va = a.sub(vertex);
		Coord3d Vb = b.sub(vertex);
		Coord3d Va_n = Va.normalizeTo(1);
		Coord3d Vb_n = Vb.normalizeTo(1);
		//
		Vector3d Va_vec = new Vector3d(vertex, a);
		Vector3d Vb_vec = new Vector3d(vertex, b);
		//
		double angle = Math.acos(Va_n.dot(Vb_n));
		Coord3d cross = Va_vec.cross(Vb_vec);
		Coord3d normal = getNormal(point1, vertex, point2);
		if (normal.dot(cross) > 0) {
			angle = 2 * Math.PI - angle;
		}

		return angle;
	}	
	
	// returns the normal vector of the plane defined by the three points
	public static Coord3d getNormal(Coord3d b, Coord3d a, Coord3d c) {
		Vector3d BminusA_v = new Vector3d(a, b);
		Vector3d CminusA_v = new Vector3d(a, c);
		return BminusA_v.cross(CminusA_v).normalizeTo(1);
	}	
	
}

