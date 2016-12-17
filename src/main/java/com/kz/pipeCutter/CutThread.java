package com.kz.pipeCutter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3DFormat;
import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.Point;

import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.GcodeViewer;

public class CutThread extends SwingWorker<String, Object> {

	static File gcodeFile;
	public static int delay = 100;
	public static float cutterYRange = 50;
	private static long longDelay = 1000;
	private MyPickablePoint point;
	double sumAngle = 0;
	float topZ;

	float pierceOffsetMm = 0;
	float cutOffsetMm = 0;
	long pierceTimeMs = 0;

	static CutThread instance;

	ArrayList<MyPickablePoint> firstPoints;
	ArrayList<MyPickablePoint> alAlreadyAddedPoints;

	private boolean wholePipe = false;
	private boolean onlySelected = false;
	private MyPickablePoint startPoint = null;

	public float filletSpeed = 0.0f;

	ArrayList<MyPickablePoint> cuttingPoints = new ArrayList<MyPickablePoint>();

	// http://www.pirate4x4.com/forum/11214232-post17.html
	// For cutting 19.05mm with your Powermax1650 (this info is in your Powermax
	// 1650 operators manual) Use 100 Amp consumables, set Amps to 100, cut
	// speed
	// to 660 mm/min (you can go up to 1168.4 mm/min,
	// 660 mm/min will provide better edge quality), set pierce delay to 1.5
	// seconds, pierce height must be at 5mm to 6.35 mm, set cut height to 3.175
	// mm
	// (adjust arc voltage during the cut to maintain 3.175mm torch to work
	// distance...voltage should be roughly 161 volts depending on calibration
	// of
	// your torch height control).
	// On 19mm pay attention to pierce height....one pierce too close, or with
	// too
	// short of a pierce delay- you will destroy the shield and nozzle at this
	// power level.
	// Use the FineCut consumables on everything under 4.76 mm for best
	// quality...as above, pay attention to pierce height and cut height and you
	// will be very satisfied with the results.
	// Best regards, Jim Colt

	private void calculateTopZ() {
		ArrayList<MyPickablePoint> sortedList = new ArrayList(SurfaceDemo.getInstance().utils.points.values());
		Collections.sort(sortedList, new MyPickablePointZYXComparator());
		topZ = sortedList.get(0).getZ();
	}

	public CutThread(boolean wholePipe, MyPickablePoint point, boolean onlySelected) {
		this();

		this.wholePipe = wholePipe;
		this.startPoint = point;
		this.alAlreadyAddedPoints = new ArrayList<MyPickablePoint>();

		this.onlySelected = onlySelected;

		if (onlySelected) {
			for (MyEdge e : SurfaceDemo.instance.utils.edges.values()) {
				if (e.isToCut()) {
					if (!this.cuttingPoints.contains(e.getPointByIndex(0)))
						this.cuttingPoints.add(e.getPointByIndex(0));
					if (!this.cuttingPoints.contains(e.getPointByIndex(1)))
						this.cuttingPoints.add(e.getPointByIndex(1));
				}
			}
		}

		if (wholePipe) {
			for (MyPickablePoint p : SurfaceDemo.getInstance().utils.points.values()) {
				this.cuttingPoints.add(p);
			}
		}

		if (!wholePipe && !onlySelected) {
			MyContinuousEdge myCEdge = SurfaceDemo.instance.utils.continuousEdges.get(point.continuousEdgeNo);
			for (MyEdge e : myCEdge.connectedEdges) {
				if (!this.cuttingPoints.contains(e.getPointByIndex(0)))
					this.cuttingPoints.add(e.getPointByIndex(0));
				if (!this.cuttingPoints.contains(e.getPointByIndex(1)))
					this.cuttingPoints.add(e.getPointByIndex(1));
			}
		}

		calculateTopZ();

		this.startPoint = point;
	}

