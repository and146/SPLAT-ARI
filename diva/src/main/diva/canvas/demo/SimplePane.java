/*
 * $Id: SimplePane.java,v 1.13 2001/11/27 02:10:19 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.demo;

import diva.canvas.*;
import diva.canvas.event.*;
import diva.canvas.toolbox.*;
import diva.canvas.interactor.*;
import diva.util.java2d.*;

import java.awt.*;
import java.util.*;
import javax.swing.*;
import java.awt.geom.*;


/** A pane containing instances of some basic figures. The
 * figures can be dragged about with the mouse.
 *
 * @author John Reekie
 * @version $Revision: 1.13 $
 */
public class SimplePane extends GraphicsPane {

    /** The controller
     */
    BasicController controller;

    /** The interactor to give to all figures
     */
    SelectionInteractor selectionInteractor;

    /** The layer to draw all figure in
     */
    FigureLayer figureLayer;

    /**
     * Constructor
     */
    public SimplePane() {
        super();

        // Get the figure layer
        figureLayer = getForegroundLayer();

        // Construct a simple controller and get the default interactor
        controller = new BasicController(this);
        selectionInteractor = controller.getSelectionInteractor();

        // Draw it
        drawFigures();
    }

    /** Draw some figures
     */
    public void drawFigures () {
        // Here's a square
        BasicRectangle rect = new BasicRectangle(40.0,40.0,80.0,80.0);
        rect.setLineWidth(8);
        rect.setStrokePaint(Color.red);
        figureLayer.add(rect);
        rect.setInteractor(selectionInteractor);
 
        // Here's an ellipse
        BasicEllipse oval = new BasicEllipse(160.0,10.0,120.0,80.0);
        oval.setLineWidth(2);
        oval.setFillPaint(Color.magenta);
        figureLayer.add(oval);
        oval.setInteractor(selectionInteractor);

        // Create a star using a general path object
        Polygon2D p = new Polygon2D.Double();
        p.moveTo(- 100.0f, - 25.0f);
        p.lineTo(+ 100.0f, - 25.0f);
        p.lineTo(- 50.0f, + 100.0f);
        p.lineTo(+ 0.0f, - 100.0f);
        p.lineTo(+ 50.0f, + 100.0f);
        p.closePath();
    
        // translate origin towards center of canvas
        AffineTransform at = new AffineTransform();
        at.translate(200.0f, 200.0f);
        p.transform(at);

        BasicFigure star = new BasicFigure(p);
        star.setLineWidth(2);
        star.setStrokePaint(Color.blue);
        figureLayer.add(star);
        star.setInteractor(selectionInteractor);

        // Create a cloud, Claude
        Shape area = ShapeUtilities.createCloudShape();

//         Area area = new Area();
//         Ellipse2D c = new Ellipse2D.Double();
//         c.setFrame(0,25,50,50);
//         area.add(new Area(c));
//         c.setFrame(25,0,40,40);
//         area.add(new Area(c));
//         c.setFrame(25,25,60,60);
//         area.add(new Area(c));
//         c.setFrame(60,30,40,40);
//         area.add(new Area(c));
//         c.setFrame(60,10,30,30);
//         area.add(new Area(c));

        AffineTransform cat = new AffineTransform();
        cat.translate(200,100);
        cat.scale(2.0,2.0);
        Shape bigarea = cat.createTransformedShape(area);

        BasicFigure cloud = new BasicFigure(bigarea, Color.blue);
        cloud.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER,0.5f));
        figureLayer.add(cloud);
        cloud.setInteractor(selectionInteractor);
    }

    /** Return the selection interactor
     */
    public SelectionInteractor getSelectionInteractor() {
        return selectionInteractor;
    }

}



