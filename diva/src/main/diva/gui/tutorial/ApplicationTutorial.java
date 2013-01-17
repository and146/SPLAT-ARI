/*
 * $Id: ApplicationTutorial.java,v 1.3 2001/07/22 22:01:33 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui.tutorial;

import diva.resource.RelativeBundle;
import diva.resource.DefaultBundle;

import diva.gui.*;
import diva.gui.toolbox.ListDataModel;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.Image;
import java.awt.datatransfer.Clipboard;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.filechooser.FileFilter;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;

/**
 * A simple text editor.  This application implements a very simple
 * mapping between view and documents.  There is only one view.
 * Documents that are not currently being viewed are hidden and not available
 * until the currently viewed document is closed.  When a new document
 * is to be viewed, the single view is associated with that document and
 * its contents updated.
 * <p>
 * This kind of architecture is enough for simple applications that contain
 * few documents (or more likely, only one document).  For applications
 * that require simultaneous editing of several documents, an MDI interface
 * makes more sense.  See MDIApplicationTutorial for an instance of the
 * same application implemented in an MDI style.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision: 1.3 $
 */
public class ApplicationTutorial extends AbstractApplication {

    /**
     * The text component that will edit our text.
     */
    private JEditorPane _editorPane;

    /**
     * The document currently being edited in the editor pane.
     */
    private TextDocument _displayedDocument;

    /**
     * Construct a new instance of the Tutorial, running in a new
     * application context.
     */
    public static void main(String argv[]) {
	AppContext context = new ApplicationContext();
	new ApplicationTutorial(context);
    }

    public ApplicationTutorial(AppContext context) {
	super(context);
	
	initializeApp();

	// Initialize the context.
	JMenuBar menuBar = new JMenuBar();
	context.setJMenuBar(menuBar);
	initializeMenuBar(menuBar);
	
	JToolBar toolBar = new JToolBar();
	context.getContentPane().add(toolBar, BorderLayout.NORTH);
	initializeToolBar(toolBar);

	// Set the icon in the upper left corner of the context.
	Image iconImage = getResources().getImage("GraphIconImage");
	context.setIconImage(iconImage);

        /*
	// create the editing component.
	_displayedDocument = null;
	displayDocument(getCurrentDocument());
        */
            
	// When the currently selected document is changed, this listener
	// is notified.
	addViewListener(new ListDataListener() {
	    public void contentsChanged(ListDataEvent e) {
		System.out.println("current document = " +
				   getCurrentView().getDocument());
                setCurrentView(getCurrentView());//FIXME
	    }
	    public void intervalAdded(ListDataEvent e) {
	    }
	    public void intervalRemoved(ListDataEvent e) {
	    }
	});

	// Set the size of the context.
	context.makeComponent().setSize(800, 600);
	// and make it visible.
	context.setVisible(true);
    }

    /** Return a view of this document.
     */
    public View createView(Document d) {
        return new TextView((TextDocument)d);
    }

    /** Display the given document. The document should already be
     * added to the application. After calling this method, most
     * callers should set this document to be the current document.
     *
    public void displayDocument (Document d) {
	// only handle text documents.
	TextDocument document = (TextDocument) d;
	System.out.println("displaying");
	
	if(_displayedDocument != null) {
	    // Pull the text out of the editor and stuff it back into the 
	    // document.
	    _displayedDocument.setText(_editorPane.getText());
	}

	_displayedDocument = document;

	if(document != null) {
	    // Pull the text out of the new document and stuff it 
	    // into the editor.
	    _editorPane.setText(document.getText());
	    _editorPane.setEditable(true);
	} else {
	    // If we've closed all the documents, then disable the 
	    // editor pane.
	    _editorPane.setText("");
	    _editorPane.setEditable(false);
	}
    }
    */

    /** Get the title of this application
     */
    public String getTitle() {
	return "Application Tutorial";
    }

    /** Initialize the application.
     */
    public void initializeApp() {
	// Create and initialize the storage policy
	try {
	    DefaultStoragePolicy storage = new DefaultStoragePolicy();
	    setStoragePolicy(storage);
	
	    FileFilter ff = new FileFilter() {
		public boolean accept (File file) {
		    return GUIUtilities.getFileExtension(file).
		    toLowerCase().equals("txt");
		}
		public String getDescription () {
		    return "Text files";
		}
	    };
	    JFileChooser fc;      
	    fc = storage.getOpenFileChooser();
	    fc.addChoosableFileFilter(ff);
	    fc.setFileFilter(ff);
	    
	    fc = storage.getSaveFileChooser();
	    fc.addChoosableFileFilter(ff);
	    fc.setFileFilter(ff);
	} catch (SecurityException ex) {
	    // FIXME: create a new "NoStoragePolicy"
	}

        setDocumentFactory(new TextDocument.Factory());
    }

    /** Initialize the menu bar
     */
    public void initializeMenuBar(JMenuBar menuBar) {
	Action action;
        JMenuItem item;

        // Create the File menu
        JMenu menuFile = new JMenu("File");
        menuFile.setMnemonic('F');
        menuBar.add(menuFile);

        action = DefaultActions.newAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'N', 
				 "Create a new graph document");

        action = DefaultActions.openAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'O', 
				 "Open a graph document");

        action = DefaultActions.closeAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'C', 
				 "Close the current graph document");

        menuFile.addSeparator();

        action = DefaultActions.saveAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'S', 
				 "Save the current graph document");

        action = DefaultActions.saveAsAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'A', 
                "Save the current graph document to a different file");

        menuFile.addSeparator();

        action = DefaultActions.exitAction(this);
        addAction(action);
        GUIUtilities.addMenuItem(menuFile, action, 'X', 
				 "Exit from the graph editor");
	// Hook the exit action into the frame's close button, if we are
	// running in an ApplicationContext.
	getAppContext().setExitAction(action);
    }

    /** Initialize the given toolbar. Image icons will be obtained
     * from the ApplicationResources object and added to the
     * actions. Note that the image icons are not added to the actions
     * -- if we did that, the icons would appear in the menus, which I
     * suppose is a neat trick but completely useless.
     */
    public void initializeToolBar (JToolBar tb) {
        Action action;
        RelativeBundle resources = getResources();

        // Conventional new/open/save buttons
        action = getAction("New");
        GUIUtilities.addToolBarButton(tb, action, null,
				      resources.getImageIcon("NewImage"));

        action = getAction("Open");
        GUIUtilities.addToolBarButton(tb, action, null, 
				      resources.getImageIcon("OpenImage"));

        action = getAction("Save");
        GUIUtilities.addToolBarButton(tb, action, null, 
				      resources.getImageIcon("SaveImage"));
    }
}


