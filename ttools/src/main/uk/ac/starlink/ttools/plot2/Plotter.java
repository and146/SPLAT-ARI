package uk.ac.starlink.ttools.plot2;

import javax.swing.Icon;
import uk.ac.starlink.ttools.plot.Style;
import uk.ac.starlink.ttools.plot2.config.ConfigException;
import uk.ac.starlink.ttools.plot2.config.ConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigMap;
import uk.ac.starlink.ttools.plot2.data.Coord;
import uk.ac.starlink.ttools.plot2.data.DataSpec;

/**
 * A Plotter can produce PlotLayers given data and apprpriate configuration.
 * It can also report what data coordinates and style configuration
 * information are needed for the plot.  This self-describing nature
 * means that a plotting framework can largely build a user interface
 * automatically from a Plotter instance.
 * Finally it acts as part of an identifier for the type of plot being
 * performed, which is necessary for determining PlotLayer equality;
 * two PlotLayers are equivalent if they match in point of
 * DataSpec, Style and Plotter.
 *
 * @author   Mark Taylor
 * @since    11 Feb 2013
 */
public interface Plotter<S extends Style> {

    /**
     * Returns the name of this plotter for use in user interface.
     *
     * @return  user-directed plotter name
     */
    String getPlotterName();

    /**
     * Returns an icon for this plotter for use in user interface.
     *
     * @return  plotter icon
     */
    Icon getPlotterIcon();

    /**
     * Indicates whether this plotter uses position coordinates.
     * 
     * @return   true iff <code>createLayer</code> uses a <code>DataGeom</code>
     */
    boolean hasPosition();

    /**
     * Returns any coordinates used by this plotter additional
     * to the base positions (those associated with the <code>DataGeom</code>).
     *
     * @return  coordinates apart from base positions
     */
    Coord[] getExtraCoords();

    /**
     * Returns the configuration keys used to configure style for this plotter.
     * The keys in the return value are used in the map supplied to
     * the {@link #createStyle} method.
     *
     * @return    keys used when creating a style for this plotter.
     */
    ConfigKey[] getStyleKeys();

    /**
     * Creates a style that can be used when creating a plot layer.
     * The keys that are significant in the supplied config map
     * are those returned by {@link #getStyleKeys}.
     * The return value can be used as input to {@link #createLayer}.
     *
     * @param   config  map of style configuration items
     * @return   plotter-specific plot style
     */
    S createStyle( ConfigMap config ) throws ConfigException;

    /**
     * Creates a PlotLayer based on the given geometry, data and style.
     *
     * <p>The <code>style</code> parameter is the result of a call to
     * {@link #createStyle}.
     *
     * <p>The <code>dataSpec</code> parameter must contain coordinate
     * entries corresponding to the results of
     * <code>dataGeom.getPosCoords</code> and
     * {@link #getExtraCoords}.
     *
     * <p>The <code>dataGeom</code> (and probably <code>dataSpec</code>)
     * parameter is only used if {@link #hasPosition} returns true,
     * otherwise the plot is dataless and those parameters are ignored.
     *
     * <p>It is legal to supply null for any of the parameters;
     * if insufficient data is supplied to generate a plot, then
     * the method should return null.
     *
     * <p>Creating a layer should be cheap; layers may be created and not used.
     *
     * @param   dataGeom  indicates base position coordinates and their
     *                    mapping to the data space
     * @param   dataSpec  specifies the data required for the plot
     * @param   style   data style as obtained from <code>createStyle</code>
     * @return   new plot layer, or null if no drawing will take place
     */
    PlotLayer createLayer( DataGeom dataGeom, DataSpec dataSpec, S style );
}
