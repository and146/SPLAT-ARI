/*
 * $Id: GrabHandleFactory.java,v 1.7 2001/07/22 22:00:38 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.canvas.interactor;

import diva.canvas.Site;

/**
 * A factory so that a client can create grab handles without
 * knowing anything about their implementation.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @version	$Revision: 1.7 $
 */
public interface GrabHandleFactory {
    /** Create a grab-handle that is attached to the given
     * site. The grab-handle will be located so that its
     * attachment point is at the site.
     */
    public GrabHandle createGrabHandle (Site s);
}


