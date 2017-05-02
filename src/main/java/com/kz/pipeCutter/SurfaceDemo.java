package com.kz.pipeCutter;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.MouseInfo;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

import org.apache.commons.math3.geometry.euclidean.threed.Line;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;
import org.apache.log4j.Logger;
import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.analysis.AnalysisLauncher;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.camera.AbstractCameraController;
import org.jzy3d.chart.controllers.mouse.picking.AWTMousePickingController;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.picking.IObjectPickedListener;
import org.jzy3d.picking.PickingSupport;
import org.jzy3d.plot3d.primitives.LineStrip;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Sphere;
import org.jzy3d.plot3d.rendering.canvas.CanvasAWT;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.jzy3d.plot3d.rendering.view.modes.CameraMode;
import org.jzy3d.plot3d.rendering.view.modes.ViewBoundMode;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;
import org.jzy3d.plot3d.text.align.Halign;
import org.jzy3d.plot3d.text.align.Valign;
import org.jzy3d.plot3d.text.drawable.DrawableTextBitmap;

import com.kz.pipeCutter.BBB.BBBStatus;
import com.kz.pipeCutter.BBB.Discoverer;
import com.kz.pipeCutter.ui.MyPopupMenu;
import com.kz.pipeCutter.ui.Settings;
import com.kz.pipeCutter.ui.SortedProperties;

public class SurfaceDemo extends AbstractAnalysis {
	// plasma torch heigh control process:
	// http://www.fabricatingandmetalworking.com/2010/12/torch-height-control-for-automated-plasma-cutting-applications-2/
	public Utils utils;
	public Discoverer discoverer;
	public MyTelnetClient smoothie;
	// private Cylinder cylinder = null;
	Sphere plasma = null;
	Point offsetPoint = null;
	public MyPickablePoint lastClickedPoint;
	public String angleTxt = "0";
	public static SurfaceDemo instance;
	public MyComposite myComposite;
	public MyComposite myTrail;
	public static boolean NUMBER_EDGES = false;
	public static boolean NUMBER_POINTS = false;
	public static boolean ZOOM_POINT = false;
	public static boolean ZOOM_PLASMA = false;

	float g0Speed = 0;
	float g1Speed = 0;
	public boolean g93mode;

	float axisLength = 30;

	public CanvasAWT canvas = null;
	private PickingSupport pickingSupport = null;

	private Coord3d rotationPoint;
	private DrawableTextBitmap currentRotTxt = null;
	public Point cylinderPoint;
	public Settings settingsFrame;

	public boolean alreadyCutting;
	public boolean spindleOn;
	public int gCodeLineNo;

	int pointId;
	int edgeNo;

