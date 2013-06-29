package uk.ac.starlink.ttools.plot2;

import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;

/**
 * Defines the graphical area on which plots are painted.
 * It maps data space coordinates to graphics coordinates
 * (and possibly vice versa) and paints axes.
 *
 * @see   SurfaceFactory
 * @author   Mark Taylor
 * @since    11 Feb 2013
 */
public interface Surface {

    /**
     * Returns the rectangle within which all of the plot data will appear.
     * This includes anything that might get drawn by a plot layer,
     * but does not necessarily include axis labels etc.
     *
     * @return   plot data area bounds
     */
    Rectangle getPlotBounds();

    /**
     * Returns the insets that this surface would like to reserve outside
     * the plot bounds.
     * This is space outside the rectangle returned by {@link #getPlotBounds}
     * to be used for axis labels etc.
     *
     * <p>If the <code>withScroll</code> parameter is set, then an attempt
     * will be made to return insets that will not alter if the current
     * plot is scrolled around a moderate amount.
     * For a one-time plot that's not important, but for an interactive
     * plot it prevents the actual plot position jumping around to
     * accommodate more or less space on the axes according to exactly
     * where ticks happen to fall on the axes.
     *
     * @param   withScroll  true to reserve space for nicer scrolling
     * @return   plot data area insets
     */
    Insets getPlotInsets( boolean withScroll );

    /**
     * Paints the plot surface background.
     * Anything that appears within the plot bounds underneath the data
     * markings must go here.
     *
     * @param   g  graphics context
     */
    void paintBackground( Graphics g );

    /**
     * Paints the plot surface foreground.
     * Anything that appears on top of the data markings or outside the
     * plot bounds must go here.  This may include axes.
     *
     * @param   g  graphics context
     */
    void paintForeground( Graphics g );

    /**
     * Returns the dimensionality of the data space used by this plot surface.
     *
     * @return  number of elements in data space coordinate array
     */
    int getDataDimCount();

    /**
     * Converts a data space position to a graphics position.
     *
     * @param  dataPos  dataDimCount-element array containing data space
     *                  coordinates
     * @param  visibleOnly  if true, then the conversion will only succeed
     *                      when the result falls within the plot bounds
     *                      of this surface
     * @param  gPos  point object into which the graphics position will
     *               be written on success
     * @return  true iff the conversion succeeds
     */
    boolean dataToGraphics( double[] dataPos, boolean visibleOnly, Point gPos );

    /**
     * Converts an offset data space position to a graphics position.
     * Context is given in the form of an existing converted nearby point
     * (both data and graphics positions).
     *
     * <p>This (somewhat hacky) method is required for surfaces in which a
     * data position may map to more than one position in graphics space,
     * for instance sky surfaces with discontinuous longitude.
     * The result does not need to be the same as the result of
     * calling {@link #dataToGraphics}, and is not required to be a legal 
     * graphics position, but it must make visual sense, for instance
     * when plotting error bars.
     * The semantics of a "nearby point" is not very well defined.
     * There are probably situations in which calling this will not
     * give the result that's wanted, but they will probably be rare.
     *
     * @param  dataPos0  context position in data space
     * @param  gpos0     context position in graphics space
     *                   (result of calling dataToGraphics on dpos0)
     * @param  dataPos1  query position in data space
     * @param  visibleOnly  if true, the call only succeeds if the result is
     *                      within the plot bounds of this surface
     * @param  gPos1     point object to which the graphics position of
     *                   dpos1 will be written on success
     * @return true for success, false for no result
     */
    boolean dataToGraphicsOffset( double[] dataPos0, Point gpos0,
                                  double[] dataPos1, boolean visibleOnly,
                                  Point gPos1 ); 
    /**
     * Attempst to turn a graphics position into a data position.
     * This is not always trivial, for instance in a 3D plot one
     * graphics position maps to a line of data positions.
     * The <code>dposIt</code> argument can optionally
     * be supplied to cope with such instances.  If a data pos cannot be
     * determined, null is returned.  If <code>dposIt</code> is absent,
     * the method will run quickly.  If it's present, it may or may
     * not run slowly.
     *
     * @param   gPos   graphics point
     * @param   dposIt  iterable over dataDimCount-element arrays
     *                  representing all the data space positions plotted,
     *                  or null
     * @return   dataDimCount-element array giving data space position for
     *           <code>gPos</code>, or null if it cannot be determined
     */
    double[] graphicsToData( Point gPos, Iterable<double[]> dposIt );

    /**
     * Formats the given data space position as a coordinate string.
     * If possible the returned string should have the same length
     * and formatting over the whole visible plot surface, so that
     * the representation doesn't jump around when the cursor is moved.
     *
     * @param   dataPos  dataDimCount-element array giving data space position
     * @return   human-readable string representing position
     */
    String formatPosition( double[] dataPos );
}
