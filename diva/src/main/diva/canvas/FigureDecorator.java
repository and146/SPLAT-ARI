/*
 * $Id: FigureDecorator.java,v 1.9 2002/08/19 07:07:54 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas;

import diva.util.NullIterator;
import diva.util.UnitIterator;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;

import java.util.Iterator;

/** A FigureDecorator is a figure container that contains a single
 * child figure. The purpose of a FigureDecorator is to change or
 * affect the way in which the child is rendered, and so this
 * class behaves somewhat differently to other figures.
 *
 * <p> This class is a reasonable example of the Decorator design
 * pattern, hence its name.
 *
 * @version	$Revision: 1.9 $
 * @author John Reekie
 * @rating Red
 */
public abstract class FigureDecorator extends AbstractFigureContainer {

    /** The child
     */
    private Figure _child = null;

    /** Add a figure. This method does not make too much sense
     * for Decorators, but has to be here anyway. This method is
     * set same as calling setChild(f).
     */
    public void add (Figure f) {
        setChild(f);
    }
  
    /** Test if the given figure is the one contained by this decorator.
     */
    public boolean contains (Figure f) {
        return f == _child;
    }
  
    /** Return an iteration containing the one child.
     */
    public Iterator figures () {
        if (_child == null) {
            return new NullIterator();
        } else {
            return new UnitIterator(_child);
        }
    }

    /** Return an iteration containing the one child.
     */
    public Iterator figuresFromBack () {
        return figures();
    }

    /** Return an iteration containing the one child.
     */
    public Iterator figuresFromFront () {
        return figures();
    }

    /** Get the bounds of this figure, which is by default the
     * same as the child figure, if there is one, or a very small
     * rectangle if there isn't.
     */
    public Rectangle2D getBounds () {
        if (_child == null) {
            return new Rectangle2D.Double();
        } else {
            return _child.getBounds();
        }
    }
 
    /** Get the child figure, or null if there isn't one.
     */
    public Figure getChild () {
        return _child;
    }
 
    /** Get the container, which is defined as the lowest
     * ancestor that is not a FigureDecorator.
     */
    public FigureContainer getContainer () {
        if (getParent() instanceof FigureDecorator) {
            return ((FigureDecorator)getParent()).getContainer();
        } else {
            return (FigureContainer)getParent();
        }
    }

    /** Get the decorated figure, which is defined as the highest
     * descendent that is not a decorator.
     */
    public Figure getDecoratedFigure () {
        if (_child instanceof FigureDecorator) {
            return ((FigureDecorator)_child).getDecoratedFigure();
        } else {
            return _child;
        }
    }
 
    /** Return zero if there is no child, or one if there is.
     */
    public int getFigureCount () {
        if (_child == null) {
            return 0;
        } else {
            return 1;
        }
    }

    /** Get the outline shape of this figure, which is by default the
     * same as the child figure, if there is one, or a very small
     * rectangle if there isn't.
     */
    public Shape getShape () {
        if (_child == null) {
            return new Rectangle2D.Double();
        } else {
            return _child.getShape();
        }
    }
 
    /** Test if the child is hit.
     */
    public boolean hit (Rectangle2D r) {
        return _child.hit(r);
    }


    /** Create a new instance of this figure decorator, modelled
     * on this one. This is used by interaction code that needs to
     * dynamically create new manipulators. The figure argument can
     * be used by this method to initialize the new instance; however,
     * the new instance must <i>not</i> be wrapped around the figure,
     * since that should be done by the caller.
     */
    public abstract FigureDecorator newInstance (Figure f);

    /** Paint the figure. By default, this method simply forwards the
     * paint request to the contained figure.
     */
    public void paint (Graphics2D g) {
        if (_child != null) {
            _child.paint(g);
        }
    }

    /** Set the child figure. If there is already a child
     * figure, remove it from this container.
     */
    public void setChild (Figure f) {
        if (_child != null) {
            _child.repaint();
            _child.setParent(null);
        }
        _child = f;
        if (_child != null) {
            _child.setParent(this);
            _child.repaint();
        }
    }
    
    /** Remove a figure. This method does not make too much sense
     * for Decorators, but has to be here anyway. If the passed
     * figure is the same as the child figure, then this method
     * is the same as calling setChild(null). Otherwise, it does
     * nothing.
     */
    public void remove (Figure f) {
        if (_child == f) {
	    setChild(null);
	}
    }
  
    /** Replace the first figure, which must be a child, with the
     * second, which must not be a child.
     */
    protected void replaceChild (Figure child, Figure replacement) {
        _child = replacement;
    }

    /** Transform the figure with the supplied transform. By default,
     * this method simply forwards the paint request to the child
     * figure.
     */
    public void transform (AffineTransform at) {
        if (_child != null) {
            _child.transform(at);
        }
    }
}
