/*
 * $Id: AbstractView.java,v 1.2 2001/07/22 22:01:29 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;
import java.awt.datatransfer.Clipboard;
import javax.swing.JComponent;

/**
 * An abstract implementation of the View interface that consists of
 * mostly empty methods to be filled in by concrete subclasses.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.2 $
 * @rating Red
 */
public abstract class AbstractView implements View {
    /** The document being viewed.
     */
    private Document _document;

    /**
     * Construct a view of the given document.
     */
    public AbstractView(Document doc) {
        _document = doc;
    }
    
    /** Close the view.
     * @exception Exception If the close operation fails.
     */
    public void close () throws Exception {
    }

    /** Get the currently selected objects from this view, if any,
     * and place them on the given clipboard.  If the view does not
     * support such an operation, then do nothing. 
     */
    public void copy (Clipboard c) {
    }
 
    /** Remove the currently selected objects from this view, if any,
     * and place them on the given clipboard.  If the view does not
     * support such an operation, then do nothing.
     */
    public void cut (Clipboard c) {
    }

    /** Return the component that implements the display of this view.
     * The returned object should be a unique object that corresponds
     * to this view.
     */
    public abstract JComponent getComponent ();
    
    /** Get the document that this view is viewing.
     */
    public Document getDocument () {
        return _document;
    }

    /** Get the title of this document
     */
    public abstract String getTitle();

    /** Get the short title of this document. The short title
     * is used in situations where the regular title is likely
     * to be too long, such as iconified windows, menus, and so on.
     */
    public abstract String getShortTitle();

    /** Clone the objects currently on the clipboard, if any, and
     * place them in the given view.  If the document does not support
     * such an operation, then do nothing.  This method is responsible
     * for copying the data.
     */
    public void paste (Clipboard c) {
    }
}


