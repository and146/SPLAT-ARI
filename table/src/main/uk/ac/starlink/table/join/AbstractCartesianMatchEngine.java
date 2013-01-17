package uk.ac.starlink.table.join;

import java.util.Arrays;
import java.util.HashSet;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.ValueInfo;

/**
 * Abstract superclass for match engines working in a Cartesian space.
 *
 * @author   Mark Taylor
 * @since    2 Sep 2011
 */
public abstract class AbstractCartesianMatchEngine implements MatchEngine {

    private final int ndim_;
    private final double[] scales_;
    private final double[] rBinSizes_;
    private final DescribedValue binFactorParam_;
    private double binFactor_;

    /**
     * Factor which determines bin size to use,
     * as a multiple of the maximum error distance, if no
     * bin factor is set explicitly.
     * This is a tuning parameter (any value will give correct results,
     * but performance may be affected).
     * The current value may not be optimal.
     */
    private static final double DEFAULT_BIN_FACTOR = 8;

    private static final DefaultValueInfo BINFACT_INFO =
        new DefaultValueInfo( "Bin Factor", Double.class,
                              "Scaling factor to adjust bin size; "
                            + "larger values mean larger bins" );

    /**
     * Constructor.
     *
     * @param   ndim  dimensionality of Cartesian space
     */
    public AbstractCartesianMatchEngine( int ndim ) {
        ndim_ = ndim;
        scales_ = new double[ ndim_ ];
        rBinSizes_ = new double[ ndim_ ];
        binFactor_ = DEFAULT_BIN_FACTOR;
        binFactorParam_ = new BinFactorParameter();
    }

    /**
     * Returns the dimensionality of the Cartesian space
     * in which this match engine works.
     *      
     * @return   number of spatial dimensions
     */
    public int getNdim() {
        return ndim_;
    }

    /**
     * Sets a multiplier for the length scale that determines bin size.
     *
     * @param  binFactor  bin size multiplier
     */
    public void setBinFactor( double binFactor ) {
        if ( ! ( binFactor > 0 ) ) {
            throw new IllegalArgumentException( "Bin factor must be >0" );
        }
        binFactor_ = binFactor;
        for ( int id = 0; id < ndim_; id++ ) {
            configureScale( id );
        }
    }

    /**
     * Returns the multiplier for length scale that determines bin size.
     *
     * @return  bin size multiplier
     */
    public double getBinFactor() {
        return binFactor_;
    }

    /**
     * Sets the scale isotropically.  All dimension scales are set to the
     * given value.
     *
     * @param  scale  guide error distance
     */
    public void setIsotropicScale( double scale ) {
        for ( int id = 0; id < ndim_; id++ ) {
            setScale( id, scale );
        }
    }

    /**
     * Returns the isotropic scale.  If all dimension scales are set to the
     * same value, that value is returned.  If they are not all set to the
     * same value, the return value is undefined.
     *
     * @return  scale  isotropic guide error distance
     */
    public double getIsotropicScale() {
        return scales_[ 0 ];
    }

    /**
     * Sets the scale value for a given dimension.  In conjunction with the
     * bin factor, this determines the bin size.
     *
     * @param  idim  dimension index
     * @param  scale  guide error distance in dimension <code>idim</code>
     */
    protected void setScale( int idim, double scale ) {
        if ( scale < 0 ) {
            throw new IllegalArgumentException( "Scale must be >0" );
        }
        scales_[ idim ] = scale;
        configureScale( idim );
    }

    /**
     * Returns the scale value for a given dimension.
     *
     * @param  idim  dimension index
     * @return  guide error distance in dimension <code>idim</code>
     */
    protected double getScale( int idim ) {
        return scales_[ idim ];
    }
 
    /**
     * Reconfigures internal state following a change to the tuning
     * parameters affecting a given dimension.
     *
     * @param  idim  dimension index
     */
    private void configureScale( int idim ) {
        rBinSizes_[ idim ] = 1.0 / ( scales_[ idim ] * binFactor_ );
    }

    public DescribedValue[] getTuningParameters() {
        return new DescribedValue[] { binFactorParam_ };
    }

    /**
     * Returns an array of the bin objects that may be covered within a
     * given distance of a given position.  Not all returned bins are
     * guaranteed to be so covered.  Validation is performed on the
     * arguments (NaNs will result in an empty return).
     *
     * @param  coords  central position
     * @param  radius  error radius
     * @return  bin objects that may be within <code>radius</code>
     *          of <code>coords</code>
     */
    protected Object[] getRadiusBins( double[] coords, double radius ) {
        return radius >= 0 ? doGetBins( coords, radius )
                           : NO_BINS;
    }

    /**
     * Returns an array of the bin objects that may be covered within the
     * current anisotropic scale length in each direction of a given position.
     * Not all returned bins are guaranteed to be so covered.
     * Validation is performed on the arguments (NaNs will result in
     * an empty return.
     *
     * @param  coords  central position
     * @return  bin objects within a scale length of <code>coords</code>
     */
    protected Object[] getScaleBins( double[] coords ) {
        return doGetBins( coords, Double.NaN );
    }

