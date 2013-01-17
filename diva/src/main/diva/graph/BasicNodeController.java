/*
 * $Id: BasicNodeController.java,v 1.5 2002/08/13 14:09:20 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;

import diva.graph.MutableGraphModel;
import diva.graph.GraphModel;

import diva.canvas.CanvasComponent;
import diva.canvas.CanvasUtilities;
import diva.canvas.CompositeFigure;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.Site;

import diva.canvas.connector.AutonomousSite;
import diva.canvas.connector.CenterSite;
import diva.canvas.connector.PerimeterSite;
import diva.canvas.connector.PerimeterTarget;
import diva.canvas.connector.Connector;
import diva.canvas.connector.ConnectorAdapter;
import diva.canvas.connector.ConnectorManipulator;
import diva.canvas.connector.ConnectorEvent;
import diva.canvas.connector.ConnectorListener;
import diva.canvas.connector.ConnectorTarget;

import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;

import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.interactor.SelectionDragger;

import diva.util.Filter;

import java.awt.geom.*;
import java.awt.event.InputEvent;
import java.util.HashMap;
import java.util.Iterator;

/**
 * A basic node controller implementation, intended for use
 * as a controller for graphs containing only one node type.
 * This class can also be subclassed to control a specific type
 * of node in graphs that have multiple node types.
 * 
 * @author 	Steve Neuendorffer
 * @version	$Revision: 1.5 $
 * @rating      Red
 */
public class BasicNodeController implements NodeController {
 
    /** The selection interactor for drag-selecting nodes
     */
    private SelectionDragger _selectionDragger;

    /** The filter for control operations
     */
    private MouseFilter _controlFilter = new MouseFilter (
            InputEvent.BUTTON1_MASK,
            InputEvent.CTRL_MASK);

    private Interactor _interactor;
    private NodeRenderer _renderer;
    private GraphController _controller;

    /**
     * Create a new basic controller with default node and edge interactors.
     */
    public BasicNodeController (GraphController controller) {
	_controller = controller;
        SelectionModel sm = controller.getSelectionModel();
        _interactor = new NodeInteractor(controller, sm);       
    }

    /** Given a node, add it to this graph editor and perform a layout
     * on the new node.
     */
    public void addNode(Object node) {
	// FIXME this may cause a classcast exception.
        MutableGraphModel model = 
	    (MutableGraphModel)_controller.getGraphModel();
	model.addNode(_controller, node, model.getRoot());
	drawNode(node);
    }

    /** Add the node to this graph editor and render it
     * at the given location.
     */
    public void addNode(Object node, double x, double y) {
        MutableGraphModel model = 
	    (MutableGraphModel) _controller.getGraphModel();
	model.addNode(_controller, node, model.getRoot());
	Figure nf = drawNode(node);
	CanvasUtilities.translateTo(nf, x, y);
    }

    /** 
     * Add the node to this graph editor, inside the given parent node
     * and place it where convenient
     */
    public void addNode(Object node, Object parent) {
        MutableGraphModel model = 
	    (MutableGraphModel) _controller.getGraphModel();
	model.addNode(_controller, node, parent);
	drawNode(node, parent);
    }

    /** 
     * Add the node to this graph editor, inside the given parent node
     * and render it at the given location relative to its parent.
     */
    public void addNode(Object node, Object parent, double x, double y) {
        MutableGraphModel model = 
	    (MutableGraphModel) _controller.getGraphModel();
	model.addNode(_controller, node, parent);
	Figure nf = drawNode(node, parent);
	CanvasUtilities.translateTo(nf, x, y);
    }

    /** 
     * Remove the figure for the given node.
     */
    public void clearNode(Object node) {
	GraphModel model = _controller.getGraphModel();
	for(Iterator i = model.outEdges(node); i.hasNext(); ) {
            Object edge = i.next();
	    _controller.clearEdge(edge);
        }
        for(Iterator i = model.inEdges(node); i.hasNext(); ) {
            Object edge = i.next();
	    _controller.clearEdge(edge);
        }	
        Figure f = _controller.getFigure(node);
        if(f != null) {
	    CanvasComponent container = f.getParent();
	    f.setUserObject(null);
            _controller.setFigure(node, null);
            if(container instanceof FigureLayer) {
		((FigureLayer)container).remove(f);
	    } else if(container instanceof CompositeFigure) {
		((CompositeFigure)container).remove(f);
	    }
        }
    }

