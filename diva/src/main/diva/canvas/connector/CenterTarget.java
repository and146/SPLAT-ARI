/*
 * $Id: CenterTarget.java,v 1.4 2001/07/22 22:00:31 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.canvas.connector;

import diva.canvas.Figure;
import diva.canvas.Site;
import diva.util.Filter;

import java.util.HashMap;

/** An implementation of connector targets that finds center sites.
 *
 * @version $Revision: 1.4 $
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 */
public class CenterTarget extends AbstractConnectorTarget {

    /** The mapping from figures to sites. Ignore the
     * problem that none of the contents will ever get
     * garbage-collected.
     */
    private HashMap _siteMap = new HashMap();

    /** Return a center site located on the figure, if the figure is not a
     * connector.
     */
    public Site getHeadSite (Figure f, double x, double y) {
        if (!(f instanceof Connector)) {
            if (_siteMap.containsKey(f)) {
                return (Site) _siteMap.get(f);
            } else {
                Site s = new CenterSite(f);
                _siteMap.put(f, s);
                return s;
            }
        }
        return null;
    }
}


