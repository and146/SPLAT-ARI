/*
 * $Id: FigureTest.java,v 1.14 2002/01/10 01:04:36 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.test;
import diva.util.jester.*;
import diva.util.java2d.*;

import diva.canvas.*;
import diva.canvas.interactor.*;
import diva.canvas.toolbox.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;

/**
 * A test suite for Figure. Since Figure is an interface, this class
 * has no main() method. It defines a factory interface that
 * concrete factories must implement.
 *
 * @author John Reekie      (johnr@eecs.berkeley.edu)
 * @version $Revision: 1.14 $
 */
public class FigureTest extends TestSuite {

    /** The figure factory interface
     */
    public interface FigureFactory {
        public Figure createFigure ();
    }

    /**
     * The unit factory
     */
    private FigureFactory factory;

    /** Constructor
     */
    public FigureTest (TestHarness harness, FigureFactory factory) {
        setTestHarness(harness);
        setFactory(factory);
        this.factory = factory;
    }

    /**
     * runSuite()
     */
    public void runSuite () {
        testHit();
        testIntersects();
        testPaint();
        testProperties();
        testTranslate();
        testTransform();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Perform the simple set/get tests.
     */
    public void testProperties () {
        runTestCase(new TestCase("Figure properties") {
            Figure figure;
            Interactor r = new DragInteractor();

            public void init () throws Exception {
                figure = factory.createFigure();
            }
            public void run () throws Exception {
                figure.setInteractor(r);
                figure.setVisible(false);
            }
            public void check () throws TestFailedException {
                assertExpr(!figure.isVisible(), "Property visible");
                assertExpr(figure.getInteractor() == r, "Property interactionRole");
            }
        });
    }

    /** Test hit. This doesn't actually do a hit test,
     * but it does check that the passed rectangle is not modified.
     */
    public void testHit () {
        runTestCase(new RegionTestCase("Figure hit") {
            public void run () throws Exception {
                result = figure.hit(region);
            }
        });
    }
     
    /** Test intersection. This doesn't actually do an intersection test,
     * but it does check that the passed rectangle is not modified.
     */
    public void testIntersects () {
        runTestCase(new RegionTestCase("Figure intersects") {
            public void run () throws Exception {
                result = figure.intersects(region);
            }
        });
    }
     
    /** Test painting. This method calls both versions of the paint
     * method. It doesn't actually test what the paint method does.
     * In either case, it verifies that the transform context of the
     * Graphics2D is not changed.  In the case of the paint method
     * that takes a region, it also verifies that the region is not
     * changed by the call.  Note that we transform the figure first
     * as this is sometimes needed to make this bug show up.
     */
    public void testPaint () {
        final BufferedImage buffer = new BufferedImage(
                100,100,BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = buffer.createGraphics();

        runTestCase(new TestCase("Figure paint") {
            Figure figure;
            AffineTransform at1, at2, at3;
            Rectangle2D region = new Rectangle2D.Double(10,20,30,40);

            public void init () throws Exception {
                figure = factory.createFigure();

                AffineTransform at = new AffineTransform();
                at.translate(10,20);
                at.scale(0.5,2.0);
                figure.transform(at);
             }
            public void run () throws Exception {
                at1 = new AffineTransform(g.getTransform());
                figure.paint(g);
                at2 = new AffineTransform(g.getTransform());
                figure.paint(g,region);
                at3 = new AffineTransform(g.getTransform());
            }
            public void check () throws TestFailedException {
                assertExpr(at1.equals(at2),
                        "Graphics2D transform changed from:\n    " 
                        + at1 + " \nto:\n    " + at2);
                assertExpr(at2.equals(at3),
                        "Graphics2D transform changed from:\n    " 
                        + at2 + " \nto:\n    " + at3);
            }
        });
 
        runTestCase(new RegionTestCase("Figure paint region test") {
            public void run () throws Exception {
                figure.paint(g,region);
            }
        });
    }
     
    /** Test how transforms affect the figure
     */
    public void testTransform () {
        runTestCase(new TestCase("Figure transform") {
            Figure figure;
            AffineTransform at;
            Shape shape;
            Rectangle2D bounds;

            public void init () throws Exception {
                figure = factory.createFigure();
                at = new AffineTransform();
                at.translate(40,-20);
                at.scale(2.0,0.5);
            }
            public void run () throws Exception {
                shape = (Shape) figure.getShape();
                shape = ShapeUtilities.transformModify(shape, at);
                bounds = (Rectangle2D) figure.getBounds();
                bounds = (Rectangle2D) bounds.clone();
                ShapeUtilities.transformModify(bounds, at);
                
                figure.transform(at);
            }
            public void check () throws TestFailedException {
                assertExpr(TestUtilities.shapeEquals(
                        shape, figure.getShape(), 0.01),
                        "Shape not transformed: "
                        + shape + " != " + figure.getShape());

                // For the bounds, we need to allow a large error,
                // because bounds don't necessarily transform correctly!
                // So this test is only useful for catching the most
                // gross errors
                assertExpr(TestUtilities.shapeEquals(
                        bounds, figure.getBounds(), 2.0),
                        "Bounds not transformed: "
                        + bounds + " != " + figure.getBounds());
            }
        });
    }

    /** Test how translates affect the figure
     */
    public void testTranslate () {
        runTestCase(new TestCase("Figure translate") {
            Figure figure;
            Shape shape;
            Rectangle2D bounds;

            public void init () throws Exception {
                figure = factory.createFigure();
            }
            public void run () throws Exception {
                shape = (Shape) figure.getShape();
                shape = new GeneralPath(shape);
                shape = ShapeUtilities.translateModify(shape, 10.0, -20.0);
                bounds = (Rectangle2D) figure.getBounds();
                bounds = (Rectangle2D) bounds.clone();
                bounds = (Rectangle2D) ShapeUtilities.translateModify(
                        bounds, 10.0, -20.0);
                
                figure.translate(10.0, -20.0);
            }
            public void check () throws TestFailedException {
                assertExpr(TestUtilities.shapeEquals(
                        shape, figure.getShape(), 0.01),
                        "Shape not translated: "
                        + shape + " != " + figure.getShape());

                assertExpr(TestUtilities.shapeEquals(
                        bounds, figure.getBounds(), 0.01),
                        "Bounds not translated: "
                        + bounds + " != " + figure.getBounds());
            }
        });
    }

    ////////////////////////////////////////////////////////////////
    //// Inner classes

    /** Region testing test case. This test case can be used for
     * methods that take a region argument, to verify that they don't
     * change that region. The run method must be overridden.
     */
    public abstract class RegionTestCase extends TestCase {
        Figure figure;
        boolean result;
        Rectangle2D region, copy;

        public RegionTestCase (String str) {
            super(str);
        }
        public void init () throws Exception {
            figure = factory.createFigure();

            AffineTransform at = new AffineTransform();
            at.translate(10,20);
            at.scale(0.5,2.0);
            figure.transform(at);

            region = (Rectangle2D) figure.getBounds().clone();
            copy = (Rectangle2D) region.clone();
        }
        // public void run () throws Exception {
        //     result = figure.intersects(region);
        //}
        public void check () throws TestFailedException {
            assertExpr(TestUtilities.shapeEquals(region, copy, 0.01),
                    "The region was changed from:\n    " 
                    + copy + " \nto:\n    " + region);
        }
    }
}


