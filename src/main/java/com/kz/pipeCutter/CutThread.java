package com.kz.pipeCutter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;
import javax.vecmath.Point3d;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Rotation;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.util.FastMath;
import org.jfree.ui.StandardGradientPaintTransformer;
import org.jzy3d.chart.controllers.camera.AbstractCameraController;
import org.jzy3d.chart.controllers.mouse.camera.AWTCameraMouseController;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Sphere;

import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.tab.GcodeViewer;

public class CutThread extends SwingWorker<String, Object> {
	float plasmaLeadinRadius;
	static File gcodeFile;
	public static int delay = 100;
	public static float cutterYRange = 220;
	private static long longDelay = 1000;
	private MyPickablePoint point;
	double sumAngle = 0;
	double topZ;

	float pierceOffsetMm = 0;
	float cutOffsetMm = 0;
	long pierceTimeMs = 0;

	static CutThread instance;

	ArrayList<Integer> firstPoints;
	ArrayList<Integer> alAlreadyAddedPoints;

	private boolean wholePipe = false;
	private boolean onlySelected = false;
	private boolean useKerfOffset = false;
	private MyPickablePoint startPoint = null;

	public float filletSpeed = 0.0f;

	ArrayList<MyPickablePoint> cuttingPoints = new ArrayList<MyPickablePoint>();
	public boolean waitForArcOK;

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
		this.alAlreadyAddedPoints = new ArrayList<Integer>();

		this.onlySelected = onlySelected;
		this.waitForArcOK = Boolean.valueOf(Settings.getInstance().getSetting("myini.plasmaWaitForArcOk"));

		if (onlySelected) {
			for (MyEdge e : SurfaceDemo.getInstance().utils.edges.values()) {
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
			MyContinuousEdge myCEdge = SurfaceDemo.getInstance().utils.continuousEdges.get(point.continuousEdgeNo);
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
		Thread.currentThread().setName("CutThread");
		plasmaLeadinRadius = Float.valueOf(Settings.getInstance().getSetting("plasma_leadin_radius"));
		SurfaceDemo.getInstance().utils.rotatePoints(0, true, false);

		System.out.println("name: " + Thread.currentThread().getName());
		SurfaceDemo.getInstance().myTrail.clear();

		Settings.getInstance().log("Pipe is" + (SurfaceDemo.getInstance().pipeIsCircular == true ? " " : " NOT ") + "circular.");

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

		SurfaceDemo.getInstance().g1Speed = Float.valueOf(Settings.getInstance().getSetting("gcode_feedrate_g1"));
		SurfaceDemo.getInstance().g0Speed = Float.valueOf(Settings.getInstance().getSetting("gcode_feedrate_g0"));

		this.useKerfOffset = Settings.getInstance().getSetting("cut_kerf_offset").equals("True");
	}

	public void cut() throws InterruptedException {
		SurfaceDemo.getInstance().myTrail.clear();
		SurfaceDemo.getInstance().getChart().getScene().getGraph().remove(SurfaceDemo.getInstance().myTrail);

		SurfaceDemo.getInstance().gCodeLineNo = 0;
		SurfaceDemo.getInstance().g93mode = false;
		SurfaceDemo.getInstance().writeToGcodeFile("G90 (switch to absolute coordinates)");
		SurfaceDemo.getInstance().writeToGcodeFile("G94 (units per minute mode)");

		// out.println(String.format(Locale.US, "G00 Z%.3f F%s", diagonal / 2.0f +
		// 20.0f, Settings.getInstance().getSetting("gcode_feedrate_g0")));
		SurfaceDemo.getInstance().utils.rotatePoints(0, false, false);
		SurfaceDemo.getInstance().writeToGcodeFile(String.format(java.util.Locale.US, "%s", "G00 A0.0 B0.0"));

		ArrayList<MyPickablePoint> sortedList = new ArrayList<MyPickablePoint>(cuttingPoints);
		Collections.sort(sortedList, new MyPickablePointYZMidXComparator());

		// SurfaceDemo.getInstance().utils.establishNeighbourPoints();
		MyPickablePoint firstOuterPoint = sortedList.get(sortedList.size() - 1);

		if (wholePipe)
			firstPoints = SurfaceDemo.getInstance().utils.findAllConnectedPoints(firstOuterPoint, new ArrayList<Integer>());
		else
			firstPoints = new ArrayList<Integer>();

		double mminY = sortedList.get(sortedList.size() - 1).xyz.y;
		double mmaxY = sortedList.get(0).xyz.y;

		ArrayList<MyPickablePoint> sortedList2 = new ArrayList<MyPickablePoint>(cuttingPoints);
		Collections.sort(sortedList2, new MyPickablePointZYmidXcomparator());
		SurfaceDemo.getInstance().utils.previousPoint = sortedList2.get(0).xyz;
		SurfaceDemo.getInstance().utils.previousAngle = 0.0f;

		double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0f));
		SurfaceDemo.getInstance().writeToGcodeFile(String.format(Locale.US, "G00 X%.3f Y%.3f Z%.3f F%s", SurfaceDemo.getInstance().utils.previousPoint.x,
				SurfaceDemo.getInstance().utils.previousPoint.y, diagonal / 2.0f + 20.0f, Settings.getInstance().getSetting("gcode_feedrate_g0")));

