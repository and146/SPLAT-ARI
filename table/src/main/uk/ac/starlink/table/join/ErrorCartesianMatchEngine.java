package uk.ac.starlink.table.join;

import java.util.ArrayList;
import java.util.List;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.ValueInfo;

/**
 * Match Engine which works in an N-dimensional Cartesian space with
 * isotropic per-row errors.
 * Tuples are N+1 element, with the last element being the error radius,
 * so that a match results when the distance between two objects is
 * no greater than the sum of their error radii.
 *
 * @author   Mark Taylor
 * @since    1 Sep 2011
 */
public class ErrorCartesianMatchEngine extends AbstractCartesianMatchEngine {

    private final int ndim_;
    private final DescribedValue[] matchParams_;

    private static final DefaultValueInfo SCORE_INFO =
        new DefaultValueInfo( "Separation", Double.class,
                              "Spatial distance between matched points" );
    private static final DefaultValueInfo ERROR_INFO =
        new DefaultValueInfo( "Error", Number.class,
                              "Per-object error radius" );
    private static final DefaultValueInfo ERRSCALE_INFO =
        new DefaultValueInfo( "Scale", Number.class,
                              "Rough average of per-object error distance; "
                            + "just used for tuning in conjunction with "
                            + "bin factor" );

    /**
     * Constructor.
     *
     * @param  ndim  dimensionality
     * @param   scale   rough scale of errors
     */
    public ErrorCartesianMatchEngine( int ndim, double scale ) {
        super( ndim );
        ndim_ = ndim;
        matchParams_ = new DescribedValue[] {
                           new IsotropicScaleParameter( ERRSCALE_INFO ) };
        setIsotropicScale( scale );
    }

    /**
     * Sets the distance scale, which should be roughly the average
     * of per-object error distance
     * This is just used in conjunction with the bin factor for tuning.
     *
     * @param   scale  characteristic scale of errors
     */
    public void setScale( double scale ) {
        super.setIsotropicScale( scale );
    }

    /**
     * Returns the distance scale.
     *
     * @return  characteristic scale of errors
     */
    public double getScale() {
        return super.getIsotropicScale();
    }

    public ValueInfo[] getTupleInfos() {
        List<ValueInfo> infoList = new ArrayList<ValueInfo>();
        for ( int id = 0; id < ndim_; id++ ) {
            infoList.add( createCoordinateInfo( id ) );
        }
        infoList.add( ERROR_INFO );
        return infoList.toArray( new ValueInfo[ 0 ] );
    }

    public DescribedValue[] getMatchParameters() {
        return matchParams_;
    }

    public ValueInfo getMatchScoreInfo() {
        return SCORE_INFO;
    }

    public String toString() {
        return ndim_ + "-d Cartesian with Errors";
    }

    public double matchScore( Object[] tuple1, Object[] tuple2 ) {
        return matchScore( ndim_,
                           getTupleCoords( tuple1 ), getTupleCoords( tuple2 ),
                           getTupleError( tuple1 ) + getTupleError( tuple2 ) );
    }

    public Object[] getBins( Object[] tuple ) {
        return getRadiusBins( getTupleCoords( tuple ), getTupleError( tuple ) );
    }

    public boolean canBoundMatch() {
        return true;
    }

    public Comparable[][] getMatchBounds( Comparable[] minTuple,
                                          Comparable[] maxTuple ) {
        return createExtendedBounds( minTuple, maxTuple,
                                     2 * getTupleError( maxTuple ),
                                     indexRange( 0, ndim_ ) );
    }

    /**
     * Returns the Cartesian position coordinates associated with an
     * input tuple.
     *
     * @param  tuple  (ndim+1)-element coords,error array
     * @return  ndim-element coordinate array
     */
    private double[] getTupleCoords( Object[] tuple ) {
        double[] coords = new double[ ndim_ ];
        for ( int id = 0; id < ndim_; id++ ) {
            coords[ id ] = getNumberValue( tuple[ id ] );
        }
        return coords;
    }

    /**
     * Returns the error value associated with an input tuple.
     *
     * @param  tuple  (ndim+1)-element coords,error array
     * @return   error value
     */
    private double getTupleError( Object[] tuple ) {
        return getNumberValue( tuple[ ndim_ ] );
    }
}
