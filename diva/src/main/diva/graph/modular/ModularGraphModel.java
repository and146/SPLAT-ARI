/*
 * $Id: ModularGraphModel.java,v 1.14 2002/05/19 21:45:49 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.modular;
import diva.util.ArrayIterator;
import diva.graph.*;
import diva.graph.toolbox.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import diva.util.SemanticObjectContainer;
import diva.util.PropertyContainer;

/**
 * A modular implementation of the graph model, whereby users with
 * heterogeneous graphs can implement the graph model interface by
 * implementing the simple interfaces of Graph, Node, CompositeNode,
 * and Edge.
 * 
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.14 $
 * @rating Red
 */
public abstract class ModularGraphModel extends AbstractGraphModel {

    /**
     * The root of the graph contained by this model.
     */
    private Object _root = null;

    /**
     * Construct an empty graph model whose
     * root is the given semantic object.
     */
    public ModularGraphModel(Object root) {
        _root = root;
    }

    /**
     * Return true if this composite node contains the given node.
     */
    public boolean containsNode(Object composite, Object node) {
	return composite.equals(getNodeModel(node).getParent(node));
    }

    /** 
     * Return the model for the given composite object.  If the object is not
     * a composite, meaning that it does not contain other nodes, 
     * then return null.
     */
    public abstract CompositeModel getCompositeModel(Object composite);

    /** 
     * Return the model for the given edge object.  If the object is not
     * an edge, then return null.
     */
    public abstract EdgeModel getEdgeModel(Object edge);

    /**
     * Return the head node of the given edge.
     */
    public Object getHead(Object edge) {
	return getEdgeModel(edge).getHead(edge);
    }
		
    /**
     * Return the number of nodes contained in
     * this graph or composite node.
     */
    public int getNodeCount(Object composite) {
	return getCompositeModel(composite).getNodeCount(composite);
    }

    /** 
     * Return the node model for the given object.  If the object is not
     * a node, then return null.
     */
    public abstract NodeModel getNodeModel(Object node);
	
    /**
     * Return the parent graph of this node, return
     * null if there is no parent.
     */
     public Object getParent(Object node) {	
	if(node == _root) {
            return null;
        } else {
            return getNodeModel(node).getParent(node);
        }
    }

    /**
     * Return the property of the object associated with
     * the given property name.
     */
    public abstract Object getProperty(Object o, String propertyName);

    /**
     * Return the root graph of this graph model.
     */
    public Object getRoot() {
        return _root;
    }

    /**
     * Return the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public abstract Object getSemanticObject(Object o);
    
    /**
     * Return the tail node of this edge.
     */
    public Object getTail(Object edge) {
	return getEdgeModel(edge).getTail(edge);
    }

    /**
     * Return whether or not this edge is directed.
     */
    public boolean isDirected(Object edge) {
	return getEdgeModel(edge).isDirected(edge);
    }

    /**
     * Return true if the given object is a composite
     * node in this model, i.e. it contains children.
     */
    public boolean isComposite(Object o) {
        return getCompositeModel(o) != null;
    }

    /**
     * Return true if the given object is a 
     * node in this model.
     */
    public boolean isEdge(Object o) {
        return getEdgeModel(o) != null;
    }

    /**
     * Return true if the given object is a 
     * node in this model.
     */
    public boolean isNode(Object o) {
        return getNodeModel(o) != null;
    }

    /**
     * Provide an iterator over the nodes in the
     * given graph or composite node.  This iterator
     * does not necessarily support removal operations.
     */
    public Iterator nodes(Object composite) {
	return getCompositeModel(composite).nodes(composite);
    }

    /**
     * Return an iterator over the <i>in</i> edges of this
     * node. This iterator does not support removal operations.
     * If there are no in-edges, an iterator with no elements is
     * returned.
     */
    public Iterator inEdges(Object node) {
	return getNodeModel(node).inEdges(node);
    }
    
    /**
     * Return an iterator over the <i>out</i> edges of this
     * node.  This iterator does not support removal operations.
     * If there are no out-edges, an iterator with no elements is
     * returned.
     */
    public Iterator outEdges(Object node) {
	return getNodeModel(node).outEdges(node);
    }

    /**
     * Set the property of the object associated with
     * the given property name.
     */
    public abstract void setProperty(Object o, String propertyName, 
				     Object value);
    
    /**
     * Set the semantic object correspoding
     * to the given node, edge, or composite.
     */
    public abstract void setSemanticObject(Object o, Object sem);
}

