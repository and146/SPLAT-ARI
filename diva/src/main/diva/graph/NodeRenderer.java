/*
 * $Id: NodeRenderer.java,v 1.10 2001/07/22 22:01:18 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;
import diva.canvas.Figure;

/**
 * A factory which creates a visual representation (Figure)
 * given a node input.  The factory is not responsible for
 * positioning the figure, but is responsible for determining
 * the size of the figure.
 *
 * @author  Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.10 $
 * @rating  Red
 */
public interface NodeRenderer {
    /**
     * Render a visual representation of the given node.
     */
    public Figure render(Object node);
}


