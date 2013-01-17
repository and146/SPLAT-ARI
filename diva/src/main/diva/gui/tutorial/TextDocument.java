/*
 * $Id: TextDocument.java,v 1.3 2001/07/22 22:01:34 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui.tutorial;

import diva.gui.*;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.Writer;

import java.net.URL;
import java.util.Iterator;

import java.awt.datatransfer.*;
import javax.swing.event.ListDataListener;

/**
 * A example document that contains plain boring old text 
 * and saves it to ascii files.  Here you can see some sample
 * implementations of the open, save, saveAs, and close methods.
 * You should also notice that this class adds methods for accessing 
 * the contained document's data in an application-useful format.  
 * (In this case, there are just simple getText and setText methods.)
 * The setText properly realized that the document's data has been changed
 * and sets the Dirty flag.  This is used in the application
 * tutorial by the StoragePolicy to prevent a user from closing the document
 * without saving changes.
 * <p>
 * This class also contains a DocumentFactory for documents of this type.
 * The document factory is used by an application to create documents of 
 * this type.  
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision: 1.3 $
 */
public class TextDocument extends AbstractDocument {

    /** The string contained in this document.
     */
    String _text;

    /** Create an text document for the given application containing an 
     * empty string.
     */
    public TextDocument(Application application) {
	super(application);
	_text = "";
    }

    /** Close the document. Do not attempt to save the document first
     * or do any other user-interface things like that. This method
     * can thrown an exception if there is a failure, but it should
     * only do do if there is no way it can recover. Note that actions
     * such as querying the user to save a modified document and so on
     * are the responsibility of the application, not the Document
     * implementation.
     *
     * @exception Exception If the close operation fails.
     */
    public void close () {
	// DO NOTHING.
    }

    /** Return the text contained in this document.
     */
    public String getText() {
	return _text;
    }

    /** Open the document from its current file or URL. Throw an
     * exception if the operation failed.
     *
     * @exception Exception If the close operation fails.
     */
    public void open () throws Exception {
	BufferedReader reader = new BufferedReader(new FileReader(getFile()));
	char[] buffer = new char[100];
	StringBuffer readResult = new StringBuffer();
	int amountRead;
	while ((amountRead = reader.read(buffer, 0, 100)) == 100) {
	    readResult.append(buffer);
	}
	readResult.append(buffer, 0, amountRead);
	_text = readResult.toString();
    }	

    /** Save the document to its current file or URL.  Throw an
     * exception if the operation failed. Reasons for failure might
     * include the fact that the file is not writable, or that the
     * document has a URL but we haven't implemented HTTP-DAV support
     * yet...
     *
     * @exception Exception If the save operation fails.
     */
    public void save () throws Exception {
	saveAs(getFile());
    }

    /** Save the document to the given file.  Throw an exception if
     * the operation failed. Return true if successful, false if
     * not. Do <i>not</i> change the file attribute to the new File
     * object as that is the responsibility of the application, which
     * it will do according to its storage policy.
     *
     * @see #save()
     * @exception Exception If the save-as operation fails.
     */
    public void saveAs (File file) throws Exception {
	Writer writer = new BufferedWriter(new FileWriter(file));
	writer.write(_text);
	writer.flush();
    }

    /** Save the document to the given URL.  Throw an exception if the
     * operation failed.  Do <i>not</i> change the URL attribute to
     * the new URL object as that is the responsibility of the
     * application, which it will do according to its storage policy.
     *
     * @see #save()
     * @exception Exception If the save-as operation fails.
     */
    public void saveAs (URL url) throws Exception {
	throw new UnsupportedOperationException("Saving as a URL is not" +
						" supported for" +
						" text documents.");
    }

    /**
     * Set the text contained by this document.  If the given text is 
     * different from the previously contained text, then set the dirty flag.
     */
    public void setText(String text) {
	if(_text != text) {
	    setDirty(true);
	    _text = text;
	}
    }

    /** TextDocument.Factory is a factory for Text Documents
     */
    public static class Factory implements DocumentFactory {
        /** Create an empty document.
         */
        public Document createDocument (Application app) {
            TextDocument d = new TextDocument(app);
            return d;
        }

        /** Create a new document that contains data from the given URL.
         */
        public Document createDocument (Application app, URL url) {
            TextDocument d = new TextDocument(app);
            d.setURL(url);
            return d;
        }

        /** Create a new document that contains data from the given file.
         */
        public Document createDocument (Application app, File file) {
            TextDocument d = new TextDocument(app);
            d.setFile(file);
            return d;
        }
    }
}


