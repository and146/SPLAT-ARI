/*
 * $Id: GraphEventMulticaster.java,v 1.3 2001/07/22 22:01:26 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.toolbox;
import diva.graph.GraphListener;
import diva.graph.GraphEvent;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

/**
 * A list of GraphListeners which is smart enough to call the correct
 * methods on these listeners given a GraphEvent's ID.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.3 $
 * @rating Yellow
 */
public class GraphEventMulticaster implements GraphListener {
    /**
     * The list of graph listeners.
     */
    private List _listeners;

    /**
     * Create an empty multicaster object.
     */
    public GraphEventMulticaster() {
        _listeners = new LinkedList();
    }

    /**
     * Add the given listener to the list of listeners.
     */
    public void add(GraphListener l) {
        _listeners.add(l);
    }

    /**
     * Dispatch an event to the list of listeners, calling
     * the appropriate method based on the event's ID.
     */
    public void dispatchEvent(GraphEvent e) {
        switch(e.getID()) {
            case GraphEvent.EDGE_HEAD_CHANGED:
                edgeHeadChanged(e);
                break;
            case GraphEvent.EDGE_TAIL_CHANGED:
                edgeTailChanged(e);
                break;
            case GraphEvent.NODE_ADDED:
                nodeAdded(e);
                break;
            case GraphEvent.NODE_REMOVED:
                nodeRemoved(e);
                break;
            case GraphEvent.STRUCTURE_CHANGED:
                structureChanged(e);
                break;
        }
    }


    /**
     * Dispatch the edgeHeadChanged() event to the
     * listeners.
     */
    public void edgeHeadChanged(GraphEvent e) {
        for(Iterator i = listeners(); i.hasNext(); ) {
            GraphListener l = (GraphListener)i.next();
            l.edgeHeadChanged(e);
        }
    }

    /**
     * Dispatch the edgeTailChanged() event to the
     * listeners.
     */
    public void edgeTailChanged(GraphEvent e) {
        for(Iterator i = listeners(); i.hasNext(); ) {
            GraphListener l = (GraphListener)i.next();
            l.edgeTailChanged(e);
        }
    }
    
    /**
     * Return an iterator over the list of listeners.
     */
    public Iterator listeners() {
        return _listeners.iterator();
    }

    /**
     * Dispatch the nodeAdded() event to each of the listeners.
     */
    public void nodeAdded(GraphEvent e) {
        for(Iterator i = listeners(); i.hasNext(); ) {
            GraphListener l = (GraphListener)i.next();
            l.nodeAdded(e);
        }
    }

    /**
     * Dispatch the nodeRemoved() event to each of the listeners.
     */
    public void nodeRemoved(GraphEvent e) {
        for(Iterator i = listeners(); i.hasNext(); ) {
            GraphListener l = (GraphListener)i.next();
            l.nodeRemoved(e);
        }
    }

    /**
     * Remove the given listener from the list
     * of listeners.
     */
    public void remove(GraphListener l) {
        _listeners.remove(l);
    }

    /**
     * Dispatch the structureChanged() event to each of the listeners.
     */
    public void structureChanged(GraphEvent e) {
        for(Iterator i = listeners(); i.hasNext(); ) {
            GraphListener l = (GraphListener)i.next();
            l.structureChanged(e);
        }
    }
}


