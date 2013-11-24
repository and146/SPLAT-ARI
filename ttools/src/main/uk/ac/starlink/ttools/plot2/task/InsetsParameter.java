package uk.ac.starlink.ttools.plot2.task;

import java.awt.Insets;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.ParameterValueException;
import uk.ac.starlink.task.TaskException;

/**
 * Parameter for receiving an Insets specification.
 * The string representation is as 4 integers separated by columns.
 *
 * @author   Mark Taylor
 * @since    5 Sep 2013
 */
public class InsetsParameter extends Parameter {

    private Insets insetsValue_;

    /**
     * Constructor.
     *
     * @param  name  parameter name
     */
    public InsetsParameter( String name ) {
        super( name );
        setUsage( "<top>,<left>,<bottom>,<right>" );
        setNullPermitted( true );
    }

    /**
     * Acquires the value of this parameter as an Insets object.
     *
     * @param  env  execution environment
     * @return  insets
     */
    public Insets insetsValue( Environment env ) throws TaskException {
        checkGotValue( env );
        return insetsValue_;
    }

    @Override
    public void setValueFromString( Environment env, String stringval )
            throws TaskException {
        final Insets insets;
        if ( stringval == null || stringval.trim().length() == 0 ) {
            insets = null;
        }
        else {
            String[] svals = stringval.split( "," );
            if ( svals.length != 4 ) {
                throw new ParameterValueException( this,
                                              "Not 4 comma-separated values" );
            }
            int[] ivals = new int[ 4 ];
            for ( int i = 0; i < 4; i++ ) {
                String sv = svals[ i ].trim();
                try {
                    ivals[ i ] = Integer.parseInt( sv );
                }
                catch ( NumberFormatException e ) {
                    throw new ParameterValueException( this,
                                                       "Not integer: " + sv );
                }
            }
            insets = new Insets( ivals[ 0 ], ivals[ 1 ],
                                 ivals[ 2 ], ivals[ 3 ] );
        }
        insetsValue_ = insets;
        super.setValueFromString( env, stringval );
    }
}
