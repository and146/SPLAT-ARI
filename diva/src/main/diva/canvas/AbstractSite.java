/*
 * $Id: AbstractSite.java,v 1.10 2001/07/22 22:00:28 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas;

import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.canvas.Figure;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

/** An abstract implementation of Site. This class provides default
 * implementations of several methods in the Site interface, to
 * make it easier to implement site classes.
 *
 * @version $Revision: 1.10 $
 * @author John Reekie
 * @rating Red
 */
public abstract class AbstractSite implements Site {

    /** The normal of the site. This is 0.0 by default.
     */
    private double _normal = 0.0;
    
    /** True if the site has had its normal set by setNormal.
     *  Default is false.
     */
    private boolean _hasNormal = false;

    /** True if the site has a fixed normal.
     *  Default is false;
     */
    private boolean _isFixed = false;

    /** Get the figure to which this site is attached.
     */
    public abstract Figure getFigure ();

    /** Get the ID of this site.
     */
    public abstract int getID ();

    /** Get the angle of the normal to this site, in radians
     * between zero and 2pi. This default method returns 0.0.
     */
    public double getNormal () {
        return _normal;
    }

    /** Get the point location of the site, in the enclosing
     * transform context with the default normal.  This method uses 
     * the getPoint(double) method, so subclasses only have to override
     * that method.
     */
    public Point2D getPoint () {
         return getPoint(getNormal());
    } 

    /** Get the point location of the site, in the given
     * transform context with the default normal. 
     * The given context must be an enclosing
     * context of the site.  This method uses 
     * the getPoint(double) method, so subclasses only have to override
     * that method.
     */
    public Point2D getPoint (TransformContext tc) {
        return CanvasUtilities.transformInto(
	           getPoint(), getTransformContext(), tc);
    }

    /** Get the point location of the site, in the enclosing
     * transform context with the given normal.
     */
    public Point2D getPoint (double normal) {
         return new Point2D.Double(getX(), getY());
    } 

    /** Get the point location of the site, in the given
     * transform context with the given normal. 
     * The given context must be an enclosing
     * context of the site.  This method uses 
     * the getPoint(double) method, so subclasses only have to override
     * that method.
     */
    public Point2D getPoint (TransformContext tc, double normal) {
        AffineTransform transform = getTransformContext().getTransform(tc);
        Point2D point = getPoint(normal);
        return transform.transform(point, point);
    }

    /** Get the enclosing transform context of this site.
     *  As a default behavior, return the transform context
     *  of the associated figure.
     */
    public TransformContext getTransformContext() {
        return getFigure().getParent().getTransformContext();
    }

    /** Get the x-coordinate of the site, in the enclosing
     * transform context.
     */
    public abstract double getX ();

    /** Get the y-coordinate of the site, in the enclosing
     * transform context.
     */
    public abstract double getY ();

    /** Test if this site has a "normal" to it. Return true if
     * setNormal has been called and false otherwise.
     */
    public boolean hasNormal () {
        return _hasNormal;
    }

    /** Test if this site has a normal in the given direction.
     * This default implementation returns false.
     */
    public boolean isNormal (int direction) {
        return false;
    }

    /** Set the normal "out" of the site. The site effectively
     * moves so that it passes through the center of the given figure.
     * The normal is limited to be between -pi and pi.  A normal of zero 
     * points to the east, and a normal of pi/2 points to the south.  This
     * "upside down" coordinate system is consistent with the upside down
     * coordinate system of the canvas, which has the origin in the upper left.
     */
    public void setNormal (double normal) {
	_hasNormal = true;
	_normal = CanvasUtilities.moduloAngle(normal);
    }
  
    /** Translate the site by the indicated distance. This
     * default implementation does nothing.
     */
    public void translate (double x, double y) {
        // do nothing
    }
}


