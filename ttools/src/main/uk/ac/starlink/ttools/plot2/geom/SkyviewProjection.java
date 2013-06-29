package uk.ac.starlink.ttools.plot2.geom;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import skyview.geometry.Deprojecter;
import skyview.geometry.Projecter;
import skyview.geometry.Transformer;
import skyview.geometry.projecter.Ait;
import skyview.geometry.projecter.Arc;
import skyview.geometry.projecter.Car;
import skyview.geometry.projecter.Stg;
import skyview.geometry.projecter.Tan;
import uk.ac.starlink.ttools.plot.Range;

/**
 * Projection implementation based on classes from the Skyview package.
 *
 * @author   Mark Taylor
 * @since    21 Feb 2013
 */
public class SkyviewProjection implements Projection {

    private final Projecter projecter_;
    private final Deprojecter deprojecter_;
    private final Shape shape_;

    /** Aitoff projection. */
    public static final SkyviewProjection AIT = createProjection( new Ait() );

    /** Cartesian projection. */
    public static final SkyviewProjection CAR1 = createProjection( new Car1() );

    /** Gnomonic projection. */
    public static final SkyviewProjection TAN = createProjection( new Tan() );

    // Not whole sky and not currently rotatable - not much use.
    private static final SkyviewProjection ARC = createProjection( new Arc() );
    private static final SkyviewProjection STG = createProjection( new Stg() );

    // Predefined Unit vectors.
    private static final double[] RX = new double[] { 1, 0, 0 };
    private static final double[] RY = new double[] { 0, 1, 0 };
    private static final double[] RZ = new double[] { 0, 0, 1 };

    /**
     * Constructor.
     *
     * @param  projecter  projecter object
     * @param  shape   shape of the sky in this projection
     */
    public SkyviewProjection( Projecter projecter, Shape shape ) {
        projecter_ = projecter;
        deprojecter_ = projecter.inverse();
        shape_ = shape;
    }

    public String getName() {
        return projecter_.getName();
    }

    public String getDescription() {
        return projecter_.getDescription();
    }

    public Shape getProjectionShape() {
        return shape_;
    }

    public boolean project( double rx, double ry, double rz,
                            Point2D.Double pos ) {
        double[] r2 = projecter_.transform( new double[] { rx, ry, rz } );
        if ( Double.isNaN( r2[ 0 ] ) ) {
            return false;
        }
        else {
            pos.x = r2[ 0 ];
            pos.y = r2[ 1 ];
        }
        return true;
    }

    public boolean unproject( Point2D.Double pos, double[] r3 ) {
        double[] xy = new double[] { pos.x, pos.y };
        if ( projecter_.validPosition( xy ) ) {
            deprojecter_.transform( xy, r3 );
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns null - rotation not implemented.
     */
    public double[] cursorRotate( double[] rotmat, Point2D.Double pos0,
                                  Point2D.Double pos1 ) {
        return null;
    }

    /**
     * Returns null - rotation not implemented.
     */
    public double[] projRotate( double[] rotmat, Point2D.Double pos0,
                                Point2D.Double pos1 ) {
        return null;
    }

    /**
     * Returns false - ranging not used.
     */
    public boolean useRanges( boolean reflect, double[] r3, double radiusRad ) {
        return false;
    }

    public SkyAspect createAspect( boolean reflect, double[] r3,
                                   double radiusRad, Range[] ranges ) {
        Point2D.Double dpos = new Point2D.Double();
        if ( r3 != null &&
             project( r3[ 0 ], r3[ 1 ], r3[ 2 ], dpos ) ) {
            double zoom = Math.PI / radiusRad;
            return new SkyAspect( this, SkyAspect.unitMatrix( reflect ),
                                  zoom, dpos.x * zoom, dpos.y * zoom );
        }
        else {
            return new SkyAspect( this, reflect );
        }
    }

    /**
     * Returns the projecter object used by this SkyviewProjection.
     *
     * @return  projecter
     */
    public Projecter getSkyviewProjecter() {
        return projecter_;
    }

    /**
     * Factory method that knows shapes for some projections.
     *
     * @param  projecter  skyview projecter
     * @throws  IllegalArgumentException  if the shape is not known
     */
    public static SkyviewProjection createProjection( Projecter projecter ) {
        return new SkyviewProjection( projecter, getShape( projecter ) );
    }

    /**
     * Returns the shape of the sky for some projections.
     * This is the region of normalised plane coordinates into which
     * all invocations of the <code>project</code> method will map.
     *
     * @param  projecter  projecter
     * @param   sky projection shap in dimensionless units
     */
    private static Shape getShape( Projecter projecter ) {
        final double S2 = Math.sqrt( 2.0 );
        final double PI = Math.PI;
        Class clazz = projecter.getClass();
        if ( clazz.equals( Ait.class ) ) {
            return new Ellipse2D.Double( -2 * S2, -S2, 4 * S2, 2 * S2 );
        }
        else if ( clazz.equals( Arc.class ) ) {
            return new Ellipse2D.Double( -PI, -PI, 2 * PI, 2 * PI );
        }
        else if ( clazz.equals( Car1.class ) ) {
            return new Rectangle2D.Double( -PI, -0.5 * PI, 2 * PI, PI );
        }
        else if ( clazz.equals( Stg.class ) ) {
            return new Ellipse2D.Double( -2, -2, 4, 4 );
        }
        // Hmm, Tan projection fills the entire plane.
        else if ( clazz.equals( Tan.class ) ) {
            return null;
        }
        throw new IllegalArgumentException( "Don't know shape for projection" );
    }

    /**
     * Like the skyview Car projection, but does not repeat to tile the plane.
     * Like Car, it is centered on the origin, which is probably not ideal
     * (would be better with longitude running from 0 to 2*PI).
     */
    private static class Car1 extends Projecter {
        final Car base_;

        /**
         * Constructor.
         */
        Car1() {
            base_ = new Car();
        }

        public String getName() {
            return base_.getName();
        }

        public String getDescription() {
            return base_.getDescription();
        }

        @Override
        public Deprojecter inverse() {
            // this is wrong!  but I don't think it matters for our purposes.
            return base_.inverse();
        }

        @Override
        public double getXTiling() {
            return 0;
        }

        @Override
        public double getYTiling() {
            return 0;
        }

        @Override
        public boolean isInverse( Transformer t ) {
            return base_.isInverse( t );
        }

        @Override
        public void transform( double[] sphere, double[] plane ) {
            base_.transform( sphere, plane );
        }

        @Override
        public boolean allValid() {
            return false;
        }

        @Override
        public boolean validPosition( double[] pos ) {
            double x = pos[ 0 ];
            double y = pos[ 1 ];
            return x >= -Math.PI && x <= Math.PI
                && y >= -Math.PI * 0.5 && y <= Math.PI * 0.5;
        }
    }
}
