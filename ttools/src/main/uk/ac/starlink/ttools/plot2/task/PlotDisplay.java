package uk.ac.starlink.ttools.plot2.task;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.JComponent;
import uk.ac.starlink.ttools.plot.Range;
import uk.ac.starlink.ttools.plot2.AuxScale;
import uk.ac.starlink.ttools.plot2.Decoration;
import uk.ac.starlink.ttools.plot2.Drawing;
import uk.ac.starlink.ttools.plot2.LayerOpt;
import uk.ac.starlink.ttools.plot2.NavigationListener;
import uk.ac.starlink.ttools.plot2.Navigator;
import uk.ac.starlink.ttools.plot2.PlotLayer;
import uk.ac.starlink.ttools.plot2.PlotPlacement;
import uk.ac.starlink.ttools.plot2.PlotType;
import uk.ac.starlink.ttools.plot2.PlotUtil;
import uk.ac.starlink.ttools.plot2.PointCloud;
import uk.ac.starlink.ttools.plot2.ShadeAxis;
import uk.ac.starlink.ttools.plot2.Slow;
import uk.ac.starlink.ttools.plot2.Subrange;
import uk.ac.starlink.ttools.plot2.Surface;
import uk.ac.starlink.ttools.plot2.SurfaceFactory;
import uk.ac.starlink.ttools.plot2.config.ConfigMap;
import uk.ac.starlink.ttools.plot2.data.DataStore;
import uk.ac.starlink.ttools.plot2.paper.PaperType;
import uk.ac.starlink.ttools.plot2.paper.PaperTypeSelector;

/**
 * Graphical component which displays a plot.
 * The plot is in general 'live', and may repaint itself differently
 * over its lifetime according to user navigation actions,
 * window size, and underlying data, depending on how it is configured.
 * 
 * <p>This class can be used as-is, or as a template.
 *
 * @author   Mark Taylor
 * @since    1 Mar 2013
 */
public class PlotDisplay<P,A> extends JComponent {

    private final PlotType plotType_;
    private final PlotLayer[] layers_;
    private final DataStore dataStore_;
    private final SurfaceFactory<P,A> surfFact_;
    private final P profile_;
    private final Icon legend_;
    private final float[] legPos_;
    private final ShadeAxis shadeAxis_;
    private final Range shadeFixRange_;
    private final boolean surfaceAuxRange_;
    private final boolean caching_;
    private Map<AuxScale,Range> auxRanges_;
    private Surface approxSurf_;
    private Surface surface_;
    private A aspect_;
    private Icon icon_;

    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.ttools.plot2" );

    /**
     * Constructor.
     *
     * @param  plotType  type of plot
     * @param  layers   layers constituting plot content
     * @param  surfFact   surface factory
     * @param  profile   surface profile
     * @param  aspect   initial surface aspect (may get changed by zooming etc)
     * @param  legend   legend icon, or null if none required
     * @param  legPos   2-element array giving x,y fractional legend placement
     *                  position within plot (elements in range 0..1),
     *                  or null for external legend
     * @param  shadeAxis  shader axis, or null if not required
     * @param  shadeFixRange  fixed shader range,
     *                        or null for auto-range where required
     * @param  dataStore   data storage object
     * @param surfaceAuxRange  determines whether aux ranges are recalculated
     *                         when the surface changes
     * @param  navigator  user gesture navigation controller,
     *                    or null for a non-interactive plot
     * @param  caching   if true, plot image will be cached where applicable,
     *                   if false it will be regenerated from the data
     *                   on every repaint
     */
    public PlotDisplay( PlotType plotType, PlotLayer[] layers,
                        SurfaceFactory<P,A> surfFact, P profile, A aspect,
                        Icon legend, float[] legPos, ShadeAxis shadeAxis,
                        Range shadeFixRange, DataStore dataStore,
                        boolean surfaceAuxRange, final Navigator<A> navigator,
                        boolean caching ) {
        plotType_ = plotType;
        layers_ = layers;
        surfFact_ = surfFact;
        profile_ = profile;
        aspect_ = aspect;
        legend_ = legend;
        legPos_ = legPos;
        shadeAxis_ = shadeAxis;
        shadeFixRange_ = shadeFixRange;
        dataStore_ = dataStore;
        surfaceAuxRange_ = surfaceAuxRange;
        caching_ = caching;

        /* Add mouse listeners if required. */
        if ( navigator != null ) {
            new NavigationListener<A>() {
                public Surface getSurface() {
                    return surface_;
                }
                public Navigator<A> getNavigator() {
                    return navigator;
                }
                public Iterable<double[]> createDataPosIterable() {
                    return new PointCloud( layers_, true )
                          .createDataPosIterable( dataStore_ );
                }
                public void setAspect( A aspect ) {
                    PlotDisplay.this.setAspect( aspect );
                }
            }.addListeners( this );
        }
    }