		// double diagonal = (SurfaceDemo.getInstance().utils.maxEdge *
		// Math.sqrt(2.0f));
		// MyPickablePoint safeRetractPoint = new MyPickablePoint(-100000, new
		// Coord3d(0, SurfaceDemo.getInstance().utils.maxY, diagonal / 2 + 20),
		// Color.BLACK,
		// 0.4f, -200000);
		// SurfaceDemo.getInstance().utils.previousPoint = safeRetractPoint.xyz;
		// SurfaceDemo.getInstance().move(safeRetractPoint, false, cutOffsetMm,
		// true);

		// lets turn on path blending
		SurfaceDemo.getInstance().writeToGcodeFile("G64 P.5 Q.5 (path blending - P away from point)");
		SurfaceDemo.getInstance().writeToGcodeFile("G93 (inverse time mode)");

		float currentY = (float) mmaxY;
		alAlreadyAddedPoints = new ArrayList<Integer>();
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
			if (wholePipe || onlySelected)
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

		if (wholePipe)
			cutSegment((float) mminY, mmaxY, false, rotationDirection);
		if (onlySelected)
			cutSegment((float) mminY, mmaxY, false, rotationDirection);

		SurfaceDemo.getInstance().writeToGcodeFile("G94");
		SurfaceDemo.getInstance().writeToGcodeFile("M2");

