/*
 * $Id: FixedNormalSite.java,v 1.4 2002/09/26 10:01:30 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 *
 */

package diva.canvas.connector;

import diva.canvas.SiteDecorator;
import diva.canvas.Site;

/**
 * @deprecated Do not use. Use ParametricSite instead.
 *
 * A site decorator that disallows changing the normal.  This is useful for
 * perimeter sites that stay at one point on a figure.  Normally connectors
 * change the normal of a site so that the figure looks better.  
 * This class is often used so that the site that points out of a terminal
 * can be properly manhattan routed so that it always leave the terminal in
 * the ight direction.
 *
 * @version	$Revision: 1.4 $
 * @author 	Steve Neuendorffer
 */
public class FixedNormalSite extends SiteDecorator {
    public FixedNormalSite(Site site) {
	super(site);
    }
    
    public void setNormal(double normal) {
	// Do nothing
    }
}





