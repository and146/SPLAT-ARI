/*
 * $Id: ConnectorTarget.java,v 1.4 2001/07/22 22:00:32 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */
package diva.canvas.connector;

import diva.canvas.Figure;
import diva.canvas.Site;

/** An object that is used to get suitable "target" sites
 * for connectors. This class provides a way for connections
 * to be connected to figures without knowing too many specifics
 * about the figure being connected. Instances of this object
 * are often given to controller objects (as in model-view-controller)
 * so they can set up view construction and interaction. In this
 * same package, an instance is required by ConnectorManipulator.
 *
 * @version $Revision: 1.4 $
 * @author John Reekie
 */
public interface ConnectorTarget {

    /** Return a suitable site to connect a connector's head to,
     * based on this figure and location. Return null if there
     * is no suitable site.  In general, it is better to use the method that
     * takes a connector, as this gives the target a chance to disallow the
     * connection.  This method is primarily useful for manually
     * creating new figures.
     */
    public Site getHeadSite (Figure f, double x, double y);

    /** Return a suitable site to connect a connector's tail to,
     * based on this figure and location. Return null if there
     * is no suitable site.  In general, it is better to use the method that
     * takes a connector, as this gives the target a chance to disallow the 
     * connection.  This method is primarily useful for manually
     * creating new figures.
     */
    public Site getTailSite (Figure f, double x, double y);

    /** Return a suitable site to connect a connector's head to.
     * The passed site is usually taken to be a site that the
     * connector is already connected to, so the target should
     * take this into account if it has restrictions such as
     * only allowing one connection to each site. The returned site
     * can be the same as the passed site, which signals that the
     * passed site is the best one available.
     * @deprecated Use getHeadSite that takes a connector.
     */
    public Site getHeadSite (Site s, double x, double y);

    /** Return a suitable site to connect a connector's tail to.
     * See the description for getheadSite().
     * @deprecated Use getTailSite that takes a connector.
     */
    public Site getTailSite (Site s, double x, double y);

    /** Return a suitable site to connect the given connector's head to,
     * based on this figure and location. Return null if there
     * is no suitable site.
     */
    public Site getHeadSite (Connector c, Figure f, double x, double y);

    /** Return a suitable site to connect the given connector's tail to,
     * based on this figure and location. Return null if there
     * is no suitable site.
     */
    public Site getTailSite (Connector c, Figure f, double x, double y);

    /** Return a suitable site to connect a connector's head to.
     * The passed site is usually taken to be a site that the
     * connector is already connected to, so the target should
     * take this into account if it has restrictions such as
     * only allowing one connection to each site. The returned site
     * can be the same as the passed site, which signals that the
     * passed site is the best one available.
     */
    public Site getHeadSite (Connector c, Site s, double x, double y);

    /** Return a suitable site to connect a connector's tail to.
     * See the description for getheadSite().
     */
    public Site getTailSite (Connector c, Site s, double x, double y);
}


