/*
 * $Id: AutonomousSite.java,v 1.4 2001/07/22 22:00:31 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import diva.canvas.AbstractSite;
import diva.canvas.CanvasLayer;
import diva.canvas.CanvasPane;
import diva.canvas.Figure;
import diva.canvas.Site;
import diva.canvas.TransformContext;

/** A site that is not attached to a figure. Autonomous sites are
 * useful in building editors in which connectors can be reshaped
 * or reconnected, as the connector can be attached to an autonomous
 * site and then the autonomous site moved. Because sites must be
 * located in a transform context, the constructor of autonomous
 * sites requires that a transform context be supplied. For convenience,
 * there are other constructors that accept a figures or pane, and
 * use the transform context of that object.
 *
 * @version	$Revision: 1.4 $
 * @author 	John Reekie
 */
public class AutonomousSite extends AbstractSite {

    /** The enclosing transform context
     */
    private TransformContext _context;

    /** The location of this site.
     */
    private double _x = 0.0;
    private double _y = 0.0;

    /** Create a new autonomous site in the given transform
     * context and at the given location within that context.
     */
    public AutonomousSite (TransformContext c, double x, double y) {
        setLocation(c, x, y);
    }

    /** Create a new autonomous site in the transform
     * context of the given pane and at the given location within that pane.
     */
    public AutonomousSite (CanvasPane p, double x, double y) {
        setLocation(p.getTransformContext(), x, y);
    }

    /** Create a new autonomous site in the transform
     * context of the given pane and at the given location within that layer.
     */
    public AutonomousSite (CanvasLayer l, double x, double y) {
        setLocation(l.getTransformContext(), x, y);
    }

    /** Create a new autonomous site in the transform context of
     * the given figure and at the given location within that figure.
     */
    public AutonomousSite (Figure f, double x, double y) {
        setLocation(f.getTransformContext(), x, y);
    }

    /** Return null. Autonomous sites are not attached to a figure.
     */
    public Figure getFigure () {
        return null;
    }

    /** Return zero. Autonomous sites don't have a meaningful ID.
     */
    public int getID () {
        return 0;
    }

    /** Get the enclosing transform context of this site. This
     * is the context given to the constructor or set in the
     * setLocation() method.
     */
    public TransformContext getTransformContext() {
        return _context;
    } 
    
    /** Get the x-coordinate of the site, in the enclosing
     * transform context.
     */
    public double getX () {
        return _x;
    }

    /** Get the y-coordinate of the site, in the enclosing
     * transform context.
     */
    public double getY () {
        return _y;
    }

    /** Set the transform context and the location within the new
     * transform context. This is typically used when dragging
     * an autonomous site across context boundaries.
     */
    public void setLocation (TransformContext c, double x, double y) {
        _context = c;
        _x = x;
        _y = y;
    }

    /** Translate the site by the indicated distance.
     */
    public void translate (double x, double y) {
        _x += x;
        _y += y;
    }
}


