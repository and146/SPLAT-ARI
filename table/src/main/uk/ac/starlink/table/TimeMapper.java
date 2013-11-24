package uk.ac.starlink.table;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DomainMapper for mapping values to epochs in a common time scale.
 * The target domain is doubles giving the number
 * of seconds since the Unix epoch (1970-01-01T00:00:00).  The time scale
 * is generally assumed to be UTC, though conversions may not always be
 * performed, for instance for values in which the intended time scale
 * is not obvious.
 *
 * <p>A java (IEEE 754) double has 52+1 bits of precision, which I make
 * 52*ln(2)/ln(10)=15.65 decimal places, and a year contains 3.15e7 seconds,
 * which gives you the following precisions:
 * <ul>
 * <li>epoch +/- 1.7 months: 1 nanosecond precision
 * <li>epoch +/- 12 years: 10 microsecond precision
 * <li>epoch +/- 120 years: 1 microsecond precision
 * <li>epoch +/- 0.12 Myr: 1 millisecond precision
 * </ul>
 * It should be OK for most purposes.
 *
 * @author   Mark Taylor
 * @since    12 Aug 2013
 */
public abstract class TimeMapper implements DomainMapper {

    private final Class sourceClass_;
    private final String sourceName_;
    private final String sourceDescription_;

    /** Returns target domain name ({@value}). */
    public static final String TARGET_NAME = "Time";

    /** Mapper for numeric values in decimal year (since 0 AD). */
    public static final TimeMapper DECIMAL_YEAR;

    /** Mapper for numeric values in Modified Julian Date. */
    public static final TimeMapper MJD;

    /** Mapper for numeric values (already) in unix seconds. */
    public static final TimeMapper UNIX_SECONDS;

    /** Mapper for ISO-8601 strings. */
    public static final TimeMapper ISO_8601;

    private static final TimeMapper[] MAPPERS = new TimeMapper[] {
        DECIMAL_YEAR =
            new DecimalYearTimeMapper( "DecYear", "Years since 0 AD" ),
        MJD =
            new MjdTimeMapper( "MJD", "Modified Julian Date" ),
        UNIX_SECONDS =
            new UnixTimeMapper( "Unix", "Seconds since midnight 1 Jan 1970" ),
        ISO_8601 =
            new Iso8601TimeMapper( "Iso8601", "ISO 8601 string" ),
    };

    /**
     * Constructor.
     *
     * @param   sourceClass  
     * @param   sourceName  source type name
     * @param   sourceDescription  source type description
     */
    protected TimeMapper( Class sourceClass, String sourceName,
                          String sourceDescription ) {
        sourceClass_ = sourceClass;
        sourceName_ = sourceName;
        sourceDescription_ = sourceDescription;
    }

    /**
     * Returns {@link #TARGET_NAME}.
     */
    public final String getTargetName() {
        return TARGET_NAME;
    }

    public Class getSourceClass() {
        return sourceClass_;
    }

    public String getSourceName() {
        return sourceName_;
    }

    public String getSourceDescription() {
        return sourceDescription_;
    }

    /**
     * Maps a source value to time in seconds since the Unix epoch.
     *
     * @param   sourceValue  value in source domain
     * @return   number of seconds since midnight 1 Jan 1970
     */
    public abstract double toUnixSeconds( Object sourceValue );

    /**
     * Returns a selection of time mapper instances.
     */
    public static TimeMapper[] getTimeMappers() {
        return MAPPERS.clone();
    }

    /**
     * TimeMapper implementation in which the source domain is
     * numbers giving years AD.
     */
    private static class DecimalYearTimeMapper extends TimeMapper {
        DecimalYearTimeMapper( String name, String description ) {
            super( Number.class, name, description );
        }
        public double toUnixSeconds( Object value ) {
            if ( value instanceof Number ) {
                double decYear = ((Number) value).doubleValue();
                if ( Double.isNaN( decYear ) ) {
                    return Double.NaN;
                }
                else {
                    int year = (int) decYear;
                    double yearFraction = decYear - year;
                    Calendar cal = new GregorianCalendar( year, 0, 1 );
                    long millis0 = cal.getTimeInMillis();
                    cal.add( Calendar.YEAR, 1 );
                    long millis1 = cal.getTimeInMillis();
                    long millisInYear = millis1 - millis0;
                    double milliOfYear = yearFraction * millisInYear;
                    double unixMillis = millis0 + milliOfYear;
                    return unixMillis / 1000.0;
                }
            }
            else {
                return Double.NaN;
            }
        }
    }

