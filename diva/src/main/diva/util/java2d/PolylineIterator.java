/*
 * $Id: PolylineIterator.java,v 1.3 2001/07/22 22:02:08 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.util.java2d;

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/** An iterator over Polyline2D. This class is private
 * to this package.
 *
 * @version	$Revision: 1.3 $
 * @author 	John Reekie
 */
public class PolylineIterator implements PathIterator {

    /** The transformed coordinates being iterated.
     */
    private double[] _coords;

    /** The current coordinate index.
     */
    private int _index = 0;

    /** Create a new iterator over the given polyline and
     * with the given transform. If the transform is null,
     * that is taken to be the same as a unit Transform.
     */
    public PolylineIterator (Polyline2D pl, AffineTransform at) {
        int count = pl.getVertexCount() * 2;
        _coords = new double[count];
         if (pl instanceof Polyline2D.Float) {
            Polyline2D.Float f = (Polyline2D.Float) pl;
            if (at == null || at.isIdentity()) {
                for (int i = 0; i < count; i++) {
                    _coords[i] = (double) f._coords[i];
                }
            } else {
                at.transform(f._coords, 0, _coords, 0, count/2);
            }
        } else {
            Polyline2D.Double d = (Polyline2D.Double) pl;
            if (at == null || at.isIdentity()) {
                System.arraycopy(d._coords, 0, _coords, 0, count);
            } else {
                at.transform(d._coords, 0, _coords, 0, count/2);
            }
        }
    }

    /** Get the current segment
     */
    public int currentSegment (double coords[]) {
        coords[0] = this._coords[_index];
        coords[1] = this._coords[_index+1];
        return ((_index == 0) 
                ? PathIterator.SEG_MOVETO 
                : PathIterator.SEG_LINETO);
    }

    /** Get the current segment
     */
    public int currentSegment (float coords[]) {
        coords[0] = (float) this._coords[_index];
        coords[1] = (float) this._coords[_index+1];
        return ((_index == 0) 
                ? PathIterator.SEG_MOVETO 
                : PathIterator.SEG_LINETO);
     }

    /** Return the winding rule. This is WIND_NON_ZERO.
     */
    public int getWindingRule () {
        return PathIterator.WIND_NON_ZERO;
    }

    /** Test if the iterator is done.
     */
    public boolean isDone () {
        return (_index >= _coords.length);
    }

    /** Move the iterator along by one point.
     */
    public void next () {
        _index+=2;
    }
}


