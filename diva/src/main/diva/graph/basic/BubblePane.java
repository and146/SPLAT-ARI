/*
 * $Id: BubblePane.java,v 1.3 2001/07/22 22:01:20 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.basic;

import diva.graph.*;

/**
 * A pane containing a bubble-and-arc editor.
 *
 * @author 	John Reekie (johnr@eecs.berkeley.edu)
 * @version	$Revision: 1.3 $
 * @rating Red
 */
public class BubblePane extends GraphPane {
    /**
     * Create a BubblePane
     */
    public BubblePane () {
        super(new BubbleGraphController(), new BasicGraphModel());

        // More halo
        getForegroundLayer().setPickHalo(4.0);
    }
}


