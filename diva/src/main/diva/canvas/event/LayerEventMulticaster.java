/*
 * $Id: LayerEventMulticaster.java,v 1.6 2001/07/22 22:00:35 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.event;

import java.awt.AWTEventMulticaster;
import java.util.EventListener;

/** A subclass of the AWT event multi-caster, which adds support
 * for layer events.
 *
 * @version	$Revision: 1.6 $
 * @author 	John Reekie
 */
public class LayerEventMulticaster extends AWTEventMulticaster implements LayerListener, LayerMotionListener {

    /**
     * Create an event multicaster from two listeners.
     */ 
    protected LayerEventMulticaster(EventListener a, EventListener b) {
        super(a, b);
    }

    /** Invoked when the mouse moves while the button is still held
     * down.
     */
    public void mouseDragged (LayerEvent e) {
        ((LayerListener)a).mouseDragged(e);
        ((LayerListener)b).mouseDragged(e);
    }

    /** Invoked when the mouse enters a layer or figure.
     */
    public void mouseEntered (LayerEvent e)  {
        ((LayerMotionListener)a).mouseEntered(e);
        ((LayerMotionListener)b).mouseEntered(e);
    }

    /** Invoked when the mouse exits a layer or figure.
     */
    public void mouseExited (LayerEvent e) {
        ((LayerMotionListener)a).mouseExited(e);
        ((LayerMotionListener)b).mouseExited(e);
    }
 
    /** Invoked when the mouse moves while over a layer or figure.
     */
    public void mouseMoved (LayerEvent e) {
        ((LayerMotionListener)a).mouseExited(e);
        ((LayerMotionListener)b).mouseExited(e);
    }
 
    /** Invoked when the mouse is pressed on a layer or figure.
     */
    public void mousePressed (LayerEvent e) {
        ((LayerListener)a).mousePressed(e);
        ((LayerListener)b).mousePressed(e);
    }
  
    /** Invoked when the mouse is released on a layer or figure.
     */
    public void mouseReleased (LayerEvent e) {
        ((LayerListener)a).mouseReleased(e);
        ((LayerListener)b).mouseReleased(e);
    }

    /** Invoked when the mouse is clicked on a layer or figure.
     */
    public void mouseClicked (LayerEvent e) {
        ((LayerListener)a).mouseClicked(e);
        ((LayerListener)b).mouseClicked(e);
    }
    
    /**
     * Adds layer-listener-a with layer-listener-b and
     * returns the resulting multicast listener.
     */
    public static LayerListener add(LayerListener a, LayerListener b) {
        return (LayerListener)addInternal(a, b);
    }


    /** 
     * Returns the resulting multicast listener from adding listener-a
     * and listener-b together.  
     * If listener-a is null, it returns listener-b;  
     * If listener-b is null, it returns listener-a
     * If neither are null, then it creates and returns
     * a new AWTEventMulticaster instance which chains a with b.
     * @param a event listener-a
     * @param b event listener-b
     */
    protected static EventListener addInternal(EventListener a, EventListener b) {
	if (a == null)  return b;
	if (b == null)  return a;
	return new LayerEventMulticaster(a, b);
    }

    /**
     * Adds layer-motion-listener-a with layer-motion-listener-b and
     * returns the resulting multicast listener.
     */
    public static LayerMotionListener add(
            LayerMotionListener a, LayerMotionListener b) {
        return (LayerMotionListener)addInternal(a, b);
    }

    /**
     * Removes the old layer-listener from layer-listener-l and
     * returns the resulting multicast listener.
     */
    public static LayerListener remove(LayerListener l, LayerListener oldl) {
	return (LayerListener) removeInternal(l, oldl);
    }

    /**
     * Removes the old layer-motion-listener from layer-motion-listener-l and
     * returns the resulting multicast listener.
     */
    public static LayerMotionListener remove(
            LayerMotionListener l, LayerMotionListener oldl) {
	return (LayerMotionListener) removeInternal(l, oldl);
    }

}



