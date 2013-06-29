package uk.ac.starlink.ttools.plot2;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import uk.ac.starlink.ttools.plot.Range;
import uk.ac.starlink.ttools.plot2.config.ConfigException;
import uk.ac.starlink.ttools.plot2.config.ConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigMap;
import uk.ac.starlink.ttools.plot2.data.DataStore;

/**
 * Generates members of a family of Surface objects.
 * Surface configuration is provided by two objects of parameterised
 * types, a Profile (type P) and an Aspect (type A).
 * The profile provides fixed configuration items, and the aspect
 * provides items that may change according to different views of the
 * same surface, in particular as the result of  pan/zoom type operations.
 * This object is self-documenting, in that it can report the
 * the configuration keys required to specify profile/aspect.
 *
 * @author   Mark Taylor
 * @since    11 Feb 2013
 */
public interface SurfaceFactory<P,A> {

    /**
     * Returns a new plot surface.
     *
     * @param   plotBounds   rectangle to containing actual plot data
     *                       (not insets)
     * @param   profile   configuration object defining plot style
     * @param   aspect    configuration object defining plot viewpoint
     * @return   new plot surface
     */
    Surface createSurface( Rectangle plotBounds, P profile, A aspect );

    /**
     * Returns the configuration keys used to configure profile for this
     * surface factory.
     * The returned keys are used in the map supplied to the
     * {@link #createProfile createProfile} method.
     *
     * @return  profile configuration keys
     */
    ConfigKey[] getProfileKeys();

    /**
     * Creates a profile that can be used when creating a plot surface.
     * The keys that are significant in the supplied config map
     * are those returned by {@link #getProfileKeys getProfileKeys}.
     * The return value can be used as input to
     * {@link #createSurface createSurface} and other methods in this class.
     *
     * @param  config  map of profile configuration items
     * @return  factory-specific plot surface profile
     */
    P createProfile( ConfigMap config ) throws ConfigException;

    /**
     * Returns the configuration keys that may be used to configure aspect
     * for this surface factory.
     * The returned keys are used in the map supplied to the
     * {@link #useRanges useRanges} and
     * {@link #createAspect createAspect} methods.
     *
     * @return   aspect configuration keys
     */
    ConfigKey[] getAspectKeys();

    /**
     * Indicates whether ranges should be provided to generate an aspect.
     * If true, it is beneficial to pass the result of
     * {@link #readRanges readRanges} to {@link #createAspect createAspect}
     * alongside the arguments of this method.
     * If false, any such ranges will be ignored.
     *
     * @param   profile  surface configuration profile
     * @param  aspectConfig  configuration map that may contain keys from
     *                       <code>getAspectKeys</code>
     * @return   true iff calculated ranges will be of use
     */
    boolean useRanges( P profile, ConfigMap aspectConfig );

    /**
     * Provides the ranges that may be passed to
     * {@link #createAspect createAspect}.
     * There is only any point calling this if {@link #useRanges useRanges}
     * returns true.
     *
     * @param  layers   plot layers to be plotted
     * @param  dataStore  contains actual data
     * @return   data ranges covered by the given layers filled in from data
     */
    @Slow
    Range[] readRanges( PlotLayer[] layers, DataStore dataStore );

    /**
     * Creates an aspect from configuration information.
     * The ranges argument will be used only if {@link #useRanges useRanges}
     * returns true.
     * It is legal to give the ranges argument as null in any case.
     * In all cases, the returned value must be non-null and usable by
     * {@link #createSurface createSurface}.
     *
     * @param  profile  surface configuration profile
     * @param  aspectConfig  configuration map that may contain keys from
     *                       <code>getAspectKeys</code>
     * @param  ranges  range data filled in from layers, or null
     */
    A createAspect( P profile, ConfigMap aspectConfig, Range[] ranges )
        throws ConfigException;

    /**
     * Returns a plot aspect representing a panned view of an existing surface.
     * Panning semantics should preferably have the outcome that the same
     * data position remains under the cursor before and after the pan.
     *
     * @param   surface  plot surface created previously by this factory
     * @param   pos0  reference screen position
     * @param   pos1  destination reference screen position (drag to)
     * @return   new surface aspect
     */
    A pan( Surface surface, Point pos0, Point pos1 );

    /**
     * Returns a plot aspect representing a zoomed view of an existing surface.
     * Zooming semantics should preferably have the outcome that the same
     * data position remains under the cursor before and after the zoom.
     *
     * @param   surface  plot surface created previously by this factory
     * @param   pos  reference screen position
     * @param   factor  zoom factor
     * @return   new surface aspect
     */
    A zoom( Surface surface, Point pos, double factor );

    /**
     * Returns a plot aspect representing a recentred view of an existing
     * surface.
     *
     * @param   surface  plot surface created previously by this factory
     * @param   dpos   dataDimCount-element array giving data coordinates of
     *                 point to appear at plot bounds center
     * @return   new surface aspect
     */
    A center( Surface surface, double[] dpos );
}
