package com.kz.pipeCutter.ui;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;

import javax.swing.JOptionPane;
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

		// MenuItem MenuItem14 = new MenuItem("Rotate around X for 90[deg]");
		// MenuItem14.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent arg0) {
		// SurfaceDemo.getInstance().rotateArroundX += 45;
		// SurfaceDemo.getInstance().init();
		// }
		// });
		// this.add(MenuItem14);
		//
		// MenuItem MenuItem15 = new MenuItem("Rotate around Y for 90[deg]");
		// MenuItem15.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent arg0) {
		// SurfaceDemo.getInstance().rotateArroundY += 45;
		// SurfaceDemo.getInstance().init();
		// }
		// });
		// this.add(MenuItem15);
		//
		// MenuItem MenuItem16 = new MenuItem("Rotate around Z for 90[deg]");
		// MenuItem16.addActionListener(new ActionListener() {
		// @Override
		// public void actionPerformed(ActionEvent arg0) {
		// SurfaceDemo.getInstance().rotateArroundZ += 45;
		// SurfaceDemo.getInstance().init();
		// }
		// });
		// this.add(MenuItem16);

		this.addSeparator();

		MenuItem MenuItem13 = new MenuItem("Select top right point (startPoint)");
		MenuItem13.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<MyPickablePoint> sortedList = new ArrayList(SurfaceDemo.getInstance().utils.origPoints.values());
				Collections.sort(sortedList, new MyPickablePointZYmidXcomparator());
				MyPickablePoint p = SurfaceDemo.getInstance().utils.points.get(sortedList.get(0).getId());

				SurfaceDemo.getInstance().lastClickedPointChanged(p);

			}
		});
		this.add(MenuItem13);
		this.addSeparator();

		MenuItem MenuItem = new MenuItem("Cut whole pipe");
		MenuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CutThread th = new CutThread(true, SurfaceDemo.getInstance().lastClickedPoint, false);
				th.execute();
			}
		});
		this.add(MenuItem);

		MenuItem menuItem6 = new MenuItem("Cut edge from point");
		menuItem6.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (SurfaceDemo.getInstance().lastClickedPoint.getClass().getName().equals("com.kz.pipeCutter.MyPickablePoint")) {
					CutThread th = new CutThread(false, SurfaceDemo.getInstance().lastClickedPoint, false);
					th.execute();
				}
			}
		});
		this.add(menuItem6);

		MenuItem menuItem16 = new MenuItem("Cut selected edges from point");
		menuItem16.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (SurfaceDemo.getInstance().lastClickedPoint.getClass().getName().equals("com.kz.pipeCutter.MyPickablePoint")) {
					CutThread th = new CutThread(false, SurfaceDemo.getInstance().lastClickedPoint, true);
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
				MyContinuousEdge myCEdge = SurfaceDemo.getInstance().utils.continuousEdges.get(SurfaceDemo.getInstance().lastClickedPoint.continuousEdgeNo);
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
				MyContinuousEdge myCEdge = SurfaceDemo.getInstance().utils.continuousEdges.get(SurfaceDemo.getInstance().lastClickedPoint.continuousEdgeNo);
				System.out.println(myCEdge.connectedEdges.size());
				for (MyEdge e : myCEdge.connectedEdges) {
					e.markToCut(false);
					if (e.txt != null)
						e.txt.setColor(Color.BLUE);
				}
			}
		});
		this.add(menuItem18);

		MenuItem menuItem20 = new MenuItem("Add specific edges to cut selection");
		menuItem20.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String edgesStr = JOptionPane.showInputDialog("Enter start edgeno and end edgeno in form [startEdgeNo] - [endEdgeNo]");
				int start = Integer.valueOf(edgesStr.split("-")[0]);
				int end = Integer.valueOf(edgesStr.split("-")[1]);
				for (int i = start; i <= end; i++) {
					MyEdge edge = SurfaceDemo.getInstance().utils.edges.get(i);
					edge.markToCut(true);
				}
			}
		});
		this.add(menuItem20);

		this.addSeparator();

		MenuItem menuItem7 = new MenuItem("Toggle edges");
		menuItem7.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.NUMBER_EDGES = !SurfaceDemo.NUMBER_EDGES;
				Settings.getInstance().setSetting("ui_number_edges", String.valueOf(SurfaceDemo.NUMBER_EDGES));
				// SurfaceDemo.getInstance().canvas.getView().setBoundMode(ViewBoundMode.AUTO_FIT);
				SurfaceDemo.getInstance().initDraw();
			}
		});
		this.add(menuItem7);

		MenuItem menuItem8 = new MenuItem("Toggle points");
		menuItem8.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.NUMBER_POINTS = !SurfaceDemo.NUMBER_POINTS;
				Settings.getInstance().setSetting("ui_number_points", String.valueOf(SurfaceDemo.NUMBER_POINTS));
				// SurfaceDemo.getInstance().canvas.getView().setBoundMode(ViewBoundMode.AUTO_FIT);
				SurfaceDemo.getInstance().initDraw();
			}
		});
		this.add(menuItem8);

		MenuItem menuItem9 = new MenuItem("Zoom point");
		menuItem9.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.ZOOM_PLASMA = false;
				SurfaceDemo.ZOOM_POINT = true;
				Settings.getInstance().setSetting("ui_zoom_plasma", "False");
				Settings.getInstance().setSetting("ui_zoom_point", "True");
				SurfaceDemo.getInstance().redrawPosition();
			}
		});
		this.add(menuItem9);

		MenuItem menuItem13 = new MenuItem("Zoom plasma");
		menuItem13.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.ZOOM_POINT = false;
				SurfaceDemo.ZOOM_PLASMA = true;
				Settings.getInstance().setSetting("ui_zoom_plasma", "True");
				Settings.getInstance().setSetting("ui_zoom_point", "False");

				SurfaceDemo.getInstance().redrawPosition();
			}
		});
		this.add(menuItem13);

		MenuItem menuItem10 = new MenuItem("Cancel zoom");
		menuItem10.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.ZOOM_POINT = false;
				SurfaceDemo.ZOOM_PLASMA = false;
				SurfaceDemo.getInstance().redrawPosition();
			}
		});
		this.add(menuItem10);

		this.addSeparator();

		MenuItem menuItem2 = new MenuItem("SMOOTHIE - Run last program");
		menuItem2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.getInstance().smoothie.send("play /sd/prog.gcode -q");
			}
		});
		this.add(menuItem2);

		MenuItem menuItem3 = new MenuItem("SMOOTHIE - Move to selected point");
		menuItem3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {

				MyPickablePoint mp = SurfaceDemo.getInstance().lastClickedPoint;
				String gCode = SurfaceDemo.getInstance().utils.coordinateToGcode(mp, null);
				SurfaceDemo.getInstance().smoothie.send(gCode);

			}
		});
		this.add(menuItem3);

		MenuItem menuItem4 = new MenuItem("SMOOTHIE - move to HOME position");
		menuItem4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.getInstance().smoothie.send("G90");
				SurfaceDemo.getInstance().smoothie.send("G28 X0 Y0 Z0");
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
						SurfaceDemo.getInstance().discoverer.discover();
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
		MenuItem menuItem12 = new MenuItem("SET POINT LOCATION AS ORIGIN - G92 (A=CurrAngle and B=CurrAngle)");
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

						float angle = Float.valueOf(SurfaceDemo.getInstance().angleTxt).floatValue();

						String mdiCommand = String.format(Locale.US, "G92 X%.3f Y%.3f Z%.3f A%.3f B%.3f", SurfaceDemo.getInstance().lastClickedPoint.xyz.x,
								SurfaceDemo.getInstance().lastClickedPoint.xyz.y, SurfaceDemo.getInstance().lastClickedPoint.xyz.z, angle, angle);
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

						mdiCommand = String.format(Locale.US, "G01 X%.3f Y%.3f Z%.3f F%s", SurfaceDemo.getInstance().lastClickedPoint.xyz.x,
								SurfaceDemo.getInstance().lastClickedPoint.xyz.y, SurfaceDemo.getInstance().lastClickedPoint.xyz.z, speed);
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
