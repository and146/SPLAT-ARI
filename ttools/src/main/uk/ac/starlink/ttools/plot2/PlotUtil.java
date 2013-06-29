package uk.ac.starlink.ttools.plot2;

import java.awt.Graphics2D;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.Icon;
import uk.ac.starlink.ttools.plot.PdfGraphicExporter;
import uk.ac.starlink.ttools.plot.Picture;
import uk.ac.starlink.ttools.plot.Range;
import uk.ac.starlink.ttools.plot2.data.DataStore;
import uk.ac.starlink.ttools.plot2.data.TupleSequence;

/**
 * Miscellaneous utilities for use with the plotting classes.
 *
 * @author   Mark Taylor
 * @since    13 Feb 2013
 */
public class PlotUtil {

    /**
     * TupleSequence instance that contains no tuples.
     */
    public static final TupleSequence EMPTY_TUPLE_SEQUENCE =
            new TupleSequence() {
        public boolean next() {
            return false;
        }
        public long getRowIndex() {
            return -1L;
        }
        public Object getObjectValue( int icol ) {
            throw new IllegalStateException();
        }
        public double getDoubleValue( int icol ) {
            throw new IllegalStateException();
        }
        public boolean getBooleanValue( int icol ) {
            throw new IllegalStateException();
        }
    };

    /** Relative location of latex font location list. */
    private static final String LATEX_FONT_PATHS = "latex_fonts.txt";

    /** PDF GraphicExporter suitable for use with JLaTeXMath. */
    public static final PdfGraphicExporter LATEX_PDF_EXPORTER =
        PdfGraphicExporter
       .createExternalFontExporter( PlotUtil.class
                                   .getResource( LATEX_FONT_PATHS ) );

    /**
     * Private constructor prevents instantiation.
     */
    private PlotUtil() {
    }

    /**
     * Compares two possibly null objects for equality.
     *
     * @param  o1  one object or null
     * @param  o2  other object or null
     * @return   true iff objects are equal or are both null
     */
    public static boolean equals( Object o1, Object o2 ) {
        return o1 == null ? o2 == null : o1.equals( o2 );
    }

    /**
     * Returns a hash code for a possibly null object.
     *
     * @param   obj  object or null
     * @return   hash value
     */
    public static int hashCode( Object obj ) {
        return obj == null ? 0 : obj.hashCode();
    }

    /**
     * Policy for whether to cache full precision coordinates.
     *
     * @return   if false, it's OK to truncate doubles to floats
     *           when it seems reasonable
     */
    public static boolean storeFullPrecision() {
        return false;
    }

    /**
     * Writes message through the logging system
     * about the time a named step has taken.
     * The elapsed time is presumed to be the time between the supplied
     * time and the time when this method is called.
     * If the elapsed time is zero, nothing is logged.
     *
     * @param  logger   log message destaination
     * @param  phase  name of step to log time of
     * @param  start   start {@link java.lang.System#currentTimeMillis
     *                              currentTimeMillis}
     */
    public static void logTime( Logger logger, String phase, long start ) {
        long time = System.currentTimeMillis() - start;
        if ( time > 0 ) {
            logger.info( phase + " time: " + time );
        }
    }

    /**
     * Concatenates two arrays to form a single one.
     *
     * @param  a1  first array
     * @param  a2  second array
     * @return  concatenated array
     */
    public static <T> T[] arrayConcat( T[] a1, T[] a2 ) {
        int count = a1.length + a2.length;
        List<T> list = new ArrayList<T>( count );
        list.addAll( Arrays.asList( a1 ) );
        list.addAll( Arrays.asList( a2 ) );
        Class eClazz = a1.getClass().getComponentType();
        @SuppressWarnings("unchecked")
        T[] result =
            (T[]) list.toArray( (Object[]) Array.newInstance( eClazz, count ) );
        return result;
    }

    /**
     * Turns an Icon into a Picture.
     *
     * @param   icon   icon
     * @return  picture  picture
     */
    public static Picture toPicture( final Icon icon ) {
        return new Picture() {
            public int getPictureWidth() {
                return icon.getIconWidth();
            }
            public int getPictureHeight() {
                return icon.getIconHeight();
            }
            public void paintPicture( Graphics2D g2 ) {
                icon.paintIcon( null, g2, 0, 0 );
            }
        };
    }

    /**
     * Determines range information for each of the coordinates of the
     * data positions in a PointCloud.
     *
     * @param   cloud  point cloud
     * @param   nDataDim  dimensionality of data points in the point cloud
     * @param   dataStore  data storage
     * @return   nDataDim-element array of ranges, each containing the
     *           range of data position coordinate values for
     *           the corresponding dimension
     */
    @Slow
    public static Range[] readCoordinateRanges( PointCloud cloud, int nDataDim,
                                                DataStore dataStore ) {
        Range[] ranges = new Range[ nDataDim ];
        for ( int idim = 0; idim < nDataDim; idim++ ) {
            ranges[ idim ] = new Range();
        }                      
        for ( double[] dpos : cloud.createDataPosIterable( dataStore ) ) {
            for ( int idim = 0; idim < nDataDim; idim++ ) {
                ranges[ idim ].submit( dpos[ idim ] );
            }       
        }
        return ranges;
    }

    /**
     * Returns a value determined by a fixed range and a scaling factor
     * within it.  The scaling factor is assumed in the range 0-1;
     * if zero the minimum value is returned, and if one the maximum value
     * is returned.
     *
     * @param  min  minimum of range
     * @param  max  maximum of range
     * @param  factor01  factor in the range 0 to 1
     * @param  isLog  true iff the range is logarithmic
     * @return   value between min and max
     */
    public static double scaleValue( double min, double max, double factor01,
                                     boolean isLog ) {
        return isLog
             ? Math.exp( Math.log( min ) + factor01 * Math.log( max / min ) )
             : min + factor01 * ( max - min );
    }

    /**
     * Returns a range determined by a fixed range and a subrange within it.
     * If the subrange is 0-1 the output range is the input range.
     *
     * @param  min  minimum of range
     * @param  max  maximum of range
     * @param  subrange  sub-range, both ends between 0 and 1
     * @param  isLog  true iff the range is logarithmic
     * @return   2-element array giving low, high values of scaled range
     */
    public static double[] scaleRange( double min, double max,
                                       Subrange subrange, boolean isLog ) {
        return new double[] {
            scaleValue( min, max, subrange.getLow(), isLog ),
            scaleValue( min, max, subrange.getHigh(), isLog ),
        };
    }

    /**
     * Indicates whether two floating point numbers are approximately equal
     * to each other.
     * Exact semantics are intentionally not well-defined by this contract.
     *
     * @param   v0  one value
     * @param   v1  other value
     * @return  true if they are about the same
     */
    public static boolean approxEquals( double v0, double v1 ) {
        if ( v0 == 0 ) {
            return v1 == 0;
        }
        else {
            double r = v1 / v0;
            return r >= .9999 && r <= 1.0001;
        }
    }
}
