/*
 * $Id: AppletContext.java,v 1.4 2001/07/22 22:01:29 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.event.ActionEvent;
import java.awt.Component;
import java.awt.Image;

import java.util.EventListener;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuBar;
import javax.swing.UIManager;

import javax.swing.JApplet;
import javax.swing.event.EventListenerList;

/**
 * A context for an applet in the diva.gui infrastructure.   
 * Generally, all this class does is
 * to pipe the AppContext method through to the appropriate method in the
 * JFrame class.  The exit action will be fired when the applet's 
 * destroy method is executed.  The default exit action does nothing.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.4 $
 */
public class AppletContext extends JApplet implements AppContext {
    /** 
     * The action that is called when this exits.
     */
    private transient Action _exitAction;

    /** 
     * The application that owns this frame
     */
    private transient Application _application;

    /**
     * The icon that is displayed in internal frames
     */
    private transient Image _iconImage;

    /**
     * The title of the context.
     */
    private transient String _title;

    /** 
     * Create a new context and set the exit action to do nothing.
     */
    public AppletContext() {
        _exitAction = new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    //do nothing?
                }
            };
	try {
	    UIManager.setLookAndFeel(
		UIManager.getSystemLookAndFeelClassName());
	} catch (Exception ex) {}
    }

    /**
     * Override the superclass's destroy method
     * to call the user-specified exit action.
     */
    public void destroy() {
        _exitAction.actionPerformed(null);
        super.destroy();
    }
    
    /**
     * Return the action that is called back when the user
     * exits the app.
     */
    public Action getExitAction() {
        return _exitAction;
    }

    /** 
     * Get the image that represents this frame.
     */
    public Image getIconImage () {
	return _iconImage;
    }

    /**
     * Return the title of the context.
     */
    public String getTitle () {
	return _title;
    }

    /**
     * Return the menu bar that the container uses.
     */
    public JMenuBar getJMenuBar() {
        return super.getJMenuBar();
    }

    /**
     * Return "this" as a component.
     */
    public Component makeComponent() {
        return this;
    }
    
    /**
     * Show the given status string at the bottom of
     * the context.  This base class does nothing.
     */
    public void showStatus(String status) {
        super.showStatus(status);
    }

    /**
     * Set the action that is called back when the user
     * exits the app.
     */
    public void setExitAction(Action exitAction) {
        _exitAction = exitAction;
    }
    
    /**
     * Set the title of the context.  This has no significance in an
     * applet context.
     */
    public void setTitle (String title) {
	_title = title;
    }

    /** 
     * Set the icon that represents this frame.
     */
    public void setIconImage (Image image) {
	_iconImage = image;
    }

    /**
     * Set the menu bar that the container uses.
     */
    public void setJMenuBar(JMenuBar menu) {
        super.setJMenuBar(menu);
    }

    /**
     * Do nothing.
     */
    public void setSize(int w, int h) {
    }

    /**
     * Do nothing.  Applets are always visible.
     */
    public void setVisible(boolean visible) {
    }

    /**
     * Return true.  Applets are always visible.
     */
    public boolean isVisible() {
        return true;
    }
}



