package uk.ac.starlink.table.formats;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.TableFormatException;

/**
 * Examines unknown rows (arrays of strings) to work out what they contain.
 * By repeatedly calling {@link #submitRow} the evaluator can refine its
 * idea of what kind of data is represented by each column.
 *
 * @author   Mark Taylor (Starlink)
 * @since    21 Sep 2004
 */
public class RowEvaluator {

    private boolean[] maybeBoolean_;
    private boolean[] maybeShort_;
    private boolean[] maybeInteger_;
    private boolean[] maybeLong_;
    private boolean[] maybeFloat_;
    private boolean[] maybeDouble_;
    private boolean[] maybeDate_;
    private boolean[] maybeHms_;
    private boolean[] maybeDms_;
    private int[] stringLength_;
    private long nrow_;
    private int ncol_ = -1;

    static final Pattern ISO8601_REGEX = Pattern.compile(
        "([0-9]+)-([0-9]{1,2})-([0-9]{1,2})" +
        "(?:[" + 'T' + " ]([0-9]{1,2})" +
            "(?::([0-9]{1,2})" +
                "(?::([0-9]{1,2}(?:\\.[0-9]*)?))?" +
            ")?" +
        "Z?)?"
    );
    private static final Pattern HMS_REGEX = Pattern.compile(
        "[ 012]?[0-9][:h ][ 0-6][0-9][:m ][0-6][0-9](\\.[0-9]*)?"
    );
    private static final Pattern DMS_REGEX = Pattern.compile(
        "[-+][ 0-9]?[0-9][:d ][ 0-6][0-9][:m ][0-6][0-9](\\.[0-9]*)?"
    );
    private static final Pattern NAN_REGEX = Pattern.compile(
        "NaN", Pattern.CASE_INSENSITIVE
    );
    private static final Pattern INFINITY_REGEX = Pattern.compile(
        "([+-]?)(Infinity|inf)", Pattern.CASE_INSENSITIVE
    );

    /** Decoder for booleans. */
    private static Decoder BOOLEAN_DECODER = new Decoder( Boolean.class ) {
        public Object decode( String value ) {
            char v1 = value.trim().charAt( 0 );
            return ( v1 == 't' || v1 == 'T' ) ? Boolean.TRUE
                                              : Boolean.FALSE;
        }
        public boolean isValid( String value ) {
            return value.equalsIgnoreCase( "false" )
                || value.equalsIgnoreCase( "true" )
                || value.equalsIgnoreCase( "f" )
                || value.equalsIgnoreCase( "t" );
        }
    };

    /* We are careful to check for "-0" type cells in the integer type
     * decoders - it is essential that they are coded as floating types
     * (which can represent negative zero) rather than integer types
     * (which can't), since a negative zero is most likely the
     * hours/degrees part of a sexegesimal angle, in which the
     * difference is very important
     * (see uk.ac.starlink.topcat.func.Angles.dmsToRadians). */

    /** Decoder for shorts. */
    private static Decoder SHORT_DECODER = new Decoder( Short.class ) {
        public Object decode( String value ) {
            return new Short( Short.parseShort( value.trim() ) );
        }
        public boolean isValid( String value ) {
            try {
                return Short.parseShort( value ) != 0
                    || value.charAt( 0 ) != '-';
            }
            catch ( NumberFormatException e ) {
                return false;
            }
        }
    };

    /** Decoder for integers. */
    private static Decoder INTEGER_DECODER = new Decoder( Integer.class ) {
        public Object decode( String value ) {
            return new Integer( Integer.parseInt( value.trim() ) );
        }
        public boolean isValid( String value ) {
            try {
                return Integer.parseInt( value ) != 0
                    || value.charAt( 0 ) != '-';
            }
            catch ( NumberFormatException e ) {
                return false;
            }
        }
    };

    /** Decoder for longs. */
    private static Decoder LONG_DECODER = new Decoder( Long.class ) {
        public Object decode( String value ) {
            return new Long( Long.parseLong( value.trim() ) );
        }
        public boolean isValid( String value ) {
            try {
                return Long.parseLong( value ) != 0L
                    || value.charAt( 0 ) != '-';
            }
            catch ( NumberFormatException e ) {
                return false;
            }
        }
    };

    /** Decoder for floats. */
    private static Decoder FLOAT_DECODER = new Decoder( Float.class ) {
        public Object decode( String value ) {
            return new Float( (float) parseFloating( value.trim() ).dValue );
        }
        public boolean isValid( String value ) {
            try {
                ParsedFloat pf = parseFloating( value );
                if ( pf.sigFig > 6 ||
                     ( Float.isInfinite( (float) pf.dValue ) &&
                       ! Double.isInfinite( pf.dValue ) ) ) {
                    return false;
                }
                return true;
            }
            catch ( NumberFormatException e ) {
                return false;
            }
        }
    };

