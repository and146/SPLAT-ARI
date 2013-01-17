/*
 * $Id: JPanner.java,v 1.12 2001/12/10 22:40:28 neuendor Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.gui.toolbox;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;

import javax.swing.*;
import javax.swing.event.*;

import diva.canvas.CanvasUtilities;
import diva.canvas.JCanvas;
import diva.canvas.TransformContext;
import diva.util.java2d.ShapeUtilities;

/**
 * A panner is a window that provides a mechanism to visualize and
 * manipulate a JViewport object without using scrollbars.  Unlike the
 * viewport, which contains a partial, full size rendition of the
 * contained component, this class contains a complete, scaled down
 * rendition of the component.  The bounds of the component are represented
 * by a blue rectangle and the bounds of the viewport on the component 
 * are visible as a red rectangle. Clicking or dragging within the
 * JPanner centers the viewport at that point on the component.
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision: 1.12 $
 */
public class JPanner extends JPanel {
    /**
     * The target window that is being wrapped.
     */
    private JViewport _target = null;

    /**
     * The scrolling listener;
     */
    private ScrollListener _listener = new ScrollListener();

    /**
     * The mouse listener on the panner that is responsible for scaling.
     */
    private ScaleMouseListener _scaleMouseListener = new ScaleMouseListener();



    /**
     * Construct a new panner that is initially viewing
     * nothing.  Use setViewport() to assign it to something.
     */
    public JPanner() {
        this(null);
    }
    
    /**
     * Construct a new wrapper that wraps the given
     * target.
     */
    public JPanner(JViewport target) {
        setViewport(target);
	addMouseListener(new PanMouseListener());
        addMouseMotionListener(new PanMouseListener());
        // NOTE: Removed this listener, since it didn't work well.  EAL
        // _scaleMouseListener = new ScaleMouseListener();
    }

    /**
     *  Set the position of the viewport associated with this panner
     *  centered on the given position relative to the rendition shown in
     *  the panner.
     */
    public void setPosition(int x, int y) {
        Dimension viewSize =_target.getView().getSize();
        Rectangle viewRect = 
            new Rectangle(0, 0, viewSize.width, viewSize.height);
        Rectangle myRect = _getInsetBounds();
        
        AffineTransform forward = 
            CanvasUtilities.computeFitTransform(viewRect, myRect);
        
        Dimension extentSize = _target.getExtentSize();
        
        x = (int)(x / forward.getScaleX()) - extentSize.width/2;
        y = (int)(y / forward.getScaleY()) - extentSize.height/2;
        
        int max;
        if(x < 0) 
            x = 0;
        max = viewSize.width - extentSize.width;
        if(x > max)
            x = max;
        
        if(y < 0) 
            y = 0;
        max = viewSize.height - extentSize.height;
        if(y > max)
            y = max;
        
        _target.setViewPosition(new Point(x, y));
    }

    /**
     * Set the target component that is being
     * wrapped.
     */
    public void setViewport(JViewport target) {
        if(_target != null) {
            _target.removeChangeListener(_listener);
            if(_target.getView() instanceof JCanvas) {
                removeMouseListener(_scaleMouseListener);
                removeMouseMotionListener(_scaleMouseListener);
            }
        }
        _target = target;
        if(_target != null) {
            _target.addChangeListener(_listener);
            if(_target.getView() instanceof JCanvas) {
                addMouseListener(_scaleMouseListener);
                addMouseMotionListener(_scaleMouseListener);
            }
        }
	repaint();
    }

    /**
     * Return the target component that is being
     * wrapped.
     */
    public JViewport getViewport() {
        return _target;
    }

    public void paintComponent(Graphics g) {
	if(_target != null) {
            JCanvas canvas = (JCanvas)_target.getView();
            Dimension viewSize = canvas.getSize();
	    Rectangle viewRect = 
		new Rectangle(0, 0, viewSize.width, viewSize.height);
            
	    Rectangle myRect = _getInsetBounds();
		
            AffineTransform forward = 
		CanvasUtilities.computeFitTransform(viewRect, myRect);
            // Also invert the current transform on the canvas.
            AffineTransform current = 
                canvas.getCanvasPane().getTransformContext().getTransform();
	    AffineTransform inverse;
            try {
                inverse = forward.createInverse();
                inverse.concatenate(current.createInverse());
            }
            catch(NoninvertibleTransformException e) {
                throw new RuntimeException(e.toString());
            }

            Graphics2D g2d = (Graphics2D)g;
            g2d.transform(forward);
            canvas.paint(g);
            
            g.setColor(Color.red);
            Rectangle r = _target.getViewRect();
            g.drawRect(r.x, r.y, r.width, r.height);

            /* NOTE: No longer meaningful, since always full space.
            g.setColor(Color.blue);
            Dimension d = canvas.getSize();
            g.drawRect(0, 0, d.width, d.height);
            */

            g2d.transform(inverse);
        } else {
	    Rectangle r = _getInsetBounds();	    
	    g.clearRect(r.x, r.y, r.width, r.height);
	}
    }

