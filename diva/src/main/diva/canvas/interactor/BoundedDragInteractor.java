/*
 * $Id: BoundedDragInteractor.java,v 1.6 2001/07/22 22:00:37 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.canvas.interactor;

import diva.canvas.Figure;
import diva.canvas.event.LayerEvent;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** An interactor that drags its target only within a given
 * rectangular region. An instance of this class sets itself up so
 * that the dragged figures or items always remain within the given
 * region. To do so, it creates an instance of BoundsConstraint that
 * it attaches to itself, and overrides the setup() method to
 * initialize the constraint according to the size of the target.
 *
 * <p> This interactor is intended more as an example of how to
 * produce a customized drag-interactor than anything else.
 *
 * @version $Revision: 1.6 $
 * @author John Reekie
 */
public class BoundedDragInteractor extends DragInteractor {

    /** The bounds
     */
    private Rectangle2D _bounds;

    /** The bounds constraint
     */
    private BoundsConstraint _constraint;

    /**
     * Create an instance that keeps figures inside the given regio
     */
    public BoundedDragInteractor (Rectangle2D bounds) {
        super();
        _bounds = bounds;
        _constraint = new BoundsConstraint(_bounds);
        appendConstraint(_constraint);
    }

    /** Adjust the bounds so that the bounding-box of the target stays
     * within the region.
     */
    public void setup (LayerEvent e) {
        // Get the size of the figure and calculate bounds
        // FIXME: how to parameterize for figure sets?
        Figure f = e.getFigureSource();
        double ex = e.getLayerX();
        double ey = e.getLayerY();
        Rectangle2D b = f.getBounds();

        double x = _bounds.getX() + ex - b.getX();
        double y = _bounds.getY() + ey - b.getY();
            
        double w = _bounds.getX() + _bounds.getWidth() +
            ex - (b.getX() + b.getWidth()) -
            x;
        double h = _bounds.getY() + _bounds.getHeight() +
            ey - (b.getY() + b.getHeight()) -
            y;

        // Finally (!), set the bounds constraint
        _constraint.setBounds(new Rectangle2D.Double(x,y,w,h));
    }
}




