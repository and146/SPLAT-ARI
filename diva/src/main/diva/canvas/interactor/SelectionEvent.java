/*
 * $Id: SelectionEvent.java,v 1.5 2001/07/22 22:00:38 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.interactor;

import java.util.Iterator;
import diva.util.IteratorAdapter;
import diva.util.NullIterator;

/**
 * An event representing a change in the graph selection
 * model. The event contains all the information to mirror
 * the selection model.
 *
 * @author 	Michael Shilman (michaels@eecs.berkeley.edu)
 * @version	$Revision: 1.5 $
 */
public class SelectionEvent extends java.util.EventObject {
    /**
     * The objects added to the selection.
     * @serial
     */
    private Object[] _added;

    /**
     * The objects removed from the selection.
     * @serial
     */
    private Object[] _removed;

    /**
     * The primary selected object.
     * @serial
     */
    private Object _primary;

    /**
     * Construct a new Selection event from the
     * given source, representing the given selection
     * additions, removals, and primary selection.
     */
    public SelectionEvent(Object source, Object[] added, Object[] removed, Object primary) {
        this(source);
        set(added, removed, primary);
    }

    /**
     * Construct an empty Selection event from the
     * given source.
     */
    SelectionEvent(Object source) {
        super(source);
    }

    /**
     * Return an iterator over the objects
     * added to the selection model.
     */
    public Iterator getSelectionAdditions() {
        if(_added == null) {
            return new NullIterator();
        }
        else {
            return new IteratorAdapter() {
                int i = 0;
                public boolean hasNext() {
                    return (i < _added.length);
                }
                public Object next() {
                    return _added[i++];
                }
            };
        }
    }

    /**
     * Return an iterator over the objects
     * removed from the selection model.
     */
    public Iterator getSelectionRemovals() {
        if(_removed == null) {
            return new NullIterator();
        }
        else {
            return new IteratorAdapter() {
                int i = 0;
                public boolean hasNext() {
                    return (i < _removed.length);
                }
                public Object next() {
                    return _removed[i++];
                }
            };
        }
    }

    /**
     * Return the primary selection object.
     */
    public Object getPrimarySelection() {
        return _primary;
    }

    /**
     * Set the contents of the selection event.
     */
    void set(Object[] added, Object[] removed, Object primary) {
        _added = added;
        _removed = removed;
        _primary = primary;
    }
}


