/*
 * $Id: ZList.java,v 1.19 2001/07/22 22:00:30 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

/** A ZList is an interface for objects that contain an ordered list
 * of figures in z-order. In addition to the methods inherited from
 * FigureChildren, ZList has methods for reordering figures in the
 * list, and in the future may have methods for locating objects in 2D
 * space. This interface is used to isolate the implementation of
 * figure containers from the z-list, to allow future optimization of
 * the z-list implementation.
 * 
 * This interface tries to mimic the AWT Container and Swing
 * JLayerPane interfaces where possible. Unfortunately, these two
 * classes differ on the relation between list ordering and display
 * ordering, so we have chosen to use the AWT Container order
 * (high-numbered elements are displayed below lower-numbered
 * elements), since we thought it would make using the <b>add</b>
 * method less
 * error-prone.
 *
 * @version	$Revision: 1.19 $
 * @author John Reekie
 * @rating Yellow
 */
public interface ZList extends FigureSet {

    /** Add a figure to the list. This interface does not define
     * where the new figure will be in the display order (i.e. at the
     * top, bottom, or somewhere else), although implementations may
     * define this. Clients should assume that an implementation of
     * this method does <i>not</i> check if the figure is already
     * contained -- clients are therefore responsible for being
     * bug-free.
     */
    public void add (Figure f);

    /** Insert a figure at the given position. To insert the figure
     *  just in front of some other figure, use getIndex() to get the
     *  other figure's index, and pass <i>index</i> as the
     *  first argument. To insert the figure just behind some other
     *  figure, pass <i>index+1</i> as the first argument. To
     *  insert so the figure displays over the top of other figures,
     *  insert at zero.
     *
     *  <p>Clients should assume that an implementation of this method
     *  does <i>not</i> check if the figure is already contained.
     */
    public void add (int index, Figure f);

    /** Removes all of the figures from this list.
     */
    public void clear();

    /** Test if this list contains the given figure. As a general
     * rule, the implementation of this method is not required to be
     * efficient -- O(n) in the length of the list is acceptable.
     * Clients should note that, in general, a much better way
     * of making this same test is to check if the parent of the figure
     * is the same object as this list.
     */
    public boolean contains (Figure f);

    /** Return the figure at the given index.
     *
     * @throws IndexOutOfBoundsException The index is out of range.
     */
    public Figure get (int index);
  
    /** Get the bounding box of all the figures in this list.
     */
    public Rectangle2D getBounds ();
  
    /** Get the figures that are entirely contained by the given
     * region.
     */
    public GeometricSet getContainedFigures (Rectangle2D region);
  
    /** Get the figures with bounding boxes that intersect the given
     * region. Note that the returned set may contained figures
     * that do not actually intersect the region -- this method only
     * looks at the bounding boxes.
     */
    public GeometricSet getIntersectedFigures (Rectangle2D region);
  
    /** Return the index of the given figure in the Z-list. Figures
     *  with a higher index are drawn behind figures with a lower index.
     *
     * @return The index of the given figure, or -1 if the figure
     * is not in this list.
     */
    public int indexOf (Figure f);

    /** Remove the given figure from this list.
     */
    public void remove (Figure f);
    
    /** Remove the figure at the given index from this list.
     *
     * @throws IndexOutOfBoundsException The index is out of range.
     */
    public void remove (int index);
  
    /** Replace the figure at the given index with the passed-in
     * figure.
     */
  public void set (int index, Figure f);

    /** Set the index of the given figure. That is, move it in the
     * display list to the given position. To move the figure to just
     * in front of some other figure, use getIndex() to get the other
     * figure's index, and pass <i>index</i> as the first argument.
     * To move the figure to just behind some other figure, pass
     * <i>index+1</i> as the first argument. (Other figures will have
     * their indexes changed accordingly.)
     *
     * <p> Clients should assume that an implementation of this method
     * does <i>not</i> check if the figure is already contained --
     * clients are therefore responsible for being bug-free.
     *
     * @throws IndexOutOfBoundsException The new index is out of range.
     */
    public void setIndex (int index, Figure f);

    /** Return the number of elements in this list.
     */
    public int getFigureCount ();
}