    /** Decoder for doubles. */
    private static Decoder DOUBLE_DECODER = new Decoder( Double.class ) {
        public Object decode( String value ) {
            return new Double( parseFloating( value.trim() ).dValue );
        }
        public boolean isValid( String value ) {
            try {
                parseFloating( value );
                return true;
            }
            catch ( NumberFormatException e ) {
                return false;
            }
        }
    };

    /** Decoder for ISO-8601 dates. */
    private static Decoder DATE_DECODER = new StringDecoder() {
        public ColumnInfo createColumnInfo( String name ) {
            ColumnInfo info = super.createColumnInfo( name );
            info.setUnitString( "iso-8601" );
            info.setUCD( "TIME" );
            return info;
        }
        public boolean isValid( String value ) {
            return ISO8601_REGEX.matcher( value ).matches();
        }
    };

    /** Decoder for HMS sexagesimal strings. */
    private static Decoder HMS_DECODER = new StringDecoder() {
        public ColumnInfo createColumnInfo( String name ) {
            ColumnInfo info = super.createColumnInfo( name );
            info.setUnitString( "hms" );
            return info;
        }
        public boolean isValid( String value ) {
            return HMS_REGEX.matcher( value ).matches();
        }
    };

    /** Decoder for DMS sexagesimal strings. */
    private static Decoder DMS_DECODER = new StringDecoder() {
        public ColumnInfo createColumnInfo( String name ) {
            ColumnInfo info = super.createColumnInfo( name );
            info.setUnitString( "dms" );
            return info;
        }
        public boolean isValid( String value ) {
            return DMS_REGEX.matcher( value ).matches();
        }
    };

    /** Decoder for any old string. */
    private static Decoder STRING_DECODER = new StringDecoder() {
        public boolean isValid( String value ) {
            return true;
        }
    };

    /**
     * Constructs a new RowEvaluator which will work out the number of
     * columns from the data.
     */
    public RowEvaluator() {
    }

    /**
     * Constructs a new RowEvaluator which will examine rows with a
     * fixed number of columns.
     *
     * @param  ncol  column count
     */
    public RowEvaluator( int ncol ) {
        init( ncol );
    }

    /**
     * Initializes to deal with rows of a given number of elements.
     */
    private void init( int ncol ) {
        ncol_ = ncol; 

        /* This data could be set up more compactly, indexing via type-specific
         * decoders rather than having a named array for each possible type. */
        maybeBoolean_ = makeFlagArray( true );
        maybeShort_ = makeFlagArray( true );
        maybeInteger_ = makeFlagArray( true );
        maybeLong_ = makeFlagArray( true );
        maybeFloat_ = makeFlagArray( true );
        maybeDouble_ = makeFlagArray( true );
        maybeDate_ = makeFlagArray( true );
        maybeHms_ = makeFlagArray( true );
        maybeDms_ = makeFlagArray( true );
        stringLength_ = new int[ ncol ];
    }

