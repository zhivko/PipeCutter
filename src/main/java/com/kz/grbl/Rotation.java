package com.kz.grbl;

/**
 * A rotation about a line.
 * 
 * The line is specified by a point (x,y,z) and a unit vector (a,b,c). Rotation
 * (r) is in radians.
 */

public class Rotation extends Transformation {
	private double m[][];

	public Rotation(double x, double y, double z, double a, double b, double c, double r) {
		double sin_r = Math.sin(r);
		double cos_r = Math.cos(r);

		double i = 1.0 - cos_r;

		double a_a_i = a * a * i;
		double b_b_i = b * b * i;
		double c_c_i = c * c * i;
		double a_b_i = a * b * i;
		double a_c_i = a * c * i;
		double b_c_i = b * c * i;

		double a_sin_r = a * sin_r;
		double b_sin_r = b * sin_r;
		double c_sin_r = c * sin_r;

		m = new double[3][3];

		m[0][0] = a_a_i + cos_r;
		m[1][0] = a_b_i - c_sin_r;
		m[2][0] = a_c_i + b_sin_r;

		m[0][1] = a_b_i + c_sin_r;
		m[1][1] = b_b_i + cos_r;
		m[2][1] = b_c_i - a_sin_r;

		m[0][2] = a_c_i - b_sin_r;
		m[1][2] = b_c_i + a_sin_r;
		m[2][2] = c_c_i + cos_r;
	}

	@Override
	public void transform(Coordinates coords) {
		if (coords == null)
			return;

		double x = coords.x * m[0][0] + coords.y * m[0][1] + coords.z * m[0][2];
		double y = coords.x * m[1][0] + coords.y * m[1][1] + coords.z * m[1][2];
		double z = coords.x * m[2][0] + coords.y * m[2][1] + coords.z * m[2][2];

		coords.x = x;
		coords.y = y;
		coords.z = z;
	}

	
}