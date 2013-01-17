/*
 * $Id: CompositeNode.java,v 1.3 2001/07/22 22:01:23 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.modular;

/**
 * A node that is also a graph, i.e.<!-- -->it can contain other nodes.
 * 
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.3 $
 * @rating Red
 */
public interface CompositeNode extends Node, Graph {
}

