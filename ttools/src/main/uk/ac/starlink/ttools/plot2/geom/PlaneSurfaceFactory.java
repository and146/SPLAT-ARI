package uk.ac.starlink.ttools.plot2.geom;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.starlink.ttools.plot.Range;
import uk.ac.starlink.ttools.plot2.Captioner;
import uk.ac.starlink.ttools.plot2.Navigator;
import uk.ac.starlink.ttools.plot2.PlotLayer;
import uk.ac.starlink.ttools.plot2.PlotUtil;
import uk.ac.starlink.ttools.plot2.Subrange;
import uk.ac.starlink.ttools.plot2.Surface;
import uk.ac.starlink.ttools.plot2.SurfaceFactory;
import uk.ac.starlink.ttools.plot2.config.BooleanConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigException;
import uk.ac.starlink.ttools.plot2.config.ConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigMap;
import uk.ac.starlink.ttools.plot2.config.ConfigMeta;
import uk.ac.starlink.ttools.plot2.config.DoubleConfigKey;
import uk.ac.starlink.ttools.plot2.config.StyleKeys;
import uk.ac.starlink.ttools.plot2.config.SubrangeConfigKey;
import uk.ac.starlink.ttools.plot2.data.DataStore;

/**
 * Surface factory for flat 2-d plotting.
 *
 * @author   Mark Taylor
 * @since    19 Feb 2013
 */
