package com.kz.grbl;


public class Coordinates
{
   public double x;
   public double y;
   public double z;

   public Coordinates()
   {
      this.x = 0.0;
      this.y = 0.0;
      this.z = 0.0;
   }

   public Coordinates(Coordinates coords)
   {
      this.x = coords.x;
      this.y = coords.y;
      this.z = coords.z;
   }

   public Coordinates(double x, double y, double z)
   {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public double length()
   {
      return Math.sqrt(x * x + y * y + z * z);
   }

   public Coordinates unitVector()
   {
      double l = length();

      return new Coordinates(x / l, y / l, z / l);
   }

   public String toString()
   {
      return "[" +
         (int)x + "," +
         (int)y + "," +
         (int)z +
         "]";
   }
}