/*
 * $Id: ConnectorEnd.java,v 1.4 2001/07/22 22:00:31 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** An interface for objects that can be attached to the end
 * of a connector. Implementations of this interface are used
 * to draw arrow-heads, circles, diamonds, and various other
 * kinds of decoration at the end of connectors.
 *
 * @version $Revision: 1.4 $
 * @author  John Reekie (johnr@eecs.berkeley.edu)
 */
public interface ConnectorEnd {

    /** Get the bounding box of the shape used to draw
     * this connector end.
     */
    public Rectangle2D getBounds ();

    /** Get the connection point of the end. The given point is
     * modified with the location to which the connector should
     * be drawn.
     */
    public void getConnection (Point2D p);

    /** Get the origin of the line end. The given point is
     * modified.
     */
    public void getOrigin (Point2D p);

   /** Paint the connector end. This method assumes that
    * the graphics context is already set up with the correct
    * paint and stroke.
     */
    public void paint (Graphics2D g);

    /** Set the normal of the connector end. The argument is the
     * angle in radians away from the origin.
     */
    public void setNormal (double angle);

    /** Set the origin of the decoration.
     */
    public void setOrigin (double x, double y);

    /** Translate the connector end by the given amount.
     */
    public void translate (double x, double y);
}