    /** 
     * Render the given node and add the resulting figure to the foreground
     * layer of the graph pane.  If the node was previously rendered, then 
     * infer the new location of the figure from the old.
     */
    public Figure drawNode(Object node) {
	Figure oldFigure = _controller.getFigure(node);
	
	// Infer the location for the new node.
	Point2D center;
        if(oldFigure != null) {
            center = CanvasUtilities.getCenterPoint(oldFigure.getBounds());
            clearNode(node);
        } else {
	    // no previous figure.  which means that we are probably 
	    // rendering for the first time.
            center = null; //FIXME: layout?
        }
	
	Figure newFigure = _renderNode(node);
        _controller.getGraphPane().getForegroundLayer().add(newFigure);

	// Now draw the contained nodes, letting them go where they want to.
	_drawChildren(node);
	
	if(center != null) {
	    // place the new figure where the old one was, if there 
	    // was an old figure.
	    CanvasUtilities.translateTo(newFigure, 
					center.getX(), center.getY());
	}

	_controller.dispatch(new GraphViewEvent(this,
	    GraphViewEvent.NODE_DRAWN, node));
			
        return newFigure;
    }

    /** 
     * Render the given node and add the resulting figure to the given
     * node's figure, which is assumed to be a CompositeFigure 
     * in the controller's graph pane.
     */
    public Figure drawNode(Object node, Object parent) {
	// FIXME what if node was previously rendered?
	Figure newFigure = _renderNode(node);
	CompositeFigure cf = (CompositeFigure)_controller.getFigure(parent);
        cf.add(newFigure);

	// Now draw the contained nodes, letting them go where they want to.
	_drawChildren(node);
	
	_controller.dispatch(new GraphViewEvent(this,
			     GraphViewEvent.NODE_DRAWN, node));
	
        return newFigure;
    }

    /** 
     * Return the graph controller containing this controller.
     */
    public GraphController getController() {
	return _controller;
    }

    /** 
     * Return the node interactor associated with this controller.
     */
    public Interactor getNodeInteractor() {
	return _interactor;
    }

    /** 
     * Return the node renderer associated with this controller.
     */
    public NodeRenderer getNodeRenderer() {
	return _renderer;
    }

    /** 
     * Remove the node.
     */
    public void removeNode(Object node) {
	// FIXME why isn't this symmetric with addNode?
        MutableGraphModel model = 
	    (MutableGraphModel) _controller.getGraphModel();
	// clearing the nodes is responsible for clearing any edges that are
	// connected
	if(model.isComposite(node)) {
	    for(Iterator i = model.nodes(node); i.hasNext(); ) {
		Object insideNode = i.next();
		_controller.clearNode(insideNode);
	    }
	}
	clearNode(node);
	// we assume that the model will remove any edges that are connected.
	model.removeNode(_controller, node);
	_controller.getGraphPane().repaint();
     }

    /** 
     * Set the node interactor for this controller
     */
    public void setNodeInteractor(Interactor interactor) {
	_interactor = interactor;
    }

    /** 
     * Set the node renderer for this controller
     */
    public void setNodeRenderer(NodeRenderer renderer) {
	_renderer = renderer;
    }

    /**
     * Render the given node using the node renderer.  Set the interactor
     * of the resulting figure to the node interactor.
     */
    protected Figure _renderNode(Object node) {
	//System.out.println("BasicNodeController node = " + node);
        Figure newFigure = getNodeRenderer().render(node);
        newFigure.setInteractor(getNodeInteractor());
        newFigure.setUserObject(node);
        _controller.setFigure(node, newFigure);
	return newFigure;
    }

    /**
     * Draw the children of the given node.
     */
    protected void _drawChildren(Object node) {
	GraphModel model = getController().getGraphModel();
	if(model.isComposite(node)) {
	    Iterator children = model.nodes(node);
	    while(children.hasNext()) {
		Object child = children.next();
		_controller.drawNode(child, node);
	    }
	}
    }
}