	public CutThread() {
		System.out.println("name: " + Thread.currentThread().getName());

		Thread.currentThread().setName("CutThread");
		System.out.println("New name: " + Thread.currentThread().getName());

		instance = this;

		String folder = Settings.getInstance().getSetting("gcode_folder");
		File f = new File(folder + File.separatorChar + "prog.gcode");
		if (GcodeViewer.instance.refreshThread != null)
			GcodeViewer.instance.refreshThread.interrupt();

		Settings.getInstance().log(String.format("Delete file %s %b", f.getAbsolutePath(), f.delete()));

		pierceOffsetMm = Float.valueOf(Settings.getInstance().getSetting("plasma_pierce_offset_mm"));
		double pierceTimeMsFloat = (Float.valueOf(Settings.getInstance().getSetting("plasma_pierce_time_s")) * 1000.0f);
		pierceTimeMs = Long.valueOf((long) pierceTimeMsFloat);
		cutOffsetMm = Float.valueOf(Settings.getInstance().getSetting("plasma_cut_offset_mm"));
		String gcodeFolder = Settings.getInstance().getSetting("gcode_folder");
		gcodeFile = new File(gcodeFolder + File.separatorChar + "prog.gcode");

		SurfaceDemo.instance.g1Speed = Float.valueOf(Settings.getInstance().getSetting("gcode_feedrate_g1"));
		SurfaceDemo.instance.g0Speed = Float.valueOf(Settings.getInstance().getSetting("gcode_feedrate_g0"));
	}