		SurfaceDemo.getInstance().getChart().getScene().getGraph().add(SurfaceDemo.getInstance().myTrail);

	}

	private void stopRotating() {
		Iterator<AbstractCameraController> it = SurfaceDemo.getInstance().getChart().getControllers().iterator();
		while (it.hasNext()) {
			AbstractCameraController cont = it.next();
			if (cont instanceof AWTCameraMouseController) {
				cont.stopThreadController();
			}
		}
	}

	private void startRotating() {

		Iterator<AbstractCameraController> it = SurfaceDemo.getInstance().getChart().getControllers().iterator();
		while (it.hasNext()) {
			AbstractCameraController cont = it.next();
			if (cont instanceof AWTCameraMouseController) {
				cont.stopThreadController();
			}
		}
	}

	private void cutSegment(float minY, double maxY, boolean withoutLastPoints, int rotationDirection) {
		System.out.println("Cutting segment " + minY + " " + maxY);

		// get all continuous edges
		ArrayList<MyEdge> edgesToCut = new ArrayList<MyEdge>();
		if (wholePipe) {
			for (MyContinuousEdge e : SurfaceDemo.getInstance().utils.continuousEdges.values()) {
				System.out.println(e.center.y);
				if (e.center.y >= minY && e.center.y <= maxY)
					edgesToCut.add(e);
			}
		} else {
			for (MyEdge e : SurfaceDemo.getInstance().utils.edges.values()) {
				if (e.isToCut() && e.center.y >= minY && e.center.y <= maxY) {
					edgesToCut.add(e);
				}
			}
		}

		if (SurfaceDemo.getInstance().pipeIsCircular) {
			boolean hasBeenCutting = false;
			ArrayList<MyPickablePoint> pointsToCut = new ArrayList<MyPickablePoint>();
			for (MyEdge edge : edgesToCut) {
				pointsToCut = new ArrayList<MyPickablePoint>();

				for (Integer pointId : edge.points) {
					pointsToCut.add(SurfaceDemo.getInstance().utils.points.get(pointId));
				}
				ArrayList<MyPickablePoint> sortedPointsToCut = new ArrayList<MyPickablePoint>(pointsToCut);
				Collections.sort(sortedPointsToCut, new MyPickablePointMidXToEdgeCenterComparator(edge.center));
				// Collections.sort(pointsToCut, new
				// MyPickablePointMidXToEdgeCenterComparator(contEdge.center));
				if (pointsToCut.size() > 0) {
					for (MyPickablePoint myPoint : sortedPointsToCut) {
						if (!listContainsPoint(myPoint, alAlreadyAddedPoints)) {
							// lets move to safe distance
							double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0f));
							MyPickablePoint safeRetractPoint = new MyPickablePoint(-100000, new Point3d(0, myPoint.xyz.y, diagonal / 2 + 10), Color.BLACK, 0.4f,
									-200000);
							SurfaceDemo.getInstance().move(safeRetractPoint, false, false, cutOffsetMm, true, null);

							// lets rotate pipe so myPoint will be topz point
							double pointAngle = Math.atan2(myPoint.getZ(), myPoint.getX()) * 180.0d / Math.PI;

							double angleDelta = pointAngle - 90;
							myPoint.setWidth(8);
							myPoint.setColor(Color.BLUE);
							SurfaceDemo.getInstance().utils.rotatePoints(angleDelta, false);
							SurfaceDemo.getInstance().getChart().render();

							float currAngle = Float.valueOf(SurfaceDemo.getInstance().angleTxt);

							SurfaceDemo.getInstance().writeToGcodeFile(String.format(java.util.Locale.US, "G00 A%.3f B%.3f", currAngle, currAngle));

							double angle = followThePath(myPoint, this.alAlreadyAddedPoints);
							hasBeenCutting = true;
						}
					}
				}
			}

			if (hasBeenCutting) {
				double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0d));
				MyPickablePoint newPoint = new MyPickablePoint(-100000, new Point3d(SurfaceDemo.getInstance().getPlasma().getPosition().x,
						SurfaceDemo.getInstance().getPlasma().getPosition().y, diagonal / 2.0f + 20), Color.BLACK, 0.4f, -200000);
				SurfaceDemo.getInstance().move(newPoint, false, false, 0, true, null);
			}

			sumAngle = Float.valueOf(SurfaceDemo.getInstance().angleTxt); // sumAngle

			/*
			 * if (sumAngle >= 360.0) rotationDirection = -1; double angle =
			 * rotationDirection * 90.0d;
			 * SurfaceDemo.getInstance().utils.rotatePoints(angle, false);
			 */
		} else {
			int startI;
			int endI;
			int dI;
			double currAngle = Double.valueOf(SurfaceDemo.getInstance().angleTxt);
			if (currAngle > 0) {
				startI = 4;
				endI = 0;
				dI = -1;
			} else {
				startI = 0;
				endI = 4;
				dI = 1;
			}
			// for (int i = startI; i < endI; i = i + dI) {
			int i = startI;
			while (true) {

				double angle = i * 90.0d;
				currAngle = Double.valueOf(SurfaceDemo.getInstance().angleTxt);
				double deltaAng = angle - currAngle;

				SurfaceDemo.getInstance().utils.rotatePoints(deltaAng, false, true);

				calculateTopZ();
				System.out.println("TopZ=" + topZ);

				ArrayList<MyPickablePoint> pointsToCut = new ArrayList<MyPickablePoint>();

				for (MyPickablePoint p : cuttingPoints) {
					// if (p.id == 279) {
					// System.out.println("");
					// }
					if (p.xyz.y >= minY && p.xyz.y <= maxY && Math.abs(p.getZ() - topZ) < 0.001) {
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
				for (MyEdge contEdge : edgesToCut) {
					pointsToCut = new ArrayList<MyPickablePoint>();
					for (Integer pointId : contEdge.points) {
						pointsToCut.add(SurfaceDemo.getInstance().utils.points.get(pointId));
					}
					Collections.sort(pointsToCut, new MyPickablePointZYmidXcomparator());
					// Collections.sort(pointsToCut, new
					// MyPickablePointMidXToEdgeCenterComparator(contEdge.center));
					if (pointsToCut.size() > 0) {
						for (MyPickablePoint myPoint : pointsToCut) {
							if (!listContainsPoint(myPoint, alAlreadyAddedPoints) && Math.abs(myPoint.getZ() - topZ) < 0.1) {
								double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0f));
								MyPickablePoint safeRetractPoint = new MyPickablePoint(-100000, new Point3d(myPoint.xyz.x, myPoint.xyz.y, diagonal / 2 + 10),
										Color.BLACK, 0.4f, -200000);

								Point p = SurfaceDemo.getInstance().utils.calculateOffsetPoint(myPoint);
								Vector3D kerfOffVec = new Vector3D(myPoint.xyz.x - p.xyz.x, myPoint.xyz.y - p.xyz.y, myPoint.xyz.z - p.xyz.z);

								SurfaceDemo.getInstance().move(safeRetractPoint, false, false, cutOffsetMm, true, kerfOffVec);
								angle = followThePath(myPoint, this.alAlreadyAddedPoints);
								hasBeenCutting = true;
							}
						}
					}
				}

				if (hasBeenCutting) {
					double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0d));
					MyPickablePoint newPoint = new MyPickablePoint(-100000, new Point3d(SurfaceDemo.getInstance().getPlasma().getPosition().x,
							SurfaceDemo.getInstance().getPlasma().getPosition().y, diagonal / 2.0f + 20), Color.BLACK, 0.4f, -200000);
					SurfaceDemo.getInstance().move(newPoint, false, false, 0, true, null);
				}
				i = i + dI;
				if (i == endI)
					break;
			}
		}
	}

	public double followThePath(MyPickablePoint myPoint, ArrayList<Integer> alAlreadyAddedPoints) {

		MyPickablePoint tempPoint = myPoint;
		MyPickablePoint prevPoint = myPoint;
		prevPoint = tempPoint;
		boolean shouldBreak = false;
		ArrayList<MyEdge> alreadyCuttedEdges = new ArrayList<MyEdge>();

		MyContinuousEdge contEdge = SurfaceDemo.getInstance().utils.continuousEdges.get(tempPoint.continuousEdgeNo);

		PointAndPlane offPointAndPlane = SurfaceDemo.getInstance().utils.calculateOffsetPointAndPlane(myPoint);
		Vector3D delt = offPointAndPlane.plane.getNormal().normalize().scalarMultiply(5);

		Point endPoint = toPoint(toVector3D(tempPoint.point).add(delt));
		LineStrip ls = new LineStrip(myPoint.clone(), endPoint);
		ls.setWireframeColor(Color.CYAN);
		Sphere sph = new Sphere(endPoint.xyz.clone(), 1, 10, Color.CYAN);
		sph.setColor(Color.CYAN);
		sph.setWireframeColor(Color.CYAN);
		SurfaceDemo.getInstance().myTrail.add(ls);
		SurfaceDemo.getInstance().myTrail.add(sph);
		// create circular leadin only if this is closed edge (it means center is on
		// edge)

		Vector3D contEdgCent = new Vector3D(contEdge.center.x, contEdge.center.y, contEdge.center.z);
		Iterator<Integer> itPoints = contEdge.points.iterator();
		boolean isOnEdge = false;
		while (itPoints.hasNext()) {
			MyPickablePoint p1 = SurfaceDemo.getInstance().utils.points.get(itPoints.next());
			if (itPoints.hasNext()) {
				MyPickablePoint p2 = SurfaceDemo.getInstance().utils.points.get(itPoints.next());
				MyEdge edge = SurfaceDemo.getInstance().utils.getEdgeFromTwoPoints(p1, p2);
				if (edge != null) {
					Line l = new Line(new Vector3D(p1.getX(), p1.getY(), p1.getZ()), new Vector3D(p2.getX(), p2.getY(), p2.getZ()), 0.001f);
					if (l.contains(contEdgCent)) {
						isOnEdge = true;
						break;
					}
				}
			}
		}
		Vector3D kerfOffVec = new Vector3D(0, 0, 0);
		if (!isOnEdge) { // we don't do this for line just for edge forming closed
											// loops
			Point offsetPoint = offPointAndPlane.point;
			if (this.useKerfOffset)
				kerfOffVec = new Vector3D(myPoint.xyz.x - offsetPoint.xyz.x, myPoint.xyz.y - offsetPoint.xyz.y, myPoint.xyz.z - offsetPoint.xyz.z);

			Vector3D vecMyPoint = new Vector3D(myPoint.point.x, myPoint.point.y, myPoint.point.z);
			Vector3D vect3DoffPoint = new Vector3D(offsetPoint.getCoord().x, offsetPoint.getCoord().y, offsetPoint.getCoord().z);
			Vector3D delta = vect3DoffPoint.subtract(vecMyPoint).normalize().scalarMultiply(plasmaLeadinRadius);
			Vector3D vect3Dcent = vecMyPoint.add(delta);
			Vector3D axis = null;

			if (contEdge.edgeType == MyContinuousEdge.EdgeType.START)
				axis = offPointAndPlane.plane.getNormal().scalarMultiply(1.0d);
			else if (contEdge.edgeType == MyContinuousEdge.EdgeType.END) {
				axis = offPointAndPlane.plane.getNormal().scalarMultiply(1.0d);
			} else if (contEdge.edgeType == MyContinuousEdge.EdgeType.ONPIPE) {
				axis = offPointAndPlane.plane.getNormal().scalarMultiply(offPointAndPlane.direction == true ? 1 : -1);
			} else {
				System.out.println("");
			}

			double angleDelta = 0, startAngle = 0, endAngle = 0;

			if (contEdge.edgeType == MyContinuousEdge.EdgeType.START) {
				startAngle = Math.PI / 2;
				endAngle = 0;
			} else if (contEdge.edgeType == MyContinuousEdge.EdgeType.END) {
				startAngle = Math.PI / 2.0d;
				endAngle = 0;
			} else if (contEdge.edgeType == MyContinuousEdge.EdgeType.ONPIPE) {
				startAngle = Math.PI / 2;
				endAngle = 0;
			}
			angleDelta = (endAngle - startAngle) / 20.0d;

			for (double angle = startAngle; Math.abs(angle - endAngle) > 0.1; angle = angle + angleDelta) {
				Rotation rotat2 = new Rotation(axis, angle);
				Vector3D rotatedVec = rotat2.applyTo(delta);
				System.out.println(rotatedVec);
				Vector3D leadPoint = vect3Dcent.subtract(rotatedVec);
				Point3d c = new Point3d((float) leadPoint.getX(), (float) leadPoint.getY(), (float) leadPoint.getZ());
				if (angle == startAngle) {
					SurfaceDemo.getInstance().redrawPosition();
					// c.add(new Vector3d(0, 0, pierceOffsetMm));
					MyPickablePoint p = new MyPickablePoint(-1, c, Color.MAGENTA, .5f, -1);

					SurfaceDemo.getInstance().moveAbove(p, pierceOffsetMm, pierceTimeMs, kerfOffVec);
					SurfaceDemo.getInstance().move(p, false, true, cutOffsetMm, kerfOffVec);
				} else {
					MyPickablePoint p = new MyPickablePoint(-1, c, Color.MAGENTA, .5f, -1);
					SurfaceDemo.getInstance().move(p, true, true, cutOffsetMm, kerfOffVec);
				}
			}
		} else {
			SurfaceDemo.getInstance().moveAbove(tempPoint, pierceOffsetMm, pierceTimeMs, kerfOffVec);
			SurfaceDemo.getInstance().move(tempPoint, true, true, cutOffsetMm, kerfOffVec);
		}

		while (!shouldBreak) {
			kerfOffVec = new Vector3D(0, 0, 0);
			if (tempPoint != null) {
				tempPoint.setColor(Color.GREEN);
				alAlreadyAddedPoints.add(Integer.valueOf(tempPoint.id));
			}
			if (contEdge.edgeType == MyContinuousEdge.EdgeType.START && wholePipe)
				if (SurfaceDemo.getInstance().pipeIsCircular)
					tempPoint = SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint, alAlreadyAddedPoints, true);
				else
					tempPoint = SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint, alAlreadyAddedPoints, true);
			else if (contEdge.edgeType == MyContinuousEdge.EdgeType.END && wholePipe)
				tempPoint = SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint, alAlreadyAddedPoints, false);
			else {
				if (contEdge.points.size() == contEdge.connectedEdges.size()) {
					// it is closed edge
					// tempPoint =
					// SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint,
					// alAlreadyAddedPoints, true);
					if (wholePipe)
						tempPoint = SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint, alAlreadyAddedPoints, offPointAndPlane.direction);
					else {
						boolean foundPoint = false;
						for (MyEdge myEdge : SurfaceDemo.getInstance().utils.edges.values()) {
							if (myEdge.isToCut() && !alreadyCuttedEdges.contains(myEdge))
								if (myEdge.points.get(0) == tempPoint.id) {
									foundPoint = true;
									tempPoint = SurfaceDemo.getInstance().utils.points.get(myEdge.points.get(1));
									break;
								} else if (myEdge.points.get(1) == tempPoint.id) {
									foundPoint = true;
									tempPoint = SurfaceDemo.getInstance().utils.points.get(myEdge.points.get(0));
									break;
								}
						}
						if (!foundPoint)
							tempPoint = null;
					}
				} else {
					if (wholePipe) {
						if (contEdge.points.indexOf(myPoint.id) == 0)
							tempPoint = SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint, alAlreadyAddedPoints, true);
						else
							tempPoint = SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint, alAlreadyAddedPoints, false);
					} else {
						for (MyEdge myEdge : SurfaceDemo.getInstance().utils.edges.values()) {
							if (myEdge.isToCut() && !alreadyCuttedEdges.contains(myEdge))
								if (myEdge.points.get(0) == tempPoint.id)
									tempPoint = SurfaceDemo.getInstance().utils.points.get(myEdge.points.get(1));
								else if (myEdge.points.get(1) == tempPoint.id)
									tempPoint = SurfaceDemo.getInstance().utils.points.get(myEdge.points.get(0));
						}
					}
				}
			}

			if (tempPoint == null) {
				shouldBreak = true;
				tempPoint = myPoint;
				MyEdge edge = SurfaceDemo.getInstance().utils.getEdgeFromTwoPoints(prevPoint, tempPoint);
				if (edge != null && !alreadyCuttedEdges.contains(edge)) {
					if (contEdge.points.size() == contEdge.connectedEdges.size() && this.useKerfOffset) {
						offPointAndPlane = SurfaceDemo.getInstance().utils.calculateOffsetPointAndPlane(myPoint);
						kerfOffVec = new Vector3D(myPoint.xyz.x - offPointAndPlane.point.xyz.x, myPoint.xyz.y - offPointAndPlane.point.xyz.y,
								myPoint.xyz.z - offPointAndPlane.point.xyz.z);
					}
					SurfaceDemo.getInstance().move(tempPoint, true, true, cutOffsetMm, kerfOffVec);
					if (contEdge.edgeNo == 1) {
						System.out.println("");
					}
				}
			} else {
				MyEdge edge = SurfaceDemo.getInstance().utils.getEdgeFromTwoPoints(prevPoint, tempPoint);
				if (edge != null && !alreadyCuttedEdges.contains(edge)) {
					if (contEdge.points.size() == contEdge.connectedEdges.size() && this.useKerfOffset) {
						offPointAndPlane = SurfaceDemo.getInstance().utils.calculateOffsetPointAndPlane(tempPoint);
						kerfOffVec = new Vector3D(tempPoint.xyz.x - offPointAndPlane.point.xyz.x, tempPoint.xyz.y - offPointAndPlane.point.xyz.y,
								tempPoint.xyz.z - offPointAndPlane.point.xyz.z);
					}
					SurfaceDemo.getInstance().move(tempPoint, true, true, cutOffsetMm, kerfOffVec);
					alreadyCuttedEdges.add(edge);
				}
			}
			double angleDelta = rotation(prevPoint, tempPoint);
			prevPoint = tempPoint;
		}
		tempPoint = myPoint;
		prevPoint = tempPoint;
		if (wholePipe) {
			tempPoint = SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint, alAlreadyAddedPoints, false);
			if (tempPoint != null) {
				double angle = rotation(prevPoint, tempPoint);
				return angle;
			}
		}
		return 0.0d;

	}

	private Point toPoint(Vector3D origin) {
		// TODO Auto-generated method stub
		return new Point(new Coord3d(origin.getX(), origin.getY(), origin.getZ()));
	}

	private Vector3D toVector3D(Point3d origin) {
		// TODO Auto-generated method stub
		return new Vector3D(origin.x, origin.y, origin.z);
	}

	private double rotation(MyPickablePoint prevPoint, MyPickablePoint tempPoint) {
		double angleDeltaDeg = 0;
		if (tempPoint != null && !tempPoint.equals(prevPoint)) {

			Vector2D vecA = new Vector2D(prevPoint.getX() - tempPoint.getX(), prevPoint.getZ() - tempPoint.getZ());
			Vector2D vecB = new Vector2D(1, 0);
			// double angleDelta = Vector2D.angle(vecB, vecA);
			// lets check origpoints if this are same points only y coordinates
			// differs.

			MyPickablePoint prevPoint_ = SurfaceDemo.getInstance().utils.origPoints.get(prevPoint.id);
			MyPickablePoint tempPoint_ = SurfaceDemo.getInstance().utils.origPoints.get(tempPoint.id);

			if (Math.abs(prevPoint_.getX() - tempPoint_.getX()) < 0.00000001 && Math.abs(prevPoint_.getZ() - tempPoint_.getZ()) < 0.00000001) {
				System.out.println("Same point");
				return 0.0d;
			}

			double angleDelta = FastMath.atan2(tempPoint.getZ() - prevPoint.getZ(), prevPoint.getX() - tempPoint.getX());

			double angleDelta2 = Math.atan2(tempPoint.getZ() - prevPoint.getZ(), prevPoint.getX() - tempPoint.getX());

			if (Math.abs(angleDelta - angleDelta2) > 0.001) {
				System.out.println(angleDelta * 180 / Math.PI + " " + angleDelta2 * 180 / Math.PI);
			}

			if (Math.abs(angleDelta - Math.PI) < Utils.Math_E)
				angleDelta = 0;
			if (Math.abs(angleDelta + Math.PI) < Utils.Math_E)
				angleDelta = 0;
			// if (Math.abs(angleDelta) < Utils.Math_E)
			// angleDelta = 0;

			angleDeltaDeg = Math.toDegrees(angleDelta);

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
			}
		}
		return angleDeltaDeg;
	}

	public boolean listContainsPoint(MyPickablePoint p, List<Integer> list) {
		for (Integer pId : list) {
			if (p.id == pId)
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
		SurfaceDemo.getInstance().gCodeLineNo = 0;
		SurfaceDemo.getInstance().g93mode = false;

		SurfaceDemo.getInstance().utils.previousPoint = this.startPoint.xyz;
		SurfaceDemo.getInstance().utils.previousAngle = Float.valueOf(SurfaceDemo.getInstance().angleTxt);

		SurfaceDemo.getInstance().writeToGcodeFile("G90 (switch to absolute coordinates)");
		SurfaceDemo.getInstance().writeToGcodeFile("G94 (units per minute mode)");

		double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0f));
		// lets turn on path blending
		SurfaceDemo.getInstance().writeToGcodeFile("G64 P.5 Q.5 (path blending - P away from point)");

		float offsetZ = (float) ((diagonal / 2.0f + 20.0f) - this.startPoint.getZ() + pierceOffsetMm);
		// SurfaceDemo.getInstance().move(this.startPoint, false, offsetZ);
		// SurfaceDemo.getInstance().moveAbove(this.startPoint, pierceOffsetMm,
		// pierceTimeMs);
		SurfaceDemo.getInstance().writeToGcodeFile("G93 (inverse time mode)");
		this.followThePath(this.startPoint, this.alAlreadyAddedPoints);
		SurfaceDemo.getInstance().move(this.startPoint, false, false, offsetZ, null);

		SurfaceDemo.getInstance().writeToGcodeFile("M2");
	}

}