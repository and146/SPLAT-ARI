package uk.ac.starlink.ttools.plot2.geom;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.logging.Logger;
import skyview.geometry.Projecter;
import skyview.geometry.Rotater;
import skyview.geometry.Scaler;
import skyview.geometry.TransformationException;
import uk.ac.starlink.ttools.func.CoordsRadians;
import uk.ac.starlink.ttools.plot.Matrices;
import uk.ac.starlink.ttools.plot2.Captioner;
import uk.ac.starlink.ttools.plot2.PlotUtil;
import uk.ac.starlink.ttools.plot2.Surface;

/**
 * Surface implementation for plotting on the celestial sphere.
 * The data coordinates are direction cosines: 3-element arrays
 * containing normalised X,Y,Z coordinates, where each coordinate
 * is in the range -1..+1 and the sum of squares is 1.
 *
 * @author   Mark Taylor
 * @since    20 Feb 2013
 */
public class SkySurface implements Surface {

    private final int gxlo_;
    private final int gxhi_;
    private final int gylo_;
    private final int gyhi_;
    private final SkySys viewSystem_;
    private final boolean grid_;
    private final SkyAxisLabeller axLabeller_;
    private final boolean sexagesimal_;
    private final double crowd_;
    private final Captioner captioner_;
    private final double[] unrotmat_;
    private final Projection projection_;
    private final double[] rotmat_;
    private final double zoom_;
    private final double xoff_;
    private final double yoff_;
    private final double gScale_;
    private final int gXoff_;
    private final int gYoff_;
    private final double gZoom_;
    private final boolean skyFillsBounds_;
    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.ttools.plot2" );

    /**
     * Constructor.
     *
     * @param   plotBounds  graphics region within which the plot must fall
     * @param  projection  sky projection
     * @param  rotmat  9-element rotation matrix
     * @param  zoom    zoom factor; 1 means the sky is approximately
     *                 the same size as plot bounds
     * @param  xoff  x offset of plot centre from plot bounds centre
     *               in dimensionless units; 0 is centred
     * @param  yoff  y offset of plot centre from plot bounds centre
     *               in dimensionless units; 0 is centred
     * @param  viewSystem  sky system into which coordinates are projected
     * @param  grid   whether to draw coordinate grid
     * @param  axLabeller  sky axis labelling object
     * @param  sexagesimal  whether to use sexagesimal coordinates
     * @param  crowd   tick mark crowding factor, 1 is normal
     * @param  captioner  text rendering object
     */
    public SkySurface( Rectangle plotBounds, Projection projection,
                       double[] rotmat, double zoom, double xoff, double yoff,
                       SkySys viewSystem, boolean grid,
                       SkyAxisLabeller axLabeller, boolean sexagesimal,
                       double crowd, Captioner captioner ) {
        gxlo_ = plotBounds.x;
        gxhi_ = plotBounds.x + plotBounds.width;
        gylo_ = plotBounds.y;
        gyhi_ = plotBounds.y + plotBounds.height;
        viewSystem_ = viewSystem;
        grid_ = grid;
        sexagesimal_ = sexagesimal;
        crowd_ = crowd;
        captioner_ = captioner;
        projection_ = projection;
        rotmat_ = rotmat;
        zoom_ = zoom;
        xoff_ = xoff;
        yoff_ = yoff;
        unrotmat_ = Matrices.invert( rotmat_ );

        /* Work out the pixel offsets and scaling factors to transform
         * the dimensionless plot coordinates to graphics coordinates. */
        Shape projShape = projection_.getProjectionShape();
        Rectangle2D pBounds = projShape.getBounds2D();
        int xpix = gxhi_ - gxlo_;
        int ypix = gyhi_ - gylo_;
        gScale_ = Math.min( xpix / pBounds.getWidth(),
                            ypix / pBounds.getHeight() );
        gXoff_ = gxlo_ + (int) ( xoff_ * gScale_ + xpix / 2 );
        gYoff_ = gylo_ + (int) ( yoff_ * gScale_ + ypix / 2 );
        gZoom_ = zoom_ * gScale_;
        Rectangle2D.Double projBounds =
            new Rectangle2D.Double( -gXoff_ / gZoom_, -gYoff_ / gZoom_,
                                    ( gxhi_ - gxlo_ ) / gZoom_,
                                    ( gyhi_ - gylo_ ) / gZoom_ );
        skyFillsBounds_ = projShape.contains( projBounds );
        axLabeller_ = axLabeller == null
                    ? SkyAxisLabellers.getAutoLabeller( skyFillsBounds_ )
                    : axLabeller;
        assert this.equals( this );
    }

