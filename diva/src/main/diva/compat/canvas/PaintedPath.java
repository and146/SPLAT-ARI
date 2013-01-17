/*
 * $Id: PaintedPath.java,v 1.2 2002/08/13 09:40:01 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.compat.canvas;

import diva.util.java2d.ShapeUtilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;

/** A utility class that strokes a shape.
 * This class is intended for use as a low-level class to simplify
 * construction of drawn graphics.  It contains a number of fields that
 * govern how the shape is stroked, such as the line width, dashing,
 * and paint.
 *
 * @version	$Revision: 1.2 $
 * @author 	John Reekie
 * @author      Nick Zamora
 */
public class PaintedPath extends AbstractPaintedGraphic {

    /** Create a painted path on the given Shape. The stroke
     * will be in black with a line width of one.
     */
    public PaintedPath (Shape s) {
	this(s, 1);
    }

    /** Create a painted path on the given Shape with a given
     * line width. The stroke will be painted in black.
     */
    public PaintedPath (Shape s, float lineWidth) {
	this(s, 1, Color.black);
    }
    
    /** Create a painted path on the given Shape with a given
     * line width and stroke color.
     */
    public PaintedPath (Shape s, float lineWidth, Paint paint) {
        shape = s;
	stroke = getStroke(lineWidth);
	strokePaint = paint;
    }

    /** Get the dash array. If the stroke is not a BasicStroke
     * then null will always be returned.
     */
    public float[] getDashArray () {
        if (stroke instanceof BasicStroke) {
            return ((BasicStroke) stroke).getDashArray();
        } else {
            return null;
        }
    }

    /** Get the line width. If the stroke is not a BasicStroke
     * then 1.0 will always be returned.
     */
    public float getLineWidth () {
        if (stroke instanceof BasicStroke) {
            return ((BasicStroke) stroke).getLineWidth();
        } else {
            return 1.0f;
        }
    }

    /** Test if this shape is hit by the given rectangle. Currently
     * this does not take into account the width of the stroke
     * or other things such as dashes, because of problems with
     * geometry testing with GeneralPath in the first version of
     * JDK1.2.
     */
    public boolean hit (Rectangle2D r) {
        return intersects(r);
    }

    /** Test if this shape intersects the given rectangle. Currently
     * this does not take into account the width of the stroke
     * or other things such as dashes, because of problems with
     * geometry testing with GeneralPath in the first version of
     * JDK1.2.
     */
    public boolean intersects (Rectangle2D r) {
        // Hit testing on strokes doesn't appear to work too
        // well in JDK1.2, so we will cheat and ignore the width
        // of the stroke
	return ShapeUtilities.intersectsOutline(r, shape);
    }

    /** Paint the shape. The shape is redrawn with the current
     *  shape, paint, and stroke.
     */
    public void paint (Graphics2D g) {
        g.setStroke(stroke);
        g.setPaint(strokePaint);
        g.draw(shape);
    }

   /** Set the dash array of the stroke. The existing stroke will
    * be removed, but the line width will be preserved if possible.
    */
    public void setDashArray (float dashArray[]) {
        if (stroke instanceof BasicStroke) {
            stroke = new BasicStroke(
                    ((BasicStroke) stroke).getLineWidth(),
                    ((BasicStroke) stroke).getEndCap(),
                    ((BasicStroke) stroke).getLineJoin(),
                    ((BasicStroke) stroke).getMiterLimit(),
                    dashArray,
                    0.0f);
        } else {
            stroke = new BasicStroke(
                    1.0f,
		    BasicStroke.CAP_SQUARE,
		    BasicStroke.JOIN_MITER,
		    10.0f,
                    dashArray,
                    0.0f);
        }
    }

    /** Set the line width. The existing stroke will
    * be removed, but the dash array will be preserved if possible.
     */
    public void setLineWidth (float lineWidth) {
        if (stroke instanceof BasicStroke) {
            stroke = new BasicStroke(
                    lineWidth,
                    ((BasicStroke) stroke).getEndCap(),
                    ((BasicStroke) stroke).getLineJoin(),
                    ((BasicStroke) stroke).getMiterLimit(),
                    ((BasicStroke) stroke).getDashArray(),
                    0.0f);
        } else {
             new BasicStroke(
		    lineWidth,
		    BasicStroke.CAP_SQUARE,
		    BasicStroke.JOIN_MITER,
		    10.0f,
		    null,
		    0.0f);
        }
    }

    /** Set the stroke
     */
    public void setStroke (Stroke s) {
        stroke = s;
    }
}


