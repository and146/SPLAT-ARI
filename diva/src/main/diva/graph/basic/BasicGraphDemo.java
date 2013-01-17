/*
 * $Id: BasicGraphDemo.java,v 1.8 2001/07/22 22:01:19 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.basic;

import diva.gui.AppContext;
import diva.gui.ApplicationContext;
import diva.gui.BasicFrame;
import diva.canvas.*;
import diva.graph.*;
import diva.graph.layout.GlobalLayout;
import diva.graph.layout.GridAnnealingLayout;
import diva.graph.layout.LevelLayout;
import diva.graph.layout.RandomLayout;
import diva.graph.toolbox.DeletionListener;
import javax.swing.JFrame;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * The graph demo demonstrates basic graph editing and layout
 * functionality, illustrates the key points of the graph
 * architecture. A graph is constructed programmatically, and can then
 * be edited interactively by the user. There are two views of the
 * graph: one which has an automatic layout algorithm applied each
 * time a new node is added, and one which uses a random or
 * user-driven layout. <p>
 *
 * The interaction and display in the graph editor, although currently
 * fairly simple, uses the features of the Diva canvas to good
 * effect. The use of two views of the graph highlights the
 * Swing-style model-view-controller architecture of the graph
 * package.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @author Steve Neuendorffer  (neuendor@eecs.berkeley.edu)
 * @version $Revision: 1.8 $
 * @rating Red
 */
public class BasicGraphDemo {
    /**
     * Construct a new instance of graph demo, which does the work of
     * setting up the graphs and displaying itself.
     */
    public static void main(String argv[]) {
	AppContext context = new BasicFrame("Basic Graph Demo");
	new BasicGraphDemo(context);
    }
    
    public BasicGraphDemo(AppContext context) {
	final BasicGraphModel model = new BasicGraphModel();
	JGraph jg = new JGraph(new GraphPane(new BasicGraphController(), 
					     model));
	JGraph jg2 = new JGraph(new GraphPane(new BasicGraphController(), 
                model));
        
	context.getContentPane().setLayout(new GridLayout(2, 1));
	context.getContentPane().add(jg);
	context.getContentPane().add(jg2);

        /*
        GraphController controller = jg.getGraphPane().getGraphController();
        final BasicLayoutTarget target = new BasicLayoutTarget(controller);
        JButton but = new JButton("Layout");
        but.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                //                GlobalLayout l = new GridAnnealingLayout();
                GlobalLayout l = new LevelLayout();
                l.layout(target, model.getRoot());
	    }
        });
        context.getContentPane().add("South", but);

	ActionListener deletionListener = new DeletionListener();
        jg.registerKeyboardAction(deletionListener, "Delete",
                KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        jg.setRequestFocusEnabled(true);
        */
        
        context.setSize(600, 400);
        context.setVisible(true);
    }
}







