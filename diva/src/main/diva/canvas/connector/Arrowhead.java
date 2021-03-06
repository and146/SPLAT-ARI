/*
 * $Id: Arrowhead.java,v 1.6 2001/07/22 22:00:31 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import diva.canvas.Figure;
import diva.canvas.Site;
import diva.util.java2d.Polygon2D;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** An arrowhead that is drawn on the end of a connector.
 * This is a low-level utility class, not a self-contained Figure.
 *
 * @version $Revision: 1.6 $
 * @author  John Reekie (johnr@eecs.berkeley.edu)
 */
public class Arrowhead implements ConnectorEnd {

    /** The arrowhead length, and its x and y components
     */
    private double _length = 12.0;

    /** x and y-origins
     */
    private double _originX = 0.0;
    private double _originY = 0.0;

    /** The normal to the line
     */
    private double _normal = 0.0;

    /** The shape to draw
     */
    private Polygon2D _polygon = null;

    /** A flag that says whether the shape is valid
     */
    private boolean _polygonValid = false;

    /** A flag that says to slip the direction in which the
     * arrowhead is drawn.
     */
    private boolean _flipped = false;

    /**
     * Create a new arrowhead at (0,0).
     */
    public Arrowhead () {
        this(0.0,0.0,0.0);
    }

    /**
     * Create a new arrowhead at the given point and
     * with the given normal.
     */
    public Arrowhead (double x, double y, double normal) {
        _originX = x;
        _originY = y;
        _normal = normal;
        reshape();
    }

    /** Get the bounding box of the shape used to draw
     * this connector end.
     */
    public Rectangle2D getBounds () {
        return _polygon.getBounds2D();
    }

    /** Get the connection point into the given point
     */
    public void getConnection (Point2D p) {
        if (!_polygonValid) {
            reshape();
        }
        // Set the point.
        p.setLocation(_originX + _length, _originY);
        AffineTransform at = new AffineTransform();
        at.setToRotation(_normal, _originX, _originY);
        at.transform(p,p);
    }

    /** Get the flag saying to flip the arrowhead.
     */
    public boolean getFlipped () {
        return _flipped;
    }

    /** Get the origin into the given point.
     */
    public void getOrigin (Point2D p) {
        p.setLocation(_originX, _originY);
    }

    /** Get the length.
     */
    public double getLength () {
        return _length;
    }

    /** Paint the arrow-head.  This method assumes that
     * the graphics context is already set up with the correct
     * paint and stroke.
     */
    public void paint (Graphics2D g) {
        if (!_polygonValid) {
            reshape();
        }
        g.fill(_polygon);
    }

    /** Recalculate the shape of the decoration.
     */
    public void reshape () {
        AffineTransform at = new AffineTransform();
        at.setToRotation(_normal, _originX, _originY);

        double l1 = _length * 1.0;
        double l2 = _length * 1.3;
        double w = _length * 0.4;

        if (_flipped) {
            l1 = -l1;
            l2 = -l2;
            at.translate(_length, 0.0);
        }
        _polygon = new Polygon2D.Double();
        _polygon.moveTo(_originX, _originY);
        _polygon.lineTo(
                _originX + l2,
                _originY + w);
        _polygon.lineTo(
                _originX + l1,
                _originY);
        _polygon.lineTo(
                _originX + l2,
                _originY - w);
        _polygon.closePath();
        _polygon.transform(at);
    }

    /** Set the normal of the decoration. The argument is the
     * angle in radians away from the origin. The arrowhead is
     * drawn so that the body of the arrowhead is in the
     * same direction as the normal -- that is, the arrowhead
     * appears to be pointed in the opposite direction to its
     * "normal."
     */
    public void setNormal (double angle) {
        _normal = angle;
        _polygonValid = false;
    }

    /** Set the flag that says the arrowhead is "flipped." This means
     * that the arrowhead is drawn so that the apparent direction
     * of the arrowhead is the same as its normal.
     */
    public void setFlipped (boolean flag) {
        _flipped = flag;
        _polygonValid = false;
    }

    /** Set the origin of the decoration.
     */
    public void setOrigin (double x, double y) {
        translate(x - _originX, y - _originY);
    }

    /** Set the length of the arrowhead.
     */
    public void setLength (double l) {
        _length = l;
        _polygonValid = false;
    }

    /** Translate the origin by the given amount.
     */
    public void translate (double x, double y) {
        _originX += x;
        _originY += y;

        if (_polygonValid) {
            _polygon.translate(x, y);
        }
    }
}


