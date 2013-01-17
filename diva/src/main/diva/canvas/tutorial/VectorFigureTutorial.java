/*
 * $Id: VectorFigureTutorial.java,v 1.5 2002/09/26 10:52:28 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.tutorial;

import diva.canvas.AbstractFigure;
import diva.canvas.CanvasPane;
import diva.canvas.Figure;
import diva.canvas.FigureLayer;
import diva.canvas.GraphicsPane;
import diva.canvas.JCanvas;

import diva.canvas.event.MouseFilter;

import diva.canvas.interactor.Interactor;
import diva.canvas.interactor.SelectionInteractor;
import diva.canvas.interactor.DragInteractor;
import diva.canvas.interactor.BoundsManipulator;

import diva.canvas.toolbox.BasicRectangle;
import diva.canvas.toolbox.BasicController;
import diva.canvas.toolbox.VectorFigure;

import diva.util.java2d.ShapeUtilities;

import diva.gui.BasicFrame;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import javax.swing.SwingUtilities;


/** An example showing how to use VectorFigure.
 *
 * <p>
 * <img src="../../../../packages/canvas/tutorial/images/VectorFigureTutorial.gif" align="right">
 *
 * The VectorFigure class provides a simple way to make figures that
 * are fairly complex graphically. It has no inherent shape, but draws
 * a series of shapes that are added one at a time to it.
 * 
 * @author John Reekie
 * @version $Revision: 1.5 $
 */
public class VectorFigureTutorial {

    // The JCanvas
    private JCanvas canvas;

    // The GraphicsPane
    private GraphicsPane graphicsPane;

    /** Create a JCanvas and put it into a window.
     */
    public VectorFigureTutorial () {
        canvas = new JCanvas();
        graphicsPane = (GraphicsPane)canvas.getCanvasPane();
        createFigures();
        BasicFrame frame = new BasicFrame("Vector figure tutorial", canvas);
    }

    /** Create instances of Vector Figures and make them
     * draggable and resizeable.
     */
    public void createFigures () {
        FigureLayer layer = graphicsPane.getForegroundLayer();

        // Create a controller to do the work.
        BasicController controller = new BasicController(graphicsPane);
        SelectionInteractor defaultInteractor
                = controller.getSelectionInteractor();
        BoundsManipulator manip = new BoundsManipulator();
        defaultInteractor.setPrototypeDecorator(manip);
        
        // Create a simple Vector Figure that draws a cross
        VectorFigure one = new VectorFigure();
	one.add(new Line2D.Double(0.0, 0.0, 100.0, 100.0));
	one.add(new Line2D.Double(100.0, 0.0, 0.0, 100.0));
        layer.add(one);
        one.setInteractor(defaultInteractor);

	// Here's a more complicated one, where we explicitly set the
	// shape to be a circle.
        VectorFigure two = new VectorFigure();
	Shape circle = new Ellipse2D.Double(0.0, 0.0, 100.0, 100.0);

        // Draw some filled circles
	two.fillMode();
	two.setShape(circle);
	two.add(Color.blue);
	two.add(circle);
	
	two.add(Color.yellow);
	two.add(new Ellipse2D.Double(10.0, 10.0, 80.0, 80.0));
	
	two.add(Color.red);
	two.add(new Ellipse2D.Double(20.0, 20.0, 60.0, 60.0));
	
        // Draw some lines
        two.lineMode();
	two.add(Color.black);
	two.add(new Line2D.Double(14.65, 14.65, 85.35, 85.35));
	two.add(new Line2D.Double(85.35, 14.65, 14.65, 85.35));

	two.translate(200,100);
        layer.add(two);
        two.setInteractor(defaultInteractor);      
    }

    /** Main function
     */
    public static void main (String argv[]) {
	// Always invoke graphics code in the event thread
	SwingUtilities.invokeLater(new Runnable() {
		public void run() {
		    VectorFigureTutorial ex = new VectorFigureTutorial();
		}
	    });
    }
}
