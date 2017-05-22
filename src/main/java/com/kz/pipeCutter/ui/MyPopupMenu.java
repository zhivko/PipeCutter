package com.kz.pipeCutter.ui;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.swing.SwingUtilities;

import org.jzy3d.colors.Color;

import com.kz.pipeCutter.CutThread;
import com.kz.pipeCutter.MyContinuousEdge;
import com.kz.pipeCutter.MyEdge;
import com.kz.pipeCutter.MyPickablePoint;
import com.kz.pipeCutter.MyPickablePointZYmidXcomparator;
import com.kz.pipeCutter.SurfaceDemo;
import com.kz.pipeCutter.BBB.BBBStatus;
import com.kz.pipeCutter.BBB.commands.ExecuteMdi;

public class MyPopupMenu extends PopupMenu {

	public MyPopupMenu() {

		MenuItem MenuItem14 = new MenuItem("Rotate around X for 90[deg]");
		/*
		 * MenuItem14.addActionListener(new ActionListener() {
		 * 
		 * @Override public void actionPerformed(ActionEvent arg0) {
		 * 
		 * double[] axisDouble = { 1.0d, 0.0d, 0.0d }; Vector3D axis = new
		 * Vector3D(axisDouble); Rotation rot = new Rotation(axis,
		 * Math.toRadians(90.0d)); // Rotation rotZ1 = new Rotation(zAxis,
		 * Math.toRadians(angleDeg)); for (MyPickablePoint point :
		 * SurfaceDemo.instance.utils.origPoints.values()) { double[] myPointDouble
		 * = { point.getX(), point.getY(), point.getZ() }; Vector3D myPoint = new
		 * Vector3D(myPointDouble); Vector3D result = rot.applyTo(myPoint);
		 * SurfaceDemo.instance.utils.origPoints.get(point.id).setCoord(result.getX(
		 * ), result.getY(), result.getZ());
		 * SurfaceDemo.instance.utils.points.get(point.id).setCoord(result.getX(),
		 * result.getY(), result.getZ()); }
		 * 
		 * 
		 * SurfaceDemo.instance.utils.calculateMaxAndMins();
		 * 
		 * SurfaceDemo.instance.utils.calculateCenters();
		 * SurfaceDemo.instance.utils.markRadiusEdges();
		 * SurfaceDemo.instance.splitLongEdges();
		 * SurfaceDemo.instance.splitNearRadiusEdge();
		 * SurfaceDemo.instance.utils.establishNeighbourPoints();
		 * 
		 * 
		 * SurfaceDemo.instance.utils.calculateContinuousEdges();
		 * 
		 * SurfaceDemo.instance.utils.origPoints = new ConcurrentHashMap<Integer,
		 * MyPickablePoint>(); for (MyPickablePoint mp :
		 * SurfaceDemo.instance.utils.points.values()) {
		 * SurfaceDemo.instance.utils.origPoints.put(new Integer(mp.id),
		 * mp.clone()); }
		 * 
		 * SurfaceDemo.instance.initDraw();
		 * 
		 * } });
		 */
		MenuItem14.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.instance.rotateArroundX = SurfaceDemo.instance.rotateArroundX + 90;
				SurfaceDemo.instance.init();
			}
		});

		this.add(MenuItem14);
		this.addSeparator();

		MenuItem MenuItem13 = new MenuItem("Select top right point (startPoint)");
		MenuItem13.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<MyPickablePoint> sortedList = new ArrayList(SurfaceDemo.getInstance().utils.origPoints.values());
				Collections.sort(sortedList, new MyPickablePointZYmidXcomparator());
				MyPickablePoint p = SurfaceDemo.getInstance().utils.points.get(sortedList.get(0).getId());

				SurfaceDemo.instance.lastClickedPointChanged(p);

			}
		});
		this.add(MenuItem13);
		this.addSeparator();

		MenuItem MenuItem = new MenuItem("Cut whole pipe");
		MenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CutThread th = new CutThread(true, SurfaceDemo.instance.lastClickedPoint, false);
				th.execute();
			}
		});
		this.add(MenuItem);

		MenuItem menuItem6 = new MenuItem("Cut edge from point");
		menuItem6.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (SurfaceDemo.instance.lastClickedPoint.getClass().getName().equals("com.kz.pipeCutter.MyPickablePoint")) {
					CutThread th = new CutThread(false, SurfaceDemo.instance.lastClickedPoint, false);
					th.execute();
				}
			}
		});
		this.add(menuItem6);

		MenuItem menuItem16 = new MenuItem("Cut selected edges from point");
		menuItem16.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (SurfaceDemo.instance.lastClickedPoint.getClass().getName().equals("com.kz.pipeCutter.MyPickablePoint")) {
					CutThread th = new CutThread(false, SurfaceDemo.instance.lastClickedPoint, true);
					th.execute();
				}
			}
		});
		this.add(menuItem16);

		this.addSeparator();

		MenuItem menuItem17 = new MenuItem("Add continuous edge to cut selection");
		menuItem17.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				MyContinuousEdge myCEdge = SurfaceDemo.instance.utils.continuousEdges.get(SurfaceDemo.instance.lastClickedPoint.continuousEdgeNo);
				System.out.println(myCEdge.connectedEdges.size());
				for (MyEdge e : myCEdge.connectedEdges) {
					e.markToCut(true);
					if (e.txt != null)
						e.txt.setColor(Color.RED);
				}
			}
		});
		this.add(menuItem17);

		MenuItem menuItem18 = new MenuItem("Remove continuous edge from cut selection");
		menuItem18.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				MyContinuousEdge myCEdge = SurfaceDemo.instance.utils.continuousEdges.get(SurfaceDemo.instance.lastClickedPoint.continuousEdgeNo);
				System.out.println(myCEdge.connectedEdges.size());
				for (MyEdge e : myCEdge.connectedEdges) {
					e.markToCut(false);
					if (e.txt != null)
						e.txt.setColor(Color.BLUE);
				}
			}
		});
		this.add(menuItem18);

		this.addSeparator();

		MenuItem menuItem7 = new MenuItem("Toggle edges");
		menuItem7.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.NUMBER_EDGES = !SurfaceDemo.NUMBER_EDGES;
				Settings.instance.setSetting("ui_number_edges", String.valueOf(SurfaceDemo.NUMBER_EDGES));
				// SurfaceDemo.instance.canvas.getView().setBoundMode(ViewBoundMode.AUTO_FIT);
				SurfaceDemo.instance.initDraw();
			}
		});
		this.add(menuItem7);

		MenuItem menuItem8 = new MenuItem("Toggle points");
		menuItem8.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.NUMBER_POINTS = !SurfaceDemo.NUMBER_POINTS;
				Settings.instance.setSetting("ui_number_points", String.valueOf(SurfaceDemo.NUMBER_POINTS));
				// SurfaceDemo.instance.canvas.getView().setBoundMode(ViewBoundMode.AUTO_FIT);
				SurfaceDemo.instance.initDraw();
			}
		});
		this.add(menuItem8);

		MenuItem menuItem9 = new MenuItem("Zoom point");
		menuItem9.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.ZOOM_PLASMA = false;
				SurfaceDemo.ZOOM_POINT = true;
				Settings.instance.setSetting("ui_zoom_plasma", "False");
				Settings.instance.setSetting("ui_zoom_point", "True");
				SurfaceDemo.instance.redrawPosition();
			}
		});
		this.add(menuItem9);

		MenuItem menuItem13 = new MenuItem("Zoom plasma");
		menuItem13.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.ZOOM_POINT = false;
				SurfaceDemo.ZOOM_PLASMA = true;
				Settings.instance.setSetting("ui_zoom_plasma", "True");
				Settings.instance.setSetting("ui_zoom_point", "False");

				SurfaceDemo.instance.redrawPosition();
			}
		});
		this.add(menuItem13);

		MenuItem menuItem10 = new MenuItem("Cancel zoom");
		menuItem10.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.ZOOM_POINT = false;
				SurfaceDemo.ZOOM_PLASMA = false;
				SurfaceDemo.instance.redrawPosition();
			}
		});
		this.add(menuItem10);

		this.addSeparator();

		MenuItem menuItem2 = new MenuItem("SMOOTHIE - Run last program");
		menuItem2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.instance.smoothie.send("play /sd/prog.gcode -q");
			}
		});
		this.add(menuItem2);

		MenuItem menuItem3 = new MenuItem("SMOOTHIE - Move to selected point");
		menuItem3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				MyPickablePoint mp = SurfaceDemo.instance.lastClickedPoint;
				String gCode = SurfaceDemo.instance.utils.coordinateToGcode(mp, null);
				SurfaceDemo.instance.smoothie.send(gCode);

			}
		});
		this.add(menuItem3);

		MenuItem menuItem4 = new MenuItem("SMOOTHIE - move to HOME position");
		menuItem4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.instance.smoothie.send("G90");
				SurfaceDemo.instance.smoothie.send("G28 X0 Y0 Z0");
			}
		});
		this.addSeparator();
		this.add(menuItem4);

		MenuItem menuItem11 = new MenuItem("Find BBB");
		menuItem11.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {
						SurfaceDemo.instance.discoverer.discover();
					}
				});
			}
		});
		this.addSeparator();
		this.add(menuItem11);

		this.addSeparator();

		// Gcodes for BBB
		MenuItem menuItem15 = new MenuItem("RESET COORDINATES");
		menuItem15.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						// @formatter:off
						/*
						 * Move to the Machine origin with G53 G0 X0 Y0 Z0 Clear any G92
						 * offset with G92.1 Use the G54 coordinate system with G54 Set the
						 * G54 coordinate system to be the same as the machine coordinate
						 * system with G10 L2 P1 X0 Y0 Z0 Turn off tool offsets with G49
						 * Turn on the Relative Coordinate Display from the menu
						 */
						// @formatter:on

						String mdiCommand = "G53 G0 X0 Y0 Z0\nG92.1\nG54\nG10 L2 P1 X0 Y0 Z0\nG49\nG90\nG21";
						Settings.getInstance().log(mdiCommand);
						new ExecuteMdi(mdiCommand).start();

						new java.util.Timer().schedule(new java.util.TimerTask() {
							@Override
							public void run() {
								BBBStatus.getInstance().reSubscribeMotion();
							}
						}, 5000);

					}
				});
			}
		});
		this.add(menuItem15);

		// Gcodes for BBB
		MenuItem menuItem12 = new MenuItem("SET POINT LOCATION AS ORIGIN - G92 (A=0deg and B=0deg)");
		menuItem12.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {

						// float x =
						// Float.valueOf(Settings.getInstance().getSetting("position_x"));
						// float y =
						// Float.valueOf(Settings.getInstance().getSetting("position_y"));
						// float z =
						// Float.valueOf(Settings.getInstance().getSetting("position_z"));
						// "G92 X%.3f Y%.3f Z%.3f\nG92.3"
						String mdiCommand = String.format(Locale.US, "G92 X%.3f Y%.3f Z%.3f A0 B0", SurfaceDemo.instance.lastClickedPoint.xyz.x,
								SurfaceDemo.instance.lastClickedPoint.xyz.y, SurfaceDemo.instance.lastClickedPoint.xyz.z);
						Settings.getInstance().log(mdiCommand);
						new ExecuteMdi(mdiCommand).start();

						// new java.util.Timer().schedule(new java.util.TimerTask() {
						// @Override
						// public void run() {
						// String mdiCommand = "G92.1";
						// Settings.getInstance().log(mdiCommand);
						// new ExecuteMdi(mdiCommand).start();
						// }
						// }, 2500);

						// new java.util.Timer().schedule(new java.util.TimerTask() {
						// @Override
						// public void run() {
						// BBBStatus.getInstance().reSubscribeMotion();
						// }
						// }, 2000);

					}
				});
			}
		});
		this.add(menuItem12);

		// Gcodes for BBB
		MenuItem menuItem14 = new MenuItem("Move to point - G00");
		menuItem14.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {

						float x = Float.valueOf(Settings.getInstance().getSetting("position_x"));
						float y = Float.valueOf(Settings.getInstance().getSetting("position_y"));
						float z = Float.valueOf(Settings.getInstance().getSetting("position_z"));

						String mdiCommand = String.format(Locale.US, "G90");
						// relative G91
						// absolute G90
						Settings.getInstance().log(mdiCommand);
						new ExecuteMdi(mdiCommand).start();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						String speed = Settings.getInstance().getSetting("gcode_feedrate_g0");

						mdiCommand = String.format(Locale.US, "G01 X%.3f Y%.3f Z%.3f F%s", SurfaceDemo.instance.lastClickedPoint.xyz.x,
								SurfaceDemo.instance.lastClickedPoint.xyz.y, SurfaceDemo.instance.lastClickedPoint.xyz.z, speed);
						Settings.getInstance().log(mdiCommand);
						new ExecuteMdi(mdiCommand).start();

						new java.util.Timer().schedule(new java.util.TimerTask() {
							@Override
							public void run() {
								BBBStatus.getInstance().reSubscribeMotion();
							}
						}, 1000);

					}
				});
			}
		});
		this.add(menuItem14);

	}
}
