/*
 * $Id: BasicSelectionRenderer.java,v 1.14 2001/07/22 22:00:37 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.canvas.interactor;

import diva.canvas.Figure;
import diva.canvas.FigureContainer;
import diva.canvas.FigureDecorator;
import diva.canvas.ZList;
import diva.canvas.toolbox.BasicHighlighter;

import java.util.Hashtable;
import java.util.Iterator;

/** A basic implementation of a selection renderer. This
 * implementation wraps each selected figure in an instance of
 * a FigureDecorator. The figure decorator is obtained by
 * cloning a prototype decorator, accessible through the
 * get/setFigureDecorator() methods. The default prototype
 * is an instance of BasicHighlighter. Often, the prototype
 * decorator will be set to instances of one of the Manipulator
 * classes.
 *
 * @version	$Revision: 1.14 $
 * @author 	John Reekie
 */
public class BasicSelectionRenderer implements SelectionRenderer {

    /** Mapping from figures to decorators
     */
    private Hashtable _decorators = new Hashtable();

    /** The prototype decorator
     */
    private FigureDecorator _prototypeDecorator;
    
    /** Create a new selection renderer with the default prototype
     * decorator.
     */
    public BasicSelectionRenderer () {
        _prototypeDecorator = new BasicHighlighter();
    }

    /** Create a new renderer with the given prototype decorator.
     */
    public BasicSelectionRenderer (FigureDecorator d) {
        _prototypeDecorator = d;
    }

    /** Get the prototype decorator.
     */
    public FigureDecorator getDecorator () {
        return _prototypeDecorator;
    }

    /** Test if the given figure is currently rendered selected.
     */
    public boolean isRenderedSelected (Figure figure) {
        return _decorators.containsKey(figure);
    }

    /** Set the rendering of the figure as deselected. The figure has
     * the decorator unwrapped off it and is inserted back into
     * its parent figure container, if there is one. If the figure is
     * not rendered selected, do nothing.
     */
    public void renderDeselected (Figure figure) {
        if ( !_decorators.containsKey(figure)) {
            return;
        }
	// Rather than just get the parent of the figure, we must get
	// the decorator out of the hashtable, since other wrappers
	// may have been inserted between the figure and its
	// decorator
	FigureDecorator d = (FigureDecorator)_decorators.get(figure);
	if (d.getParent() != null) {
            figure.repaint();
            ((FigureContainer) d.getParent()).undecorate(d);
	}
	_decorators.remove(figure);
    }

    /** Set the rendering of the figure as selected. If the figure is
     * already rendered selected, just repaint. Otherwise create a new
     * BasicHighlighter, and wrap the figure in the decorator,
     * inserting the decorator into the figure's parent.
     */
    public void renderSelected (Figure figure) {
        if (_decorators.containsKey(figure)) {
            ((Figure)_decorators.get(figure)).repaint();
        } else {
            FigureContainer parent = (FigureContainer) figure.getParent();
            if (parent != null) {
                FigureDecorator d = _prototypeDecorator.newInstance(figure);
                parent.decorate(figure,d);
                _decorators.put(figure,d);
            }
	}
    }

    /** Set the prototype decorator.
     */
    public void setDecorator (FigureDecorator d) {
        _prototypeDecorator = d;
    }
}


