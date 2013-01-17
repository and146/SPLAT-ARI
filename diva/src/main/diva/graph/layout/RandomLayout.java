/*
 * $Id: RandomLayout.java,v 1.9 2001/07/22 22:01:22 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.layout;
import diva.graph.GraphModel;
import java.util.Iterator;
import java.awt.geom.Rectangle2D;

/**
 * A static random layout engine.  This class tries to be smart by
 * not placing nodes on top of one another if possible, but doesn't
 * guarantee anything about the layout except that it will fall
 * into the required viewport.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.9 $
 * @rating Red
 */
public class RandomLayout extends AbstractGlobalLayout {
    /**
     * The number of iterations that it will try
     * to place a node not on top of other nodes
     * before it gives up.
     */
    private static final int NUM_ITER = 10;

    /**
     * Simple constructor.
     */
    public RandomLayout(LayoutTarget target) {
	super(target);
    }

    /**
     * Layout the graph model and viewport specified by the given
     * target environment.  Tries to be smart by not placing nodes on
     * top of one another if possible, but doesn't guarantee anything
     * about the layout except that it will fall into the required
     * viewport.
     */
    public void layout(Object composite) {
	LayoutTarget target = getLayoutTarget();
        GraphModel model = target.getGraphModel();
        for(Iterator ns = model.nodes(composite); ns.hasNext(); ) {
            Object node = ns.next();
            if(target.isNodeVisible(node)) {
                Rectangle2D vp = target.getViewport(composite);
                Rectangle2D bounds = target.getBounds(node);
                for(int i = 0; i < NUM_ITER; i++) {
                    double x = vp.getX() + Math.abs(Math.random())*vp.getWidth();
                    double y = vp.getY() + Math.abs(Math.random())*vp.getHeight();
                    LayoutUtilities.place(target, node, x, y);
                    bounds = target.getBounds(node);
                    boolean overlap = false;
                    Iterator j = target.intersectingNodes(bounds);
                    while(j.hasNext()) {
                        Object n2 = j.next();
                        if(node != n2) { overlap = false; }
                    }
                    if(!overlap) {
                        break;
                    }
                }
            }
        }
	LayoutUtilities.routeVisibleEdges(composite, target);
    }
}


