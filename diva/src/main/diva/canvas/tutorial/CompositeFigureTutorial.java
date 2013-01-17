/*
 * $Id: CompositeFigureTutorial.java,v 1.16 2002/01/04 04:12:11 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */

package diva.canvas.tutorial;

import diva.canvas.AbstractFigure;
import diva.canvas.CanvasPane;
import diva.canvas.CanvasUtilities;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.FigureWrapper;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;
import diva.canvas.Site;
import diva.canvas.TransformContext;
import diva.canvas.CompositeFigure;

import diva.canvas.connector.CenterSite;
import diva.canvas.connector.Connector;
import diva.canvas.connector.StraightConnector;

import diva.canvas.event.MouseFilter;

import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;

import diva.canvas.toolbox.BasicFigure;
import diva.canvas.toolbox.BasicEllipse;
import diva.canvas.toolbox.BasicRectangle;

import diva.gui.BasicFrame;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingUtilities;


/**
 * This tutorial demonstrates how to use composite figures.
 * <img src="../../../../packages/canvas/tutorial/images/CompositeFigureTutorial.gif" align="right">
 *
 * <P>
 * It instantiates a composite figure which contains a
 * square (which represents the body of a component) and
 * four circles (which represents "ports" on the component).
 * The ports can be moved and scaled independently of the body, and
 * when the body is scaled, the ports are scaled proportionally.
 *
 * <p>
 * In general, it would be better to create a custom class for something
 * like a component with port -- the purpose here is simply
 * to illustrate how to use this class. The way that the composite figure
 * is created is straight-forward:
 * 
 * <pre>
 *     CompositeFigure tc = new CompositeFigure();
 * </pre>
 * 
 * <P> Adding child figures to the composite figure is also straight-forward.
 * For example,
 * <pre>
 *     Figure p1 = new BasicEllipse(150.0, 100.0, 20.0, 20.0, Color.red);
 *     p1.translate(-10,-10);
 *     tc.add(p1);
 * </pre>
 * 
 * (The call to translate is there simply because it was easier to
 * figure out the coordinates of all the figures relative to their centers.
 * The translate() moves the center to the coordinates set in
 * the call to the constructor.)
 * 
 * <p>
 * In this tutorial we also introduce the notion of controllers and
 * interactors. Any but the simplest interactive graphical applications
 * should have a <i>controller</i> object that takes care of managing
 * user interaction.  The class
 * <b>diva.canvas.toolbox.BasicController</b> is a very basic interaction
 * controller that we use in some of the tutorials. It contains an object
 * called a DragInteractor that is used to move figures about on the
 * screen:
 * 
 * <pre>
 *     BasicController controller = new BasicController(graphicsPane);
 *     Interactor defaultInteractor = controller.getSelectionInteractor();
 * </pre>
 * 
 * <p>
 * To make each figure respond to the mouse, we just give it the
 * interactor. For example,
 * 
 * <pre>
 *     p1.setInteractor(defaultInteractor);
 * </pre>
 * 
 * In this example, an interactor is set up on each port so that they
 * can be dragged independently, and an interactor is set up on
 * the composite figure itself, so dragging on any figure that is
 * not part of the background itself will drag the whole figure and
 * its contents.
 * 
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.16 $
 * @rating Red
 */
public class CompositeFigureTutorial {
    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    // More objects
    FigureLayer layer;
    BasicController controller;
    Interactor defaultInteractor;

    /** Create a JCanvas and put it into a window.
     */
    public CompositeFigureTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();
        layer = graphicsPane.getForegroundLayer();
        controller = new BasicController(graphicsPane);
        defaultInteractor = controller.getSelectionInteractor();

        BasicFrame frame = new BasicFrame("Composite figure tutorial", canvas);
        frame.setSize(600,400);
        frame.setVisible(true);
    }

    /** Main function
     */
    public static void main (String argv[]) {
	// Always invoke graphics code in the event thread
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    CompositeFigureTutorial ex = new CompositeFigureTutorial();
		    ex.createCompositeFigure();
		    ex.createBackgroundedCompositeFigure();
		    ex.graphicsPane.repaint();
		}
	    });
    }
    
    /** Create a composite figure that does not have a background
     */
    public void createCompositeFigure () {
	CompositeFigure tc = new CompositeFigure();
	Figure bg = new BasicRectangle(100.0, 100.0, 100.0, 100.0,
                Color.green);
	tc.add(bg);
        layer.add(tc);   
	tc.setInteractor(defaultInteractor);
        addPorts(tc);
    }

    /** Create a composite figure that uses the background facility.
     * Generally, for figures of this nature, this is a better thing to do.
     */
    public void createBackgroundedCompositeFigure () {
	CompositeFigure tc = new CompositeFigure();
	Figure bg = new BasicRectangle(100.0, 100.0, 100.0, 100.0,
                Color.blue);
        tc.setBackgroundFigure(bg);
        layer.add(tc);   
	tc.setInteractor(defaultInteractor);
        addPorts(tc);
        tc.translate(200.0,0);
    }

    /** Utility function to add the "ports" to the composite figure.
     */
    public void addPorts (CompositeFigure tc) {
	Figure p1 = new BasicEllipse(150.0, 100.0, 20.0, 20.0, Color.red);
	p1.translate(-10,-10);
	Figure p2 = new BasicEllipse(200.0, 150.0, 20.0, 20.0, Color.blue);
	p2.translate(-10,-10);
	Figure p3 = new BasicEllipse(150.0, 200.0, 20.0, 20.0, Color.yellow);
	p3.translate(-10,-10);
	Figure p4 = new BasicEllipse(100.0, 150.0, 20.0, 20.0, Color.magenta);
	p4.translate(-10,-10);

	tc.add(p1);
	tc.add(p2);
	tc.add(p3);
	tc.add(p4);

	p1.setInteractor(defaultInteractor);
	p2.setInteractor(defaultInteractor);
	p3.setInteractor(defaultInteractor);
	p4.setInteractor(defaultInteractor);
    }
}


