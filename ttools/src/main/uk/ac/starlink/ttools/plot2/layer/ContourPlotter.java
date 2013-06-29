package uk.ac.starlink.ttools.plot2.layer;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Map;
import java.util.Arrays;
import java.util.logging.Logger;
import uk.ac.starlink.ttools.gui.ResourceIcon;
import uk.ac.starlink.ttools.plot.Range;
import uk.ac.starlink.ttools.plot2.AuxScale;
import uk.ac.starlink.ttools.plot2.DataGeom;
import uk.ac.starlink.ttools.plot2.Decal;
import uk.ac.starlink.ttools.plot2.Drawing;
import uk.ac.starlink.ttools.plot2.LayerOpt;
import uk.ac.starlink.ttools.plot2.PlotLayer;
import uk.ac.starlink.ttools.plot2.PointCloud;
import uk.ac.starlink.ttools.plot2.Surface;
import uk.ac.starlink.ttools.plot2.config.ConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigMap;
import uk.ac.starlink.ttools.plot2.config.ConfigMeta;
import uk.ac.starlink.ttools.plot2.config.DoubleConfigKey;
import uk.ac.starlink.ttools.plot2.config.IntegerConfigKey;
import uk.ac.starlink.ttools.plot2.config.StyleKeys;
import uk.ac.starlink.ttools.plot2.data.Coord;
import uk.ac.starlink.ttools.plot2.data.DataSpec;
import uk.ac.starlink.ttools.plot2.data.DataStore;
import uk.ac.starlink.ttools.plot2.paper.Paper;
import uk.ac.starlink.ttools.plot2.paper.PaperType;

/**
 * Plotter implementation that draws contours for a density map of points.
 *
 * @author   Mark Taylor
 * @since    17 Feb 2013
 */
public class ContourPlotter extends TuplePlotter<ContourStyle> {

    private static final ConfigKey<Integer> NLEVEL_KEY =
        new IntegerConfigKey( new ConfigMeta( "nlevel", "Level Count" ),
                              0, 999, 5 );
    private static final ConfigKey<Integer> SMOOTH_KEY =
        new IntegerConfigKey( new ConfigMeta( "smooth", "Smoothing" ),
                              1, 40, 3 );
    private static final ConfigKey<Double> OFFSET_KEY =
        DoubleConfigKey.createSliderKey( new ConfigMeta( "zero", "Zero Point" ),
                                         0, -2, +2, false );
    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.ttools.plot2" );

    /**
     * Constructor.
     */
    public ContourPlotter() {
        super( "Contour", ResourceIcon.PLOT_CONTOUR, new Coord[ 0 ] );
    }

    public ConfigKey[] getStyleKeys() {
        return new ConfigKey[] {
            StyleKeys.COLOR,
            NLEVEL_KEY,
            SMOOTH_KEY,
            StyleKeys.LEVEL_MODE,
            OFFSET_KEY,
        };
    }

    public ContourStyle createStyle( ConfigMap config ) {
        Color color = config.get( StyleKeys.COLOR );
        int nlevel = config.get( NLEVEL_KEY );

        /* Map offset to the range 0..1. */
        double offset = ( ( config.get( OFFSET_KEY ) % 1 ) + 1 ) % 1;
        int nsmooth = config.get( SMOOTH_KEY );
        LevelMode levMode = config.get( StyleKeys.LEVEL_MODE );
        return new ContourStyle( color, nlevel, offset, nsmooth, levMode );
    }

    public PlotLayer createLayer( DataGeom geom, DataSpec dataSpec,
                                  final ContourStyle style ) {
        final PointCloud pointCloud = new PointCloud( geom, dataSpec );
        LayerOpt opt = new LayerOpt( style.getColor(), true );
        return new AbstractPlotLayer( this, geom, dataSpec, style, opt ) {
            public Drawing createDrawing( Surface surface,
                                          Map<AuxScale,Range> auxRanges,
                                          PaperType paperType ) {

                /* It would be nice to draw vector contours.
                 * The algorithm is quite a bit more complicated though.
                 * Maybe one day. */
                if ( ! paperType.isBitmap() ) {
                    logger_.warning( "Sorry - "
                                   + "contours are ugly in vector plots" );
                }
                return new BitmapContourDrawing( surface, pointCloud,
                                                 style, paperType );
            }
        };
    }

    /**
     * Drawing implementation that plans and paints contours.
     * The plan is a density map (2d histogram).
     * Given this it works out where the level boundaries are and
     * just fills in a pixel anywhere two adjacent pixels are in
     * different levels.
     */
    private static class BitmapContourDrawing implements Drawing {

        private final Surface surface_;
        private final PointCloud pointCloud_;
        private final ContourStyle style_;
        private final PaperType paperType_;

        /**
         * Constructor.
         *
         * @param  surface  plot surface
         * @param  pointCloud  data point positions
         * @param  dataGeom  coordinate geometry
         * @param  dataSpec  coordinate specification
         */
        BitmapContourDrawing( Surface surface, PointCloud pointCloud,
                              ContourStyle style, PaperType paperType ) {
            surface_ = surface;
            pointCloud_ = pointCloud;
            style_ = style;
            paperType_ = paperType;
        }

        /**
         * Plan is a density map.
         */
        public Object calculatePlan( Object[] knownPlans,
                                     DataStore dataStore ) {
            return BinPlan.calculatePointCloudPlan( pointCloud_, surface_,
                                                    dataStore, knownPlans );
        }

