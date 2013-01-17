/*
 * $Id: EdgeController.java,v 1.28 2001/11/29 18:09:36 neuendor Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph;

import diva.graph.MutableGraphModel;
import diva.graph.GraphModel;

import diva.canvas.CanvasComponent;
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
import diva.canvas.connector.Terminal;

import diva.canvas.event.LayerAdapter;
import diva.canvas.event.LayerEvent;
import diva.canvas.event.MouseFilter;

import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.AbstractInteractor;
import diva.canvas.interactor.BasicSelectionRenderer;
import diva.canvas.interactor.GrabHandle;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.SelectionModel;
import diva.canvas.interactor.SelectionDragger;

import diva.util.Filter;

import java.awt.geom.*;
import java.awt.event.InputEvent;
import java.util.HashMap;

/**
 * Specifies the interface for objects that manage creation
 * of and interaction with graph edges. GraphControllers
 * contain one or more instances of EdgeController.
 *
 * @author 	Michael Shilman (michaels@eecs.berkeley.edu)
 * @version	$Revision: 1.28 $
 * @rating      Red
 */
public interface EdgeController {

    /** Add an edge to this graph editor and render it from the given
     * tail node to an autonomous site at the given location. Give the
     * new edge the given semanticObject.  The "end" flag is either
     * HEAD_END or TAIL_END, from diva.canvas.connector.ConnectorEvent.
     * @return The new edge.
     * @exception GraphException If the connector target cannot return a 
     * valid site on the node's figure.
     * @see diva.canvas.connector.ConnectorEvent
     */
    public void addEdge(Object edge, Object node, 
			int end, double x, double y);

    /** 
     * Add an edge to this graph between the given tail and head
     * nodes.  Give the new edge the given semanticObject.
     */
    public void addEdge(Object edge, Object tail, Object head);

    /** 
     * Remove the figure for the given edge, but do not remove the
     * edge from the graph model.
     */
    public void clearEdge(Object edge);

    /**
     * Draw the edge and add it to the layer, establishing
     * a two-way correspondence between the model and the
     * view.  If the edge already has been associated with some figure in
     * the view, then use any information in that figure to help draw the
     * edge.
     */
    public Figure drawEdge(Object edge);

    /**
     * Get the target used to find sites on nodes to connect to.
     */
    public ConnectorTarget getConnectorTarget();
    /**
     * Get the graph controller that this controller is contained in.
     */
    public GraphController getController();

    /**
     * Get the interactor given to edge figures.
     */
    public Interactor getEdgeInteractor();

    /**
     * Return the edge renderer for this view.
     */
    public EdgeRenderer getEdgeRenderer();

    /** 
     * Remove the edge.
     */
    public void removeEdge(Object edge);

    /**
     * Set the target used to find sites on nodes to connect to.  This
     * sets the local connector target (which is often used to find the
     * starting point of an edge) and the manipulator's connector target, which
     * is used after the connector is being dragged.
     */
    public void setConnectorTarget (ConnectorTarget t);

    /**
     * Set the interactor given to edge figures.
     */
    public void setEdgeInteractor (Interactor interactor);

    /**
     * Set the edge renderer for this view.
     */
    public void setEdgeRenderer(EdgeRenderer er);
    
    /** Render the edge on the given layer between the two sites.
     */
    public Connector render(Object edge, FigureLayer layer, 
			       Site tailSite, Site headSite);

}


