/*
 * $Id: PaintedFigure.java,v 1.10 2002/08/12 06:34:15 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.toolbox;

import diva.canvas.Figure;
import diva.canvas.AbstractFigure;
import diva.canvas.interactor.ShapedFigure;

import diva.util.java2d.PaintedObject;
import diva.util.java2d.PaintedList;
import diva.util.java2d.ShapeUtilities;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.Rectangle2D;

import diva.util.java2d.PaintedShape;

/**
 * A PaintedFigure is contains an arbitrary set of PaintedObjects
 * in a list. This figure can be used for constructing more complex
 * figures than classes such as BasicFigure. It contains a transform
 * that is used for scaling the contained painted objects. (Note
 * that, if using this class to construct a figure from an external
 * source, the initial transform should operate on the coordinates
 * only.) 
 *
 * @version	$Revision: 1.10 $
 * @author 	John Reekie
 * @author      Nick Zamora
 * @deprecated Will be removed in Diva 0.4. Use diva.compat.canvas if needed.
 */
public class PaintedFigure extends AbstractFigure {

    /** The color compositing operator
     */
    private Composite _composite = AlphaComposite.SrcOver; // opaque
    
    /** The transform for the internals
     */
    private AffineTransform _transform = new AffineTransform();
    
     /** The list containing the objects that we paint
     */
    private PaintedList _paintedList;

    /** Create a new blank figure.
     */
    public PaintedFigure () {
        super();
        _paintedList = new PaintedList();
    }

    /** Create a new figure that paints itself using the given PaintedList.
     */
    public PaintedFigure (PaintedList objects) {
        super();
        _paintedList = objects;
    }

    /** Add a new painted object to the objects displayed
     * by this figure.
     */
    public void add (PaintedObject po) {
        _paintedList.add(po);
        repaint();
    }

    /** Get the painted list of painted objects of this figure.
     */
    public PaintedList getPaintedList() {
        return _paintedList;
    }

    /** Get the bounding box of this figure.
     */
    public Rectangle2D getBounds () {
        Rectangle2D bounds = (Rectangle2D) ShapeUtilities.transformModify(
                _paintedList.getBounds(), _transform);
        return bounds;
    }

    /** Return the origin, which is the point relative to which all of the
     *  contained objects are drawn.
     *  @return The origin.
     */
    public Point2D getOrigin () {
        return new Point2D.Double(
                _transform.getTranslateX(), _transform.getTranslateY());
    }

    /** Get the shape of this figure. This is the same as the
     * bounding box.
     */
    public Shape getShape () {
        return getBounds();        
    }

    /** Get the color composition operator of this figure.
     */
    public Composite getComposite () {
        return _composite;
    }

    /** Paint the figure.
     */
    public void paint (Graphics2D g) {
        if (!isVisible()) {
             return;
        }
        if (_composite != null) {
            g.setComposite(_composite);
        }
        AffineTransform savedTransform = g.getTransform();
        g.transform(_transform);
        _paintedList.paint(g);
        g.setTransform(savedTransform);
    }

    /** Set the color composition operator of this figure. If the
     * composite is set to null, then the composite will not be
     * changed when the figure is painted. By default, the composite
     * is set to opaque.
     */
    public void setComposite (Composite c) {
        _composite = c;
        repaint();
    }

    /** Transform the figure with the supplied transform. This can be
     * used to perform arbitrary translation, scaling, shearing, and
     * rotation operations.
     */
    public void transform (AffineTransform at) {
        repaint();
        _transform.preConcatenate(at);
	repaint();
    }
}





