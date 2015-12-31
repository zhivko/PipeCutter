package com.kz.grbl;

import java.util.Enumeration;

/**
 * A transformation.
 */

public abstract class Transformation
{
   /**
    * Transform coordinates.
    */

   public abstract void transform(Coordinates coords);

   /**
    * Transform a collection of coordinates.
    */

   public void transform(Enumeration e)
   {
      while (e.hasMoreElements()) transform((Coordinates)e.nextElement());
   }
}