    /**
     * Clears the current cached plot image, if any, so that regeneration
     * of the image from the data is forced when the next paint operation
     * is performed; otherwise it may be copied from a cached image.
     * This method is called automatically by <code>invalidate()</code>,
     * but may also be called manually, for instance if the data in the
     * data store may have changed.
     *
     * <p>This method has no effect if caching is not in force.
     */
    public void clearPlot() {
        icon_ = null;
    }

    @Override
    public void invalidate() {
        clearPlot();
        super.invalidate();
    }

    @Override
    protected void paintComponent( Graphics g ) {
        super.paintComponent( g );
        Insets insets = getInsets();

        /* If we already have a cached image, it is for the right plot,
         * just draw that.  If not, generate a new one. */
        Icon icon = icon_;
        if ( icon == null ) {
            Rectangle box =
                new Rectangle( insets.left, insets.top,
                               getWidth() - insets.left - insets.right,
                               getHeight() - insets.top - insets.bottom );

            /* (Re)calculate aux ranges if required. */
            Surface approxSurf =
                surfFact_.createSurface( box, profile_, aspect_ );
            Map<AuxScale,Range> auxRanges;
            if ( auxRanges_ != null &&
                 ! surfaceAuxRange_ || approxSurf.equals( approxSurf_ ) ) {
                auxRanges = auxRanges_;
            }
            else {
                auxRanges = getAuxRanges( layers_, approxSurf, shadeFixRange_,
                                          shadeAxis_, dataStore_ );
            }
            auxRanges_ = auxRanges;
            approxSurf_ = approxSurf;

            /* Work out plot positioning. */
            boolean withScroll = true;
            PlotPlacement placer =
                PlotPlacement
               .createPlacement( box, surfFact_, profile_, aspect_, withScroll,
                                 legend_, legPos_, shadeAxis_ );
            surface_ = placer.getSurface();

            /* Get rendering implementation. */
            LayerOpt[] opts = PaperTypeSelector.getOpts( layers_ );
            PaperType paperType = plotType_.getPaperTypeSelector()
                                 .getPixelPaperType( opts, this );

            /* Perform the plot to a possibly cached image. */
            long start = System.currentTimeMillis();
            icon = createIcon( placer, layers_, auxRanges, dataStore_,
                               paperType, true );
            PlotUtil.logTime( logger_, "Cache", start );
        }
        if ( caching_ ) {
            icon_ = icon;
        }

        /* Paint the image to this component. */
        long start = System.currentTimeMillis();
        icon.paintIcon( this, g, insets.left, insets.top );
        PlotUtil.logTime( logger_, "Paint", start );
    }

    /**
     * Sets the surface aspect.  This triggers a repaint if appropriate.
     *
     * @param  aspect  new aspect
     */
    public void setAspect( A aspect ) {
        if ( aspect != null && ! aspect.equals( aspect_ ) ) {
            aspect_ = aspect;
            clearPlot();
            repaint();
        }
    }

    /**
     * Returns the most recently set aspect.
     *
     * @return  current aspect
     */
    public A getAspect() {
        return aspect_;
    }

    /**
     * Creates a new PlotDisplay, interrogating a supplied ConfigMap object.
     * This will perform ranging from data if it is required;
     * in that case, it may take time to execute.
     *
     * @param  plotType  type of plot
     * @param  layers   layers constituting plot content
     * @param  surfFact   surface factory
     * @param  config   map containing surface profile, initial aspect
     *                  and navigator configuration
     * @param  legend   legend icon, or null if none required
     * @param  legPos   2-element array giving x,y fractional legend placement
     *                  position within plot (elements in range 0..1),
     *                  or null for external legend
     * @param  shadeAxis  shader axis, or null if not required
     * @param  shadeFixRange  fixed shader range,
     *                        or null for auto-range where required
     * @param  dataStore   data storage object
     * @param surfaceAuxRange  determines whether aux ranges are recalculated
     *                         when the surface changes
     * @param  navigable true for an interactive plot
     * @param  caching   if true, plot image will be cached where applicable,
     *                   if false it will be regenerated from the data
     *                   on every repaint
     * @return  new plot component
     */
    @Slow
    public static <P,A> PlotDisplay
            createPlotDisplay( PlotType plotType, PlotLayer[] layers,
                               SurfaceFactory<P,A> surfFact, ConfigMap config,
                               Icon legend, float[] legPos,
                               ShadeAxis shadeAxis, Range shadeFixRange,
                               DataStore dataStore, boolean surfaceAuxRange,
                               boolean navigable, boolean caching ) {
        P profile = surfFact.createProfile( config );

        /* Read ranges from data if necessary. */
        long t0 = System.currentTimeMillis();
        Range[] ranges = surfFact.useRanges( profile, config )
                       ? surfFact.readRanges( layers, dataStore )
                       : null;
        PlotUtil.logTime( logger_, "Range", t0 );

        /* Work out the initial aspect. */
        A aspect = surfFact.createAspect( profile, config, ranges );

        /* Get a navigator. */
        Navigator<A> navigator = navigable ? surfFact.createNavigator( config )
                                           : null;
     
        /* Create and return the component. */
        return new PlotDisplay<P,A>( plotType, layers, surfFact, profile,
                                     aspect, legend, legPos, shadeAxis,
                                     shadeFixRange, dataStore,
                                     surfaceAuxRange, navigator, caching );
    }

