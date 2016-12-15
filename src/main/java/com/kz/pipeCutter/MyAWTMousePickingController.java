package com.kz.pipeCutter;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.mouse.AWTMouseUtilities;
import org.jzy3d.chart.controllers.mouse.picking.AWTMousePickingController;
import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
import org.jzy3d.maths.Coord2d;

import com.kz.pipeCutter.ui.Settings;

public class MyAWTMousePickingController extends AWTMousePickingController implements MouseListener, MouseWheelListener, MouseMotionListener {
	public MyAWTMousePickingController() {
	}

	public MyAWTMousePickingController(Chart chart) {
		register(chart);
		addSlaveThreadController(new CameraThreadController(chart));
	}

	public MyAWTMousePickingController(Chart chart, int brushSize) {
		super(chart, brushSize);
		addSlaveThreadController(new CameraThreadController(chart));
	}

	public void register(Chart chart) {
		super.register(chart);
		chart.getCanvas().addMouseController(this);
	}

	public void dispose() {
		for (Chart chart : targets) {
			chart.getCanvas().removeMouseController(this);
		}
		super.dispose();
	}

	/**
	 * Handles toggle between mouse rotation/auto rotation: double-click starts
	 * the animated rotation, while simple click stops it.
	 */
	@Override
	public void mousePressed(MouseEvent e) {
		//
		if (handleSlaveThread(e))
			return;

		prevMouse.x = e.getX();
		prevMouse.y = e.getY();
	}

	/** Compute shift or rotate */
	@Override
	public void mouseDragged(MouseEvent e) {
		Coord2d mouse = new Coord2d(e.getX(), e.getY());

		// Rotate
		if (AWTMouseUtilities.isLeftDown(e)) {
			Coord2d move = mouse.sub(prevMouse).div(100);
			rotate(move);
		}
		// Shift
		else if (AWTMouseUtilities.isRightDown(e)) {
			Coord2d move = mouse.sub(prevMouse);
			if (move.y != 0)
				shift(move.y / 500);
		}
		prevMouse = mouse;
	}

	/** Compute zoom */
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		stopThreadController();
		float factor = 1 + (e.getWheelRotation() / 10.0f);

		String center = chart.getView().getBounds().getCenter().toString();
		String radius = String.valueOf(chart.getView().getBounds().getRadius());

		Settings.instance.setSetting("ui_zoom_center", center);
		if (radius.equals("0"))
			radius = "0.1";
		Settings.instance.setSetting("ui_zoom_radius", radius);
		zoomZ(factor);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		super.mouseClicked(e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		super.mouseEntered(e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		super.mouseMoved(e);
	}

	public boolean handleSlaveThread(MouseEvent e) {
		if (AWTMouseUtilities.isDoubleClick(e)) {
			if (threadController != null) {
				threadController.start();
				return true;
			}
		}
		if (threadController != null)
			threadController.stop();
		return false;
	}

}
