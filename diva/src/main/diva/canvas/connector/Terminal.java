/*
 * $Id: Terminal.java,v 1.3 2001/07/22 22:00:33 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import diva.canvas.Figure;
import diva.canvas.Site;
import java.awt.geom.Point2D;

/** A Terminal is a figure that provides a visible place for connectors
 * to connect to. In general, connectors are able to connect to
 * anything, but in certain types of diagrams, such as circuit
 * schematics, connectors are expected to connect to objects
 * that are clearly identifiable as a "connection point." Terminals
 * serve this purpose.
 *
 * <p> The terminal interface exposes access to two sites: The
 * "attach" site is the site on some other figure that the terminal
 * positions itself on, and the "connect" site is the site
 * that connectors can connect to.
 * 
 * @version $Revision: 1.3 $
 * @author  John Reekie (johnr@eecs.berkeley.edu)
 */
public interface Terminal extends Figure {

    /** Get the site that the terminal is attached to.
     */
    public Site getAttachSite ();

    /** Get the site that a connector can connect to.
     */
    public Site getConnectSite ();

    /** Tell the terminal to relocate itself because the
     * attachment site (or the figure that owns it) has moved.
     */
    public void relocate ();

    /** Set the site that the terminal is attached to.
     */
    public void setAttachSite (Site s);
}


