package uk.ac.starlink.ttools.plot2.config;

import java.text.SimpleDateFormat;
import java.util.Date;
import uk.ac.starlink.ttools.func.Times;
import uk.ac.starlink.ttools.plot2.geom.TimeFormat;

/**
 * Config key for values in the time domain.
 * The value returned is a time in the domain defined by
 * {@link uk.ac.starlink.table.TimeMapper}, that is unix seconds.
 *
 * @author   Mark Taylor
 * @since    15 Aug 2013
 */
public class TimeConfigKey extends ConfigKey<Double> {

    /**
     * Constructs a key with no default value.
     *
     * @param   meta  metadata
     */
    public TimeConfigKey( ConfigMeta meta ) {
        this( meta, Double.NaN );
    }

    /**
     * Constructs a key with a given default value.
     *
     * @param   meta  metadata
     * @param   dfltUnixSeconds  default value as seconds since Unix epoch
     */
    public TimeConfigKey( ConfigMeta meta, double dfltUnixSeconds ) {
        super( meta, Double.class, new Double( dfltUnixSeconds ) );
    }

    public String valueToString( Double value ) {
        double stime = value == null ? Double.NaN : value.doubleValue();
        return Double.isNaN( stime ) ? ""
                                     : formatTime( stime );
    }

    public Double stringToValue( String txt ) {
        if ( txt == null || txt.trim().length() == 0 ) {
            return new Double( Double.NaN );
        }
        txt = txt.trim();
        double dval;
        try {
            dval = Double.parseDouble( txt );
        }
        catch ( NumberFormatException e ) {
            dval = Double.NaN;
        }
        if ( ! Double.isNaN( dval ) ) {
            return TimeFormat.decimalYearToUnixSeconds( dval );
        }
        double mjd;
        try {
            mjd = Times.isoToMjd( txt );
        }
        catch ( RuntimeException e ) {
            mjd = Double.NaN;
        }
        if ( ! Double.isNaN( mjd ) ) {
            return Times.mjdToUnixMillis( mjd ) * 0.001;
        }
        else {
            String msg = "Can't parse \"" + txt
                       + "\" as decimal year or ISO-8601 time";
            throw new ConfigException( this, msg );
        }
    }

    public Specifier<Double> createSpecifier() {
        return new TextFieldSpecifier<Double>( this, new Double( Double.NaN ) );
    }

    /**
     * Formats a time in unix seconds to a string.
     *
     * @param  unixSec  time in seconds since unix epoch
     * @return   formatted value (currently ISO-8601)
     */
    private String formatTime( double unixSec ) {
        String ftime = new SimpleDateFormat( "yyyy-MM-dd'T'hh:mm:ss" )
                      .format( new Date( (long) ( unixSec * 1000 ) ) );
        while ( ftime.endsWith( ":00" ) ) {
            ftime = ftime.substring( 0, ftime.length() - 3 );
        }
        if ( ftime.endsWith( "T" ) ) {
            ftime = ftime.substring( 0, ftime.length() - 1 );
        }
        if ( ftime.endsWith( "-01-01" ) ) {
            ftime = ftime.substring( 0, ftime.length() - 6 );
        }
        return ftime;
    }
}
