/*
 * $Id: PaintedString.java,v 1.2 2002/08/13 09:40:01 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.compat.canvas;

import diva.util.java2d.ShapeUtilities;

import java.awt.Font;
import java.awt.Shape;
import java.awt.Paint;
import java.awt.Color;
import java.awt.Graphics2D;

import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextLayout;
import java.awt.font.LineBreakMeasurer;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import java.text.AttributedString;

/**
 * <b>Note</b>: use of this class is no longer recommended for new
 * development.
 * <P>
 * A utility class that paints a string. This is a low-level class
 * which is designed to simplify the construction of drawn graphics.
 * It contains enough font and painting information to be useful in
 * many cases where fonts are needed for labels and so on in graphic
 * diagrams.
 *
 * @author Michael Shilman  (michaels@eecs.berkeley.edu)
 * @author John Reekie  (johnr@eecs.berkeley.edu)
 * @author Steve Neuendorffer  (neuendor@eecs.berkeley.edu)
 * @version $Revision: 1.2 $
 */
public class PaintedString implements PaintedObject {

    /** The string that gets painted.
     */
    private String _string;

    /** The default font.
     */
    private static Font _defaultFont = new Font("Serif", Font.PLAIN, 16);

    /** The font.
     */
    private Font _font;

    /** The fill paint of the string.
     */
    private Paint _fillPaint = Color.black;

    /** The transform of the label
     */
    private AffineTransform _transform = new AffineTransform();

    /** The shape of the label
     */
    private List _shapes = new ArrayList();

    /** The bounds
     */
    private Rectangle2D _bounds;

    /**
     * Construct an empty label figure.
     */
    public PaintedString() {
        this("");
    }

    /**
     * Construct a label figure displaying the
     * given string, using the default font.
     */
    public PaintedString(String s) {
        _string = s;
        _font = _defaultFont;
        _update();
    }

    /**
     * Construct a label figure displaying the
     * given string in the given font. This is the best constructor
     * to use if you are creating a lot of labels in a font other
     * than the default, as a single instance of Font can then
     * be shared by many labels.
     */
    public PaintedString(String s, Font f) {
        _string = s;
        _font = f;
        _update();
    }

    /**
     * Construct a label figure displaying the
     * given string in the given face, style, and size. A new
     * Font object representing the face, style, and size is
     * created for this label.
     */
    public PaintedString(String s, String face, int style, int size) {
        _string = s;
        _font = new Font(face, style, size);
        _update();
    }

    /**
     * Get the bounds of this string
     */
    public Rectangle2D getBounds () {
        if(_bounds == null) {
            _update();
        }
        return _bounds;
    }

    /**
     * Get the font that this label is drawn in.
     */
    public Font getFont() {
        return _font;
    }

    /**
     * Get the fill paint for this label.
     */
    public Paint getFillPaint() {
        return _fillPaint;
    }
    
    /**
     * Get the font name.
     */
    public String getFontName() {
        return _font.getFontName();
    }
    
    /**
     * Get the font style.
     */
    public int getStyle() {
        return _font.getStyle();
    }
    
    /**
     * Get the font size.
     */
    public int getSize() {
        return _font.getSize();
    }

    /**
     * Get the shape of this label figure. This just returns
     * the bounds, since hit-testing on the actual filled
     * latter shapes is way slow (and not that useful, since
     * usually you want to treat the whole label as a single
     * object anyway, and not have to click on an actual
     * filled pixel).
     */
    public Shape getShape () {
        if(_bounds == null) {
            _update();
        }
        return _bounds;
    }

    /**
     * Get the string of this label.
     */
    public String getString() {
        return _string;
    }

    /**
     * Paint the label.
     */
    public void paint(Graphics2D g) {
        if(_bounds == null) {
            _update();
        }
        if(getString() != null) {
            g.setPaint(_fillPaint);
            Iterator i = _shapes.iterator();
            while(i.hasNext()) {
                Shape shape = (Shape) i.next();
                g.fill(shape);
            }
        }
    }

    /**
     * Set the fill paint that this shape
     * is drawn with.
     */
    public void setFillPaint(Paint p) {
        _fillPaint = p;
    }

    /**
     * Set the font.
     */
    public void setFont(Font f) {
        _font = f;
        // clear the bounds.
        _bounds = null;
    }

    /**
     * Set the font family by name.
     */
    public void setFontName(String s) {
        setFont(new Font(s, _font.getStyle(), _font.getSize()));
    }

    /**
     * Set the font style.
     */
    public void setStyle(int style) {
        setFont(new Font(_font.getFontName(), style, _font.getSize()));
    }

    /**
     * Set the font size.
     */
    public void setSize(int size) {
        setFont(new Font(_font.getFontName(), _font.getStyle(), size));
    }

    /**
     * Set the string.
     */
    public void setString(String s) {
        _string = s;
        // clear the bounds.
        _bounds = null;
    }

    /**
     * Change the transform of this label. Note that the anchor
     * of the figure will appear to move -- use translateTo()
     * to move it back again if this method being called to
     * (for example) rotate the label.
     */
    public void setTransform (AffineTransform at) {
        _transform = at;
        // clear the bounds.
        _bounds = null;
    }

    /**
     * Transform the label with the given transform.  Note that the anchor
     * of the figure will appear to nmove -- use translateTo()
     * to move it back again if this method being called to
     * (for example) rotate the label.
     */
    public void transform (AffineTransform at) {
        _transform.preConcatenate(at);
        // clear the bounds.
        _bounds = null;
     }

    /**
     * Translate the label the given distance.
     */
    public void translate (double x, double y) {
        _transform.translate(x,y);
        // clear the bounds.
        _bounds = null;
     }

    /** Update internal variables after changing the transform
     * or font or string. In the current implementation,
     * we get a new outline shape and bounds using the current
     * transform and cache them, since this is probably the
     * fastest thing to do. Note that though there is a
     * drawGlyphVector() method in Graphics2D, when we tried
     * to use it, it appears to perform the translation
     * TWICE. After screwing around with it for a while,
     * I gave up [johnr].
     */
    private void _update () {
        // Since we are generating a shape and drawing that, it makes
        // no difference what the values of the flags are
        FontRenderContext frc = new FontRenderContext(null, true, false);
        // Return the delimeters, so that we get the right line count.
        StringTokenizer lines = new StringTokenizer(_string, "\n", true);
        double dy = _font.getMaxCharBounds(frc).getHeight();
        
        double x = 0;
        double y = 0;
        _bounds = null;
        _shapes.clear();
        while (lines.hasMoreElements()) {
            String line = lines.nextToken();
            if(line.equals("\n")) {
                // Note that leading or trailing newlines are ignored.
                y += dy;
            } else {
                GlyphVector gv = _font.createGlyphVector(frc, line);
                
		// Get the shape and bounds. Work around JDK1.4 bug.
		Shape s = gv.getOutline();
		Rectangle2D b;
                s = _transform.createTransformedShape(s);
                b = s.getBounds2D();
                if(_bounds == null) {
                    // implicit translate by (0,0)
                    _bounds = b;
                    _shapes.add(s);
                } else {
                    Rectangle2D.union(
                            _bounds, 
                            (Rectangle2D)ShapeUtilities.translateModify(
                                    b, x, y),
                            _bounds);
                    _shapes.add(ShapeUtilities.translateModify(
                            s, x, y));
                }
            }
        }
    }
}