    /**
     * Returns 3.
     */
    public int getDataDimCount() {
        return 3;
    }

    public Rectangle getPlotBounds() {
        return new Rectangle( gxlo_, gylo_, gxhi_ - gxlo_, gyhi_ - gylo_ );
    }

    public Insets getPlotInsets( boolean withScroll ) {
        GridLiner gl = grid_ ? createGridder() : null;
        return gl == null
             ? new Insets( 0, 0, 0, 0 )
             : axLabeller_.createAxisAnnotation( gl, captioner_ )
                          .getPadding( withScroll );
    }

    public void paintBackground( Graphics g ) {
        Graphics2D g2 = (Graphics2D) g.create();
        final Shape gShape;
        if ( skyFillsBounds_ ) {
            gShape = new Rectangle( getPlotBounds() );
            ((Rectangle) gShape).width--;
            ((Rectangle) gShape).height--;
        }
        else {
            g2.setClip( new Rectangle( getPlotBounds() ) );
            g2.translate( gXoff_, gYoff_ );
            g2.scale( gZoom_, -gZoom_ );
            g2.setStroke( new BasicStroke( (float) ( 1.0 / gZoom_ ) ) );
            gShape = projection_.getProjectionShape();
        }
        g2.setColor( Color.WHITE );
        g2.fill( gShape );
        g2.setColor( Color.GRAY );
        g2.draw( gShape );
        g2.dispose();
    }

    public void paintForeground( Graphics g ) {
        if ( grid_ ) {
            Color color0 = g.getColor();
            Graphics2D g2 = (Graphics2D) g;
            g2.setColor( Color.GRAY );
            GridLiner gl = createGridder();
            if ( gl == null ) {
                return;
            }
            double[][][] lines = gl.getLines();
            String[] labels = gl.getLabels();
            int nl = labels.length;
            for ( int il = 0; il < nl; il++ ) {
                double[][] line = lines[ il ];
                int nseg = line.length;
                GeneralPath path =
                    new GeneralPath( GeneralPath.WIND_NON_ZERO, nseg );
                double[] seg0 = line[ 0 ];
                path.moveTo( (float) seg0[ 0 ], (float) seg0[ 1 ] );
                for ( int is = 1; is < nseg; is++ ) {
                    double[] seg = line[ is ];
                    path.lineTo( (float) seg[ 0 ], (float) seg[ 1 ] );
                }
                g2.draw( path );
            }
            g2.setColor( Color.BLACK );
            axLabeller_.createAxisAnnotation( gl, captioner_ )
                       .drawLabels( g2 );
            g2.setColor( color0 );
        }
    }

    /**
     * Attempts to constructs a GridLiner object which can
     * draw grid lines on this plot.
     * The work is done by classes from the SkyView package.
     *
     * @return   gridliner, or null on failure
     */
    private GridLiner createGridder() {
        if ( projection_ instanceof SkyviewProjection ) {
            Projecter projecter =
                ((SkyviewProjection) projection_).getSkyviewProjecter();
            Scaler scaler = new Scaler( gXoff_ - gxlo_, gYoff_ -gylo_,
                                        gZoom_, 0, 0, -gZoom_ );
            Rotater rotater;
            try {
                rotater = new Rotater( Matrices.toPal( rotmat_ ) );
            }
            catch ( TransformationException e ) {
                assert false;
                return null;
            }
            Rectangle plotBounds = getPlotBounds();
            if ( plotBounds.width < 32 || plotBounds.height < 32 ) {
                return null;
            }
            GridLiner gl = new GridLiner( plotBounds, rotater, projecter,
                                          scaler, sexagesimal_,
                                          crowd_, crowd_ );
            try {
                gl.grid();
                return gl;
            }
            catch ( TransformationException e ) {
                logger_.warning( "Grid error: " + e );
                return null;
            }
        }
        else {
            return null;
        }
    }

