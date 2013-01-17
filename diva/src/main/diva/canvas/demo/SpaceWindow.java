/*
 * $Id: SpaceWindow.java,v 1.4 2001/07/22 22:00:34 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.demo;

import diva.canvas.*;
import diva.canvas.event.*;
import diva.gui.toolbox.JPanner;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * SpaceWindow is the JFrame that is used to display the complete demo.
 * It contains a small set of menu entries, and some sliders to
 * control rotation and shear of the top-level transform, which
 * cannot be done using the gesture-based navigation.
 *
 * @author John Reekie
 * @version $Revision: 1.4 $
 */
public class SpaceWindow extends JFrame {
    // My menubar
    JMenuBar menubar = null;

    // My panner
    JPanner panner = null;

    // The right-side panel
    JPanel rightPanel;

    // The control panel
    JPanel controlPanel;

    // The text widget
    JEditorPane textPanel;

    // The canvas
    JCanvas canvas;

    // The sliders
    //JSlider scaleSlider;
    JSlider rotationSlider;
    //JSlider xoffsetSlider;
    //JSlider yoffsetSlider;
    JSlider xshearSlider;
    JSlider yshearSlider;

    // The slider values
    //int scale = 100;
    int rotation = 0;
    //int xoffset = 0;
    //int yoffset = 0;
    int xshear = 0;
    int yshear = 0;

    // Constructor -- create the Frame and give it a title.
    // Pack the menubar
    public SpaceWindow (String title) {
        super(title);

        // Create the layout manager and set up constraints
        // and the main panels
        getContentPane().setLayout(new BorderLayout());

        rightPanel = new JPanel();
        rightPanel.setLayout(new GridLayout(3, 1));
        getContentPane().add(rightPanel, BorderLayout.EAST);

        controlPanel = new JPanel();
        rightPanel.add(controlPanel);

        textPanel = new JEditorPane();
        rightPanel.add(textPanel);

        this.panner = new JPanner();
        rightPanel.add(panner);
        
        // Create the transform sliders
        //scaleSlider = new JSlider(JSlider.VERTICAL, 20, 200, 100);
        //scaleSlider.addChangeListener(new ChangeListener() {
        //    public void stateChanged (ChangeEvent e) {
        //        scale = scaleSlider.getValue();
        //        updateTransform();
        //    }
        //});
        rotationSlider = new JSlider(JSlider.VERTICAL, -100, 100, 0);
        rotationSlider.addChangeListener(new ChangeListener() {
            public void stateChanged (ChangeEvent e) {
                rotation = rotationSlider.getValue();
                updateTransform();
            }
        });
        //xoffsetSlider = new JSlider(JSlider.VERTICAL, 0, 400, 0);
        //xoffsetSlider.addChangeListener(new ChangeListener() {
        //    public void stateChanged (ChangeEvent e) {
        //        xoffset = xoffsetSlider.getValue();
        //        updateTransform();
        //    }
        //});
        //yoffsetSlider = new JSlider(JSlider.VERTICAL, 0, 400, 0);
        //yoffsetSlider.addChangeListener(new ChangeListener() {
        //    public void stateChanged (ChangeEvent e) {
        //        yoffset = yoffsetSlider.getValue();
        //        updateTransform();
        //    }
        //});
        xshearSlider = new JSlider(JSlider.VERTICAL, -200, 200, 0);
        xshearSlider.addChangeListener(new ChangeListener() {
            public void stateChanged (ChangeEvent e) {
                xshear = xshearSlider.getValue();
                updateTransform();
            }
        });
        yshearSlider = new JSlider(JSlider.VERTICAL, -200, 200, 0);
        yshearSlider.addChangeListener(new ChangeListener() {
            public void stateChanged (ChangeEvent e) {
                yshear = yshearSlider.getValue();
                updateTransform();
            }
        });

        //controlPanel.add(scaleSlider);
        controlPanel.add(rotationSlider);
        //controlPanel.add(xoffsetSlider);
        //controlPanel.add(yoffsetSlider);
        controlPanel.add(xshearSlider);
        controlPanel.add(yshearSlider);

        // Create the menubar and set it
        setJMenuBar(createMenuBar());

        // Close the window on any window event
        addWindowListener(windowListener);

    }

    /**
     * Update the transform of the contained pane
     */
    public void updateTransform() {
        CanvasPane pane = canvas.getCanvasPane();
        AffineTransform t = new AffineTransform();

        // FIXME
        //t.translate(xoffset, yoffset);
        t.translate(200,150);
        t.shear(xshear/100.0, yshear/100.0);
        //t.scale(scale/100.0, scale/100.0);
        t.rotate(rotation/100.0 * java.lang.Math.PI);
        t.translate(-200,-150);

        pane.setTransform(t);
        pane.repaint();
    }

    /**
     * Set the displayed canvas component
     */
    public void setCanvas(JCanvas canvas) {
        // Remove old one
        AffineTransform t = null;
        if (this.canvas != null) {
            t = this.canvas.getCanvasPane().getTransformContext().getTransform();
            getContentPane().remove(canvas);
        }
        // Add new one
        this.canvas = canvas;
        JScrollPane scroll = new JScrollPane(canvas);
        getContentPane().add(scroll, BorderLayout.CENTER);
        this.panner.setViewport(scroll.getViewport());

        // Set current transform
        if (t != null) {
            // canvas.getCanvasPane().setTransform(t);
        }
    }

    /** Create the menubar
     */
    public JMenuBar createMenuBar () {
        JMenuBar menubar;
        JMenu menuFile;
        JMenu menuControl;
        JMenuItem itemClose;
        JMenuItem itemReset;

        // Create the menubar and menus
        menubar = new JMenuBar();

        menuFile = new JMenu("File");
        menuFile.setMnemonic('F');

        menuControl = new JMenu("Control");
        menuFile.setMnemonic('C');

        // Create the menu items
        itemClose = menuFile.add(actionClose);
        itemClose.setMnemonic('C');
        itemClose.setToolTipText("Close this window");
     
        itemReset = menuFile.add(actionReset);
        itemReset.setMnemonic('R');
        itemReset.setToolTipText("Reset zoom and pan");
     
        // Build the menus
        menubar.add(menuFile);

        return menubar;
    }

    /////////////////////////////////////////////////////////////////
    // The action classes
    //
    private Action actionClose = new AbstractAction ("Close") {
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    };

    private Action actionReset = new AbstractAction ("Reset") {
        public void actionPerformed(ActionEvent e) {
            rotation = 0;
            xshear = 0;
            yshear = 0;
            updateTransform();
        }
    };

    /////////////////////////////////////////////////////////////////
    // The window listener
    //
    WindowListener windowListener = new WindowAdapter() {
        public void windowClosing(WindowEvent e) {
            actionClose.actionPerformed(null);
        }
        public void windowIconified (WindowEvent e) {
            System.out.println(e);
        }
        public void windowDeiconified (WindowEvent e) {
            System.out.println(e);
        }
    };

}


