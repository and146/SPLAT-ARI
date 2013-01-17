/*
 * $Id: View.java,v 1.2 2001/07/22 22:01:31 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui;
import java.awt.datatransfer.Clipboard;
import javax.swing.JComponent;

/**
 * View is an interface that captures the notion of a view on a
 * document in a graphical application.  Typically, there are one or
 * more views on a particular document, or even on different parts of
 * the document (such as discrete pages), although this is not
 * necessarily so.  Each application will typically create one or more
 * implementations of this interface for the types of documents it can
 * work with.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.2 $
 * @rating Red
 */
public interface View {
    /** Close the view.
     *
     * @exception Exception If the close operation fails.
     */
    public void close () throws Exception;

    /** Get the currently selected objects from this view, if any,
     * and place them on the given clipboard.  If the view does not
     * support such an operation, then do nothing. 
     */
    public void copy (Clipboard c);
 
    /** Remove the currently selected objects from this view, if any,
     * and place them on the given clipboard.  If the view does not
     * support such an operation, then do nothing.
     */
    public void cut (Clipboard c);

    /** Return the component that implements the display of this view.
     */
    public JComponent getComponent ();
    
    /** Get the document that this view is viewing.
     */
    public Document getDocument (); 

    /** Get the title of this document
     */
    public String getTitle();

    /** Get the short title of this document. The short title
     * is used in situations where the regular title is likely
     * to be too long, such as iconified windows, menus, and so on.
     */
    public String getShortTitle();

    /** Clone the objects currently on the clipboard, if any, and
     * place them in the given view.  If the document does not support
     * such an operation, then do nothing.  This method is responsible
     * for copying the data.
     */
    public void paste (Clipboard c);
}