    // Return a rectangle that fits inside the border
    private Rectangle _getInsetBounds() {
	Dimension mySize = getSize();
	Insets insets = getInsets();
	Rectangle myRect = 
	    new Rectangle(insets.left, insets.top, 
			  mySize.width - insets.top - insets.bottom,
			  mySize.height - insets.left - insets.right);
	return myRect;
    }
 

    //paint???
    private class ScrollListener implements ChangeListener {
        public void stateChanged(ChangeEvent e) {
            repaint();
        }
    }

    private Point _p;
    
    private class PanMouseListener extends MouseAdapter
        implements MouseMotionListener {

        public void mousePressed(MouseEvent evt) {
            if(_target != null &&
                    (evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
		setPosition(evt.getX(), evt.getY());
            }
        }
        public void mouseMoved(MouseEvent evt) {
        }
        public void mouseDragged(MouseEvent evt) {
            if(_target != null &&
                    (evt.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {
		setPosition(evt.getX(), evt.getY());
	    }
        }
    }

    private class ScaleMouseListener extends MouseAdapter
        implements MouseMotionListener {
        public Point2D origin = null;
        public Point2D scaled = null;
        public AffineTransform transformOrigin = null;
	public void setScale(int x, int y) {
            double scale;
            // The 5.0 and 1.3 below were determined by trial and error
            // tuning.
            if(x > origin.getX() && y > origin.getY()) {
                if(x - origin.getX() > y - origin.getY()) {
                    scale = (y - origin.getY()) / 5.0;
                } else {
                    scale = (x - origin.getX()) / 5.0;
                }
            } else if(x < origin.getX() && y < origin.getY()) {
                if(origin.getX() - x > origin.getY() - y) {
                    scale = (y - origin.getY()) / 5.0;
                } else {
                    scale = (x - origin.getX()) / 5.0;
                }
            } else {
                scale = 0.0;
            }
            scale = Math.pow(1.3, scale);
            JCanvas canvas = (JCanvas)_target.getView();

            AffineTransform current = 
                canvas.getCanvasPane().getTransformContext().getTransform();
            current.setTransform(transformOrigin);
            current.translate(scaled.getX(), scaled.getY());
            current.scale(scale, scale);
            current.translate(-scaled.getX(), -scaled.getY());
            canvas.getCanvasPane().setTransform(current);
	}

        public void mousePressed(MouseEvent evt) {
            if(_target != null &&
                    (evt.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
                setPosition(evt.getX(), evt.getY());
                origin = evt.getPoint();
                JCanvas canvas = ((JCanvas)_target.getView());
                TransformContext context = 
                    canvas.getCanvasPane().getTransformContext();
                // clone the transform that is in the context, so we can
                // avoid alot of repeated scaling of the same transform.
                transformOrigin = 
                    (AffineTransform)context.getTransform().clone();

                // Take the event and first transform it from the panner
                // coordinates into the view coordinates.
                Dimension viewSize =_target.getView().getSize();
                Rectangle viewRect = 
                    new Rectangle(0, 0, viewSize.width, viewSize.height);
                Rectangle myRect = _getInsetBounds();
		
                AffineTransform forward = 
                    CanvasUtilities.computeFitTransform(viewRect, myRect);
                
                double xScaled = 
                    (origin.getX() - myRect.getX()) / forward.getScaleX();
                double yScaled = 
                    (origin.getY() - myRect.getY()) / forward.getScaleY();
                scaled = new Point2D.Double(xScaled, yScaled);

                // Now transform from the view coordinates into the 
                // pane coordinates.
                try {
                    context.getInverseTransform().transform(scaled, scaled);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        public void mouseMoved(MouseEvent evt) {
        }
        public void mouseDragged(MouseEvent evt) {
            if(_target != null &&
                    (evt.getModifiers() & MouseEvent.BUTTON3_MASK) != 0) {
		setScale(evt.getX(), evt.getY());
	    }
        }
    }

    public static void main(String argv[]) {
        JFrame f = new JFrame();
        JList l = new JList();
        String[] data = {"oneeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee",
                         "twoooooooooooooooooooooooooooooooooooooooo",
                         "threeeeeeeeeeeeeeeee",
                         "fourrrrrrrrrrrrrrrrrrrrrrrrr"};
        JList dataList = new JList(data);
        JScrollPane p = new JScrollPane(dataList);
        p.setSize(200, 200);
        JPanner pan = new JPanner(p.getViewport());
        pan.setSize(200, 200);
        f.getContentPane().setLayout(new GridLayout(2, 1));
        f.getContentPane().add(p);
        f.getContentPane().add(pan);
        f.setSize(200, 400);
        f.setVisible(true);
    }
}


