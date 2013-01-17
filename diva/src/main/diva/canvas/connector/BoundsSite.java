/*
 * $Id: BoundsSite.java,v 1.5 2001/07/22 22:00:31 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import diva.canvas.AbstractSite;
import diva.canvas.Site;
import diva.canvas.Figure;

import java.awt.geom.Rectangle2D;
import javax.swing.SwingConstants;

/** A site that locates itself on the bounds of a figure's shape.
 * It has two fields that govern its position on the bounds.
 *
 * @version	$Revision: 1.5 $
 * @author 	John Reekie
 */
public class BoundsSite extends AbstractSite {

    /** The id
     */
    private int _id;

    /** The normal
     */
    private double _normal;

    /** The side to be located on: NORTH, SOUTH, EAST, WEST
     */
    private int _side;

    /** The distance to be located along that side, in percent
     */
    private double _offset;

    /** The parent figure
     */
    private Figure _parentFigure;

    /** Create a new site on the given figure with the given ID
     * and at the location given by the side and the offset.
     */
    public BoundsSite (Figure figure, int id, int side, double offset) {
        _parentFigure = figure;
        _id = id;
        _side = side;
        _offset = offset;
    }

    /** Get the figure to which this site is attached.
     */
    public Figure getFigure () {
        return _parentFigure;
    }

    /** Get the ID of this site.
     */
    public int getID () {
        return _id;
    }

    /** Get the normal of the site.
     */
    public double getNormal () {
        switch (_side) {
        case SwingConstants.NORTH:
            return -Math.PI/2;

        case SwingConstants.SOUTH:
            return Math.PI/2;

        case SwingConstants.EAST:
            return 0.0;

        case SwingConstants.WEST:
            return Math.PI;
        }
        return 0.0;
    }

    /** Get the distance to be located along the side, in percent.
     */
    public double getOffset () {
        return _offset;
    }

    /** Get the side to be located on: NORTH, SOUTH, EAST, WEST.
     */
    public int getSide () {
        return _side;
    }
    
    /** Get the x-coordinate of the site.
     */
    public double getX () {
        Rectangle2D bounds = _parentFigure.getShape().getBounds();
        double x = 0.0;
        switch (_side) {
        case SwingConstants.NORTH:
        case SwingConstants.SOUTH:
            x = bounds.getX() + _offset / 100.0 * bounds.getWidth();
            break;
        case SwingConstants.EAST:
            x = bounds.getX() + bounds.getWidth();
            break;
        case SwingConstants.WEST:
            x = bounds.getX();
            break;
        }
        return x;
    }

    /** Get the y-coordinate of the site.
     */
    public double getY () {
        Rectangle2D bounds = _parentFigure.getShape().getBounds();
        double y = 0.0;
        switch (_side) {
        case SwingConstants.EAST:
        case SwingConstants.WEST:
            y = bounds.getY() + _offset / 100.0 * bounds.getHeight();
            break;
        case SwingConstants.SOUTH:
            y = bounds.getY() + bounds.getHeight();
            break;
        case SwingConstants.NORTH:
            y = bounds.getY();
            break;
        }
        return y;
    }

    public String toString() {
	return "BoundsSite[" + getX() + "," + getY() + "," + getNormal() + "]";
    }
}