    /**
     * TimeMapper implementation in which the source domain is
     * numbers giving Modified Julian Date.
     */
    private static class MjdTimeMapper extends TimeMapper {

        /** Date of the Unix epoch as a Modified Julian Date. */
        private static final double MJD_EPOCH = 40587.0;

        /** Number of seconds in a day. */
        private static final double SECONDS_PER_DAY = 60 * 60 * 24;

        MjdTimeMapper( String name, String description ) {
            super( Number.class, name, description );
        }
        public double toUnixSeconds( Object value ) {
            if ( value instanceof Number ) {
                double mjd = ((Number) value).doubleValue();
                return ( mjd - MJD_EPOCH ) * SECONDS_PER_DAY;
            }
            else {
                return Double.NaN;
            }
        }
    }

    /**
     * TimeMapper implementation in which the source domain is
     * numbers giving seconds since the Unix epoch (1970-01-01T00:00:00).
     */
    private static class UnixTimeMapper extends TimeMapper {
        UnixTimeMapper( String name, String description ) {
            super( Number.class, name, description );
        }

        /**
         * Unit mapping.
         */
        public double toUnixSeconds( Object value ) {
           
            return value instanceof Number
                 ? ((Number) value).doubleValue()
                 : Double.NaN;
        }
    }

    /**
     * TimeMapper implementation in which the source domain is
     * strings giving ISO-8601 date/times.
     */
    private static class Iso8601TimeMapper extends TimeMapper {

        /** Regular expression for parsing ISO 8601 dates. */
        private final static Pattern ISO_REGEX =
            Pattern.compile( "([0-9]+)-([0-9]{1,2})-([0-9]{1,2})" +
                             "(?:[T ]([0-9]{1,2})" +
                                "(?::([0-9]{1,2})" +
                                   "(?::([0-9]{1,2}(?:\\.[0-9]*)?))?" +
                                ")?" +
                             "Z?)?" );

        Iso8601TimeMapper( String name, String description ) {
            super( String.class, name, description );
        }

        public double toUnixSeconds( Object value ) {
            if ( value instanceof String ) {
                String sval = ((String) value).trim();
                Matcher matcher = ISO_REGEX.matcher( ((String) value).trim() );
                if ( matcher.matches() ) {
                    String[] groups = new String[ 6 ];
                    int ng = matcher.groupCount();
                    for ( int i = 0; i < ng; i++ ) {
                        groups[ i ] = matcher.group( i + 1 );
                    }
                    try {
                        int year = Integer.parseInt( groups[ 0 ] );
                        int month = Integer.parseInt( groups[ 1 ] );
                        int dom = Integer.parseInt( groups[ 2 ] );
                        int hour = groups[ 3 ] == null
                                 ? 0
                                 : Integer.parseInt( groups[ 3 ] );
                        int min = groups[ 4 ] == null
                                ? 0 
                                : Integer.parseInt( groups[ 4 ] );
                        double sec = groups[ 5 ] == null
                                   ? 0.0
                                   : Double.parseDouble( groups[ 5 ] );
                        return dateToUnix( year, month, dom, hour, min, sec );
                    }
                    catch ( NumberFormatException e ) {
                        return Double.NaN;
                    }
                }
                else {
                    return Double.NaN;
                }
            }
            else {
                return Double.NaN;
            }
        }

        /**
         * Converts date elements to unix seconds.
         *
         * @param  year   calendar year
         * @param  month  month in year (January is 1)
         * @param  day    day of month (first day is 1)
         * @param  hour   hour in day (midnight is 0)
         * @param  minute minute in hour
         * @param  sec    second in minute
         * @return  seconds since unix epoch
         */
        private static double dateToUnix( int year, int month, int dom,
                                          int hour, int min, double sec ) {
            int intSec = (int) sec;
            double fracSec = sec - intSec;
            Calendar cal = new GregorianCalendar( year, month - 1, dom,
                                                  hour, min, (int) sec );
            double calMillis = cal.getTimeInMillis();
            double calSec = calMillis * 1e-3;
            return calSec + fracSec;
        }
    }
}