    public boolean dataToGraphics( double[] dpos, boolean visibleOnly,
                                   Point gpos ) {
        double[] rot = rotmat_;
        double sx = dpos[ 0 ];
        double sy = dpos[ 1 ];
        double sz = dpos[ 2 ];
        double rx = rot[ 0 ] * sx + rot[ 1 ] * sy + rot[ 2 ] * sz;
        double ry = rot[ 3 ] * sx + rot[ 4 ] * sy + rot[ 5 ] * sz;
        double rz = rot[ 6 ] * sx + rot[ 7 ] * sy + rot[ 8 ] * sz;
        Point2D.Double proj = new Point2D.Double();
        if ( projection_.project( rx, ry, rz, proj ) ) {
            int xp = (int) ( gXoff_ + proj.x * gZoom_ );
            int yp = (int) ( gYoff_ - proj.y * gZoom_ );
            if ( ! visibleOnly || 
                 ( xp >= gxlo_ && xp < gxhi_ && yp >= gylo_ && yp < gyhi_ ) ) {
                gpos.x = xp;
                gpos.y = yp;
                return true;
            }
        }
        return false;
    }

    public boolean dataToGraphicsOffset( double[] dpos0, Point gpos0,
                                         double[] dpos1, boolean visibleOnly,
                                         Point gpos1 ) {

        /* Because sky plots do not in general have continuous coordinates
         * (wrap around) implementing this method is not trivial. */

        /* Get the graphics position of the offset point the
         * straightforward way. */
        boolean aStatus = dataToGraphics( dpos1, visibleOnly, gpos1 );
        int ax = gpos1.x;
        int ay = gpos1.y;

        /* Get the graphics position of the point offset from the context
         * point in the opposite direction.  Some shuffling of values
         * is done to avoid requiring additional workspace arrays. */
        double dp1x = dpos1[ 0 ];
        double dp1y = dpos1[ 1 ];
        double dp1z = dpos1[ 2 ];
        reflectPoint( dpos0, dpos1 );
        boolean bStatus = dataToGraphics( dpos1, visibleOnly, gpos1 );
        dpos1[ 0 ] = dp1x;
        dpos1[ 1 ] = dp1y;
        dpos1[ 2 ] = dp1z;
        int bx = gpos1.x;
        int by = gpos1.y;

        /* Select one or the other to use as the output graphics position,
         * depending on which transformations worked at all, and which ones
         * give the shortest distance between the context and output graphics
         * positions.  This is not very respectable, but should give a
         * reasonable result in most cases for offsets which are not too
         * large in data (3D) space. */
        final boolean forward;
        if ( aStatus && bStatus ) {
            double ad = Math.hypot( ax - gpos0.x, ay - gpos0.y );
            double bd = Math.hypot( bx - gpos0.x, by - gpos0.y );
            forward = ad <= bd;
        }
        else if ( aStatus && ! bStatus ) {
            forward = true;
        }
        else if ( ! aStatus && bStatus ) {
            forward = false;
        }
        else {
            assert ! aStatus && ! bStatus;
            return false;
        }
        if ( forward ) {
            gpos1.x = ax;
            gpos1.y = ay;
        }
        else {
            gpos1.x = 2 * gpos0.x - bx;
            gpos1.y = 2 * gpos0.y - by;
        }
        return true;
    }

