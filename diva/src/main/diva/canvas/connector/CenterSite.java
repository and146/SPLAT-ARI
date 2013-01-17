/*
 * $Id: CenterSite.java,v 1.3 2001/07/22 22:00:31 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import diva.canvas.AbstractSite;
import diva.canvas.Site;
import diva.canvas.Figure;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** A concrete implementation of Site that is located in the
 * center of the bounding box of a figure. This is a utility class
 * provided for convenience of figures that need to make their
 * center points connectible.
 *
 * @version	$Revision: 1.3 $
 * @author 	John Reekie
 */
public class CenterSite extends AbstractSite {

    /** The id
     */
    private int _id;

    /** The parent figure
     */
    private Figure _parentFigure;

    /** Create a new site on the given figure. The site will have 
     * the ID zero.
     *
     * @ FIXME deprecated Use the constructor that takes an ID.
     */ 
    public CenterSite (Figure figure) {
        this(figure,0);
    }

    /** Create a new site on the given figure and with the given ID
     */ 
    public CenterSite (Figure figure, int id) {
        this._id = id;
        this._parentFigure = figure;
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

    /** Get the x-coordinate of the site. The site
     * is located in the center of the parent figure's bounding
     * box.
     */
    public double getX () {
        Rectangle2D bounds = _parentFigure.getBounds();
        return bounds.getX() + bounds.getWidth()/2;
    }

    /** Get the y-coordinate of the site.  The site
     * is located in the center of the parent figure's bounding
     * box.
     */
    public double getY () {
        Rectangle2D bounds = _parentFigure.getBounds();
        return bounds.getY() + bounds.getHeight()/2;
    } 
}


