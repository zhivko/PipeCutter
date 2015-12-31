package com.kz.pipeCutter;

import org.jzy3d.colors.Color;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.primitives.pickable.Pickable;
import org.jzy3d.plot3d.text.drawable.DrawableTextBitmap;

public class PickableDrawableTextBitmap extends DrawableTextBitmap implements Pickable {

	public PickableDrawableTextBitmap(String txt, Coord3d position, Color color) {
		super(txt, position, color);
	}

	@Override
	public void setPickingId(int id) {
		this.pid = id;
	}

	@Override
	public int getPickingId() {
		return pid;
	}

	protected int pid = -1;
}