    /**
     * Does the work for the get*Bins methods.
     * Returns bins within some range of the given position.
     * If radius is a number, it is used;
     * if it's NaN, the scale length is used instead.
     *
     * @param   coords  central position
     * @param  radius  error radius or NaN
     * @return  list of bin objects
     */
    private Object[] doGetBins( double[] coords, double radius ) {
        boolean useScale = Double.isNaN( radius );

        /* Work out the range of cell label coordinates in each dimension
         * corresponding to a cube extending + and -err away from the
         * submitted position. */
        int[] llo = new int[ ndim_ ];     // lowest coord label index
        int[] lhi = new int[ ndim_ ];     // highest coord label index
        int ncell = 1;                    // total number of cells in cube
        for ( int id = 0; id < ndim_; id++ ) {
            double c0 = coords[ id ];
            if ( Double.isNaN( c0 ) ) {
                return NO_BINS;
            }
            else {
                double r = useScale ? scales_[ id ] : radius;
                llo[ id ] = getLabelComponent( id, c0 - r );
                lhi[ id ] = getLabelComponent( id, c0 + r );
                ncell *= lhi[ id ] - llo[ id ] + 1;
            }
        }

        /* Iterate over the cube of cells in ndim dimensions to construct
         * a list of all the cells inside it. */
        Cell[] cells = new Cell[ ncell ];
        int[] label = (int[]) llo.clone();
        for ( int ic = 0; ic < ncell; ic++ ) {
            cells[ ic ] = new Cell( (int[]) label.clone() );
            for ( int jd = 0; jd < ndim_; jd++ ) {
                if ( ++label[ jd ] <= lhi[ jd ] ) {
                    break;
                }
                else {
                    label[ jd ] = llo[ jd ];
                }
            }
        }

        /* Sanity check. */
        assert Arrays.equals( label, llo );
        assert new HashSet<Cell>( Arrays.asList( cells ) ).size()
               == cells.length;

        /* Return the list of cells. */
        return cells;
    }

    /** 
     * Returns the integer label of a cell position in a given dimension.
     * This identifies one of the coordinates of the discrete cube 
     * corresponding to any continuous position.
     *      
     * @param   idim  dimension index 
     * @param   coord  position in space in dimension <code>idim</code>
     * @return   index of cell coordinate in dimension <code>idim</code>
     */     
    private int getLabelComponent( int idim, double coord ) { 
        return (int) Math.floor( coord * rBinSizes_[ idim ] ); 
    }

    /**
     * Utility method to calculate a match score using an isotropic error
     * radius between two given Carteian positions.
     *
     * @param  ndim  coordinate dimensionality
     * @param  coords1  position 1, ndim-element array
     * @param  coords2  position 2, ndim-element array
     * @param  err  maximum separation for match
     * @return   Pythagoras distance between positions 1 and 2 if they are
     *           within err of each other, otherwise -1
     * @see  MatchEngine#matchScore
     */
    static double matchScore( int ndim, double[] coords1, double[] coords2,
                              double err ) {
        double err2 = err * err;
        double dist2 = 0;
        for ( int id = 0; id < ndim; id++ ) {
            double d = coords2[ id ] - coords1[ id ];
            dist2 += d * d;
            if ( ! ( dist2 <= err2 ) ) {
                return -1;
            }
        }
        double score = Math.sqrt( dist2 );
        assert score >= 0 && score <= err;
        return score;
    }

    /**
     * Utility method to return a pair of min/max comparable arrays
     * based on an input pair, but with some coordinates extended
     * by a given scalar value.  For the indicated tuple elements,
     * the output minima will be reduced, and maxima will be increased,
     * by the supplied error value.  Other elements will be null.
     * Elements which cannot be increased/reduced appropriately for some
     * reason will also be null.
     *
     * @param  minTuple  array of minimum values, may contain nulls
     * @param  maxTuple  array of maximum values, may contain nulls
     * @param  err    amount to extend min/max values
     * @param  idims  array of array indices for which minTuple and maxTuple
     *                should be extended
     * @return  2-element array of tuples - 
     *          effectively (minTuple,maxTuple) broadened by errors
     * @see   MatchEngine#getMatchBounds
     */
    static Comparable[][] createExtendedBounds( Comparable[] minTuple,
                                                Comparable[] maxTuple,
                                                double err, int[] idims ) {
        Comparable[] outMins = new Comparable[ minTuple.length ];
        Comparable[] outMaxs = new Comparable[ maxTuple.length ];
        for ( int jd = 0; jd < idims.length; jd++ ) {
            int id = idims[ jd ];
            outMins[ id ] = add( minTuple[ id ], -err );
            outMaxs[ id ] = add( maxTuple[ id ], +err );
        }
        return new Comparable[][] { outMins, outMaxs };
    }

    /**
     * Utility method to create a array which contains a given continuous
     * range of integer values.
     *
     * @param  ibase  lowest value
     * @param  icount  number of values
     * @return   icount-element array [ibase, ibase+1, ... ibase+icount-1]
     */
    static int[] indexRange( int ibase, int icount ) {
        int[] jxs = new int[ icount ];
        for ( int i = 0; i < icount; i++ ) {
            jxs[ i ] = ibase + i;
        }
        return jxs;
    }

