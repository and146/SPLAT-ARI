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
import uk.ac.starlink.ttools.plot2.config.ConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigMap;
import uk.ac.starlink.ttools.plot2.config.ConfigMeta;
import uk.ac.starlink.ttools.plot2.config.OptionConfigKey;
import uk.ac.starlink.ttools.plot2.config.StringConfigKey;
import uk.ac.starlink.ttools.plot2.config.StyleKeys;
import uk.ac.starlink.ttools.plot2.config.SubrangeConfigKey;
import uk.ac.starlink.ttools.plot2.config.TimeConfigKey;
import uk.ac.starlink.ttools.plot2.data.DataStore;

/**
 * Surface factory for time plots.
 *
 * @author   Mark Taylor
 * @since    15 Aug 2013
 */
public class TimeSurfaceFactory
        implements SurfaceFactory<TimeSurfaceFactory.Profile,TimeAspect> {

    /** Config key for time axis lower bound, before subranging. */
    public static final ConfigKey<Double> TMIN_KEY =
        new TimeConfigKey( new ConfigMeta( "tmin", "Time Minimum" ) );

    /** Config key for time axis upper bound, before subranging. */
    public static final ConfigKey<Double> TMAX_KEY =
        new TimeConfigKey( new ConfigMeta( "tmax", "Time Maximum" ) );

    /** Config key for time axis subrange. */
    public static final ConfigKey<Subrange> TSUBRANGE_KEY =
        new SubrangeConfigKey( new ConfigMeta( "tsub", "Time Subrange" ) );

    /** Config key for Y axis lower bound, before subranging. */
    public static final ConfigKey<Double> YMIN_KEY =
        PlaneSurfaceFactory.YMIN_KEY;

    /** Config key for Y axis upper bound, before subranging. */
    public static final ConfigKey<Double> YMAX_KEY =
        PlaneSurfaceFactory.YMAX_KEY;

    /** Config key for Y axis subrange. */
    public static final ConfigKey<Subrange> YSUBRANGE_KEY =
        PlaneSurfaceFactory.YSUBRANGE_KEY;

    /** Config key for Y axis log scale flag. */
    public static final ConfigKey<Boolean> YLOG_KEY =
        PlaneSurfaceFactory.YLOG_KEY;

    /** Config key for Y axis flip flag. */
    public static final ConfigKey<Boolean> YFLIP_KEY =
        PlaneSurfaceFactory.YFLIP_KEY;

    /** Config key for time axis text label. */
    public static final ConfigKey<String> TLABEL_KEY =
        new StringConfigKey( new ConfigMeta( "timelabel", "Time Label" ),
                             null );

    /** Config key for Y axis text label.*/
    public static final ConfigKey<String> YLABEL_KEY =
        PlaneSurfaceFactory.YLABEL_KEY;

    /** Config key to determine if grid lines are drawn. */
    public static final ConfigKey<Boolean> GRID_KEY =
        new BooleanConfigKey( new ConfigMeta( "grid", "Draw Grid" ), false );

    /** Config key to control tick mark crowding on time axis. */
    public static final ConfigKey<Double> TCROWD_KEY =
        StyleKeys.createCrowdKey( new ConfigMeta( "tcrowd",
                                                  "Time Tick Crowding" ) );

    /** Config key to control tick mark crowding on Y axis. */
    public static final ConfigKey<Double> YCROWD_KEY =
        PlaneSurfaceFactory.YCROWD_KEY;

    /** Config key to control time value formatting. */
    public static final ConfigKey<TimeFormat> TFORMAT_KEY =
        new OptionConfigKey<TimeFormat>( new ConfigMeta( "tformat",
                                                         "Time Format" ),
                                         TimeFormat.class,
                                         TimeFormat.getKnownFormats() );

    public Surface createSurface( Rectangle plotBounds, Profile profile,
                                  TimeAspect aspect ) {
        Profile p = profile;
        return TimeSurface
              .createSurface( plotBounds, aspect,
                              p.ylog_, p.yflip_, p.tlabel_, p.ylabel_,
                              p.captioner_, p.grid_, p.tformat_,
                              p.tcrowd_, p.ycrowd_, p.minor_ );
    }

    public ConfigKey[] getProfileKeys() {
        List<ConfigKey> list = new ArrayList<ConfigKey>();
        list.addAll( Arrays.asList( new ConfigKey[] {
            YLOG_KEY,
            YFLIP_KEY,
            TLABEL_KEY,
            YLABEL_KEY,
            GRID_KEY,
            TCROWD_KEY,
            YCROWD_KEY,
            TFORMAT_KEY,
            StyleKeys.MINOR_TICKS,
        } ) );
        list.addAll( Arrays.asList( StyleKeys.getCaptionerKeys() ) );
        return list.toArray( new ConfigKey[ 0 ] );
    }

    public Profile createProfile( ConfigMap config ) {
        boolean ylog = config.get( YLOG_KEY );
        boolean yflip = config.get( YFLIP_KEY );
        String tlabel = config.get( TLABEL_KEY );
        String ylabel = config.get( YLABEL_KEY );
        boolean grid = config.get( GRID_KEY );
        double tcrowd = config.get( TCROWD_KEY );
        double ycrowd = config.get( YCROWD_KEY );
        TimeFormat tformat = config.get( TFORMAT_KEY );
        boolean minor = config.get( StyleKeys.MINOR_TICKS );
        Captioner captioner = StyleKeys.createCaptioner( config );
        return new Profile( ylog, yflip, tlabel, ylabel, captioner,
                            grid, tcrowd, ycrowd, tformat, minor );
    }

    public ConfigKey[] getAspectKeys() {
        return new ConfigKey[] {
            TMIN_KEY, TMAX_KEY, TSUBRANGE_KEY,
            YMIN_KEY, YMAX_KEY, YSUBRANGE_KEY,
        };
    }

    public boolean useRanges( Profile profile, ConfigMap config ) {
        return createUnrangedAspect( profile, config ) == null;
    }

    public TimeAspect createAspect( Profile profile, ConfigMap config,
                                    Range[] ranges ) {
        TimeAspect unrangedAspect = createUnrangedAspect( profile, config );
        if ( unrangedAspect != null ) {
            return unrangedAspect;
        }
        else {
            Range trange = ranges == null ? new Range() : ranges[ 0 ];
            Range yrange = ranges == null ? new Range() : ranges[ 1 ];
            double[] tlimits =
                PlaneSurfaceFactory
               .getLimits( config, TMIN_KEY, TMAX_KEY, TSUBRANGE_KEY,
                           false, trange );
            double[] ylimits =
                PlaneSurfaceFactory
               .getLimits( config, YMIN_KEY, YMAX_KEY, YSUBRANGE_KEY,
                           profile.ylog_, yrange );
                return new TimeAspect( tlimits, ylimits );
        }
    }

    public Range[] readRanges( PlotLayer[] layers, DataStore dataStore ) {
        return PlotUtil.readCoordinateRanges( layers, 2, dataStore );
    }

    public ConfigKey[] getNavigatorKeys() {
        return TimeNavigator.getConfigKeys();
    }

    public Navigator<TimeAspect> createNavigator( ConfigMap navConfig ) {
        return TimeNavigator.createNavigator( navConfig );
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
    private static TimeAspect createUnrangedAspect( Profile profile,
                                                    ConfigMap config ) {
        double[] tlimits =
            PlaneSurfaceFactory
           .getLimits( config, TMIN_KEY, TMAX_KEY, TSUBRANGE_KEY,
                       false, null );
        double[] ylimits =
            PlaneSurfaceFactory
           .getLimits( config, YMIN_KEY, YMAX_KEY, YSUBRANGE_KEY,
                       profile.ylog_, null );
        return tlimits == null || ylimits == null
             ? null
             : new TimeAspect( tlimits, ylimits );
    }

    /**
     * Profile class which defines fixed configuration items for a TimeSurface.
     * Instances of this class are usually obtained from the
     * {@link #createProfile createProfile} method.
     */
    public static class Profile {
        private final boolean ylog_;
        private final boolean yflip_;
        private final String tlabel_;
        private final String ylabel_;
        private final Captioner captioner_;
        private final boolean grid_;
        private final double tcrowd_;
        private final double ycrowd_;
        private final TimeFormat tformat_;
        private final boolean minor_;

        /**
         * Constructor.
         *
         * @param  ylog   whether to use logarithmic scaling on Y axis
         * @param  yflip  whether to invert direction of Y axis
         * @param  tlabel text for labelling time axis
         * @param  ylabel  text for labelling Y axis
         * @param  captioner  text renderer for axis labels etc
         * @param  grid   whether to draw grid lines
         * @param  tcrowd  crowding factor for tick marks on time axis;
         *                 1 is normal
         * @param  ycrowd  crowding factor for tick marks on Y axis;
         *                 1 is normal
         * @param  tformat time labelling format
         * @param  minor   whether to draw minor ticks
         */
        public Profile( boolean ylog, boolean yflip,
                        String tlabel, String ylabel, Captioner captioner,
                        boolean grid, double tcrowd, double ycrowd,
                        TimeFormat tformat, boolean minor ) {
            ylog_ = ylog;
            yflip_ = yflip;
            tlabel_ = tlabel;
            ylabel_ = ylabel;
            captioner_ = captioner;
            grid_ = grid;
            tcrowd_ = tcrowd;
            ycrowd_ = ycrowd;
            tformat_ = tformat;
            minor_ = minor;
        }

        /**
         * Indicates whether Y axis is logarithmic.
         *
         * @return  true for Y logarithmic scaling, false for linear
         */
        public boolean getYLog() {
            return ylog_;
        }
    }
}