    /**
     * Creates an icon which will paint the content of this plot.
     *
     * @param  placer  plot placement
     * @param  layers   layers constituting plot content
     * @param  auxRanges  requested range information calculated from data
     * @param  dataStore  data storage object
     * @param  paperType  rendering type
     * @param  cached  whether to cache pixels for future use
     */
    @Slow
    public static Icon createIcon( PlotPlacement placer, PlotLayer[] layers,
                                   Map<AuxScale,Range> auxRanges,
                                   DataStore dataStore, PaperType paperType,
                                   boolean cached ) {
        Surface surface = placer.getSurface();
        int nl = layers.length;
        logger_.info( "Layers: " + nl + ", Paper: " + paperType );
        Drawing[] drawings = new Drawing[ nl ];
        Object[] plans = new Object[ nl ];
        long t1 = System.currentTimeMillis();
        for ( int il = 0; il < nl; il++ ) {
            drawings[ il ] = layers[ il ]
                            .createDrawing( surface, auxRanges, paperType );
            plans[ il ] = drawings[ il ].calculatePlan( plans, dataStore );
        }
        PlotUtil.logTime( logger_, "Plans", t1 );
        Icon dataIcon =
            paperType.createDataIcon( surface, drawings, plans, dataStore,
                                      cached );
        return placer.createPlotIcon( dataIcon );
    }

    /**
     * Gathers requested ranging information from data.
     *
     * @param  layers  plot layers
     * @param  surface  plot surface
     * @param  shadeFixRange  fixed shade range limits, if any
     * @param  shadeAxis   shade axis, if any
     * @param  dataStore  data storage object
     */
    @Slow
    public static Map<AuxScale,Range> getAuxRanges( PlotLayer[] layers,
                                                    Surface surface,
                                                    Range shadeFixRange,
                                                    ShadeAxis shadeAxis,
                                                    DataStore dataStore ) {

        /* Work out what ranges have been requested by plot layers. */
        AuxScale[] scales = AuxScale.getAuxScales( layers );

        /* Add any known fixed range values. */
        Map<AuxScale,Range> auxFixRanges = new HashMap<AuxScale,Range>();
        if ( shadeFixRange != null ) {
            auxFixRanges.put( AuxScale.COLOR, shadeFixRange );
        }

        /* Prepare list of ranges known to be logarithmic. */
        Map<AuxScale,Boolean> auxLogFlags = new HashMap<AuxScale,Boolean>();
        if ( shadeAxis != null ) {
            auxLogFlags.put( AuxScale.COLOR, shadeAxis.isLog() );
        }

        /* We will not be using subranges, so prepare an empty map. */
        Map<AuxScale,Subrange> auxSubranges = new HashMap<AuxScale,Subrange>();

        /* Work out what ranges we need to calculate. */
        AuxScale[] calcScales =
            AuxScale.getMissingScales( scales, new HashMap<AuxScale,Range>(),
                                       auxFixRanges );

        /* Calculate the ranges from the data. */
        long start = System.currentTimeMillis();
        Map<AuxScale,Range> auxDataRanges =
            AuxScale.calculateAuxRanges( calcScales, layers,
                                         surface, dataStore );
        PlotUtil.logTime( logger_, "AuxRange", start );

        /* Combine all the gathered information to acquire actual
         * data ranges for the plot. */
        return AuxScale.getClippedRanges( scales, auxDataRanges, auxFixRanges,
                                          auxSubranges, auxLogFlags );
    }
}