	public void cut() throws InterruptedException {
		SurfaceDemo.instance.gCodeLineNo = 0;
		SurfaceDemo.instance.g93mode = false;
		ArrayList<MyPickablePoint> sortedList = new ArrayList<MyPickablePoint>(cuttingPoints);
		Collections.sort(sortedList, new MyPickablePointYComparator());

		// SurfaceDemo.getInstance().utils.establishNeighbourPoints();
		MyPickablePoint firstOuterPoint = sortedList.get(0);

		if (wholePipe)
			firstPoints = SurfaceDemo.getInstance().utils.findAllConnectedPoints(firstOuterPoint, new ArrayList<MyPickablePoint>());
		else
			firstPoints = new ArrayList<MyPickablePoint>();

		double mminY = sortedList.get(0).xyz.y;
		double mmaxY = sortedList.get(sortedList.size() - 1).xyz.y;

		ArrayList<MyPickablePoint> sortedList2 = new ArrayList<MyPickablePoint>(cuttingPoints);
		Collections.sort(sortedList2, new MyPickablePointZMidXYComparator());
		SurfaceDemo.instance.utils.previousPoint = sortedList2.get(0).xyz;
		SurfaceDemo.instance.utils.previousAngle = 0.0f;

		SurfaceDemo.getInstance().angleTxt = "0.0";
		SurfaceDemo.instance.writeToGcodeFile("G90 (switch to absolute coordinates)");
		SurfaceDemo.instance.writeToGcodeFile("G94 (units per minute mode)");

		// out.println(String.format(Locale.US, "G00 Z%.3f F%s", diagonal / 2.0f +
		// 20.0f, Settings.getInstance().getSetting("gcode_feedrate_g0")));

		double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0f));
		SurfaceDemo.instance.writeToGcodeFile(String.format(Locale.US, "G00 X%.3f Y%.3f Z%.3f F%s", SurfaceDemo.instance.utils.previousPoint.x,
				SurfaceDemo.instance.utils.previousPoint.y, diagonal / 2.0f + 20.0f, Settings.getInstance().getSetting("gcode_feedrate_g0")));

		// double diagonal = (SurfaceDemo.getInstance().utils.maxEdge *
		// Math.sqrt(2.0f));
		// MyPickablePoint safeRetractPoint = new MyPickablePoint(-100000, new
		// Coord3d(0, SurfaceDemo.getInstance().utils.maxY, diagonal / 2 + 20),
		// Color.BLACK,
		// 0.4f, -200000);
		// SurfaceDemo.instance.utils.previousPoint = safeRetractPoint.xyz;
		// SurfaceDemo.getInstance().move(safeRetractPoint, false, cutOffsetMm,
		// true);

		// lets turn on path blending
		SurfaceDemo.instance.writeToGcodeFile("G64 P.05 Q.05 (path blending - P away from point)");
		SurfaceDemo.instance.writeToGcodeFile("G93 (inverse time mode)");

		float currentY = (float) mmaxY;
		alAlreadyAddedPoints = new ArrayList<MyPickablePoint>();
		float minY = 0;
		float maxY = 0;
		int rotationDirection = 1;
		while (currentY - cutterYRange / 2 > mminY) {
			minY = currentY - cutterYRange / 2;
			maxY = currentY + cutterYRange / 2;

			System.out.println(minY + " - " + maxY);
			// sumAngle = 0;
			// SurfaceDemo.getInstance().utils.rotatePoints(sumAngle,true);
			// SurfaceDemo.getInstance().angleTxt = "0.0";
			cutSegment(minY, maxY, true, rotationDirection);
			double angle = Double.valueOf(SurfaceDemo.getInstance().angleTxt).doubleValue();
			if (angle > 0)
				rotationDirection = -1;
			else
				rotationDirection = 1;
			currentY = currentY - cutterYRange;
		}
		sumAngle = Float.valueOf(SurfaceDemo.getInstance().angleTxt); // sumAngle
		if (sumAngle >= 360.0)
			rotationDirection = -1;

		cutSegment(minY, maxY, false, rotationDirection);
		SurfaceDemo.instance.writeToGcodeFile("G94");
		SurfaceDemo.instance.writeToGcodeFile("M2");
	}

	private void cutSegment(float minY, double maxY, boolean withoutLastPoints, int rotationDirection) {
		System.out.println("Cutting segment " + minY + " " + maxY);
		for (int i = 0; i < 4; i++) {
			calculateTopZ();
			System.out.println("TopZ=" + topZ);

			ArrayList<MyPickablePoint> pointsToCut = new ArrayList<MyPickablePoint>();

			for (MyPickablePoint p : cuttingPoints) {
				if (p.id == 280) {
					System.out.println("");
				}
				if (p.xyz.y > minY && p.xyz.y <= maxY && Math.abs(p.getZ() - topZ) < Utils.Math_E) {
					{
						if (withoutLastPoints) {
							if (!listContainsPoint(p, firstPoints)) {
								if (!listContainsPoint(p, alAlreadyAddedPoints)) {
									pointsToCut.add(p);
									p.setColor(Color.MAGENTA);
								}
							}
						} else {
							if (!listContainsPoint(p, alAlreadyAddedPoints)) {
								pointsToCut.add(p);
								p.setColor(Color.MAGENTA);
							}
						}
					}
				}
			}

			boolean hasBeenCutting = false;
			Collections.sort(pointsToCut, new MyPickablePointMidXComparator());
			if (pointsToCut.size() > 0) {
				for (MyPickablePoint myPoint : pointsToCut) {
					if (!listContainsPoint(myPoint, alAlreadyAddedPoints) && Math.abs(myPoint.getZ() - topZ) < 0.1) {
						double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0f));
						MyPickablePoint safeRetractPoint = new MyPickablePoint(-100000, new Coord3d(myPoint.xyz.x, myPoint.xyz.y, diagonal / 2 + 20), Color.BLACK,
								0.4f, -200000);
						SurfaceDemo.getInstance().move(safeRetractPoint, false, cutOffsetMm, true);
						// SurfaceDemo.getInstance().moveAbove(safeRetractPoint, 0, 0);
						// unenececssary pause
						// SurfaceDemo.getInstance().writeToGcodeFile(String.format(Locale.US,
						// "G04 P%.3f", 3.0));
						double angle = followThePath(myPoint, this.alAlreadyAddedPoints, (rotationDirection == -1 ? true : false));
						hasBeenCutting = true;
					}
				}
			}
			if (hasBeenCutting) {
				double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0d));
				MyPickablePoint newPoint = new MyPickablePoint(-100000,
						new Coord3d(SurfaceDemo.getInstance().cylinderPoint.xyz.x, SurfaceDemo.getInstance().cylinderPoint.xyz.y, diagonal / 2.0f + 20),
						Color.BLACK, 0.4f, -200000);
				SurfaceDemo.getInstance().move(newPoint, false, cutOffsetMm, true);
			}

			sumAngle = Float.valueOf(SurfaceDemo.getInstance().angleTxt); // sumAngle
			if (sumAngle >= 360.0)
				rotationDirection = -1;
			double angle = rotationDirection * 90.0d;
			SurfaceDemo.getInstance().utils.rotatePoints(angle, true);

		}
	}

	public double followThePath(MyPickablePoint myPoint, ArrayList<MyPickablePoint> alAlreadyAddedPoints, Boolean order) {

		MyPickablePoint tempPoint = myPoint;
		MyPickablePoint prevPoint = myPoint;
		prevPoint = tempPoint;
		boolean shouldBreak = false;
		ArrayList<MyEdge> alreadyCuttedEdges = new ArrayList<MyEdge>();

		PointAndPlane offPointAndPlane = SurfaceDemo.instance.utils.calculateOffsetPointAndPlane(myPoint);
		// create circular leadin only if this is closed edge (it means center is on
		// edge)
		MyContinuousEdge contEdge = SurfaceDemo.instance.utils.continuousEdges.get(tempPoint.continuousEdgeNo);
		Vector3D contEdgCent = new Vector3D(contEdge.center.x, contEdge.center.y, contEdge.center.z);
		Iterator<Integer> itPoints = contEdge.points.iterator();
		boolean isOnEdge = false;
		while (itPoints.hasNext()) {
			MyPickablePoint p1 = SurfaceDemo.instance.utils.points.get(itPoints.next());
			if (itPoints.hasNext()) {
				MyPickablePoint p2 = SurfaceDemo.instance.utils.points.get(itPoints.next());
				MyEdge edge = SurfaceDemo.instance.utils.getEdgeFromTwoPoints(p1, p2);
				if (edge != null) {
					Line l = new Line(new Vector3D(p1.getX(), p1.getY(), p1.getZ()), new Vector3D(p2.getX(), p2.getY(), p2.getZ()), 0.001f);
					if (l.contains(contEdgCent)) {
						isOnEdge = true;
						break;
					}
				}
			}
		}
		if (!isOnEdge) {
			float radius = 3.0f;
			Point offsetPoint = offPointAndPlane.point;
			Vector3D vect3DoffPoint = new Vector3D(offsetPoint.getCoord().x, offsetPoint.getCoord().y, offsetPoint.getCoord().z);
			Vector3D vect3DmyPoint = new Vector3D(myPoint.getCoord().x, myPoint.getCoord().y, myPoint.getCoord().z);
			Vector3D delta = vect3DoffPoint.subtract(vect3DmyPoint).normalize().scalarMultiply(radius);
			Vector3D vect3Dcent = vect3DmyPoint.add(delta);

			Vector3D vect3Dcent1 = vect3Dcent.add(new Vector3D(0, 0, 1));
			Vector3D axis = vect3Dcent1.subtract(vect3Dcent);
			for (double angle = 3 * Math.PI / 2; angle > Math.PI; angle = angle - Math.PI / 20.0d) {
				Rotation rotat = new Rotation(axis, angle);
				Vector3D leadPoint = vect3Dcent.add(rotat.applyTo(delta));
				Coord3d c = new Coord3d((float) leadPoint.getX(), (float) leadPoint.getY(), (float) leadPoint.getZ());
				MyPickablePoint p = new MyPickablePoint(-1, c, Color.MAGENTA, .5f, -1);
				if (angle == 0.0f)
					SurfaceDemo.getInstance().moveAbove(p, pierceOffsetMm, pierceTimeMs);
				else
					SurfaceDemo.getInstance().move(p, true, cutOffsetMm);
			}
		} else {
			SurfaceDemo.getInstance().moveAbove(myPoint, pierceOffsetMm, pierceTimeMs);
		}

		SurfaceDemo.getInstance().move(tempPoint, true, cutOffsetMm);
		while (!shouldBreak) {
			if (tempPoint != null) {
				tempPoint.setColor(Color.GREEN);
				alAlreadyAddedPoints.add(tempPoint);
			}
			tempPoint = SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint, alAlreadyAddedPoints, true);

			if (tempPoint == null) {
				shouldBreak = true;
				tempPoint = myPoint;
				MyEdge edge = SurfaceDemo.instance.utils.getEdgeFromTwoPoints(prevPoint, tempPoint);
				if (!alreadyCuttedEdges.contains(edge))
					SurfaceDemo.getInstance().move(tempPoint, true, cutOffsetMm);
			} else {
				SurfaceDemo.getInstance().move(tempPoint, true, cutOffsetMm);
				MyEdge edge = SurfaceDemo.instance.utils.getEdgeFromTwoPoints(prevPoint, tempPoint);
				if (!alreadyCuttedEdges.contains(edge)) {
					alreadyCuttedEdges.add(edge);
				}
			}
			// if (tempPoint == null) {
			// shouldBreak = true;
			// // check if edge to first point exist
			// for (MyEdge e : SurfaceDemo.instance.utils.edges.values()) {
			// if ((e.getPointByIndex(0).id == prevPoint.id && e.getPointByIndex(1).id
			// == myPoint.id)
			// || (e.getPointByIndex(1).id == prevPoint.id && e.getPointByIndex(0).id
			// == myPoint.id)) {
			// tempPoint = myPoint;
			// break;
			// }
			// }
			// }

			double angleDelta = rotation(prevPoint, tempPoint);
			// System.out.println(prevPoint.id + " " + tempPoint.id + " " +
			// angleDelta);
			prevPoint = tempPoint;
		}
		// if (tempPoint != null)
		// SurfaceDemo.getInstance().move(tempPoint, true, cutOffsetMm);
		tempPoint = myPoint;
		prevPoint = tempPoint;
		tempPoint = SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint, alAlreadyAddedPoints, true);
		if (tempPoint != null) {
			double angle = rotation(prevPoint, tempPoint);
			return angle;
		}
		return 0.0d;

	}

	private double rotation(MyPickablePoint prevPoint, MyPickablePoint tempPoint) {

		double angleDeltaDeg = 0;
		if (tempPoint != null && !tempPoint.equals(prevPoint)) {
			double angleDelta = Math.atan2(tempPoint.xyz.z - prevPoint.xyz.z, prevPoint.xyz.x - tempPoint.xyz.x);
			if (Math.abs(angleDelta - Math.PI) < Utils.Math_E)
				angleDelta = 0;
			if (Math.abs(angleDelta + Math.PI) < Utils.Math_E)
				angleDelta = 0;

			angleDeltaDeg = (angleDelta) * 180 / Math.PI;

			if (Math.abs(angleDeltaDeg) < Utils.rotationAngleMin)
				angleDeltaDeg = 0;
			if (Math.abs(180 - angleDeltaDeg) < Utils.rotationAngleMin)
				angleDeltaDeg = 0;

			if (angleDeltaDeg > -180 && angleDeltaDeg < -90)
				angleDeltaDeg = -(180 + angleDeltaDeg);
			else if (angleDeltaDeg > -90 && angleDeltaDeg < 0) {
				angleDeltaDeg = -angleDeltaDeg;
			}

			if (angleDeltaDeg != 0) {
				SurfaceDemo.getInstance().utils.rotatePoints(angleDeltaDeg, false);
				sumAngle = sumAngle + angleDeltaDeg;
				// System.out.print("\tSumAngle=" + sumAngle + " (delta=" +
				// angleDeltaDeg + ")");
				// System.out.println("");

			}

			// try {
			// Thread.sleep(100);
			// } catch (InterruptedException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}
		return angleDeltaDeg;
	}

	public boolean listContainsPoint(MyPickablePoint p, List<MyPickablePoint> list) {
		for (MyPickablePoint myPickablePoint : list) {
			if (myPickablePoint.id == p.id)
				return true;

		}
		return false;
	}

	@Override
	protected String doInBackground() throws Exception {

		System.out.println("File " + gcodeFile.getAbsolutePath() + " deleted?" + gcodeFile.delete());
		if (this.wholePipe || this.onlySelected)
			cut();
		else {
			cutFromPoint(this.startPoint);
		}
		return "Done";
	}

	protected void done() {
		try {
			System.out.println("Done");
			get();
		} catch (ExecutionException e) {
			e.getCause().printStackTrace();
		} catch (InterruptedException e) {
			// Process e here
		}
	}

	private void cutFromPoint(MyPickablePoint startPoint2) {
		SurfaceDemo.instance.gCodeLineNo = 0;
		SurfaceDemo.instance.g93mode = false;

		SurfaceDemo.instance.utils.previousPoint = this.startPoint.xyz;
		SurfaceDemo.instance.utils.previousAngle = Float.valueOf(SurfaceDemo.getInstance().angleTxt);

		SurfaceDemo.instance.writeToGcodeFile("G90 (switch to absolute coordinates)");
		SurfaceDemo.instance.writeToGcodeFile("G94 (units per minute mode)");

		double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0f));
		// lets turn on path blending
		SurfaceDemo.instance.writeToGcodeFile("G64 P.05 Q.05 (path blending - P away from point)");

		float offsetZ = (float) ((diagonal / 2.0f + 20.0f) - this.startPoint.getZ() + pierceOffsetMm);
		SurfaceDemo.getInstance().move(this.startPoint, false, offsetZ);
		SurfaceDemo.getInstance().moveAbove(this.startPoint, pierceOffsetMm, pierceTimeMs);
		SurfaceDemo.instance.writeToGcodeFile("G93 (inverse time mode)");
		this.followThePath(this.startPoint, this.alAlreadyAddedPoints, true);
		SurfaceDemo.getInstance().move(this.startPoint, false, offsetZ);

		SurfaceDemo.instance.writeToGcodeFile("M2");
	}

}