    /**
     * Looks at a given row (list of strings) and records information about
     * what sort of things it looks like it contains.
     *
     * @param   row  <tt>ncol</tt>-element list of strings
     * @throws  TableFormatException  if the number of elements in
     *          <tt>row</tt> is not the same as on the first call
     */
    public void submitRow( List row ) throws TableFormatException {
        nrow_++;
        if ( ncol_ < 0 ) {
            init( row.size() );
        }
        if ( row.size() != ncol_ ) {
            throw new TableFormatException(
                "Wrong number of columns at row " + nrow_ +
                " (expecting " + ncol_ + ", found " + row.size() +  ")" );
        }
        for ( int icol = 0; icol < ncol_; icol++ ) {
            boolean done = false;
            String cell0 = (String) row.get( icol );
            int leng0 = cell0 == null ? 0 : cell0.length();
            String cell = cell0 == null ? "" : cell0.trim();
            int leng = cell.length();
            if ( leng == 0 ) {
                done = true;
            }
            if ( leng0 > stringLength_[ icol ] ) {
                stringLength_[ icol ] = leng0;
            }
            if ( ! done && maybeBoolean_[ icol ] ) {
                if ( BOOLEAN_DECODER.isValid( cell ) ) {
                    done = true;
                }
                else {
                    maybeBoolean_[ icol ] = false;
                }
            }
            if ( ! done && maybeShort_[ icol ] ) {
                if ( SHORT_DECODER.isValid( cell ) ) {
                    done = true;
                }
                else {
                    maybeShort_[ icol ] = false;
                }
            }
            if ( ! done && maybeInteger_[ icol ] ) {
                if ( INTEGER_DECODER.isValid( cell ) ) {
                    done = true;
                }
                else {
                    maybeInteger_[ icol ] = false;
                }
            }
            if ( ! done && maybeLong_[ icol ] ) {
                if ( LONG_DECODER.isValid( cell ) ) {
                    done = true;
                }
                else {
                    maybeLong_[ icol ] = false;
                }
            }
            if ( ! done && maybeFloat_[ icol ] ) {
                if ( FLOAT_DECODER.isValid( cell ) ) {
                    done = true;
                }
                else {
                    maybeFloat_[ icol ] = false;
                }
            }
            if ( ! done && maybeDouble_[ icol ] ) {
                if ( DOUBLE_DECODER.isValid( cell ) ) {
                    done = true;
                }
                else {
                    maybeDouble_[ icol ] = false;
                }
            }
            if ( ! done && maybeDate_[ icol ] ) {
                if ( DATE_DECODER.isValid( cell ) ) {
                    done = true;
                }
                else {
                    maybeDate_[ icol ] = false;
                }
            }
            if ( ! done && maybeHms_[ icol ] ) {
                if ( HMS_DECODER.isValid( cell ) ) {
                    done = true;
                }
                else {
                    maybeHms_[ icol ] = false;
                }
            }
            if ( ! done && maybeDms_[ icol ] ) {
                if ( DMS_DECODER.isValid( cell ) ) {
                    done = true;
                }
                else {
                    maybeDms_[ icol ] = false;
                }
            }
        }
    }

    /**
     * Returns information gleaned from previous <tt>submitRow</tt>
     * calls about the kind of data that appears to be in the columns.
     *
     * @return  metadata
     */
    public Metadata getMetadata() {
        ColumnInfo[] colInfos = new ColumnInfo[ ncol_ ];
        Decoder[] decoders = new Decoder[ ncol_ ];
        for ( int icol = 0; icol < ncol_; icol++ ) {
            final Decoder decoder;
            String name = "col" + ( icol + 1 );
            if ( maybeBoolean_[ icol ] ) {
                decoder = BOOLEAN_DECODER;
            }
            else if ( maybeShort_[ icol ] ) {
                decoder = SHORT_DECODER;
            }
            else if ( maybeInteger_[ icol ] ) {
                decoder = INTEGER_DECODER;
            }
            else if ( maybeLong_[ icol ] ) {
                decoder = LONG_DECODER;
            }
            else if ( maybeFloat_[ icol ] ) {
                decoder = FLOAT_DECODER;
            }
            else if ( maybeDouble_[ icol ] ) {
                decoder = DOUBLE_DECODER;
            }
            else if ( maybeDate_[ icol ] ) {
                decoder = DATE_DECODER;
            }
            else if ( maybeHms_[ icol ] ) {
                decoder = HMS_DECODER;
            }
            else if ( maybeDms_[ icol ] ) {
                decoder = DMS_DECODER;
            }
            else {
                decoder = STRING_DECODER;
            }
            decoders[ icol ] = decoder;
            ColumnInfo info = decoder.createColumnInfo( name );
            if ( decoder instanceof StringDecoder ) {
                info.setElementSize( stringLength_[ icol ] );
            }
            colInfos[ icol ] = info;
        }
        return new Metadata( colInfos, decoders, nrow_ );
    }

    /**
     * Returns a new <tt>ncol</tt>-element boolean array.
     *
     * @param   val  initial value of all flags
     * @return  new flag array initialized to <tt>val</tt>
     */
    private boolean[] makeFlagArray( boolean val ) {
        boolean[] flags = new boolean[ ncol_ ];
        Arrays.fill( flags, val );
        return flags;
    }

