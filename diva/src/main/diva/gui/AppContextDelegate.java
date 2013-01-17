/*
 * $Id: AppContextDelegate.java,v 1.2 2001/07/22 22:01:29 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Image;
import javax.swing.Action;
import javax.swing.JLayeredPane;
import javax.swing.JMenuBar;

import javax.swing.JRootPane;

/**
 * This class provides basic support for an instance of AppContext which 
 * delegates its operation to one of the basic AppContexts (usually an
 * AppContext or an ApplicationContext.)  This is similar to an interface
 * adapter, except the default implementation of each method is to 
 * call the identical method on the delegate context.  
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision: 1.2 $
 */
public class AppContextDelegate implements AppContext {

    /** The app-context that implements the windowing facilities.
     */
    private transient AppContext _delegate;

    /** Create a new app context that delegates to the given context.
     */
    public AppContextDelegate(AppContext context) {
	_delegate = context;
    }

    /**
     * Returns the contentPane of the delegate.
     */
    public Container getContentPane() {
        return _delegate.getContentPane();
    }

    /**
     * Return the context delegate.
     */
    public AppContext getDelegate() {
	return _delegate;
    }
    
    /**
     * Returns the glassPane of the delegate.
     */
    public Component getGlassPane() {
        return _delegate.getGlassPane();
    }

    /**
     * Returns the layeredPane of the delegate.
     */
    public JLayeredPane getLayeredPane() {
        return _delegate.getLayeredPane();
    }
       
    /**
     * Returns the rootPane of the delegate.
     */
    public JRootPane getRootPane() {
        return _delegate.getRootPane();
    }

    /**
     * Return the title of the context.
     */
    public String getTitle () {
	return _delegate.getTitle();
    }

    /**
     * Set the content pane of the delegate.  The "contentPane" is the
     * primary container for application specific components.
     */
    public void setContentPane(Container contentPane) {
        _delegate.setContentPane(contentPane);
    }
    
    
    /**
     * Set the glassPane of the delegate.  The glassPane is always the
     * first child of the rootPane and the rootPanes layout manager
     * ensures that it's always as big as the rootPane.
     */
    public void setGlassPane(Component glassPane) {
        _delegate.setGlassPane(glassPane);
    }

    /**
     * Set the layerd pane of the delegate.  A Container that manages
     * the contentPane and in some cases a menu bar
     */
    public void setLayeredPane(JLayeredPane layeredPane) {
        _delegate.setLayeredPane(layeredPane);
    }

    /**
     * Set the exit action of the delegate.
     */
    public void setExitAction(Action action) {
        _delegate.setExitAction(action);
    }

    /**
     * Return the exit action of the delegate.
     */
    public Action getExitAction() {
        return _delegate.getExitAction();
    }

    /**
     * Set the image icon of the delegate.
     */
    public void setIconImage(Image image) {
        _delegate.setIconImage(image);
    }

    /**
     * Return the image icon of the delegate.
     */
    public Image getIconImage() {
        return _delegate.getIconImage();
    }

    /**
     * Return the menu bar of the delegate.
     */
    public JMenuBar getJMenuBar() {
        return _delegate.getJMenuBar();
    }

    /**
     * Set the menu bar of the delegate.
     */
    public void setJMenuBar(JMenuBar menu) {
        _delegate.setJMenuBar(menu);
    }

    /**
     * Show the status in the delegate.
     */
    public void showStatus(String status) {
        _delegate.showStatus(status);
    }

    /**
     * Set the size in the delegate.
     */
    public void setSize(int w, int h) {
        _delegate.setSize(w, h);
    }

    /**
     * Set the title of the context.  This has no significance in an
     * applet context.
     */
    public void setTitle (String title) {
	_delegate.setTitle(title);
    }

    /**
     * Invoke the delegate's setvisible().
     */
    public void setVisible(boolean visible) {
        _delegate.setVisible(visible);
    }

    /**
     * Invoke the delegate's isvisible().
     */
    public boolean isVisible() {
        return _delegate.isVisible();
    }

    /**
     * Call makeComponent() on the delegate.
     */
    public Component makeComponent() {
        return _delegate.makeComponent();
    }
}