        public void paintData( final Object plan, Paper paper,
                               final DataStore dataStore ) {
            /* For 2D we could fairly easily implement this as a Glyph
             * instead, which would probably be more efficient. */
            paperType_.placeDecal( paper, new Decal() {
                public void paintDecal( Graphics g ) {
                    paintContours( g, (BinPlan) plan, dataStore );
                }
                public boolean isOpaque() {
                    return true;
                }
            } );
        }

        /**
         * Does the work for plotting contours given a density map.
         *
         * @param   g  graphics context
         * @param   binPlan  point density map
         * @param   dataStore   data storage
         */
        private void paintContours( Graphics g, BinPlan binPlan,
                                    DataStore dataStore ) {
            final Binner binner = binPlan.getBinner();
            final Gridder gridder = binPlan.getGridder();
            Color color0 = g.getColor();
            g.setColor( style_.getColor() );
            Rectangle bounds = surface_.getPlotBounds();
            int xoff = bounds.x;
            int yoff = bounds.y;
            int nx = gridder.getWidth();
            int ny = gridder.getHeight();
            final int leng = gridder.getLength();

            /* Get an object which reports the density at each grid point.
             * To start with, this is just the counts in the supplied
             * density map. */
            NumberArray array = new NumberArray() {
                public int getLength() {
                    return leng;
                }
                public double getValue( int index ) {
                    return binner.getCount( index );
                }
            };

            /* If required, adjust this by smoothing. */
            int smooth = style_.getSmoothing();
            if ( smooth > 1 ) {
                array = smooth( array, gridder, smooth );
            }

            /* Set up a list of contour levels.  Contours are defined as
             * the boundaries of groups of contiguous pixels falling within
             * a single level. */
            Leveller leveller = createLeveller( array, style_ );

            /* For each pixel, see whether the next one along (+1 in X/Y
             * direction) is in a different level.  If so, paint a
             * contour pixel.  Note that really there is a systematic
             * positional offset of these contour pixels of +0.5 in
             * both directions.
             * Sweep in both directions (X,Y, then Y,X).  This paints some
             * contour pixels twice, but if you don't then lines that
             * are too steep miss pixels. */
            for ( int ix = 0; ix < nx; ix++ ) {
                int lev0 = leveller.getLevel(
                               array.getValue( gridder.getIndex( ix, 0 ) ) );
                for ( int iy = 1; iy < ny; iy++ ) {
                    int lev1 = leveller.getLevel(
                             array.getValue( gridder.getIndex( ix, iy ) ) );
                    if ( lev1 != lev0 ) {
                        g.fillRect( xoff + ix, yoff + iy - 1, 1, 1 );
                    }
                    lev0 = lev1;
                }
            }
            for ( int iy = 0; iy < ny; iy++ ) {
                int lev0 = leveller.getLevel(
                               array.getValue( gridder.getIndex( 0, iy ) ) );
                for ( int ix = 1; ix < nx; ix++ ) {
                    int lev1 = leveller.getLevel(
                             array.getValue( gridder.getIndex( ix, iy ) ) );
                    if ( lev1 != lev0 ) {
                        g.fillRect( xoff + ix - 1, yoff + iy, 1, 1 );
                    }
                    lev0 = lev1;
                }
            }
            g.setColor( color0 );
        }
    }

    /**
     * Smooths a pixel grid.
     * The smoothing just uses a square top hat kernel with full width given
     * by the smoothing parameter.
     *
     * @param   inArray  original grid data
     * @param   gridder  grid geometry
     * @param   smooth   smoothing parameter
     * @return  smoothed grid data
     */
    private static NumberArray smooth( NumberArray inArray, Gridder gridder,
                                       int smooth ) {
        int nx = gridder.getWidth();
        int ny = gridder.getHeight();
        final float[] out = new float[ gridder.getLength() ];
        int lo = - smooth / 2;
        int hi = ( smooth + 1 ) / 2;
        for ( int px = lo; px < hi; px++ ) {
            for ( int py = lo; py < hi; py++ ) {
                int ix0 = Math.max( 0, px );
                int ix1 = Math.min( nx, nx + px );
                int iy0 = Math.max( 0, py );
                int iy1 = Math.min( ny, ny + py );
                for ( int iy = iy0; iy < iy1; iy++ ) {
                    int jy = iy - py;
                    for ( int ix = ix0; ix < ix1; ix++ ) {
                        int jx = ix - px;
                        out[ gridder.getIndex( ix, iy ) ] +=
                            inArray.getValue( gridder.getIndex( jx, jy ) );
                    }
                }
            }
        }
        float factor = 1f / ( ( hi - lo ) * ( hi - lo ) );
        for ( int i = 0; i < out.length; i++ ) {
            out[ i ] *= factor;
        }
        return new NumberArray() {
            public int getLength() {
                return out.length;
            }
            public double getValue( int i ) {
                return out[ i ];
            }
        };
    }

    /**
     * Returns a leveller for a given data grid and contour style.
     *
     * @param   array  grid data
     * @param  style  contour style
     * @return  leveller object
     */
    private static Leveller createLeveller( NumberArray array,
                                            ContourStyle style ) {
        final double[] levels = style.getLevelMode()
                               .calculateLevels( array, style.getLevelCount(),
                                                 style.getOffset(), true );
        return new Leveller() {
            public int getLevel( double count ) {
                int ipos = Arrays.binarySearch( levels, count );
                return ipos < 0 ? - ( ipos + 1 ) : ipos;
            }
        };
    }

    /**
     * Knows how to turn a pixel value into an integer level.
     */
    private interface Leveller {

        /**
         * Returns the level value for a given pixel value.
         *
         * @param  pixel value
         * @return  contour level
         */
        int getLevel( double count );
    }
}
