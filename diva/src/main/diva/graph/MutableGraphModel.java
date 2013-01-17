/*
 * $Id: MutableGraphModel.java,v 1.9 2001/07/22 22:01:18 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;
import java.util.Iterator;
import diva.util.SemanticObjectContainer;
import diva.util.PropertyContainer;

/**
 * A mutable graph model is a read-write subclass of the read-only
 * graph model, allowing users to actually create new nodes and
 * edges, and to modify the topology of the graph.
 * 
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @version $Revision: 1.9 $
 * @rating Yellow
 */
public interface MutableGraphModel extends GraphModel {
    /**
     * Return true if the head of the given edge can be attached to the
     * given node.
     */
    public boolean acceptHead(Object edge, Object node);

    /**
     * Return true if the tail of the given edge can be attached to the
     * given node.
     */
    public boolean acceptTail(Object edge, Object node);

    /**
     * Add a node to the given graph and notify listeners with a
     * NODE_ADDED event whose source is the given source object. <p>
     *
     * @param eventSource The source of the event that will be dispatched, e.g.
     *                    the view that made this call.
     * @exception GraphException if the operation fails.
     */
    public void addNode(Object eventSource, Object node, Object parent) 
	throws GraphException;

    /**
     * Connect the given edge to the given tail and head nodes,
     * and notify listeners with events whose source is the given
     * eventSource object
     *
     * @param eventSource The source of the event that will be dispatched, e.g.
     *                    the view that made this call.
     * @exception GraphException if the operation fails.
     */
    public void connectEdge(Object eventSource, Object edge, 
			    Object tailNode, Object headNode)
	throws GraphException;

    /**
     * Disconnect an edge from its two enpoints and notify graph
     * listeners with an EDGE_HEAD_CHANGED and an EDGE_TAIL_CHANGED
     * event whose source is the given source.
     *
     * @param eventSource The source of the event that will be dispatched, e.g.
     *                    the view that made this call.
     * @exception GraphException if the operation fails.
     */
    public void disconnectEdge(Object eventSource, Object edge)
	throws GraphException;
	
    /**
     * Delete a node from its parent graph and notify
     * graph listeners with a NODE_REMOVED event.
     *
     * @param eventSource The source of the event that will be dispatched, e.g.
     *                    the view that made this call.
     * @exception GraphException if the operation fails.
     */
    public void removeNode(Object eventSource, Object node)
	throws GraphException;

    /**
     * Connect an edge to the given head node and notify listeners
     * with an EDGE_HEAD_CHANGED event whose source is the given
     * eventSource object.
     *
     * @param eventSource The source of the event that will be dispatched, e.g.
     *                    the view that made this call.
     * @exception GraphException if the operation fails.
     */
    public void setEdgeHead(Object eventSource, Object edge, Object newHead) 
	throws GraphException;

    /**
     * Connect an edge to the given tail node and notify listeners
     * with an EDGE_TAIL_CHANGED event whose source is the given
     * eventSource object.
     *
     * @param eventSource The source of the event that will be dispatched, e.g.
     *                    the view that made this call.
     * @exception GraphException if the operation fails.
     */
    public void setEdgeTail(Object eventSource, Object edge, Object newTail)
	throws GraphException;
}

