/*
 * $Id: BasicFrame.java,v 1.14 2001/07/22 22:01:29 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * BasicFrame is a very simple application context that is used
 * to display tutorial examples.  It contains a menubar with a quit
 * entry and a location for the main content pane. This kind of frame
 * doesn't understand applications, and should therefore be used only
 * for the simplest examples and tutorials.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @author Nick Zamora (nzamor@eecs.berkeley.edu)
 * @version $Revision: 1.14 $
 */
public class BasicFrame extends ApplicationContext {

    /** The component displaying the content
     */
    private transient JComponent _component = null;

    /** Create an instance of this Frame with the given title
     * and no main component.
     */
    public BasicFrame(String title) {
        this(title, null);
    }

    /** Create an instance of this Frame with the given title
     * and no main component.
     */
    public BasicFrame(String title, boolean show_and_size_window) {
        this(title, null, show_and_size_window);
    }
    public BasicFrame(String title, JComponent component, boolean show_and_size_window) {
        super();
	
        setTitle(title);
	setJMenuBar(new JMenuBar());

        // Create the menus
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');
        
	// NOT a default action.
	Action action = new AbstractAction ("Exit") {
            public void actionPerformed(ActionEvent e) {
		System.exit(0);
	    }
	};
	setExitAction(action);

        JMenuItem itemQuit = menuFile.add(action);
        itemQuit.setMnemonic('E');
        itemQuit.setToolTipText("Exit this application");

        getJMenuBar().add(menuFile);

        // Add the content pane and make the frame visible
        if (component != null) {
            setMainComponent(component);
        }
	if (show_and_size_window) {
	    setSize(600,400);
	    setVisible(true);
	}
    }

    /** Create an instance of this Frame with the given title
     * and with the given main component. The component can be
     * null if desired.
     */
    public BasicFrame(String title, JComponent component) {
        super();
	
        setTitle(title);
	setJMenuBar(new JMenuBar());

        // Create the menus
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');
        
	// NOT a default action.
	Action action = new AbstractAction ("Exit") {
            public void actionPerformed(ActionEvent e) {
		System.exit(0);
	    }
	};
	setExitAction(action);

        JMenuItem itemQuit = menuFile.add(action);
        itemQuit.setMnemonic('E');
        itemQuit.setToolTipText("Exit this application");

        getJMenuBar().add(menuFile);

        // Add the content pane and make the frame visible
        if (component != null) {
            setMainComponent(component);
        }
        setSize(600,400);
        setVisible(true);
    }

    /** Set the main component. If there already is one,
     * it is removed first.
     */
    public void setMainComponent (JComponent component) {
        if (_component != null) {
            getContentPane().remove(_component);
        }
        if (component != null) {
            getContentPane().add("Center", component);
        }
        this._component = component;
    }
}


