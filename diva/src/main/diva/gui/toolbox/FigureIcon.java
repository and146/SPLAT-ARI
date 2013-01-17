/*
 * $Id: FigureIcon.java,v 1.6 2001/07/22 22:01:32 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui.toolbox;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import diva.canvas.*;

/**
 * An icon that looks like a diva figure.  This class renders the figure into 
 * a buffered image and then points the icon at the buffer.  This process is 
 * rather slow, so you might want to cache the returned icon somehow to 
 * avoid repeating it, especially if you have a large number of icons to
 * render such as in a TreeCellRenderer.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision: 1.6 $
 */
public class FigureIcon extends ImageIcon {
    /**
     * Create a new icon that looks like the given figure.  The icon will
     * have the size of the bounds of the figure.
     * The figure will be rendered into the icon with antialiasing turned off.
     */
    public FigureIcon(Figure figure) {
	this(figure, false);
    }

    /**
     * Create a new icon that looks like the given figure.  The icon will
     * have the size of the bounds of the figure.
     * The figure will be rendered into the icon with antialiasing according
     * to the given flag.
     * @param antialias True if antialiasing should be used.
     */
    public FigureIcon(Figure figure, boolean antialias) {   
	super();
	Rectangle2D bounds = figure.getBounds();
	BufferedImage image = new BufferedImage((int)bounds.getWidth(), 
						(int)bounds.getHeight(),
						BufferedImage.TYPE_INT_RGB);
	Graphics2D graphics = image.createGraphics();
	if(antialias) {
	    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				      RenderingHints.VALUE_ANTIALIAS_ON);
	} else {
	    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				      RenderingHints.VALUE_ANTIALIAS_OFF);
	}
	figure.paint(graphics);	
	setImage(image);
    }

    /**
     * Create a new icon that looks like the given figure.  The figure will be 
     * scaled to fit inside the given size, with any excess size filled 
     * with a transparent background color.
     * The figure will be rendered into the icon with antialiasing turned off.
     */
    public FigureIcon(Figure figure, int x, int y) {
	this(figure, x, y, 0, false);
    }

    /**
     * Create a new icon that looks like the given figure.  The icon will be
     * made the given size, and 
     * given a border of the given number of pixels.  The rendition of the
     * figure will be scaled to fit inside the border,
     * with any excess size filled with a transparent background color.
     * The figure will be rendered into the icon with antialiasing  according
     * to the given flag.
     * @param antialias True if antialiasing should be used.
     */
    public FigureIcon(Figure figure, int x, int y,
		      int border, boolean antialias) {
	super();
	Rectangle2D bounds = figure.getBounds();
	Rectangle2D size = new Rectangle2D.Double(border, border,
						  x - 2 * border, 
						  y - 2 * border);
	AffineTransform transform = 
	    CanvasUtilities.computeFitTransform(bounds, size);
	figure.transform(transform);
	BufferedImage image = 
	    new BufferedImage(x, y, BufferedImage.TYPE_4BYTE_ABGR);
	Graphics2D graphics = image.createGraphics();
	if(antialias) {
	    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				      RenderingHints.VALUE_ANTIALIAS_ON);
	} else {
	    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				      RenderingHints.VALUE_ANTIALIAS_OFF);
	}
	graphics.setBackground(new Color(0,0,0,0));
	graphics.clearRect(0, 0, x, y);
	figure.paint(graphics);
	setImage(image);
    }
}