    /**
     * Returns a description of the tuple element containing one of
     * the Cartesian coordinates.
     *
     * @param  idim  index of the coordinate in question
     * @return  metadata for coordinate <tt>idim</tt>
     */
    ValueInfo createCoordinateInfo( int idim ) {
        DefaultValueInfo info =
            new DefaultValueInfo( getCoordinateName( idim ), Number.class,
                                  getCoordinateDescription( idim ) );
        info.setNullable( false );
        return info;
    }

    /**
     * Returns a name for one of the coordinates.
     *
     * @param  idim  index of coordinate
     * @return  name to use for coordinate <tt>idim</tt>
     */
    String getCoordinateName( int idim ) {
        return ndim_ <= 3 ? new String[] { "X", "Y", "Z" }[ idim ]
                          : ( "Co-ord #" + ( idim + 1 ) );
    }

    /**
     * Returns the description of one of the coordinates.
     *
     * @param  idim  index of coordinate
     * @return  description to use for coordinate <tt>idim</tt>
     */
    String getCoordinateDescription( int idim ) {
        return "Cartesian co-ordinate #" + ( idim + 1 );
    }

    public abstract String toString();

    /**
     * Returns the numeric value for an object if it is a Number,
     * and NaN otherwise.
     *
     * @param  numobj  object
     * @return  numeric value
     */
    static double getNumberValue( Object numobj ) {
        return numobj instanceof Number
             ? ((Number) numobj).doubleValue()
             : Double.NaN;
    }

    /**
     * Attempts to add a numeric value to a Comparable object,
     * and returns an object of the same type having the new value.
     * If the addition can't be done (for instance if the input value
     * is not a Number, or if the increment value is NaN), null is returned.
     * Rounding where necessary is done in the direction of <code>incr</code>
     * (down for negative <code>incr</code>, up for positive).
     *
     * @param   in   input comparable object
     * @param   incr value to increment input number by
     * @return  object like <code>in</code>,
     *          but incremented by <code>incr</code>
     */
    static Comparable add( Comparable in, double incr ) {
        if ( ! ( in instanceof Number) || Double.isNaN( incr ) ) {
            return null;
        }
        double dval = ((Number) in).doubleValue() + incr;
        Class clazz = in.getClass();
        if ( incr < 0 ) {
            if ( clazz == Byte.class &&
                 Math.floor( dval ) >= Byte.MIN_VALUE ) {
                return new Byte( (byte) Math.floor( dval ) );
            }
            else if ( clazz == Short.class &&
                      Math.floor( dval ) >= Short.MIN_VALUE ) {
                return new Short( (short) Math.floor( dval ) );
            }
            else if ( clazz == Integer.class &&
                      Math.floor( dval ) >= Integer.MIN_VALUE ) {
                return new Integer( (int) Math.floor( dval ) );
            }
            else if ( clazz == Long.class &&
                      Math.floor( dval ) >= Long.MIN_VALUE ) {
                return new Long( (long) Math.floor( dval ) );
            }
            else if ( clazz == Float.class ) {
                return new Float( (float) dval );
            }
            else if ( clazz == Double.class ) {
                return new Double( dval );
            }
            else {
                return null;
            }
        }
        else if ( incr > 0 ) {
            if ( clazz == Byte.class &&
                 Math.ceil( dval ) <= Byte.MAX_VALUE ) {
                return new Byte( (byte) Math.ceil( dval ) );
            }
            else if ( clazz == Short.class &&
                      Math.ceil( dval ) <= Short.MAX_VALUE ) {
                return new Short( (short) Math.ceil( dval ) );
            }
            else if ( clazz == Integer.class &&
                      Math.ceil( dval ) <= Integer.MAX_VALUE ) {
                return new Integer( (int) Math.ceil( dval ) );
            }
            else if ( clazz == Long.class &&
                      Math.ceil( dval ) <= Long.MAX_VALUE ) {
                return new Long( (long) Math.ceil( dval ) );
            }
            else if ( clazz == Float.class ) {
                return new Float( (float) dval );
            }
            else if ( clazz == Double.class ) {
                return new Double( dval );
            }
            else {
                return null;
            }
        }
        else {
            assert incr == 0;
            return in instanceof Comparable ? (Comparable) in : null;
        }
    }

    /**
     * Tuning parameter which controls the bin factor.
     */
    class BinFactorParameter extends DescribedValue {
        BinFactorParameter() {
            super( BINFACT_INFO );
        }
        public Object getValue() {
            return new Double( getBinFactor() );
        }
        public void setValue( Object value ) {
            setBinFactor( ((Number) value).doubleValue() );
        }
    }

    /**
     * Parameter which controls the isotropic scale value.
     */
    class IsotropicScaleParameter extends DescribedValue {
        public IsotropicScaleParameter( ValueInfo info ) {
            super( info );
        }
        public Object getValue() {
            return new Double( getIsotropicScale() );
        }
        public void setValue( Object value ) {
            setIsotropicScale( ((Number) value).doubleValue() );
        }
    }
}
