/*
 * $Id: LabelWrapper.java,v 1.3 2001/07/22 22:00:45 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.toolbox;

import diva.canvas.Figure;
import diva.canvas.AbstractFigure;
import diva.canvas.CanvasUtilities;

import diva.util.NullIterator;
import diva.util.UnitIterator;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import java.util.Iterator;
import javax.swing.SwingConstants;

/** A LabelWrapper is a figure that attaches a label to some other figure.
 * The location at which the label is attached can (in this class) be
 * set to the center or to any of the four edges or corners of the
 * boundig box. (Maybe later we'll figure out a way to have the label
 * locate at a site.) In addition, the anchor and padding attributes
 * of the figure itself can be used to adjust the label location relative
 * to the anchoring point on the main figure.
 *
 * <P> Note that this class is intended for use in simple applications
 * where a simple label is attached to something. For more complex
 * applications, such as attaching multiple labels, you will need
 * to implement your own class.
 *
 * @version	$Revision: 1.3 $
 * @author John Reekie
 */
public class LabelWrapper extends AbstractFigure {

    /** The child
     */
    private Figure _child = null;
    
    /** The label
     */
    private LabelFigure _label = null;
    
    /** The label anchor
     */
    private int _anchor = SwingConstants.CENTER;
    
    /** Construct a new figure with the given child figure and
     * the given string.
     */
    public LabelWrapper (Figure f, String label) {
        _child = f;
        f.setParent(this);

        _label = new LabelFigure(label);
        Point2D pt = CanvasUtilities.getLocation(_child.getBounds(), _anchor);
        _label.translateTo(pt);
    }

    /** Get the bounds of this figure.
     */
    public Rectangle2D getBounds () {
        Rectangle2D bounds = _child.getBounds();
        Rectangle2D.union(bounds, _label.getBounds(), bounds);
        return bounds;
    }
 
    /** Get the child figure
     */
    public Figure getChild () {
        return _child;
    }
 
    /** Get the label. This can be used to adjust the label
     * appearance, anchor, and so on.
     */
    public LabelFigure getLabel () {
        return _label;
    }
 
    /** Get the shape of this figure. This is the shape
     * of the child figure only -- the label is not included
     * in the shape.
     */
    public Shape getShape () {
        return _child.getShape();
    }

    /** We are hit if either the child or the figure is hit.
     */
    public boolean hit (Rectangle2D r) {
        return _child.hit(r) || _label.hit(r);
    }

    /** Paint this figure
     */
    public void paint (Graphics2D g) {
        if (_child != null && isVisible()) {
            _child.paint(g);
            _label.paint(g);
        }
    }

    /** Set the anchor of the label. The anchor is the position on
     * the child figure at which the label will be located.
     * It can be any of the positioning constants defined
     * in SwingConstants.
     */
    public void setAnchor (int anchor) {
        this._anchor = anchor;
        Point2D pt = CanvasUtilities.getLocation(_child.getBounds(), anchor);
        repaint();
        _label.translateTo(pt);
        repaint();
    }

    /** Transform the figure with the supplied transform.
     */
    public void transform (AffineTransform at) {
        repaint();
        _child.transform(at);
        Point2D pt = CanvasUtilities.getLocation(_child.getBounds(), _anchor);
        _label.translateTo(pt);
        repaint();
    }
}


