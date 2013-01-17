/*
 * $Id: CanvasLayer.java,v 1.23 2001/12/10 22:35:18 neuendor Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas;

import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import diva.canvas.event.LayerEvent;

/** A canvas layer is a single layer that lives within a CanvasPane.
 * This is an abstract class -- concrete subclasses provide facilities
 * for drawing graphics or handling events.
 *
 * @version	$Revision: 1.23 $
 * @author John Reekie
 * @rating Yellow
 */
public abstract class CanvasLayer implements CanvasComponent {

    /** The pane containing this layer.
     */
    CanvasPane _containingPane;

    /** Create a new layer that is not in a pane. The layer will
     * not be displayed, and its coordinate tranformation will be
     * as though it were a one-to-one mapping. Use of this constructor
     * is strongly discouraged, as many of the geometry-related methods
     * expect to see a pane.
     */
    public CanvasLayer () {
        _containingPane = null;  // OK... be careful...
    }

    /** Create a new layer within the given pane.
     */
    public CanvasLayer (CanvasPane pane) {
        _containingPane = pane;
    }

    /** Get the pane containing this layer. This may be null.
     */
    public final CanvasPane getCanvasPane () {
        return _containingPane;
    }
  
    /** Get the bounds of the shapes draw in this layer.  In this base
     *  class, return an empty rectangle.
     */
    public Rectangle2D getLayerBounds () {
        return new Rectangle2D.Double();
    }        

    /** Get the parent component, or null if there isn't one.
     * This will return the same object as getCanvasPane().
     */
    public final CanvasComponent getParent () {
        return _containingPane;
    }

    /** Get the toolTipText for the point in the given MouseEvent.
     *  This works pretty much as regular event propagation in 
     *  processLayerEvent.
     */
    public String getToolTipText(LayerEvent e) {
        return null;
    }

    /** Return the transform context of the parent pane, if there is one.
     */
    public final TransformContext getTransformContext () {
        if (_containingPane == null) {
            return null;
        } else {
            return _containingPane.getTransformContext();
        }
    }

    /** Schedule a repaint of this layer. The layer passes
     * the repaint request to its containing pane, if there is one.
     * Otherwise it does nothing.
     */
    public void repaint () { 
        if (_containingPane != null) {
            _containingPane.repaint();
        }
    }

    /** Accept notification that a repaint has occurred somewhere
     * in this layer. Pass the notification up to the parent pane.
     */
    public void repaint (DamageRegion d) {
        if (_containingPane != null) {
            _containingPane.repaint(d);
	}
    }

    /** Set the parent component of this layer. This must be an
     * instance of CanvasPane.
     */
    public final void setParent (CanvasComponent parent) {
        if ( !(parent instanceof CanvasPane)) {
            throw new IllegalArgumentException(
                    "The component " +
                    parent +
                    " is not an instance of CanvasPane");
        }
        this._containingPane = (CanvasPane)parent;
    }
}