    /**
     * Reflects a point in another point.
     * The input and output coordinates are normalized 3-vectors.
     * This method works only with supplied arrays, it does not allocate
     * any new objects.
     *
     * @param  a   coordinates of reference point
     * @param  b   on entry, the coordinates of the point to reflect;
     *             on exit, the coordinates of the reflected point
     */
    private static void reflectPoint( double[] a, double[] b ) {
        double a0 = a[ 0 ];
        double a1 = a[ 1 ];
        double a2 = a[ 2 ];
        double b0 = b[ 0 ];
        double b1 = b[ 1 ];
        double b2 = b[ 2 ];

        /* Calculate cross product of vectors.  This represents the magnitude
         * and direction of the rotation that moves A to B. */
        double x = + ( a1 * b2 - a2 * b1 );
        double y = - ( a0 * b2 - a2 * b0 );
        double z = + ( a0 * b1 - a1 * b0 );

        /* Reverse the cross product. */
        x = -x;
        y = -y;
        z = -z;

        /* Construct a rotation matrix using the cross product as an
         * axial vector.  Use the code from SLA_DAV2M. */
        double phi = Math.sqrt( x * x + y * y + z * z );
        double s = Math.sin( phi );
        double c = Math.cos( phi );
        double w = 1.0 - c;
        if ( phi != 0.0 ) {
            double phi1 = 1.0 / phi;
            x *= phi1;
            y *= phi1;
            z *= phi1;
        }
        double m00 = x * x * w + c;
        double m01 = x * y * w + z * s;
        double m02 = x * z * w - y * s;
        double m10 = x * y * w - z * s;
        double m11 = y * y * w + c;
        double m12 = y * z * w + x * s;
        double m20 = x * z * w + y * s;
        double m21 = y * z * w - x * s;
        double m22 = z * z * w + c;

        /* Write the rotated vector into b. */
        b[ 0 ] = m00 * a0 + m10 * a1 + m20 * a2;
        b[ 1 ] = m01 * a0 + m11 * a1 + m21 * a2;
        b[ 2 ] = m02 * a0 + m12 * a1 + m22 * a2;
    }

    /**
     * Converts graphics coordinates to the dimensionless coordinates used
     * by the sky projection.
     *
     * @param   gpos  point in graphics coordinates
     * @return  point in dimensionless sky projection coordinates
     */
    private Point2D.Double graphicsToProjected( Point gpos ) {
        return new Point2D.Double( ( gpos.x - gXoff_ ) / +gZoom_,
                                   ( gpos.y - gYoff_ ) / -gZoom_ );
    }

    public double[] graphicsToData( Point gpos, Iterable<double[]> dposIt ) {
        Point2D.Double ppos = graphicsToProjected( gpos );
        if ( projection_.getProjectionShape().contains( ppos ) ) {
            double[] dpos = new double[ 3 ];
            if ( projection_.unproject( ppos, dpos ) ) {
                return Matrices.mvMult( unrotmat_, dpos );
            }
        }
        return null;
    }

    public String formatPosition( double[] dpos ) {
        return formatPosition( dpos, 2.0 * Math.PI / gZoom_ );
    }

    /**
     * Formats a position in data coordinates given the approximate size
     * of a screen pixel.
     * The pixel size is used to determine how much precision to give.
     *
     * @param  dpos  3-element array giving normalised X,Y,Z coordinates
     * @param  pixRad  approximate size of a screen pixel in radians
     */
    private static String formatPosition( double[] dpos, double pixRad ) {
        double x = dpos[ 0 ];
        double y = dpos[ 1 ];
        double z = dpos[ 2 ];
        double lat = Math.PI * 0.5 - Math.acos( z );
        double lon = Math.atan2( y, x );
        while ( lon < 0 ) {
            lon += 2 * Math.PI;
        }
        int secondDp = getDecimalPlaces( pixRad / Math.PI * 12 * 60 * 60 );
        int arcsecDp = getDecimalPlaces( pixRad / Math.PI * 180 * 60 * 60 );
        String lonSex =
            CoordsRadians.radiansToHms( lon, Math.max( 0, secondDp ) );
        if ( secondDp < -1 ) {
            lonSex = lonSex.substring( 0, lonSex.lastIndexOf( ':' ) );
        }
        String latSex =
            CoordsRadians.radiansToDms( lat, Math.max( 0, arcsecDp ) );
        if ( arcsecDp < -1 ) {
            latSex = latSex.substring( 0, latSex.lastIndexOf( ':' ) );
        }
        return new StringBuilder()
            .append( lonSex )
            .append( ", " )
            .append( latSex )
            .toString();
    }

