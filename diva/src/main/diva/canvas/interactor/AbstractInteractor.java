/*
 * $Id: AbstractInteractor.java,v 1.8 2001/07/24 06:33:00 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.canvas.interactor;

import diva.canvas.event.LayerEvent;
import diva.canvas.event.LayerEventMulticaster;
import diva.canvas.event.LayerListener;
import diva.canvas.event.LayerMotionListener;
import diva.canvas.event.MouseFilter;

import java.awt.event.MouseEvent;
import java.util.Hashtable;

/** 
 * An abstract class that implements Interactor. This class provides
 * simple implementations of most of the required methods of
 * interactors. The listener methods are all implemented,
 * to make it easier to implement interactor subclasses.
 *
 * @version $Revision: 1.8 $
 * @author John Reekie
 */
public abstract class AbstractInteractor implements Interactor {

    /** The consuming flag.
     */
    private boolean _consuming = true;

    /** The flag that says that layer events are enabled
     */
    private boolean _enabled = true;

    /** The flag that says that layer motion events are enabled
     */
    private boolean _motionEnabled = false;

    /** The mouse filter
     */
    private MouseFilter _mouseFilter = MouseFilter.defaultFilter;

    /** Test if the interactor accepts the given event. This default
     * implementation returns true if the interactor is enabled
     * and the mouse filter accepts the event.
     */
    public boolean accept (LayerEvent event) {
        return isEnabled() && getMouseFilter().accept(event);
    }

    /** Get the mouse filter used by this interactor to
     * decide whether to accept an event. The result may
     * be null.
     */
    public MouseFilter getMouseFilter () {
        return _mouseFilter;
    }

    /** Test the consuming flag of this interactor. If this flag is
     * set, the interactor consumes all input events that get past its
     * mouse filter.
     */
    public boolean isConsuming () {
        return _consuming;
    }

    /** Test the enabled flag of this interactor. If true, the interactor
     * is prepared to handle layer events.
     */
    public boolean isEnabled () {
        return _enabled;
    }

    /** Test the motion enabled flag of this interactor. If true, the
     * interactor is prepared to handle layer motion events.
     */
    public boolean isMotionEnabled () {
        return _motionEnabled;
    }

    /** Do nothing.
     */
    public void mouseDragged (LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    public void mouseEntered (LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    public void mouseExited (LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    public void mouseMoved (LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    public void mousePressed (LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    public void mouseReleased (LayerEvent layerEvent) {
        // empty
    }

    /** Do nothing.
     */
    public void mouseClicked (LayerEvent layerEvent) {
        //empty
    }

    /** Set the consuming flag of this layer.  If this flag is
     * set, the interactor consumes all events that get past its mouse
     * filter. By default, the flag is true.
     */
    public void setConsuming (boolean flag) {
        _consuming = flag;
    }

    /** Set the enabled flag of this interactor.  If true, the interactor
     * is prepared to handle layer events. The default setting
     * of this flag is <b>true</b>.
     */
    public void setEnabled (boolean flag) {
        _enabled = flag;
    }

    /** Set the motion enabled flag of this interactor.  If true, the
     * interactor is prepared to handle layer motion events. The default
     * setting of this flag is <b>false</b>.
     */
    protected void setMotionEnabled (boolean flag) {
        _motionEnabled = flag;
    }

    /** Set the mouse filter of this interactor.
     */
    public void setMouseFilter (MouseFilter filter) {
        _mouseFilter = filter;
    }
}


