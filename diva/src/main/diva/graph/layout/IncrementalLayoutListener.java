 /*
 * $Id: IncrementalLayoutListener.java,v 1.2 2001/07/22 22:01:21 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.graph.layout;
import diva.graph.GraphModel;
import diva.graph.GraphEvent;
import diva.graph.GraphViewEvent;
import diva.util.Filter;
import java.awt.geom.*;

/**
 * A Listener that applies the given incremental layout whenever a graph
 * event is received.  The listener applies an optional filter which can
 * be used to limit the context into which the layout algorithm will be
 * applied.
 *
 * @author Steve Neuendorffer (neuendor@eecs.berkeley.edu)
 * @version $Revision: 1.2 $
 * @rating Red
 */
public class IncrementalLayoutListener 
    implements diva.graph.GraphViewListener {
    private IncrementalLayout _layout;
    private Filter _filter;

    /**
     * Construct a new listener that invokes the given incremental layout
     * whenever a graph event is received.
     */
    public IncrementalLayoutListener(IncrementalLayout layout, 
				     Filter filter) {
        _layout = layout;
	_filter = filter;
    }

    /**
     */
    public void edgeDrawn(GraphViewEvent e) {
	if(_filter != null && !_filter.accept(e.getTarget())) return;
	_layout.edgeDrawn(e.getTarget());
    }

    /**
     */
    public void edgeRouted(GraphViewEvent e) {
	if(_filter != null && !_filter.accept(e.getTarget())) return;
	_layout.edgeRouted(e.getTarget());
    }

    /**
     * Return the filter for this listener.
     */
    public Filter getFilter() {
	return _filter;
    }

    /** 
     * Return the layout.
     */
    public IncrementalLayout getLayout() {
	return _layout;
    }

    /**
     */
    public void nodeDrawn(GraphViewEvent e) {
	if(_filter != null && !_filter.accept(e.getTarget())) return;
	_layout.nodeDrawn(e.getTarget());
    }

    /**
     */
    public void nodeMoved(GraphViewEvent e) {
	if(_filter != null && !_filter.accept(e.getTarget())) return;
	_layout.nodeMoved(e.getTarget());
    }
}