    /**
     * Returns the approximate number of places after the decimal point
     * required to represent a given value to 1 significant figure.
     * Note the result may be negative, indicating a value approximately
     * greater than 1.
     *
     * @param   value small value
     * @return  number of decimal places required to represent value
     */
    private static int getDecimalPlaces( double value ) {
        return - (int) Math.round( Math.log( value ) / Math.log( 10 ) );
    }

    /**
     * Returns a SkyAspect representing this surface adjusted by a
     * user pan gesture between two points.
     *
     * @param  pos0  source graphics position
     * @param  pos1  destination graphics position
     * @return  panned sky aspect
     */
    SkyAspect pan( Point pos0, Point pos1 ) {
        return projPan( pos0, pos1 );
    }

    /**
     * Returns a SkyAspect representing this surface adjusted by a
     * user zoom gesture centred at a given point.
     *
     * @param  pos  reference graphics position
     * @param  factor   zoom factor
     * @return  zoomed sky aspect
     */
    SkyAspect zoom( Point pos, double factor ) {
        return projZoom( pos, factor );
    }

    /**
     * Returns a SkyAspect representing this surface recentred at a given point.
     *
     * @param  dpos  data position (normalised X,Y,Z) 
     * @return  re-centred sky aspect
     */
    SkyAspect center( double[] dpos ) {
        Point gp = new Point();
        return dataToGraphics( dpos, false, gp )
             ? pan( gp, new Point( ( gxlo_ + gxhi_ ) / 2,
                                   ( gylo_ + gyhi_ ) / 2 ) )
             : null;
    }

    /**
     * Pan gesture which just translates the entire graphics plane.
     *
     * @param  pos0  source graphics position
     * @param  pos1  destination graphics position
     * @return  panned sky aspect
     */
    public SkyAspect flatPan( Point pos0, Point pos1 ) {
        double xoff1 = xoff_ + ( pos1.x - pos0.x ) / gScale_;
        double yoff1 = yoff_ + ( pos1.y - pos0.y ) / gScale_;
        return createAspect( projection_, rotmat_, zoom_, xoff1, yoff1 );
    }

    /**
     * Zoom gesture which just magnifies the entire graphics plane.
     *
     * @param  pos  reference graphics position
     * @param  factor   zoom factor
     * @return  zoomed sky aspect
     */
    public SkyAspect flatZoom( Point pos, double factor ) {
        double dz = zoom_ * ( 1.0 - factor );
        double zoom1 = zoom_ * factor;
        double xoff1 = xoff_ + ( pos.x - gXoff_ ) / gZoom_ * dz;
        double yoff1 = yoff_ + ( pos.y - gYoff_ ) / gZoom_ * dz;
        return createAspect( projection_, rotmat_, zoom1, xoff1, yoff1 );
    }

    /**
     * Pan gesture which attempts to rotate the sky while leaving the
     * size and location of the graphics plane unchanged.
     * If that functionality is not supported by the projection,
     * it will fall back to flat panning.
     *
     * @param  pos0  source graphics position
     * @param  pos1  destination graphics position
     * @return  panned sky aspect
     */
    public SkyAspect projPan( Point pos0, Point pos1 ) {
        double[] rotmat1 =
            projection_.cursorRotate( rotmat_, graphicsToProjected( pos0 ),
                                               graphicsToProjected( pos1 ) );
        return rotmat1 == null
             ? flatPan( pos0, pos1 )
             : createAspect( projection_, rotmat1, zoom_, xoff_, yoff_ );
    }

