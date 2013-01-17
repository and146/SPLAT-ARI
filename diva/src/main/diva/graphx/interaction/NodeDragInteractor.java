/*
 * $Id: NodeDragInteractor.java,v 1.2 2002/08/19 07:12:30 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graphx.interaction;

import diva.graphx.GraphModel;
import diva.graphx.GraphController;
import diva.graphx.GraphUtilities;

import diva.canvas.Figure;
import diva.canvas.TransformContext;
import diva.canvas.connector.Connector;
import diva.canvas.event.LayerEvent;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.SelectionModel;

import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * An interactor that drags nodes.
 *
 * @author 	Michael Shilman (michaels@eecs.berkeley.edu)
 * @author 	John Reekie (johnr@eecs.berkeley.edu)
 * @version	$Revision: 1.2 $
 * @rating Red
 */
public class NodeDragInteractor extends DragInteractor {
    /**
     * The graph controller that manages this interactor.
     */
    private GraphController _controller;
    
    /** Create a new NodeDragInteractor and give it a pointer
     * to its controller to it can find other useful objects
     */
    public NodeDragInteractor (GraphController controller) {
        _controller = controller;
    }

    /** Drag all selected nodes and move any attached edges
     */
    public void translate (LayerEvent e, double x, double y) {
        GraphModel model = _controller.getGraphModel();
        Iterator i = targets();
        Point2D.Double pt = new Point2D.Double(x,y);
        Point2D.Double localpt = new Point2D.Double();
	Set edgeSet = new HashSet();
	while (i.hasNext()) {
            Figure t = (Figure) i.next();
            // Translate anything that is not an edge
	    // FIXME: we want to drag edges that are not connected
	    // at either end
	    // FIXME: translate edges that are connected on both ends
	    // to targets?
	    if ( !(model.isEdge(t.getUserObject()))) {
                // Perform an inverse transform on coordinates for
                // composite nodes??  
                // FIXME: This isn't right for scaling canvases... so I 
                // doubt it is right for composite nodes.
                // TransformContext tc = t.getParent().getTransformContext();
                // tc.getInverseTransform().deltaTransform(pt, localpt);
                //t.translate(localpt.x,localpt.y);
                t.translate(x, y);
	    } 

	    // Remember the edges so we route them later.
            if (model.isNode(t.getUserObject())) {
		Iterator j;
		j = model.inEdges(t.getUserObject());
		while(j.hasNext()) {
		    edgeSet.add(j.next());
		}
		j = model.outEdges(t.getUserObject());
		while(j.hasNext()) {
		    edgeSet.add(j.next());
		}
		if(model.isComposite(t.getUserObject())) {
		    j = GraphUtilities.partiallyContainedEdges(t.getUserObject(),
							       model);
		    while(j.hasNext()) {
			edgeSet.add(j.next());
		    }
		}
            }
        }
	for (Iterator edges = edgeSet.iterator(); edges.hasNext(); ) {
	    Object edge = edges.next();
            Connector c = (Connector) (_controller.getEdgeController(edge).getConnector(edge));
            if (c != null) {
                c.reroute();
            }
        }
        
    }

    /**
     * Route all edges attached to the given node.
     *
     * FIXME: currently inefficient. 
     * Rewrite this to use the X and Y coordinates.
     
    private void routeAttachedEdges(Object node, double x, double y) {
        // We have to check for null figures, since if an edge is
        // connected at only one end in one view, other views will
        // have no edge figure for that edge.
        GraphModel model = _controller.getGraphModel();
        for (Iterator ins = model.inEdges(node); ins.hasNext(); ) {
            Connector c = (Connector) (_controller.getFigure(ins.next()));
            if (c != null) {
                c.reroute();
            }
        }
        for (Iterator outs = model.outEdges(node); outs.hasNext(); ) {
            Connector c = (Connector) (_controller.getFigure(outs.next()));
            if (c != null) {
                c.reroute();
            }
        }
        if(model.isComposite(node)) {
            for (Iterator contained = model.nodes(node);
                 contained.hasNext(); ) {
                routeAttachedEdges(contained.next(), x, y);
            }
        }
	}*/
}


