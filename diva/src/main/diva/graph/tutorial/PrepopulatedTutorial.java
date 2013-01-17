/*
 * $Id: PrepopulatedTutorial.java,v 1.15 2001/07/22 22:01:28 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.tutorial;
import diva.graph.*;
import diva.graph.modular.*;
import diva.graph.basic.*;
import diva.graph.layout.*;
import diva.canvas.*;
import diva.gui.AppContext;
import diva.gui.BasicFrame;
import java.awt.GridLayout;
import javax.swing.SwingUtilities;

/**
 * This example shows three alternatives to display a prepopulated
 * graph model in a window.  Prepopulated means that there are
 * already nodes in the model before the model was placed in a
 * GraphPane. There was a problem in the initial release which made
 * this break. That problem has been fixed and it will just work now,
 * except for one little gotcha, which is if you set the model while
 * the window is closed, the GraphPane thinks its size is 0x0, so the
 * nodes all get layed out in the upper-left corner of the canvas. The
 * way to fix this is to either set the model once the window is open,
 * or explicitly call a global layout once the window has been opened.
 * More comments below in the individual methods.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.15 $
 * @rating Red
 */
public class PrepopulatedTutorial {
    public static void main(String argv[]) {
        final AppContext context = new BasicFrame("Prepopulated Tutorial");
        context.setSize(300, 600);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {       
                new PrepopulatedTutorial(context);
                context.setVisible(true);
            }
        });
    }

    private PrepopulatedTutorial(AppContext context) {
        // Construct the prepopulated model
        //
        //        A
        //       / \
        //      B   C
        //
        BasicGraphModel model = new BasicGraphModel();
        CompositeNode root = (CompositeNode)model.getRoot();
        Node a = model.createNode("a");
        Node b = model.createNode("b");
        Node c = model.createNode("c");
        model.addNode(this, a, root);
        model.addNode(this, b, root);
        model.addNode(this, c, root);
        Edge x = model.createEdge("x");
        Edge y = model.createEdge("y");
        model.connectEdge(this, x, a, b);
        model.connectEdge(this, y, a, c);
        
        // Display the model in three
        // different panes, using three
        // different techniques.
        try {
            context.getContentPane().setLayout(new GridLayout(3, 1));
            bogusLayout(model, context);
            layoutPostDisplay(model, context);
            setModelPostDisplay(model, context);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.exit(0);
        }
    }

    /**
     * This is the first thing you'd probably think of, but this
     * happens to be bogus, because the layout
     * is applied to the nodes before the window is showing,
     * meaning that the nodes are layed out in a 0x0 frame,
     * and are all clustered in the upper-left corner.  This
     * is remedied in the other techiques given below.
     */
    public void bogusLayout(MutableGraphModel model,
            AppContext context) {
        BasicGraphController gc = new BasicGraphController();
        context.getContentPane().add(
                new JGraph(new GraphPane(gc, model)));
        RandomLayout random =
            new RandomLayout(new BasicLayoutTarget(gc));
        random.layout(model.getRoot());
     }

    /**
     * In this version you construct the graph widget with
     * the model, and apply a layout to the graph once
     * the window is showing.  I think the "set model
     * post display" version is preferable, but this might
     * be useful in some cases.
     */
    public void layoutPostDisplay(final MutableGraphModel model, 
            AppContext context) {
        final BasicGraphController bgc = new BasicGraphController();
        context.getContentPane().add(
                new JGraph(new GraphPane(bgc, model)));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {       
                RandomLayout random =
                    new RandomLayout(new BasicLayoutTarget(bgc));
                random.layout(model.getRoot());
           
            }
        }); 
    }
    
    /**
     * In this version you construct the graph widget with
     * the default constructor (giving it an empty graph),
     * and then set the model once the window is showing.
     */
    public void setModelPostDisplay(MutableGraphModel model, 
            AppContext context) {
        BasicGraphController gc = new BasicGraphController();
        gc.addGraphViewListener(new IncrementalLayoutListener(
                new IncrLayoutAdapter(new LevelLayout(
                        new BasicLayoutTarget(gc))), null));
        context.getContentPane().add(
                new JGraph(new GraphPane(gc, model)));
    }
}


