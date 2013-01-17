/*
 * $Id: GraphUtilities.java,v 1.15 2001/07/22 22:01:17 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */

package diva.graph;
import java.util.AbstractSequentialList;
import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;
import diva.util.IteratorAdapter;
import diva.util.CompoundIterator;
import diva.util.FilteredIterator;
import diva.util.Filter;

/**
 * A set of utilities for traversing/manipulating/etc. graphs.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.15 $
 * @rating Red
 */
public final class GraphUtilities {

    //You can't instantiate this class
    private GraphUtilities() {}


    /**
     * Check to make sure that all nodes and edges
     * are self-consistent within a graph.
     */
    public static final boolean checkConsistency(Object composite,
            GraphModel model) {
        for(Iterator i = model.nodes(composite); i.hasNext(); ) {
            Object node = i.next();
            for(Iterator j = model.outEdges(node); j.hasNext(); ) {
                Object edge = j.next();
                if((edge == null) || (model.getHead(edge) == null)) {
                    return false;
                }
            }
            for(Iterator j = model.inEdges(node); j.hasNext(); ) {
                Object edge = j.next();
                if((edge == null) || (model.getTail(edge) == null) ){
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Check to make sure that all nodes and edges
     * are contained with in a graph and are consistent.
     */
    public static final boolean checkContainment(Object composite, 
						 GraphModel model) {
        if(!checkConsistency(composite, model)) {
            return false;
        }

        for(Iterator i = model.nodes(composite); i.hasNext(); ) {
            Object node = i.next();
            if(model.getParent(node) != composite) {
                return false;
            }
            for(Iterator j = model.outEdges(node); j.hasNext(); ) {
                Object edge = j.next();
                Object head = model.getHead(edge);
                if(model.getParent(head) != composite) {
                    return false;
                }
            }
            for(Iterator j = model.inEdges(node); j.hasNext(); ) {
                Object edge = j.next();
                Object tail = model.getTail(edge);
                if(model.getParent(tail) != composite) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Return a new set that contains any edges that are connected
     * to any nodes in the given composite.
     */
    public static final Set edgeSet(Object composite, 
				    GraphModel model) {	
	if(model.isComposite(composite)) {
	    Set nodeSet = nodeSet(composite, model);
	    Set set = new HashSet();
	    Iterator nodes = nodeSet.iterator();
	    while(nodes.hasNext()) {
		Object node = nodes.next();
		Iterator i;
		i = model.outEdges(node);
		while(i.hasNext()) {
		    set.add(i.next());
		}
		i = model.inEdges(node);
		while(i.hasNext()) {
		    set.add(i.next());
		}
	    }
	    return set;
	} else {
	    return new HashSet();
	}
    }

    /* Return true if the given node is
     * deeply contained within the given composite.
     * @param node A node in the given model.
     * @param composite A composite in the given model.
     * @param model The graph model.
     */
    public static boolean isContainedNode(Object node, 
				      Object composite, 
				      GraphModel model) {
	if(model.isNode(node)) {
	    boolean isOK = false;
	    Object parent = model.getParent(node);
	    while(!isOK && parent != null) {
		if(parent == composite) {
		    isOK = true;
		} else if(model.isNode(node)) {
		    parent = model.getParent(parent);
		} else {
		    parent = null;
		}
	    }
	    return isOK;
	} else {
	    return false;
	}
    }          
  
    /* Return true if the given edge is
     * totally contained within the given composite.  i.e. either it's
     * head an tail nodes are deeply contained in the composite.
     * @param edge An edgee in the given model.
     * @param composite A composite in the given model.
     * @param model The graph model.
     */
    public static boolean isPartiallyContainedEdge(Object edge, 
						 Object composite, 
						 GraphModel model) {
	if(model.isEdge(edge)) {
	    Object head = model.getHead(edge);
	    Object tail = model.getTail(edge);
	    
	    boolean headIsOK = isContainedNode(head, composite, model);
	    boolean tailIsOK = isContainedNode(tail, composite, model);
	    
	    return headIsOK || tailIsOK;
	} else {
	    return false;
	}
    }

    /* Return true if the given edge is
     * totally contained within the given composite.  i.e. both it's
     * head an tail nodes are deeply contained in the composite.
     * @param edge An edgee in the given model.
     * @param composite A composite in the given model.
     * @param model The graph model.
     */
    public static boolean isTotallyContainedEdge(Object edge, 
						 Object composite, 
						 GraphModel model) {
	if(model.isEdge(edge)) {
	    Object head = model.getHead(edge);
	    Object tail = model.getTail(edge);
	   
	    if(head == null || tail == null) return false;
	    
	    boolean headIsOK = isContainedNode(head, composite, model);
	    boolean tailIsOK = isContainedNode(tail, composite, model);
	    
	    return headIsOK && tailIsOK;
	} else {
	    return false;
	}
    }            
    
    /**
     * Return an iterator over the nodes on the
     * other side of edges arriving in the given node.
     */
    public static final Iterator inNodes(Object node, GraphModel model) {
        final GraphModel m = model;
        return new diva.util.ProxyIterator(model.inEdges(node)) {
            public Object next() {
                Object edge = super.next();
                return m.getTail(edge);
            }
        };
    }

   /** Return a new set that contains all the nodes that are deeply
     * contained in the given composite.
     */
    public static final Set nodeSet(Object composite, 
				    GraphModel model) {	
	if(model.isComposite(composite)) {
	    Set set = new HashSet();
	    Iterator i = model.nodes(composite);
	    while(i.hasNext()) {
		Object node = i.next();
		set.add(node);
		if(model.isComposite(node)) {
		    set.addAll(nodeSet(node, model));
		} 
	    }
	    return set;
	} else {
	    return new HashSet();
	}
    }

    /**
     * Return an iterator over the nodes on the
     * other side of edges emanating from the given node.
     */
    public static final Iterator outNodes(Object node, GraphModel model) {
        final GraphModel m = model;
        return new diva.util.ProxyIterator(model.outEdges(node)) {
            public Object next() {
                Object edge = super.next();
                return m.getHead(edge);
            }
        };
    }

    /**
     * Disconnect all of the edges connected to the given node, then
     * remove it from its graph.
     *
     * @throws GraphException if the operation fails.
     */
    public static final void purgeNode(Object eventSource, Object node, 
				       MutableGraphModel model)
            throws GraphException {

        for(Iterator i = model.outEdges(node); i.hasNext(); ) {
            model.disconnectEdge(eventSource, i.next());
        }
        for(Iterator i = model.inEdges(node); i.hasNext(); ) {
            model.disconnectEdge(eventSource, i.next());
        }
        model.removeNode(eventSource, node);
     }

    /**
     * Return an iterator over the edges in a graph
     * which are partially contained within the given composite node.
     * (i.e. the edges whose head node, or tail node is in the
     * composite, or a subnode).
     */
    public static final Iterator partiallyContainedEdges(Object composite, 
					    GraphModel model) {
        return new FilteredIterator(edgeSet(model.getRoot(), model).iterator(),
				    new PartiallyContainedEdgeFilter(model,
								     composite));
    }

    /**
     * Return an iterator over the edges in a graph
     * which are totally contained within the given composite node.
     * (i.e. the edges whose head and tail nodes are both in the
     * composite, or a subnode).
     */
    public static final Iterator totallyContainedEdges(Object composite, 
					    GraphModel model) {
        return new FilteredIterator(edgeSet(model.getRoot(), model).iterator(),
				    new TotallyContainedEdgeFilter(model, 
								   composite));
    }

    /**
     * Return an iterator over the edges in a graph
     * which are local to that graph (i.e. the edges
     * whose head and tail nodes are both in the
     * graph, or a subgraph)
     * @deprecated use totallyContainedEdges instead.
     */
    public static final Iterator localEdges(Object composite, 
					    GraphModel model) {
	return totallyContainedEdges(composite, model);
    }
    
    // Filter all edges that don't have at least one end contained in the 
    // given composite.
    private static final class PartiallyContainedEdgeFilter implements Filter {
        private GraphModel _model;
        private Object _composite;

        public PartiallyContainedEdgeFilter(GraphModel model, 
					    Object composite) {
            _model = model;
            _composite = composite;
        }

        public boolean accept(Object o) {
	    return isPartiallyContainedEdge(o, _composite, _model);
        }
    }

    // Filter all edges that don't have both ends contained in the 
    // given composite.
    private static final class TotallyContainedEdgeFilter implements Filter {
        private GraphModel _model;
        private Object _composite;

        public TotallyContainedEdgeFilter(GraphModel model, Object composite) {
            _model = model;
            _composite = composite;
        }

        public boolean accept(Object o) {
	    return isTotallyContainedEdge(o, _composite, _model);
        }
    }
}


