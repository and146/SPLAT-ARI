/*
 * $Id: SelectionModel.java,v 1.8 2001/07/22 22:00:39 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.interactor;

import java.util.Iterator;

/**
 * A model for graph selections.  Listeners can add themselves
 * to the model and are notified by SelectionEvents when
 * the model is modified.
 *
 * @author 	Michael Shilman (michaels@eecs.berkeley.edu)
 * @version	$Revision: 1.8 $
 */
public interface SelectionModel {
    /**
     * Allow only one screen object to
     * be selected at a time.
     */
    public static int SINGLE_SELECTION = 0;

    /**
     * Allow multiple screen objects to be
     * selected at once.
     */
    public static int MULTIPLE_SELECTION = 1;

    /**
     * Add a selection listener to this model.
     */
    public void addSelectionListener(SelectionListener l);

    /**
     * Add an object to the selection. The model should highlight
     * the selection.
     */
    public void addSelection(Object sel);

    /**
     * Add an array of objects to the selection. the model should
     * highlight the selected objects.
     */
    public void addSelections(Object[] sel);

    /**
     * Clear the selection. The model should remove highlighting from
     * the previously selected objects.
     */
    public void clearSelection();

    /**
     * Test if the selection contains the given object
     */
    public boolean containsSelection(Object sel);

    /**
     * Return the first selection in the list.
     */
    public Object getFirstSelection();

    /**
     * Return the last selection in the list.
     */
    public Object getLastSelection();

    /**
     * Return an iterator over the selected objects.
     */
    public Iterator getSelection();

    /**
     * Return the contents of the selection as an array.
     * This method is typically used by clients that
     * need to pass the selection contents to another object
     * that does not know about selection models, and which needs
     * to traverse the selection contents multiple times.
     */
    public Object[] getSelectionAsArray();

    /**
     * Return the number of selected objects.
     */
    public int getSelectionCount();

    /**
     * Return the mode of the selection, either
     * SINGLE_SELECTION or MULTIPLE_SELECTION.
     */
    public int getSelectionMode();

    /**
     * Remove a listener from the list of listeners.
     */
    public void removeSelectionListener(SelectionListener l);

    /**
     * Remove an object from the selection. The model should
     * also remove highlighting from the objects.
     */
    public void removeSelection(Object sel);

    /**
     * Set the selection mode, either
     * SINGLE_SELECTION or MULTIPLE_SELECTION.
     */
    public void setSelectionMode(int mode);

}


