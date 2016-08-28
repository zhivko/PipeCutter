package com.kz.pipeCutter;

import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.plot3d.rendering.view.modes.ViewBoundMode;

import com.kz.pipeCutter.BBB.BBBStatus;
import com.kz.pipeCutter.BBB.commands.ExecuteMdi;
import com.kz.pipeCutter.ui.Settings;

public class MyPopupMenu extends PopupMenu {

	public MyPopupMenu() {
		MenuItem menuItem = new MenuItem("Cut whole pipe");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				CutThread th = new CutThread(true);
				th.execute();
			}
		});
		this.add(menuItem);
		this.addSeparator();

		MenuItem menuItem7 = new MenuItem("Toggle edges");
		menuItem7.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.NUMBER_EDGES = !SurfaceDemo.NUMBER_EDGES;
				SurfaceDemo.instance.canvas.getView().setBoundMode(ViewBoundMode.AUTO_FIT);
				SurfaceDemo.ZOOM_PLASMA = false;
				SurfaceDemo.ZOOM_POINT = false;
				SurfaceDemo.instance.initDraw();
			}
		});
		this.add(menuItem7);

		MenuItem menuItem8 = new MenuItem("Toggle points");
		menuItem8.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SurfaceDemo.NUMBER_POINTS = !SurfaceDemo.NUMBER_POINTS;
				SurfaceDemo.instance.canvas.getView().setBoundMode(ViewBoundMode.AUTO_FIT);
				SurfaceDemo.ZOOM_PLASMA = false;
				SurfaceDemo.ZOOM_POINT = false;
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

		MenuItem menuItem6 = new MenuItem("Move on edge");
		menuItem6.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (SurfaceDemo.instance.lastClickedPoint.getClass().getName().equals("com.kz.pipeCutter.MyPickablePoint")) {

					CutThread th = new CutThread(false, SurfaceDemo.instance.lastClickedPoint);
					th.execute();
				}
			}
		});
		this.add(menuItem6);
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
				String gCode = SurfaceDemo.instance.utils.coordinateToGcode(mp);
				SurfaceDemo.instance.smoothie.send(gCode);

			}
		});
		this.add(menuItem3);

		MenuItem menuItem5 = new MenuItem("SMOOTHIE - Move on edge");
		menuItem5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if (SurfaceDemo.instance.lastClickedPoint.getClass().getName().equals("com.kz.pipeCutter.MyPickablePoint")) {
					CutThread ct = new CutThread(false, SurfaceDemo.instance.lastClickedPoint);

					ct.execute();
				}

			}
		});
		this.add(menuItem5);

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
		MenuItem menuItem12 = new MenuItem("SET POINT AS ORIGIN - G92");
		menuItem12.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						String mdiCommand = String.format(Locale.US, "G92 X%.3f Y%.3f Z%.3f", -1.0 * SurfaceDemo.instance.lastClickedPoint.xyz.x,
								-1.0 * SurfaceDemo.instance.lastClickedPoint.xyz.y, -1.0 * SurfaceDemo.instance.lastClickedPoint.xyz.z);
						Settings.getInstance().log(mdiCommand);
						new ExecuteMdi(mdiCommand).start();

						new java.util.Timer().schedule(new java.util.TimerTask() {
							@Override
							public void run() {
								String mdiCommand = "G92.1";
								Settings.getInstance().log(mdiCommand);
								new ExecuteMdi(mdiCommand).start();
							}
						}, 2500);

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
		this.add(menuItem12);

		// Gcodes for BBB
		MenuItem menuItem14 = new MenuItem("MOVE IN ABSOLUTE COORDINATES - G53");
		menuItem14.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						String mdiCommand = String.format(Locale.US, "G53 G00 X%.3f Y%.3f Z%.3f F200", SurfaceDemo.instance.lastClickedPoint.xyz.x,
								SurfaceDemo.instance.lastClickedPoint.xyz.y, SurfaceDemo.instance.lastClickedPoint.xyz.z);
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
		this.add(menuItem14);

		// Gcodes for BBB
		MenuItem menuItem15 = new MenuItem("Move to point - G00");
		menuItem15.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						String mdiCommand = String.format(Locale.US, "G00 X%.3f Y%.3f Z%.3f F200", SurfaceDemo.instance.lastClickedPoint.xyz.x,
								SurfaceDemo.instance.lastClickedPoint.xyz.y, SurfaceDemo.instance.lastClickedPoint.xyz.z);
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

	}
}