public class PlaneSurfaceFactory
            implements SurfaceFactory<PlaneSurfaceFactory.Profile,PlaneAspect> {

    /** Config key for X axis lower bound, before subranging. */
    public static final ConfigKey<Double> XMIN_KEY =
        DoubleConfigKey.createTextKey( new ConfigMeta( "xmin", "X Minimum" ) );

    /** Config key for X axis upper bound, before subranging. */
    public static final ConfigKey<Double> XMAX_KEY =
        DoubleConfigKey.createTextKey( new ConfigMeta( "xmax", "X Maximum" ) );

    /** Config key for X axis subrange. */
    public static final ConfigKey<Subrange> XSUBRANGE_KEY =
        new SubrangeConfigKey( new ConfigMeta( "xsub", "X Subrange" ) );

    /** Config key for Y axis lower bound, before subranging. */
    public static final ConfigKey<Double> YMIN_KEY =
        DoubleConfigKey.createTextKey( new ConfigMeta( "ymin", "Y Minimum" ) );

    /** Config key for Y axis upper bound, before subranging. */
    public static final ConfigKey<Double> YMAX_KEY =
        DoubleConfigKey.createTextKey( new ConfigMeta( "ymax", "Y Maximum" ) );

    /** Config key for Y axis subrange. */
    public static final ConfigKey<Subrange> YSUBRANGE_KEY =
        new SubrangeConfigKey( new ConfigMeta( "ysub", "Y Subrange" ) );

    /** Config key for X axis log scale flag. */
    public static final ConfigKey<Boolean> XLOG_KEY =
        new BooleanConfigKey( new ConfigMeta( "xlog", "X Log" ) );

    /** Config key for Y axis log scale flag. */
    public static final ConfigKey<Boolean> YLOG_KEY =
        new BooleanConfigKey( new ConfigMeta( "ylog", "Y Log" ) );

    /** Config key for X axis flip flag. */
    public static final ConfigKey<Boolean> XFLIP_KEY =
        new BooleanConfigKey( new ConfigMeta( "xflip", "X Flip" ) );

    /** Config key for Y axis flip flag. */
    public static final ConfigKey<Boolean> YFLIP_KEY =
        new BooleanConfigKey( new ConfigMeta( "yflip", "Y Flip" ) );

    /** Config key for X axis text label. */
    public static final ConfigKey<String> XLABEL_KEY =
        StyleKeys.createAxisLabelKey( "X" );

    /** Config key for Y axis text label.*/
    public static final ConfigKey<String> YLABEL_KEY =
        StyleKeys.createAxisLabelKey( "Y" );

    /** Config key for axis aspect ratio fix. */
    public static final ConfigKey<Double> XYFACTOR_KEY =
        DoubleConfigKey.createToggleKey( new ConfigMeta( "aspect",
                                                         "Aspect Lock" ),
                                         Double.NaN, 1.0 );

    /** Config key to determine if grid lines are drawn. */
    public static final ConfigKey<Boolean> GRID_KEY =
        new BooleanConfigKey( new ConfigMeta( "grid", "Draw Grid" ), false );

    /** Config key to control tick mark crowding on X axis. */
    public static final ConfigKey<Double> XCROWD_KEY =
        StyleKeys.createCrowdKey( new ConfigMeta( "xcrowd",
                                                  "X Tick Crowding" ) );

    /** Config key to control tick mark crowding on Y axis. */
    public static final ConfigKey<Double> YCROWD_KEY =
        StyleKeys.createCrowdKey( new ConfigMeta( "ycrowd",
                                                  "Y Tick Crowding" ) );

    public Surface createSurface( Rectangle plotBounds, Profile profile,
                                  PlaneAspect aspect ) {
        Profile p = profile;
        return PlaneSurface
              .createSurface( plotBounds, aspect,
                              p.xlog_, p.ylog_, p.xflip_, p.yflip_,
                              p.xlabel_, p.ylabel_, p.captioner_,
                              p.xyfactor_, p.grid_, p.xcrowd_, p.ycrowd_,
                              p.minor_ );
    }

    public ConfigKey[] getProfileKeys() {
        List<ConfigKey> list = new ArrayList<ConfigKey>();
        list.addAll( Arrays.asList( new ConfigKey[] {
            XLOG_KEY,
            YLOG_KEY,
            XFLIP_KEY,
            YFLIP_KEY,
            XLABEL_KEY,
            YLABEL_KEY,
            XYFACTOR_KEY,
            GRID_KEY,
            XCROWD_KEY,
            YCROWD_KEY,
            StyleKeys.MINOR_TICKS,
        } ) );
        list.addAll( Arrays.asList( StyleKeys.getCaptionerKeys() ) );
        return list.toArray( new ConfigKey[ 0 ] );
    }

    public Profile createProfile( ConfigMap config ) throws ConfigException {
        boolean xlog = config.get( XLOG_KEY );
        boolean ylog = config.get( YLOG_KEY );
        boolean xflip = config.get( XFLIP_KEY );
        boolean yflip = config.get( YFLIP_KEY );
        String xlabel = config.get( XLABEL_KEY );
        String ylabel = config.get( YLABEL_KEY );
        double xyfactor = config.get( XYFACTOR_KEY );
        boolean grid = config.get( GRID_KEY );
        double xcrowd = config.get( XCROWD_KEY );
        double ycrowd = config.get( YCROWD_KEY );
        boolean minor = config.get( StyleKeys.MINOR_TICKS );
        Captioner captioner = StyleKeys.createCaptioner( config );
        return new Profile( xlog, ylog, xflip, yflip, xlabel, ylabel,
                            captioner, xyfactor, grid, xcrowd, ycrowd, minor );
    }

    public ConfigKey[] getAspectKeys() {
        return new ConfigKey[] {
            XMIN_KEY, XMAX_KEY, XSUBRANGE_KEY,
            YMIN_KEY, YMAX_KEY, YSUBRANGE_KEY,
        };
    }

    public boolean useRanges( Profile profile, ConfigMap config ) {
        return createUnrangedAspect( profile, config ) == null;
    }

    public PlaneAspect createAspect( Profile profile, ConfigMap config,
                                     Range[] ranges ) {
        PlaneAspect unrangedAspect = createUnrangedAspect( profile, config );
        if ( unrangedAspect != null ) {
            return unrangedAspect;
        }
        else {
            Range xrange = ranges == null ? new Range() : ranges[ 0 ];
            Range yrange = ranges == null ? new Range() : ranges[ 1 ];
            double[] xlimits =
                getLimits( config, XMIN_KEY, XMAX_KEY, XSUBRANGE_KEY,
                           profile.xlog_, xrange );
            double[] ylimits =
                getLimits( config, YMIN_KEY, YMAX_KEY, YSUBRANGE_KEY,
                           profile.ylog_, yrange );
            return new PlaneAspect( xlimits, ylimits );
        }
    }

    public Range[] readRanges( PlotLayer[] layers, DataStore dataStore ) {
        return PlotUtil.readCoordinateRanges( layers, 2, dataStore );
    }

    public ConfigKey[] getNavigatorKeys() {
        return PlaneNavigator.getConfigKeys();
    }

    public Navigator<PlaneAspect> createNavigator( ConfigMap navConfig ) {
        return PlaneNavigator.createNavigator( navConfig );
    }

    /**
     * Utility method to interrogate axis range configuration variables
     * and work out the actual range to use on a given Cartesian axis.
     * If not enough information is supplied to determine the definite range,
     * null is returned.
     *
     * @param  config  config map containing config values
     * @param  minKey  config key giving axis lower bound before subranging
     * @param  maxKey  config key giving axis upper bound before subranging
     * @param  subrangeKey  config key giving subrange value
     * @param  isLog  true for logarithmic axis, false for linear
     * @param  range   data range on axis; may be partially populated or null
     * @return  2-element array giving definite axis (lower,upper) bounds,
     *          or null
     */
    public static double[] getLimits( ConfigMap config,
                                      ConfigKey<Double> minKey,
                                      ConfigKey<Double> maxKey,
                                      ConfigKey<Subrange> subrangeKey,
                                      boolean isLog, Range range ) {
        double lo = config.get( minKey );
        double hi = config.get( maxKey );
        Subrange subrange = config.get( subrangeKey );
        boolean isFinite = lo < hi                 // entails neither is NaN
                        && ! Double.isInfinite( lo )
                        && ! Double.isInfinite( hi )
                        && ! ( isLog && lo <= 0 );
        if ( isFinite ) {
            return PlotUtil.scaleRange( lo, hi, subrange, isLog );
        }
        else if ( range != null ) {
            Range r1 = new Range( range );
            r1.limit( lo, hi );
            double[] b1 = r1.getFiniteBounds( isLog );
            return PlotUtil.scaleRange( b1[ 0 ], b1[ 1 ], subrange, isLog );
        }
        else {
            return null;
        }
    }

    /**
     * Attempts to determine an aspect value from profile and configuration,
     * but not ranging, information.  If not enough information is supplied,
     * null will be returned.
     *
     * @param  profile   config profile
     * @param  config  map which may contain additional range config info
     * @return  aspect, or null
     */
    private static PlaneAspect createUnrangedAspect( Profile profile,
                                                     ConfigMap config ) {
        double[] xlimits =
            getLimits( config, XMIN_KEY, XMAX_KEY, XSUBRANGE_KEY,
                       profile.xlog_, null );
        double[] ylimits =
            getLimits( config, YMIN_KEY, YMAX_KEY, YSUBRANGE_KEY,
                       profile.ylog_, null );
        return xlimits == null || ylimits == null
             ? null
             : new PlaneAspect( xlimits, ylimits );
    }

    /**
     * Profile class which defines fixed configuration items for
     * a PlaneSurface.
     * Instances of this class are normally obtained from the
     * {@link #createProfile createProfile} method.
     */
    public static class Profile {
        private final boolean xlog_;
        private final boolean ylog_;
        private final boolean xflip_;
        private final boolean yflip_;
        private final String xlabel_;
        private final String ylabel_;
        private final Captioner captioner_;
        private final double xyfactor_;
        private final boolean grid_;
        private final double xcrowd_;
        private final double ycrowd_;
        private final boolean minor_;

        /**
         * Constructor.
         *
         * @param  xlog   whether to use logarithmic scaling on X axis
         * @param  ylog   whether to use logarithmic scaling on Y axis
         * @param  xflip  whether to invert direction of X axis
         * @param  yflip  whether to invert direction of Y axis
         * @param  xlabel  text for labelling X axis
         * @param  ylabel  text for labelling Y axis
         * @param  captioner  text renderer for axis labels etc
         * @param  xyfactor   ratio (X axis unit length)/(Y axis unit length),
         *                    or NaN to use whatever bounds shape and
         *                    axis limits give you
         * @param  grid   whether to draw grid lines
         * @param  xcrowd  crowding factor for tick marks on X axis;
         *                 1 is normal
         * @param  ycrowd  crowding factor for tick marks on Y axis;
         *                 1 is normal
         * @param  minor   whether to paint minor tick marks on axes
         */
        public Profile( boolean xlog, boolean ylog,
                        boolean xflip, boolean yflip,
                        String xlabel, String ylabel, Captioner captioner,
                        double xyfactor, boolean grid,
                        double xcrowd, double ycrowd, boolean minor ) {
            xlog_ = xlog;
            ylog_ = ylog;
            xflip_ = xflip;
            yflip_ = yflip;
            xlabel_ = xlabel;
            ylabel_ = ylabel;
            captioner_ = captioner;
            xyfactor_ = xyfactor;
            grid_ = grid;
            xcrowd_ = xcrowd;
            ycrowd_ = ycrowd;
            minor_ = minor;
        }

        /**
         * Returns a 2-element array giving X and Y log flags.
         *
         * @return  (xlog, ylog) array
         */
        public boolean[] getLogFlags() {
            return new boolean[] { xlog_, ylog_ };
        }
    }
}