    /**
     * Zoom gesture which attempts to zoom the sky, with the cursor staying
     * at the same sky position, while leaving the size and location of
     * the graphics plane unchanged.
     * If that functionality is not supported by the projection,
     * it will fall back to flat zooming.
     *
     * @param  pos  reference graphics position
     * @param  factor   zoom factor
     * @return  zoomed sky aspect
     */
    public SkyAspect projZoom( Point pos, double factor ) {
        Point2D.Double ppos0 = graphicsToProjected( pos );
        Point2D.Double ppos1 = new Point2D.Double( ppos0.x / factor,
                                                   ppos0.y / factor );
        double zoom1 = zoom_ * factor;
        double[] rotmat1 = projection_.projRotate( rotmat_, ppos0, ppos1 );
        return rotmat1 == null
             ? flatZoom( pos, factor )
             : createAspect( projection_, rotmat1, zoom1, xoff_, yoff_ );
    }

    @Override
    public boolean equals( Object o ) {
        if ( o instanceof SkySurface ) {
            SkySurface other = (SkySurface) o;
            return this.gxlo_ == other.gxlo_
                && this.gxhi_ == other.gxhi_
                && this.gylo_ == other.gylo_
                && this.gyhi_ == other.gyhi_
                && this.projection_.equals( other.projection_ )
                && Arrays.equals( this.rotmat_, other.rotmat_ )
                && this.zoom_ == other.zoom_
                && this.xoff_ == other.xoff_
                && this.yoff_ == other.yoff_
                && PlotUtil.equals( this.viewSystem_, other.viewSystem_ )
                && this.grid_ == other.grid_
                && PlotUtil.equals( this.axLabeller_, other.axLabeller_ )
                && this.sexagesimal_ == other.sexagesimal_
                && this.crowd_ == other.crowd_
                && PlotUtil.equals( this.captioner_, other.captioner_ );
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        int code = 9873;
        code = 23 * code + gxlo_;
        code = 23 * code + gxhi_;
        code = 23 * code + gylo_;
        code = 23 * code + gyhi_;
        code = 23 * code + projection_.hashCode();
        code = 23 * code + Arrays.hashCode( rotmat_ );
        code = 23 * code + Float.floatToIntBits( (float) zoom_ );
        code = 23 * code + Float.floatToIntBits( (float) xoff_ );
        code = 23 * code + Float.floatToIntBits( (float) yoff_ );
        code = 23 * code + PlotUtil.hashCode( viewSystem_ );
        code = 23 * code + ( grid_ ? 11 : 77 );
        code = 23 * code + PlotUtil.hashCode( axLabeller_ );
        code = 23 * code + ( sexagesimal_ ? 5 : 13 );
        code = 23 * code + Float.floatToIntBits( (float) crowd_ );
        code = 23 * code + PlotUtil.hashCode( captioner_ );
        return code;
    }

    /**
     * Creates a new sky aspect related to the one represented by this surface.
     * This method contains an assertion that the returned aspect has
     * the same reflection state as this one.
     *
     * @param  projection  sky projection
     * @param  rotmat  9-element rotation matrix
     * @param  zoom    zoom factor; 1 means the sky is approximately
     *                 the same size as plot bounds
     * @param  xoff  x offset of plot centre from plot bounds centre
     *               in dimensionless units; 0 is centred
     * @param  yoff  y offset of plot centre from plot bounds centre
     *               in dimensionless units; 0 is centred
     */
    private SkyAspect createAspect( Projection proj, double[] rotmat,
                                    double zoom, double xoff, double yoff ) {

        /* Check that the reflection status of the returned aspect is the
         * same as that for this surface's aspect.  Although that's not an
         * absolute invariant, all of the aspect-creating methods currently
         * in this class should obey it.   This test is equivalent to
         * checking that the SkyAspect.isReflected method returns the
         * same value for the aspect represented by this surface and the
         * one being generated. */
        assert Matrices.det( rotmat_ ) * Matrices.det( rotmat ) >= 0;
        return new SkyAspect( proj, rotmat, zoom, xoff, yoff );
    }
}
