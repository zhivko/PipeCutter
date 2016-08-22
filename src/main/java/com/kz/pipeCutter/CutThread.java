package com.kz.pipeCutter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.swing.SwingWorker;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;

import com.kz.pipeCutter.ui.Settings;

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
	private MyPickablePoint startPoint = null;

	float g0Speed = 0;
	float g1Speed = 0;
	public boolean g93mode;
	public float filletSpeed = 0.0f;

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

	public CutThread(boolean wholePipe) {
		this();
		calculateTopZ();
		this.wholePipe = wholePipe;
		this.startPoint = point;
		this.alAlreadyAddedPoints = new ArrayList<MyPickablePoint>();
	}

	private void calculateTopZ() {
		ArrayList<MyPickablePoint> sortedList = new ArrayList(SurfaceDemo.getInstance().utils.points.values());
		Collections.sort(sortedList, new MyPickablePointZYXComparator());
		topZ = sortedList.get(0).getZ();
	}

	public CutThread(boolean wholePipe, MyPickablePoint point) {
		this(wholePipe);
		this.startPoint = point;
	}

	public CutThread() {
		System.out.println("name: " + Thread.currentThread().getName());

		Thread.currentThread().setName("CutThread");
		System.out.println("New name: " + Thread.currentThread().getName());

		instance = this;

		String folder = Settings.getInstance().getSetting("gcode_folder");
		File f = new File(folder + File.separatorChar + "prog.gcode");
		Settings.getInstance().log(String.format("Delete file %s %b", f.getAbsolutePath(), f.delete()));

		pierceOffsetMm = Float.valueOf(Settings.getInstance().getSetting("plasma_pierce_offset_mm"));
		double pierceTimeMsFloat = (Float.valueOf(Settings.getInstance().getSetting("plasma_pierce_time_s")) * 1000.0f);
		pierceTimeMs = Long.valueOf((long) pierceTimeMsFloat);
		cutOffsetMm = Float.valueOf(Settings.getInstance().getSetting("plasma_cut_offset_mm"));
		String gcodeFolder = Settings.getInstance().getSetting("gcode_folder");
		gcodeFile = new File(gcodeFolder + File.separatorChar + "prog.gcode");

		g1Speed = Float.valueOf(Settings.getInstance().getSetting("gcode_feedrate_g1"));
		g0Speed = Float.valueOf(Settings.getInstance().getSetting("gcode_feedrate_g0"));
	}

	public void cut() throws InterruptedException {
		int prevInventorEdge = 0;
		ArrayList<MyPickablePoint> sortedList = new ArrayList(SurfaceDemo.getInstance().utils.points.values());
		Collections.sort(sortedList, new MyPickablePointYComparator());

		// SurfaceDemo.getInstance().utils.establishNeighbourPoints();
		MyPickablePoint lastOuterPoint = sortedList.get(sortedList.size() - 1);
		MyPickablePoint firstOuterPoint = sortedList.get(0);
		firstPoints = SurfaceDemo.getInstance().utils.findAllConnectedPoints(firstOuterPoint, new ArrayList<MyPickablePoint>());

		double mminY = sortedList.get(0).xyz.y;
		double mmaxY = sortedList.get(sortedList.size() - 1).xyz.y;

		SurfaceDemo.getInstance().angleTxt = "0.0";
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(this.gcodeFile.getAbsolutePath(), true)));
			double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * 1.41);
			out.println("G94");
			out.println(String.format(Locale.US, "G00 Z%.3f F%s", diagonal / 2.0f + 20.0f, Settings.getInstance().getSetting("gcode_feedrate_g1")));
			out.println(String.format(Locale.US, "G00 X%.3f Y%.3f Z%.3f A0 B0 F%s", 0.0f, SurfaceDemo.getInstance().utils.maxY,
					SurfaceDemo.getInstance().utils.maxZ, Settings.getInstance().getSetting("gcode_feedrate_g1")));
			if (Settings.instance.getSetting("gcode_g93").equals("1")) {
				this.g93mode = true;
				out.println("G93");
			} else
				this.g93mode = false;
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(this.gcodeFile.getAbsolutePath(), true)));
			out.println("M2");
			out.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void cutSegment(float minY, double maxY, boolean withoutLastPoints, int rotationDirection) {
		System.out.println("Cutting segment " + minY + " " + maxY);
		for (int i = 0; i < 4; i++) {
			calculateTopZ();
			System.out.println("TopZ=" + topZ);

			ArrayList<MyPickablePoint> pointsToCut = new ArrayList<MyPickablePoint>();
			for (MyPickablePoint p : SurfaceDemo.getInstance().utils.points.values()) {
				if (p.xyz.y > minY && p.xyz.y <= maxY && Math.abs(p.getZ() - topZ) < 0.1) {
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

						// try {
						// PrintWriter out = new PrintWriter(
						// new BufferedWriter(new
						// FileWriter(CutThread.gcodeFile.getAbsolutePath(), true)));
						// out.println(String.format(java.util.Locale.US, "G01 A%.3f B%.3f
						// F%s",
						// Float.valueOf(SurfaceDemo.getInstance().angleTxt),
						// Float.valueOf(SurfaceDemo.getInstance().angleTxt),
						// Settings.getInstance().getSetting("gcode_feedrate_g0")));
						// out.close();
						// } catch (Exception e) {
						// e.printStackTrace();
						// }
						double diagonal = (SurfaceDemo.getInstance().utils.maxEdge * Math.sqrt(2.0f));
						MyPickablePoint safeRetractPoint = new MyPickablePoint(-100000, new Coord3d(myPoint.xyz.x, myPoint.xyz.y, diagonal / 2 + 20), Color.BLACK,
								0.4f, -200000);
						SurfaceDemo.getInstance().move(safeRetractPoint, false, cutOffsetMm, true);

						SurfaceDemo.getInstance().moveAbove(myPoint, pierceOffsetMm, pierceTimeMs);
						double angle = folowThePath(myPoint, this.alAlreadyAddedPoints, (rotationDirection == -1 ? true : false));
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

	public double folowThePath(MyPickablePoint myPoint, ArrayList<MyPickablePoint> alAlreadyAddedPoints, Boolean order) {

		MyPickablePoint tempPoint = myPoint;
		MyPickablePoint prevPoint = myPoint;
		prevPoint = tempPoint;
		boolean shouldBreak = false;
		while (!shouldBreak) {
			SurfaceDemo.getInstance().move(tempPoint, true, cutOffsetMm);

			if (tempPoint != null) {
				tempPoint.setColor(Color.GREEN);
				alAlreadyAddedPoints.add(tempPoint);
			}
			tempPoint = SurfaceDemo.getInstance().utils.findConnectedPoint(tempPoint, alAlreadyAddedPoints, true);
			if (tempPoint == null) {
				shouldBreak = true;
				tempPoint = myPoint;
			}

			double angleDelta = rotation(prevPoint, tempPoint);
			// System.out.println(prevPoint.id + " " + tempPoint.id + " " +
			// angleDelta);
			prevPoint = tempPoint;
		}
		SurfaceDemo.getInstance().move(tempPoint, true, cutOffsetMm);
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
		if (this.wholePipe)
			cut();
		else
			try {
				this.folowThePath(this.startPoint, this.alAlreadyAddedPoints, true);
				PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(CutThread.gcodeFile.getAbsolutePath(), true)));
				out.println("M2");
				out.flush();
				out.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		return "Done";
	}

	@Override
	protected void done() {
		try {
			System.out.println("Done.");
		} catch (Exception ignore) {
		}
	}

}