	protected SurfaceDemo() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				SwingUtilities.invokeLater(new Runnable() {

					@Override
					public void run() {

						instance = SurfaceDemo.this;
						System.setProperty("java.net.preferIPv4Stack", "true");
						// discoverer = new Discoverer();
						try {
							Logger.getLogger(this.getClass()).info("AnalysisLauncher open instance...");
							AnalysisLauncher.open(instance);
							Logger.getLogger(this.getClass()).info("AnalysisLauncher open instance...DONE.");
						} catch (Exception e2) {
							// TODO Auto-generated catch block
							e2.printStackTrace();
						}
						Logger.getLogger(this.getClass()).info("Is daemon: " + Thread.currentThread().isDaemon());
						Logger.getLogger(this.getClass()).info("Is eventdispatched thread? " + javax.swing.SwingUtilities.isEventDispatchThread());

						instance.canvas = (CanvasAWT) instance.getChart().getCanvas();

						// instance.canvas.getAnimator().setUpdateFPSFrames(20,
						// System.out);
						// instance.canvas.setSize(600, 600);

						// Iterator<AbstractCameraController> itController =
						// instance.getChart().getControllers().iterator();
						// while (itController.hasNext()) {
						// AbstractCameraController controller =
						// itController.next();
						// if (controller instanceof ICameraKeyController) {
						// controller.dispose();
						// itController.remove();
						// }
						// System.out.println(controller.getClass().toString());
						// }
						// ICameraKeyController keyControll =
						// instance.chart.addKeyController();
						instance.canvas.addKeyListener(new KeyListener() {

							@Override
							public void keyTyped(KeyEvent e) {
								// TODO Auto-generated method stub
							}

							@Override
							public void keyReleased(KeyEvent e) {
								// TODO Auto-generated method stub

							}

							@Override
							public void keyPressed(KeyEvent e) {
								if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
									MyEdge e1 = utils.continuousEdges.get(lastClickedPoint.continuousEdgeNo);
									int index = e1.points.indexOf(lastClickedPoint.id);
									int newInd;
									if (index + 1 == e1.points.size() - 1)
										newInd = 0;
									else
										newInd = index + 1;
									MyPickablePoint p1 = utils.points.get(e1.points.get(newInd));
									lastClickedPointChanged(p1);
									e.consume();
								} else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
									MyEdge e1 = utils.continuousEdges.get(lastClickedPoint.continuousEdgeNo);
									int index = e1.points.indexOf(lastClickedPoint.id);
									int newInd;
									if (index - 1 == -1)
										newInd = e1.points.size() - 1;
									else
										newInd = index - 1;
									MyPickablePoint p1 = utils.points.get(e1.points.get(newInd));
									lastClickedPointChanged(p1);
									e.consume();
								}
							}
						});

						final PopupMenu menu = new MyPopupMenu();
						instance.canvas.add(menu);
						instance.canvas.addMouseListener(new MouseListener() {

							@Override
							public void mouseReleased(MouseEvent arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public void mousePressed(final MouseEvent arg0) {
								// TODO Auto-generated method stub
								if (arg0.getButton() == MouseEvent.BUTTON3) {

									SwingWorker menuShower = new SwingWorker<String, Object>() {
										@Override
										protected String doInBackground() throws Exception {
											// TODO Auto-generated method stub
											menu.show(instance.canvas, arg0.getX(), arg0.getY());
											return "Menu closed.";
										}
									};
									menuShower.execute();

								}
							}

							@Override
							public void mouseExited(MouseEvent arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public void mouseEntered(MouseEvent arg0) {
								// TODO Auto-generated method stub

							}

							@Override
							public void mouseClicked(MouseEvent arg0) {
								// TODO Auto-generated method stub

							}
						});

						instance.getChart().getView().setViewPositionMode(ViewPositionMode.FREE);

						try {
							FileInputStream in = new FileInputStream(Settings.iniFullFileName);
							SortedProperties props = new SortedProperties();
							props.load(in);
							in.close();

							if (props.get("surfaceDemo_position") != null) {
								String size = props.get("surfaceDemo_position").toString();
								try {
									String[] splittedSize = size.split("x");
									Integer x = Integer.valueOf(splittedSize[0]);
									Integer y = Integer.valueOf(splittedSize[1]);
									((Frame) (SurfaceDemo.instance.canvas.getParent())).setLocation(new java.awt.Point(x, y));
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}

						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}

						instance.canvas.addComponentListener(new ComponentListener() {

							@Override
							public void componentShown(ComponentEvent e) {
								// TODO Auto-generated method stub
								float radiusOfPlasma = Double.valueOf(SurfaceDemo.instance.canvas.getView().getBounds().getRadius()).floatValue() / 20.0f;
								plasma.setVolume(radiusOfPlasma);

								Component c = (Component) e.getSource();
								if (c.getName().equals("frame1")) {
									FileInputStream in;
									try {
										in = new FileInputStream(Settings.iniFullFileName);
										SortedProperties props = new SortedProperties();
										props.load(in);
										in.close();

										if (props.get("surfaceDemo_size") != null) {
											String size = props.get("surfaceDemo_size").toString();
											try {
												String[] splittedSize = size.split("x");
												instance.canvas.validate();
												instance.canvas.repaint();
												instance.canvas.getParent()
														.setSize(new Dimension(Double.valueOf(splittedSize[0]).intValue(), Double.valueOf(splittedSize[1]).intValue()));
											} catch (Exception ex) {
												ex.printStackTrace();
											}
										}

									} catch (Exception e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
								}
							}

							@Override
							public void componentResized(ComponentEvent evt) {

								Component c = (Component) evt.getSource();
								// System.out.println(c.getName() + " resized: "
								// +
								// c.getSize().toString());
								if (c.getParent().getName().equals("frame1")) {
									try {
										FileInputStream in = new FileInputStream(Settings.iniFullFileName);
										SortedProperties props = new SortedProperties();
										props.load(in);
										in.close();

										FileOutputStream out = new FileOutputStream(Settings.iniFullFileName);
										props.setProperty("surfaceDemo_size", instance.canvas.getSize().getWidth() + "x" + instance.canvas.getSize().getHeight());
										props.store(out, null);
										out.close();

									} catch (Exception ex) {
										ex.printStackTrace();
									}
								}
							}

							@Override
							public void componentMoved(ComponentEvent evt) {
								// TODO Auto-generated method stub
								Component c = (Component) evt.getSource();
								// System.out.println(c.getName() + " resized: "
								// +
								// c.getSize().toString());
								if (c.getName().equals("frame1")) {
									try {
										FileInputStream in = new FileInputStream(Settings.iniFullFileName);
										SortedProperties props = new SortedProperties();
										props.load(in);
										in.close();

										FileOutputStream out = new FileOutputStream(Settings.iniFullFileName);
										props.setProperty("surfaceDemo_position", (int) c.getLocationOnScreen().getX() + "x" + (int) c.getLocationOnScreen().getY());
										props.store(out, null);
										out.close();

									} catch (Exception ex) {
										ex.printStackTrace();
									}
									// splitPane.setDividerLocation(1 -
									// (commandPanel.getHeight() /
									// Settings.instance.getHeight()));
								}

							}

							@Override
							public void componentHidden(ComponentEvent e) {
								// TODO Auto-generated method stub

							}
						});
						// TODO Auto-generated method stub

						System.out.println("Thread name:" + Thread.currentThread().getName());
						// on linux the rotation of chart is freezing if I add
						// this line
						// if I comment it - rotation works but I get no picking
						// events
						// as said same code works OK on windows on same java 8
						// jvm
						if (OSValidator.isWindows())
							new MyAWTMousePickingController(instance.chart);

					}

				});
			}
		});
		t.setName("SurfaceDemoConstructor");
		t.start();
	}

	public static SurfaceDemo getInstance() {
		if (instance == null)
			instance = new SurfaceDemo();
		return instance;
	}

	public static void main(String[] args) throws Exception {
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");

		getInstance();

	}

	private void enablePickingTexts(MyComposite edgeTexts, Chart chart, int i) {
		if (getPickingSupport() != null) {
			for (int j = 0; j < edgeTexts.size(); j++) {
				PickableDrawableTextBitmap t = (PickableDrawableTextBitmap) edgeTexts.get(j);
				getPickingSupport().registerPickableObject(t, t);
			}

			getPickingSupport().addObjectPickedListener(new IObjectPickedListener() {
				@Override
				public void objectPicked(List<?> picked, PickingSupport ps) {
					if (picked.size() > 0) // && (System.currentTimeMillis() -
					// camMouse.clickTimeMillis < 200)) {
					{
						// System.out.println("Size: " + picked.size());
						// for (int i = 0; i < picked.size(); i++) {
						if (picked.get(0) instanceof PickableDrawableTextBitmap) {
							final PickableDrawableTextBitmap e = ((PickableDrawableTextBitmap) picked.get(0));
							final Integer edgeNo = Integer.valueOf(e.getText().split(" ")[0]);

							MyEdge edge = utils.edges.get(edgeNo);
							Settings.instance.log(edge.toString());

							final JPopupMenu menu = new JPopupMenu();
							JMenuItem menuItem = new JMenuItem("Remove edge: " + edgeNo);
							menuItem.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									MyEdge edge = utils.edges.get(edgeNo);
									edge.markAsRemoved();
									myComposite.remove(e);

									instance.getChart().render();
									utils.edges.remove(edgeNo);
								}
							});
							menu.add(menuItem);

							JMenuItem menuItem3 = new JMenuItem("Add to cut selection");
							menuItem3.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									MyEdge edge = utils.edges.get(edgeNo);
									e.setColor(Color.RED);
									edge.markToCut(true);
								}
							});
							menu.add(menuItem3);

							JMenuItem menuItem4 = new JMenuItem("Remove from cut selection");
							menuItem4.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									MyEdge edge = utils.edges.get(edgeNo);
									e.setColor(Color.BLUE);
									edge.markToCut(false);
								}
							});
							menu.add(menuItem4);

							JMenuItem menuItem2 = new JMenuItem("Set edge speed");
							menuItem2.addActionListener(new ActionListener() {
								@Override
								public void actionPerformed(ActionEvent arg0) {
									MyEdge edge = utils.edges.get(edgeNo);
									Object input = JOptionPane.showInputDialog(null, "Speed [mm/min]:", "Edge cutting speed", JOptionPane.QUESTION_MESSAGE, null, null,
											edge.cutVelocity);
									if (input != null) {
										edge.setVelocity(Float.valueOf(input.toString()));
										e.setText(edge.edgeNo + " (" + edge.cutVelocity + ")");
									}
								}
							});
							menu.add(menuItem2);

							java.awt.Point point = MouseInfo.getPointerInfo().getLocation();
							SwingUtilities.convertPointFromScreen(point, canvas);
							menu.show(instance.canvas, point.x, point.y);
						}
					}
				}
			});
		}
	}

	private PickingSupport getPickingSupport() {
		if (instance.pickingSupport == null) {
			// TODO: Need to ask Martin about this next line of code not working
			// in
			// Linux, but working
			// in Windows
			if (OSValidator.isWindows()) {
				AWTMousePickingController mousePicker = new AWTMousePickingController();
				instance.getChart().addController(mousePicker);
				instance.pickingSupport = mousePicker.getPickingSupport();
			}
			// AWTMousePickingController mousePicker = null;
			// for (AbstractCameraController controller :
			// chart.getControllers()) {
			// if (controller instanceof AWTMousePickingController) {
			// mousePicker = (AWTMousePickingController) controller;
			// instance.pickingSupport = mousePicker.getPickingSupport();
			// }
			// }
		}
		return instance.pickingSupport;
	}

	@Override
	public void init() {
		try {
			Logger.getLogger(this.getClass()).info("Creating chart! Thread: " + Thread.currentThread().getName());
			// smoothie = new MyTelnetClient(SmoothieUploader.smoothieIP,
			// SmoothieUploader.smoothieRemotePort);
			// Create a chart
			chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
			canvas = (CanvasAWT) chart.getCanvas();
			Logger.getLogger(this.getClass()).info("Creating chart! Thread: " + Thread.currentThread().getName() + "... DONE.");

			// chart = newt SwingChartComponentFactory.chart(Quality.Advanced);
			// chart.getView().setMaximized(true);
			// chart.getView().setBoundManual(instance.chart.getView().getBounds());

			// canvas.getAnimator().start();
			// System.out.println("Animator started?: " + chart.ge
			// .getAnimator().isStarted());

			String filePath = null;
			if (OSValidator.isUnix())
				filePath = "./";
			else
				filePath = "./";

			String fileName = Settings.getInstance().getSetting("gcode_input_file");
			Path path;
			if (fileName != null) {
				File f = new File(fileName);
				path = f.toPath();
			} else
				path = FileSystems.getDefault().getPath(filePath, "data4.csv");
			List<String> lines;
			lines = Files.readAllLines(path, Charset.forName("UTF-8"));
			utils = new Utils();

			ArrayList<String> line = new ArrayList<String>();

			// Point previousPolyPoint = null;
			LineStrip ls_p = null;
			int previousSurfaceNo = -1;
			int previousEdgeNo = -1;
			int edgeNoInSurface = -1;
			pointId = -1;

			edgeNo = 0;
			for (String string : lines) {
				line.add(string);
				String splitted[] = string.split(";");
				int surfaceNo = Integer.valueOf(splitted[0].split("_")[1]);
				int inventorEdgeNo = Integer.valueOf(splitted[0].split("_")[2]);
				int colorNo = inventorEdgeNo % Color.COLORS.length;

				if (surfaceNo != previousSurfaceNo)
					edgeNoInSurface = 0;

				if (splitted[0].startsWith("POLY")) {
					MyPickablePoint mp2 = null;
					MyPickablePoint mp1 = null;
					for (int i = 1; i < splitted.length; i = i + 3) {
						Double x = Double.valueOf(splitted[i].replace(",", "."));
						Double y = Double.valueOf(splitted[i + 1].replace(",", "."));
						Double z = Double.valueOf(splitted[i + 2].replace(",", "."));
						Point3d p = new Point3d(x.floatValue(), y.floatValue(), z.floatValue());
						pointId++;
						mp2 = utils.createOrFindMyPickablePoint(pointId, p, inventorEdgeNo);
						if (mp1 != null && mp2.distance(mp1) > 0) {
							// try to find edge with this two points
							boolean found = false;
							for (MyEdge e : utils.edges.values()) {
								if ((e.getPointByIndex(0).distance(mp1) == 0 && (e.getPointByIndex(1).distance(mp2) == 0)
										|| (e.getPointByIndex(1).distance(mp1) == 0 && (e.getPointByIndex(0).distance(mp2) == 0)))) {
									found = true;
									break;
								}
							}
							if (!found) {
								MyEdge edge = new MyEdge(edgeNo, surfaceNo);
								edge.addPoint(mp1.id);
								edge.addPoint(mp2.id);
								MySurface surface = utils.surfaces.get(new Integer(surfaceNo));
								if (surface == null) {
									surface = new MySurface(surfaceNo);
									utils.surfaces.put(new Integer(surfaceNo), surface);
								}
								surface.addEdge(edge);
								utils.edges.put(edgeNo, edge);
								edgeNo++;
							}
						}
						mp1 = mp2;
					}
				}

			}
			getPipeMax();
			if (!utils.isPipeCircular()) {
				utils.markRadiusEdges();
				splitLongEdges();
				splitNearRadiusEdge();
			}

			// remove edges marked to be removed
			try {
				FileInputStream in = new FileInputStream(Settings.iniEdgeProperties);
				SortedProperties props = new SortedProperties();
				props.load(in);
				Enumeration<String> e = (Enumeration<String>) props.propertyNames();
				while (e.hasMoreElements()) {
					String key = e.nextElement();
					if (key.endsWith(".isRemoved")) {
						int edgeNo = Integer.valueOf(key.split("\\.")[0]);
						utils.edges.remove(edgeNo);
					}
				}
				in.close();
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			// remove points that do not belong to either edge
			utils.removeNotUsedPoints();

			utils.markRadiusEdges();

			System.out.println("Points: " + utils.points.size());
			System.out.println("Edges: " + utils.edges.size());
			System.out.println("Surfaces: " + utils.surfaces.size());

			utils.establishNeighbourPoints();
			utils.calculateContinuousEdges();
			utils.calculateMaxAndMins();
			utils.establishRighMostAndLeftMostPoints();

			// utils.calculateAllOffsetPoints();

			utils.origPoints = new ConcurrentHashMap<Integer, MyPickablePoint>();
			for (MyPickablePoint mp : utils.points.values()) {
				utils.origPoints.put(new Integer(mp.id), mp.clone());
			}

			System.out.println("Points: " + utils.points.size());
			System.out.println("Edges: " + utils.edges.size());
			System.out.println("Surfaces: " + utils.surfaces.size());

			// Creates the 3d object
			System.out.println("Thread: " + Thread.currentThread().getName());
			chart.getAxeLayout().setXAxeLabel("X");
			chart.getAxeLayout().setYAxeLabel("Y");
			chart.getAxeLayout().setZAxeLabel("Z");
			chart.getView().setSquared(false);

			// pauseAnimator();
			// resumeAnimator();
			SurfaceDemo.instance.canvas.getAnimator().getThread().setName("ANIMATOR_TREAD");
			if (Settings.instance.getSetting("ui_zoom_plasma").equals("True"))
				SurfaceDemo.ZOOM_PLASMA = true;
			if (Settings.instance.getSetting("ui_zoom_point").equals("True"))
				SurfaceDemo.ZOOM_POINT = true;

			SwingUtilities.invokeLater(new Runnable() {
				public void run() {
					try {
						Settings.instance.log("Sleeping...");
						Thread.sleep(100);
						Settings.instance.log("Sleeping...Done.");
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					Settings.instance.log("Starting zoom and focus to last point...");
					if (!Settings.instance.getSetting("ui_zoom_center").equals("")) {
						try {
							String center_str = Settings.instance.getSetting("ui_zoom_center");
							String radius = Settings.instance.getSetting("ui_zoom_radius");

							float x = Float.valueOf(center_str.split("\\s")[0].split("=")[1]);
							float y = Float.valueOf(center_str.split("\\s")[1].split("=")[1]);
							float z = Float.valueOf(center_str.split("\\s")[2].split("=")[1]);

							instance.chart.getView().setBoundManual(new BoundingBox3d(new Coord3d(x, y, z), Float.valueOf(radius)));
							instance.canvas.getAnimator().start();

							MyPickablePoint lastClicked = findSelectedPoint();
							if (lastClicked != null)
								lastClickedPointChanged(lastClicked);

						} catch (Exception ex) {
							ex.printStackTrace();
						}
					}
					Settings.instance.log("Starting zoom and focus to last point...Done.");

				}
			});

			instance.canvas.getView().setViewPositionMode(ViewPositionMode.FREE);
			instance.canvas.getView().setMaximized(true);
			instance.canvas.getView().setCameraMode(CameraMode.ORTHOGONAL);

			// this.NUMBER_EDGES = true;

			this.NUMBER_EDGES = Boolean.valueOf(Settings.instance.getSetting("ui_number_edges"));
			this.NUMBER_POINTS = Boolean.valueOf(Settings.instance.getSetting("ui_number_points"));

			initDraw();
			System.out.println("Thread: " + Thread.currentThread().getName());

			canvas.addKeyListener(new KeyListener() {

				@Override
				public void keyReleased(KeyEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub
					// System.out.println(e.getKeyCode());
					if (e.getKeyCode() == KeyEvent.VK_0 || e.getKeyCode() == KeyEvent.VK_1) {
						double angleDelta = 0;
						if (e.getKeyCode() == KeyEvent.VK_0) {
							angleDelta = 1;
						}
						if (e.getKeyCode() == KeyEvent.VK_1) {
							angleDelta = -1;
						}
						SurfaceDemo.instance.utils.rotatePoints(angleDelta, false);
						System.out.println("angle=" + angleTxt);
					}
				}

				@Override
				public void keyTyped(KeyEvent e) {
					// TODO Auto-generated method stub

				}
			});

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private MyPickablePoint findSelectedPoint() {
		MyPickablePoint ret = null;
		String val = Settings.instance.getSetting("ui_zoom_center");
		Float x = Float.valueOf(val.split(" ")[0].split("=")[1]);
		Float y = Float.valueOf(val.split(" ")[1].split("=")[1]);
		Float z = Float.valueOf(val.split(" ")[2].split("=")[1]);
		Coord3d c1 = new Coord3d(x, y, z);
		for (MyPickablePoint p : utils.points.values()) {
			if (p.getCoord().distance(c1) < Utils.Math_E) {
				ret = p;
				break;
			}
		}
		return ret;
	}

	private void splitNearRadiusEdge() {
		// find intersection of lines to add
		float dx = Float.valueOf(Settings.instance.getSetting("pipe_dim_x"));
		float dz = Float.valueOf(Settings.instance.getSetting("pipe_dim_z"));
		float radius = Float.valueOf(Settings.instance.getSetting("pipe_radius"));
		float offset = 1;

		float length = 1;
		double offX = (dx / 2) - offset - radius;
		double offZ = (dz / 2) - offset - radius;

		double u1[] = { -offX, length, (dz / 2) };
		double u2[] = { -offX, -length, (dz / 2) };
		double u1_1[] = { offX, length, (dz / 2) };
		double u2_1[] = { offX, -length, (dz / 2) };

		double r1[] = { -(dx / 2), length, offZ };
		double r2[] = { -(dx / 2), -length, offZ };
		double r1_1[] = { -(dx / 2), length, -offZ };
		double r2_1[] = { -(dx / 2), -length, -offZ };

		double s1[] = { -offX, length, -(dz / 2) };
		double s2[] = { -offX, -length, -(dz / 2) };
		double s1_1[] = { offX, length, -(dz / 2) };
		double s2_1[] = { offX, -length, -(dz / 2) };

		double t1[] = { (dx / 2), length, offZ };
		double t2[] = { (dx / 2), -length, offZ };
		double t1_1[] = { (dx / 2), length, -offZ };
		double t2_1[] = { (dx / 2), -length, -offZ };

		Line line[] = new Line[8];
		line[0] = new Line(new Vector3D(u1), new Vector3D(u2), 0.1);
		line[1] = new Line(new Vector3D(r1), new Vector3D(r2), 0.1);
		line[2] = new Line(new Vector3D(s1), new Vector3D(s2), 0.1);
		line[3] = new Line(new Vector3D(t1), new Vector3D(t2), 0.1);
		line[4] = new Line(new Vector3D(u1_1), new Vector3D(u2_1), 0.1);
		line[5] = new Line(new Vector3D(r1_1), new Vector3D(r2_1), 0.1);
		line[6] = new Line(new Vector3D(s1_1), new Vector3D(s2_1), 0.1);
		line[7] = new Line(new Vector3D(t1_1), new Vector3D(t2_1), 0.1);

		ArrayList<MyEdge> edgesToRemove = new ArrayList<MyEdge>();
		ArrayList<MyEdge> edgesToAdd = new ArrayList<MyEdge>();
		Iterator<MyEdge> edgeIt = utils.edges.values().iterator();

		int i = 0;
		while (edgeIt.hasNext()) {
			i++;
			System.out.println(String.format("%d/%d", i, utils.edges.size()));
			MyEdge edge = edgeIt.next();
			// check to see if it splits with line1,2,3,4
			double[] e1 = { utils.points.get(edge.points.get(0)).xyz.x, utils.points.get(edge.points.get(0)).xyz.y,
					utils.points.get(edge.points.get(0)).xyz.z };
			double[] e2 = { utils.points.get(edge.points.get(1)).xyz.x, utils.points.get(edge.points.get(1)).xyz.y,
					utils.points.get(edge.points.get(1)).xyz.z };
			try {
				if (new Vector3D(e1).distance(new Vector3D(e2)) > 0) {
					Vector3D start = new Vector3D(e1);
					Vector3D end = new Vector3D(e2);

					Line edgeLine = new Line(start, end, 0.1);

					for (int k = 0; k < line.length; k++) {
						Vector3D inters = line[k].intersection(edgeLine);
						if (inters != null && (start.distance(inters) + end.distance(inters) == start.distance(end))) {
							MyPickablePoint p1 = SurfaceDemo.instance.utils.points.get(edge.points.get(0));
							MyPickablePoint p2 = SurfaceDemo.instance.utils.points.get(edge.points.get(1));

							Point3d p = new Point3d(inters.getX(), inters.getY(), inters.getZ());
							int id = pointId++;
							MyPickablePoint newP = utils.createOrFindMyPickablePoint(id, p, -1);
							newP.continuousEdgeNo = p1.continuousEdgeNo;

							MyEdge edge1 = new MyEdge(edgeNo, -1);
							edge1.addPoint(p1.id);
							edge1.addPoint(newP.id);
							MySurface surface = utils.surfaces.get(edge.surfaceNo);
							if (surface == null) {
								surface = new MySurface(edge.surfaceNo);
								utils.surfaces.put(edge.surfaceNo, surface);
							}
							surface.addEdge(edge1);
							edgesToAdd.add(edge1);
							// utils.edges.put(edgeNo, edge1);
							edgeNo++;

							MyEdge edge2 = new MyEdge(edgeNo, -1);
							edge2.addPoint(newP.id);
							edge2.addPoint(p2.id);
							surface.addEdge(edge2);
							edgesToAdd.add(edge2);
							// utils.edges.put(edgeNo, edge2);
							edgeNo++;

							edgesToRemove.add(edge);
						}
					}
				} else {
					// System.out.println(edge.edgeNo + " points: " +
					// edge.points.get(0) +
					// " "
					// + edge.points.get(1));
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		for (MyEdge myEdge : edgesToAdd) {
			utils.edges.put(myEdge.edgeNo, myEdge);
		}

		edgeIt = utils.edges.values().iterator();
		while (edgeIt.hasNext()) {
			MyEdge edge = edgeIt.next();
			if (edgesToRemove.contains(edge)) {
				edgeIt.remove();
			}
		}
	}

	private void splitLongEdges() {
		// splitLongEdges
		getPipeMax();

		ArrayList<MyEdge> edgesToRemove = new ArrayList<MyEdge>();
		ArrayList<MyEdge> edgesToAdd = new ArrayList<MyEdge>();

		// get longest nonradius edge
		Iterator<MyEdge> edgeIt = utils.edges.values().iterator();
		double minLength = Double.MAX_VALUE;
		while (edgeIt.hasNext()) {
			MyEdge edge = edgeIt.next();
			if (edge.edgeType == MyEdge.EdgeType.NORMAL) {
				if (edge.length < minLength)
					minLength = edge.length;
			}
		}

		edgeIt = utils.edges.values().iterator();
		while (edgeIt.hasNext()) {
			MyEdge edge = edgeIt.next();
			if (edge.length >= minLength * 0.5 && edge.edgeType == MyEdge.EdgeType.NORMAL) {
				MyPickablePoint p1 = SurfaceDemo.instance.utils.points.get(edge.points.get(0));
				MyPickablePoint p2 = SurfaceDemo.instance.utils.points.get(edge.points.get(1));

				double x = (p1.getX() + p2.getX()) / 2;
				double y = (p1.getY() + p2.getY()) / 2;
				double z = (p1.getZ() + p2.getZ()) / 2;

				Point3d p = new Point3d(x, y, z);
				int id = pointId++;
				MyPickablePoint newP = utils.createOrFindMyPickablePoint(id, p, -1);

				MyEdge edge1 = new MyEdge(edgeNo, -1);
				edge1.addPoint(p1.id);
				edge1.addPoint(newP.id);
				MySurface surface = utils.surfaces.get(edge.surfaceNo);
				if (surface == null) {
					surface = new MySurface(edge.surfaceNo);
					utils.surfaces.put(edge.surfaceNo, surface);
				}
				surface.addEdge(edge1);
				edgesToAdd.add(edge1);
				edgeNo++;

				MyEdge edge2 = new MyEdge(edgeNo, -1);
				edge2.addPoint(newP.id);
				edge2.addPoint(p2.id);
				surface.addEdge(edge2);
				edgesToAdd.add(edge2);
				edgeNo++;

				edgesToRemove.add(edge);
				System.out.println("Points used: " + p1.id + " " + newP.id + " " + p2.id);
			}
		}

		for (MyEdge myEdge : edgesToAdd) {
			utils.edges.put(myEdge.edgeNo, myEdge);
		}

		edgeIt = utils.edges.values().iterator();
		while (edgeIt.hasNext()) {
			MyEdge edge = edgeIt.next();
			if (edgesToRemove.contains(edge)) {
				edgeIt.remove();
			}
		}
	}

	private void getPipeMax() {
		ArrayList<MyPickablePoint> sortedXList = new ArrayList(SurfaceDemo.instance.utils.points.values());
		Collections.sort(sortedXList, new MyPickablePointXComparator());
		ArrayList<MyPickablePoint> sortedZList = new ArrayList(SurfaceDemo.instance.utils.points.values());
		Collections.sort(sortedZList, new MyPickablePointZComparator());

		ArrayList<MyPickablePoint> sortedYList = new ArrayList(SurfaceDemo.instance.utils.points.values());
		Collections.sort(sortedYList, new MyPickablePointYComparator());

		double minX = sortedXList.get(0).getX();
		double maxX = sortedXList.get(sortedXList.size() - 1).getX();
		double minZ = sortedZList.get(0).getZ();
		double maxZ = sortedZList.get(sortedZList.size() - 1).getZ();

		double minY = sortedYList.get(0).getY();
		double maxY = sortedYList.get(sortedYList.size() - 1).getY();

		Settings.instance.setSetting("pipe_dim_x", Double.valueOf(maxX - minX));
		Settings.instance.setSetting("pipe_dim_z", Double.valueOf(maxZ - minZ));
		Settings.instance.setSetting("pipe_dim_max_y", Double.valueOf(maxY));
		Settings.instance.setSetting("pipe_dim_min_y", Double.valueOf(minY));
	}

	public void initDraw() {

		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				// instance.canvas.getAnimator().stop();
				clearPicking();
				plasma = null;
				instance.getChart().getScene().getGraph().remove(instance.myComposite);
				myComposite = new MyComposite();
				myTrail = new MyComposite();
				addAxis();
				addCurrentRotation();
				utils.edgeTexts = new MyComposite();
				utils.pointTexts = new MyComposite();
				myComposite.add(new ArrayList<MyPickablePoint>(utils.points.values()));
				ArrayList<Integer> alreadyAddedPointsText = new ArrayList<Integer>();
				for (MyEdge edge : utils.edges.values()) {
					LineStrip ls = edge.lineStrip;
					if (NUMBER_EDGES) {
						Coord3d cent = utils.continuousEdges.get(edge.getPointByIndex(0).continuousEdgeNo).center;
						Coord3d delta = edge.center.sub(cent);
						String radius = "";

						// 2 mm toward center
						Coord3d textPoint = edge.center.sub(delta.getNormalizedTo(0.2f));
						if (edge.edgeType == MyEdge.EdgeType.ONRADIUS)
							radius = "R";
						Rotation r = new Rotation(0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, -1.0f * Float.valueOf(SurfaceDemo.instance.angleTxt) * Math.PI / 180);
						Coordinates c = new Coordinates(textPoint.x, textPoint.y, textPoint.z);
						r.transform(c);

						PickableDrawableTextBitmap t5 = new PickableDrawableTextBitmap(String.valueOf(edge.edgeNo + " (" + edge.cutVelocity + ")"),
								new Coord3d(c.x, c.y, c.z), edge.isToCut() ? Color.RED : Color.BLUE);
						edge.setTxt(t5);
						t5.setHalign(Halign.CENTER); // TODO: invert
						t5.setValign(Valign.CENTER); // TODO: invert
						// t5.setValign(Valign.BOTTOM); // TODO: invert
						// left/right
						t5.setPickingId(edge.edgeNo);
						utils.edgeTexts.add(t5);
					}
					for (Integer pointNo : edge.points) {
						MyPickablePoint point = SurfaceDemo.instance.utils.points.get(pointNo);
						ls.add(utils.points.get(point.id));
						if (SurfaceDemo.NUMBER_POINTS) {
							if (!alreadyAddedPointsText.contains(pointNo)) {
								Coord3d cent = utils.continuousEdges.get(edge.getPointByIndex(0).continuousEdgeNo).center;
								Coord3d delta = point.xyz.sub(cent);
								Coord3d textPoint = point.xyz.sub(delta.getNormalizedTo(0.2f));
								PickableDrawableTextBitmap t4 = new PickableDrawableTextBitmap(String.valueOf(point.id),
										new Coord3d(textPoint.x, textPoint.y, textPoint.z), Color.BLUE);
								t4.setHalign(Halign.CENTER); // TODO: invert
								t4.setValign(Valign.CENTER); // TODO: invert
								t4.setPickingId(point.id);
								utils.pointTexts.add(t4);
							}
						}
					}

					myComposite.add(ls);
				}
				// centers of continuous edges
				for (MyEdge continuousEdge : utils.continuousEdges.values()) {
					Point edgeCenter = new Point(continuousEdge.center, Color.RED, 3.0f);
					myComposite.add(edgeCenter);
				}

				if (NUMBER_EDGES) {
					myComposite.add(utils.edgeTexts);
					instance.enablePickingTexts(instance.utils.edgeTexts, instance.chart, 10);
				}
				if (NUMBER_POINTS) {
					myComposite.add(utils.pointTexts);
				}
				instance.getChart().getScene().getGraph().add(instance.myComposite);
				System.out.println("Composite element size: " + myComposite.getDrawables().size());
				getPlasma();
				redrawPosition();
				instance.canvas.getAnimator().start();
				instance.enablePicking(instance.utils.points.values(), instance.chart, 10);
			}
		});
		t.start();

	}

	public Sphere getPlasma() {
		if (instance != null && plasma == null) {
			Color color = Color.BLUE; // mapper.getColor(new
			// Coord3d(0,0,height));
			// color.a = 0.55f;
			color.a = 1.0f;
			plasma = new Sphere(new Coord3d(0, 0, 0), 2.0f, 4, color);
			plasma.setWireframeColor(color);
			if (BBBStatus.instance != null)
				plasma.setPosition(new Coord3d(BBBStatus.instance.x, BBBStatus.instance.y, BBBStatus.instance.z));
			else
				plasma.setPosition(new Coord3d(0, 0, 0));

			plasma.setDisplayed(true);
			instance.myComposite.add(plasma);
		}
		return plasma;
	}

	private void clearPicking() {

		for (AbstractCameraController controller : chart.getControllers()) {
			if (controller instanceof AWTMousePickingController) {
				AWTMousePickingController mousePicker = (AWTMousePickingController) controller;
				mousePicker.getPickingSupport().unRegisterAllPickableObjects();
				break;
			}
		}
	}

	private void enablePicking(Collection<MyPickablePoint> points, Chart chart, int brushSize) {

		if (getPickingSupport() != null) {
			for (MyPickablePoint p : points) {
				getPickingSupport().registerPickableObject(p, p);
			}

			getPickingSupport().addObjectPickedListener(new IObjectPickedListener() {
				@Override
				public void objectPicked(List<?> picked, PickingSupport ps) {

					if (picked.size() > 0) // && (System.currentTimeMillis() -
					{
						if (picked.get(0).getClass().getName().equals("com.kz.pipeCutter.MyPickablePoint")) {
							MyPickablePoint mp = ((MyPickablePoint) picked.get(0));
							Settings.instance.log(mp.toString());
							SurfaceDemo.this.lastClickedPointChanged(mp);
						} else if (picked.get(0).getClass().getName().equals("org.jzy3d.plot3d.primitives.pickable.PickablePolygon")) {
						} else {
						}

					}
				}

			});
		}
	}

	public void lastClickedPointChanged(MyPickablePoint mp) {
		try {

			if (lastClickedPoint != null) {
				lastClickedPoint.setWidth(4f);
				lastClickedPoint.setColor(Color.BLACK);
			}
			mp.setWidth(8f);
			mp.setColor(Color.BLUE);

			lastClickedPoint = mp;

			if (offsetPoint != null)
				myComposite.remove(offsetPoint);

			offsetPoint = SurfaceDemo.instance.utils.calculateOffsetPoint(mp);
			offsetPoint.setColor(Color.GREEN);
			offsetPoint.setWidth(6.0f);
			myComposite.add(offsetPoint);
			SurfaceDemo.instance.getChart().render();

			if (!SurfaceDemo.ZOOM_POINT) {
				SurfaceDemo.ZOOM_POINT = true;
				SurfaceDemo.ZOOM_PLASMA = false;
			}
			this.redrawPosition();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		redrawPosition();
	}

	public void addAxis() {

		LineStrip yAxis = new LineStrip();
		yAxis.setWireframeColor(Color.GREEN);
		yAxis.add(new Point(new Coord3d(0, 0, 0)));
		yAxis.add(new Point(new Coord3d(0, 1.4 * axisLength, 0)));
		DrawableTextBitmap yAxisTxt = new DrawableTextBitmap("y", new Coord3d(0, 1.5 * axisLength, 0), Color.GREEN);
		yAxisTxt.setHalign(Halign.CENTER); // TODO: invert
		yAxisTxt.setValign(Valign.CENTER); // TODO: invert
		myComposite.add(yAxisTxt);

		LineStrip xAxis = new LineStrip();
		xAxis.setWireframeColor(Color.BLUE);
		xAxis.add(new Point(new Coord3d(0, 0, 0)));
		xAxis.add(new Point(new Coord3d(0.9 * axisLength, 0, 0)));
		DrawableTextBitmap xAxisTxt = new DrawableTextBitmap("x", new Coord3d(1 * axisLength, 0, 0), Color.BLUE);
		xAxisTxt.setHalign(Halign.CENTER); // TODO: invert
		xAxisTxt.setValign(Valign.CENTER); // TODO: invert
		myComposite.add(xAxisTxt);

		LineStrip zAxis = new LineStrip();
		zAxis.setWireframeColor(Color.RED);
		zAxis.add(new Point(new Coord3d(0, 0, 0)));
		zAxis.add(new Point(new Coord3d(0, 0, 0.9 * axisLength)));
		myComposite.add(xAxis);
		myComposite.add(zAxis);
		myComposite.add(yAxis);
		DrawableTextBitmap zAxisTxt = new DrawableTextBitmap("z", new Coord3d(0, 0, 1 * axisLength), Color.RED);
		zAxisTxt.setHalign(Halign.CENTER); // TODO: invert
		zAxisTxt.setValign(Valign.CENTER); // TODO: invert
		myComposite.add(zAxisTxt);
	}

	public void addCurrentRotation() {
		LineStrip currentRot = null;

		currentRot = new LineStrip();
		currentRot.setWidth(0.3f);
		currentRot.setWireframeColor(Color.BLUE);
		currentRot.add(new Point(new Coord3d(0, 0, 0)));
		rotationPoint = new Coord3d();

		currentRot.add(new Point(rotationPoint));

		currentRotTxt = new DrawableTextBitmap("0.0", rotationPoint, Color.BLUE);
		currentRotTxt.setHalign(Halign.CENTER);
		currentRotTxt.setValign(Valign.CENTER);
		myComposite.add(currentRot);
		myComposite.add(currentRotTxt);
		float value = Float.valueOf(SurfaceDemo.instance.angleTxt).floatValue();
		calculateRotationPoint(value);

	}

	public void calculateRotationPoint(double angle) {

		float x = axisLength * 1.5f * (float) Math.sin(Math.toRadians(angle));
		float z = axisLength * 1.5f * (float) Math.cos(Math.toRadians(angle));
		rotationPoint.set(x, 0, z);
		currentRotTxt.setText(String.format("%.4f", angle));
	}

	public void pauseAnimator() {
		instance.getChart().pauseAnimator();
	}

	public void resumeAnimator() {
		instance.getChart().resumeAnimator();
	}

	public void move(MyPickablePoint mp, boolean slow, boolean cut, float zOffset, Vector3D kerOffsetVec) {
		move(mp, slow, cut, zOffset, true, kerOffsetVec);
	}

	public void move(MyPickablePoint tempPoint, boolean slow, boolean cut, float zOffset, boolean writeToGCode, Vector3D kerOffsetVec) {

		if (plasma == null) {
			// cylinder = new Cylinder(tempPoint);
			getPlasma();
		}
		// }
		Point p = new Point();
		p.xyz.set(tempPoint.xyz.x, tempPoint.xyz.y, tempPoint.xyz.z);
		if (kerOffsetVec != null) {
			p.xyz = p.xyz.add((float) -kerOffsetVec.getX(), (float) -kerOffsetVec.getY(), (float) -kerOffsetVec.getZ());
		}
		Coord3d offsetedPoint = p.xyz.add(new Coord3d(0, 0, zOffset));
		plasma.setPosition(offsetedPoint);

		Point p1 = new Point(plasma.getPosition());
		p1.setWidth(4f);
		SurfaceDemo.getInstance().myTrail.add(p1);

		Color color;
		if (cut) {
			color = Color.RED;
			color.a = 0.55f;
			plasma.setColor(color);
			plasma.setWireframeColor(Color.RED);
		} else {
			color = Color.BLUE;
			p1.setColor(Color.BLUE);
			color.a = 0.55f;
			plasma.setColor(color);
			plasma.setWireframeColor(Color.BLUE);
		}
		p1.setColor(color);
		p.setCoord(tempPoint.xyz);

		if (writeToGCode) {
			String gcode = SurfaceDemo.instance.utils.coordinateToGcode(tempPoint, zOffset, slow, kerOffsetVec);
			if (cut) {
				writeToGcodeFile(String.format(java.util.Locale.US, "%s", gcode));
				alreadyCutting = true;
			} else {
				if (alreadyCutting) {
					writeToGcodeFile("M5");
				}
				writeToGcodeFile(String.format(java.util.Locale.US, "%s (pointId: %d)", gcode, tempPoint.id));
				alreadyCutting = false;
			}
		} else {
			System.out.println("OOPS");
		}

		// if (instance.getChart().getView().getCanvas() != null)
		// instance.getChart().render();
	}

	public void moveAbove(MyPickablePoint tempPoint, float offset, long pierceTimeMs, Vector3D kerOffsetVec) {

		if (kerOffsetVec != null) {
			tempPoint.xyz.add((float) -kerOffsetVec.getX() / 2, (float) -kerOffsetVec.getY() / 2, (float) -kerOffsetVec.getZ() / 2);
		}

		Coord3d abovePoint = tempPoint.xyz.add(0f, 0f, offset);
		plasma.setPosition(abovePoint);

		Point p1 = new Point(plasma.getPosition());
		p1.setColor(Color.BLUE);
		p1.setWidth(4f);
		SurfaceDemo.getInstance().myTrail.add(p1);

		String gcode = SurfaceDemo.instance.utils.coordinateToGcode(tempPoint, offset, false, kerOffsetVec);
		plasma.setColor(Color.BLUE);
		plasma.setWireframeColor(Color.BLUE);

		if (pierceTimeMs > 0) {
			try {
				Thread.sleep(Long.valueOf(10));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			writeToGcodeFile(gcode);
			if (!alreadyCutting) {
				// SurfaceDemo.instance.utils.previousEdge = null;
				writeToGcodeFile("M3 S400");
				alreadyCutting = true;
			}
			plasma.setColor(Color.RED);
			plasma.setWireframeColor(Color.RED);

			writeToGcodeFile(String.format(Locale.US, "G04 P%.3f", (pierceTimeMs / 1000.0)));
			try {
				TimeUnit.MILLISECONDS.sleep(pierceTimeMs);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		if (ZOOM_POINT) {
			float edge = canvas.getView().getBounds().getXmax() - canvas.getView().getBounds().getXmin();
			canvas.getView().setBoundManual(new BoundingBox3d(lastClickedPoint.xyz, edge));
		}
		if (ZOOM_PLASMA) {
			float edge = canvas.getView().getBounds().getXmax() - canvas.getView().getBounds().getXmin();
			canvas.getView().setBoundManual(new BoundingBox3d(plasma.getPosition(), edge));
		}

		// instance.getChart().render();
		try {
			TimeUnit.MILLISECONDS.sleep(Cylinder.sleep);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public float getKerfOffset() {
		float ret = 0.0f;
		ret = Float.valueOf(Settings.getInstance().getSetting("plasma_kerf_offset_mm")).floatValue();
		return ret;
	}

	public void redrawPosition() {
		if (instance.getChart().getView().getCanvas() != null) {
			float currentViewRadius = Float.valueOf(Settings.instance.getSetting("ui_zoom_radius"));
			if (currentViewRadius == 0)
				currentViewRadius = 0.1f;
			if (ZOOM_PLASMA && SurfaceDemo.instance.getPlasma().getPosition() != null) {
				SurfaceDemo.instance.canvas.getView().setBoundMode(ViewBoundMode.MANUAL);
				SurfaceDemo.instance.canvas.getView().setBoundManual(new BoundingBox3d(SurfaceDemo.instance.getPlasma().getPosition(), currentViewRadius));
			} else if (ZOOM_POINT && SurfaceDemo.instance.lastClickedPoint != null) {
				SurfaceDemo.instance.canvas.getView().setBoundMode(ViewBoundMode.MANUAL);
				SurfaceDemo.instance.canvas.getView().setBoundManual(new BoundingBox3d(SurfaceDemo.instance.lastClickedPoint.getCoord(), currentViewRadius));
			} else
				SurfaceDemo.instance.canvas.getView().setBoundMode(ViewBoundMode.AUTO_FIT);
			// instance.getChart().render();
		}
	}

	public void writeToGcodeFile(String txt) {
		PrintWriter out = null;
		try {

			if (CutThread.gcodeFile == null) {
				String gcodeFolder = Settings.getInstance().getSetting("gcode_folder");
				CutThread.gcodeFile = new File(gcodeFolder + File.separatorChar + "prog.gcode");
			}
			out = new PrintWriter(new BufferedWriter(new FileWriter(CutThread.gcodeFile.getAbsolutePath(), true)));
			if (txt.startsWith("G01 X0.00 Y250.00 Z61.5 A360.0000 B360.0000 F36.4"))
				System.out.println(txt);
			out.println(txt);
			out.flush();
			this.gCodeLineNo++;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (out != null)
				out.close();
		}
	}

}
