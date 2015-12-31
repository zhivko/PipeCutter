package com.kz.grbl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.AbstractComposite;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Point;
import org.jzy3d.plot3d.primitives.Polygon;
import org.jzy3d.plot3d.primitives.Quad;
import org.jzy3d.plot3d.rendering.scene.Graph;

public class Cylinder extends AbstractComposite {
	Coord3d position;
	public static double diameter = 0.2;
	public static float trueHoleDiameter = 0.4f;
	public static long sleep = 100;

	public static float offsetX = 100;
	public static float offsetY = 30;
	public static float offsetZ = 30;

	double height = 0.7;

	public Cylinder(Point point) {
		this.move(point.getCoord());
	}

	public void setData(Coord3d position, float height, int slices, int rings, Color color) {
		// Create sides
		top = new Polygon();
		low = new Polygon();
		this.position = position;

		MyPickablePoint newPoint = new MyPickablePoint(-100000, position,Color.BLACK,0.4f,-200000);

		String gcode = SurfaceDemo.instance.utils.coordinateToGcode(newPoint);
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("prog.gcode", true)))) {
			out.println(gcode);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (int i = 0; i < slices; i++) {
			float angleBorder1 = (float) i * 2 * (float) Math.PI / (float) slices;
			float angleBorder2 = (float) (i + 1) * 2 * (float) Math.PI / (float) slices;

			Coord2d border1 = new Coord2d(angleBorder1, diameter / 2).cartesian();
			Coord2d border2 = new Coord2d(angleBorder2, diameter / 2).cartesian();

			Quad face = new Quad();
			face.add(new Point(new Coord3d(position.x + border1.x, position.y + border1.y, position.z)));
 			face.add(new Point(new Coord3d(position.x + border1.x, position.y + border1.y, position.z + height)));
			face.add(new Point(new Coord3d(position.x + border2.x, position.y + border2.y, position.z + height)));
			face.add(new Point(new Coord3d(position.x + border2.x, position.y + border2.y, position.z)));
			face.setColor(color);
			face.setWireframeDisplayed(false);

			// add the polygon to the cylinder
			add(face);

			// compute top and low faces
			low.add(new Point(new Coord3d(position.x + border1.x, position.y + border1.y, position.z)));
			top.add(new Point(new Coord3d(position.x + border1.x, position.y + border1.y, position.z + height)));
		}
		low.setColor(color);
		top.setColor(color);
		add(top);
		add(low);

		setWireframeDisplayed(true);
		setWireframeColor(Color.BLACK);
		position = new Coord3d(position.x, position.y, position.z);

	}

	public void clean() {
		this.dispose();
		List<AbstractDrawable> drawables = this.getDrawables();

		for (AbstractDrawable abstractDrawable : drawables) {
			System.out.println(abstractDrawable.getClass().getName());
			if (abstractDrawable instanceof org.jzy3d.plot3d.primitives.Quad)
				((org.jzy3d.plot3d.primitives.Quad) abstractDrawable).setColor(Color.RED);
			SurfaceDemo.instance.myComposite.remove(abstractDrawable);
			//SurfaceDemo.instance.getChart().removeDrawable(abstractDrawable);
		}
		SurfaceDemo.instance.myComposite.remove(this);
	

	}

	public void move(Coord3d newCoord) {
		clean();
		this.setData(newCoord, (float) height, 15, 5, Color.BLUE);
		SurfaceDemo.instance.myComposite.add(this);
		//SurfaceDemo.instance.getChart().render();

		// Rotate rotate = new Rotate(angleDelta / 10, new Coord3d(0, 1, 0));
		// Translate translate = new Translate(newCoord.sub(position));
		// Transform t = new Transform();
		// t.add(translate);
		// this.applyGeometryTransform(t);
		// position.set(newCoord.x, newCoord.y, newCoord.z);
	}

	public void move(Point p) {
		this.move(p.xyz);
	}

	private Polygon top;
	private Polygon low;
}
