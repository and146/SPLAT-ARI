/*
 * $Id: ConcreteFigures.java,v 1.8 2002/01/10 01:04:35 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.test;
import diva.util.jester.*;
import diva.util.java2d.*;

import diva.canvas.*;
import diva.canvas.connector.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;

import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;

/**
 * A test suite for testing concrete figures. This suite creates a factory
 * for each available concrete figure class and runs the Figure tests
 * on figures it produces.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision: 1.8 $
 */
public class ConcreteFigures extends TestSuite {

    /** Constructor
     */
    public ConcreteFigures (TestHarness harness) {
        setTestHarness(harness);
    }

    /**
     * runSuite()
     */
    public void runSuite () {
        new FigureTest(getTestHarness(), new BasicRectangleFactory1()).run();
        new FigureTest(getTestHarness(), new BasicRectangleFactory2()).run();
        new FigureTest(getTestHarness(), new CompositeFigureFactory1()).run();
        new FigureTest(getTestHarness(), new PaneWrapperFactory()).run();

        /// These ones fail!
        //new FigureTest(getTestHarness(), new LabelFigureFactory()).run();
        //new FigureTest(getTestHarness(), new IconFigureFactory()).run();
        //new FigureTest(getTestHarness(), new ImageFigureFactory()).run();
    }

    //////////////////////////////////////////////////////////// 
    ////  main

    /** Create a default test harness and
     * run all tests on it.
     */
    public static void main (String argv[]) {
        new ConcreteFigures(new TestHarness()).run();
    }

    //////////////////////////////////////////////////////////// 
    //// Factories

    /**
     * Create a BasicRectangle with stroked outline
     */
    public class BasicRectangleFactory1 implements FigureTest.FigureFactory {
        public Figure createFigure () {
            return new BasicRectangle(10,10,20,20);
        }
        public String toString() {
            return "Basic rectangle, no fill";
        }
    }

    /**
     * Create a filled BasicRectangle
     */
    public class BasicRectangleFactory2 implements FigureTest.FigureFactory {
        public Figure createFigure () {
            return new BasicRectangle(10,10,20,20, Color.blue);
        }
        public String toString() {
            return "Basic rectangle, filled blue";
        }
    }

    /**
     * Create an icon figure
     */
    public class IconFigureFactory implements FigureTest.FigureFactory {
        /** Create a collection of terminals an an icon
         */
        public void createTerminals (IconFigure icon) {
            // NORTH
            StraightTerminal north = new StraightTerminal();
            Site connectNorth = north.getConnectSite();
            Blob blobNorth = new Blob();
            blobNorth.setSizeUnit(5.0);
            north.setEnd(blobNorth);
            icon.addTerminal(north, SwingConstants.NORTH, 50);

            // SOUTH
            StraightTerminal south = new StraightTerminal();
            Site connectSouth = south.getConnectSite();
            Blob blobSouth = new Blob();
            blobSouth.setStyle(Blob.BLOB_DIAMOND);
            blobSouth.setSizeUnit(5.0);
            blobSouth.setFilled(false);
            south.setEnd(blobSouth);
            icon.addTerminal(south, SwingConstants.SOUTH, 50);

            // WEST
            StraightTerminal west = new StraightTerminal();
            Site connectWest = west.getConnectSite();
            Arrowhead arrowWest = new Arrowhead();
            west.setEnd(arrowWest);
            icon.addTerminal(west, SwingConstants.WEST, 50);

            // EAST
            StraightTerminal east = new StraightTerminal();
            Site connectEast = east.getConnectSite();
            Arrowhead arrowEast = new Arrowhead();
            arrowEast.setFlipped(true);
            east.setEnd(arrowEast);
            icon.addTerminal(east, SwingConstants.EAST, 50);
        }

        public Figure createFigure () {
            // Create the graphic
            PaintedList graphic = new PaintedList();

            Polygon2D polygon = new Polygon2D.Double();
            polygon.moveTo(30,50);
            polygon.lineTo(70,80);
            polygon.lineTo(70,20);
            graphic.add(new PaintedShape(polygon, Color.red, 1.0f));

            Line2D line1 = new Line2D.Double(10,50,30,50);
            graphic.add(new PaintedPath(line1));

            Line2D line2 = new Line2D.Double(70,50,90,50);
            graphic.add(new PaintedPath(line2));

            // Create the icon
            BasicRectangle background = new BasicRectangle(0,0,100,100,
                    Color.green);
            IconFigure icon = new IconFigure(background, graphic);

            // Add its terminals
            createTerminals(icon);
            icon.translate(100, 100);
            return icon;
        }
        public String toString() {
            return "Icon figure";
        }
    }

    /**
     * Create an image figure
     */
    public class ImageFigureFactory implements FigureTest.FigureFactory {
        public static final String IMAGE_FILE_NAME = "demo.gif";
        public Component component = new Canvas();
    
        public Figure createFigure () {
            Image img = Toolkit.getDefaultToolkit().getImage(IMAGE_FILE_NAME);
            MediaTracker tracker = new MediaTracker(component);
            tracker.addImage(img,0);
            try {
                tracker.waitForID(0);
            }
            catch (InterruptedException e) {
                System.err.println(e + "... in LayerImageFigure");
            }
            ImageFigure imgFig = new ImageFigure(img);
            imgFig.translate(300,100);
            return imgFig;
        }
        public String toString() {
            return "Image figure";
        }
    }

    /**
     * Create an image figure
     */
    public class LabelFigureFactory implements FigureTest.FigureFactory {
        public Figure createFigure () {
            LabelFigure label = new LabelFigure("Hello!");
            label.translate(200,200);
            return label;
        }
        public String toString() {
            return "Label figure";
        }
    }

    /**
     * Create a CompositeFigure with a filled rectangle background
     */
    public class CompositeFigureFactory1 implements FigureTest.FigureFactory {
        public Figure createFigure () {
            Figure bg = new BasicRectangle(10,10,20,20, Color.blue);
            Figure cf = new CompositeFigure(bg);
            return cf;
        }
        public String toString() {
            return "Composite figure with basic rectangle background";
        }
    }

    /**
     * Create a PaneWrapper with a filled rectangle background
     */
    public class PaneWrapperFactory implements FigureTest.FigureFactory {
        public Figure createFigure () {
            Figure bg = new BasicRectangle(10,10,20,20, Color.blue);
            CanvasPane pane = new GraphicsPane();
            pane.setSize(300.0,300.0);
            PaneWrapper wrapper = new PaneWrapper(pane);
            wrapper.setBackground(bg);
            return wrapper;
        }
        public String toString() {
            return "Pane wrapper containing an empty graphics pane";
        }
    }
}