    /**
     * Parses a floating point value.  This does a couple of extra things
     * than Double.parseDouble - it understands 'd' or 'D' as the exponent
     * signifier as well as 'e' or 'E', and it counts the number of
     * significant figures.
     *
     * @param   item  string representing a floating point number
     * @return  object encapsulating information about the floating pont
     *          value extracted from <tt>item</tt> - note it's always the
     *          same instance returned, so don't hang onto it
     * @throws  NumberFormatException  if <tt>item</tt> can't be understood
     *          as a float or double
     */
    private static ParsedFloat parseFloating( String item ) {

        /* Check for special values.  Although parseDouble picks up 
         * some of these, it only works with java-friendly forms like
         * "NaN" and not (e.g.) python-friendly ones like "nan". */
        if ( NAN_REGEX.matcher( item ).matches() ) {
            return ParsedFloat.NaN;
        }
        Matcher infMatcher = INFINITY_REGEX.matcher( item );
        if ( infMatcher.matches() ) {
            String sign = infMatcher.group( 1 );
            return sign.length() > 0 && sign.charAt( 0 ) == '-'
                 ? ParsedFloat.NEGATIVE_INFINITY
                 : ParsedFloat.POSITIVE_INFINITY;
        }

        /* Do a couple of jobs by looking at the string directly:
         * Substitute 'd' or 'D' which may indicate an exponent in
         * FORTRAN77-style output for an 'e', and count the number of
         * significant figures.  With some more work it would be possible
         * to do the actual parse here, but since this probably isn't
         * a huge bottleneck we leave it to Double.parseDouble. */
        int nc = item.length();
        boolean foundExp = false;
        int sigFig = 0;
        for ( int i = 0; i < nc; i++ ) {
            char c = item.charAt( i );
            switch ( c ) {
                case 'd':
                case 'D':
                    if ( ! foundExp ) {
                        StringBuffer sbuf = new StringBuffer( item );
                        sbuf.setCharAt( i, 'e' );
                        item = sbuf.toString();
                    }
                    foundExp = true;
                    break;
                case 'e':
                case 'E':
                    foundExp = true;
                    break;
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                    if ( ! foundExp ) {
                        sigFig++;
                    }
                    break;
                default:
            }
        }

        /* Parse the number. */
        double dvalue = Double.parseDouble( item );
        return new ParsedFloat( sigFig, dvalue );
    }

    /**
     * Helper class used to group quantities which describe what the
     * data types found in the columns of a table are.
     */
    public static class Metadata {
        public final ColumnInfo[] colInfos_;
        public final Decoder[] decoders_;
        public final long nrow_;
        public final int ncol_;
        public Metadata( ColumnInfo[] colInfos, Decoder[] decoders,
                         long nrow ) {
            colInfos_ = colInfos;
            decoders_ = decoders;
            nrow_ = nrow;
            if ( colInfos_.length != decoders_.length ) {
                throw new IllegalArgumentException();
            }
            ncol_ = colInfos_.length;
        }
    }

    /**
     * Interface for an object that can turn a string into a cell content
     * object.
     */
    public static abstract class Decoder {
        private final Class clazz_;

        /**
         * Constructor.
         *
         * @param   clazz  class of object to be returned by decode method
         */
        public Decoder( Class clazz ) {
            clazz_ = clazz;
        }

        /**
         * Returns a new ColumnInfo suitable for the decoded values.
         *
         * @param  name  column name
         * @return  new metadata object
         */
        public ColumnInfo createColumnInfo( String name ) {
            return new ColumnInfo( name, clazz_, null );
        }

        /**
         * Decodes a value.
         * Will complete without exception if {@link #isValid} returns true
         * for the presented <code>value</code>; otherwise may throw an
         * unchecked exception.
         *
         * @param  value  string to decode
         * @return   typed object corresponding to <code>value</code>
         */
        public abstract Object decode( String value );

        /**
         * Indicates whether this decoder is capable of decoding a 
         * given string.
         *
         * @param  value  string to decode
         * @return  true iff this decoder can make sense of the string
         */
        public abstract boolean isValid( String value );
    }

    /**
     * Partial Decoder implementation for strings..
     */
    private static abstract class StringDecoder extends Decoder {
        StringDecoder() {
            super( String.class );
        }

        /**
         * Returns the value unchanged.
         */
        public Object decode( String value ) {
            return value;
        }
    }

    /**
     * Helper class to encapsulate the result of a floating point number
     * parse.
     */
    private static class ParsedFloat {

        /** Number of significant figures. */
        final int sigFig;

        /** Value of the number. */
        final double dValue;

        static final ParsedFloat NaN = new ParsedFloat( 0, Double.NaN );
        static final ParsedFloat POSITIVE_INFINITY =
            new ParsedFloat( 0, Double.POSITIVE_INFINITY );
        static final ParsedFloat NEGATIVE_INFINITY =
            new ParsedFloat( 0, Double.NEGATIVE_INFINITY );

        /**
         * Constructor.
         *
         * @param  sigFig  number of significant figures
         * @param  dValue  floating point value
         */
        ParsedFloat( int sigFig, double dValue ) {
            this.sigFig = sigFig;
            this.dValue = dValue;
        }
    }
}
