package uk.ac.starlink.ttools.plot2.config;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JRadioButton;

/**
 * Config key for use with items that can be chosed from a list of options.
 *
 * @author   Mark Taylor
 * @since    23 Feb 2013
 */
public class OptionConfigKey<T> extends ConfigKey<T> {

    private final T[] options_;
    private final boolean useRadio_;

    /**
     * Constructor that specifies an explicit default and specifier type.
     *
     * @param   meta   metadata
     * @param   clazz  class to which all the possible options belong
     * @param   options  array of possible values for this key
     * @param   dflt   default option, should be one of <code>options</code>
     * @param   useRadio  true to use radio buttons, false for a combo box
     */
    public OptionConfigKey( ConfigMeta meta, Class<T> clazz, T[] options,
                            T dflt, boolean useRadio ) {
        super( meta, clazz, dflt );
        options_ = options;
        useRadio_ = useRadio;
    }

    /**
     * Constructor that specifies an explicit default and uses the
     * default specifier type.
     *
     * @param   meta   metadata
     * @param   clazz  class to which all the possible options belong
     * @param   options  array of possible values for this key
     * @param   dflt   default option, should be one of <code>options</code>
     */
    public OptionConfigKey( ConfigMeta meta, Class<T> clazz, T[] options,
                            T dflt ) {
        this( meta, clazz, options, dflt, false );
    }

    /**
     * Constructor that uses the first of the supplied options as a default.
     *
     * @param   meta   metadata
     * @param   clazz  class to which all the possible options belong
     * @param   options  array of possible values for this key,
     *                   first element is used as the default value
     */
    public OptionConfigKey( ConfigMeta meta, Class<T> clazz, T[] options ) {
        this( meta, clazz, options, options[ 0 ] );
    }

    /**
     * Invokes the <code>toString</code> method of the supplied option.
     * May be overridden.
     */
    public String valueToString( T value ) {
        return value == null ? null : value.toString();
    }

    /**
     * Calls <code>valueToString</code> repeatedly looking for a match.
     * This means that if <code>valueToString</code> is overridden it
     * is usually not necessary to override this method.
     */
    public T stringToValue( String txt ) {
        if ( txt == null || txt.trim().length() == 0 ) {
            return null;
        }
        for ( int i = 0; i < options_.length; i++ ) {
            T option = options_[ i ];
            if ( txt.equalsIgnoreCase( valueToString( option ) ) ) {
                return option;
            }
        }
        StringBuffer sbuf = new StringBuffer()
           .append( "Unknown value \"" )
           .append( txt )
           .append( "\"" )
           .append( " should be " );
        for ( int i = 0; i < options_.length; i++ ) {
            if ( i > 0 ) {
                sbuf.append( ", " );
            }
            sbuf.append( valueToString( options_[ i ] ) );
        }
        throw new ConfigException( this, sbuf.toString() );
    }

    /**
     * Returns the available options for this key.
     *
     * @return  choosable values
     */
    public T[] getOptions() {
        return options_;
    }

    public Specifier<T> createSpecifier() {
        if ( useRadio_ ) {
            return new RadioButtonSpecifier();
        }
        else {
            Specifier<T> spec = new ComboBoxSpecifier<T>( options_ ) {
                public String stringify( T value ) {
                    return valueToString( value );
                }
            };
            spec.setSpecifiedValue( getDefaultValue() );
            return spec;
        }
    }

    /**
     * Option specifier that uses horizontally laid out
     * radio buttons to present the options.
     * There had better not be too many of them.
     */
    private class RadioButtonSpecifier extends SpecifierPanel<T> {
        private final JRadioButton[] buttons_;

        /**
         * Constructor.
         */
        RadioButtonSpecifier() {
            super( false );
            ButtonGroup grp = new ButtonGroup();
            buttons_ = new JRadioButton[ options_.length ];
            for ( int i = 0; i < options_.length; i++ ) {
                T opt = options_[ i ];
                JRadioButton butt = new JRadioButton( valueToString( opt ) );
                buttons_[ i ] = butt;
                grp.add( butt );
                if ( opt != null && opt.equals( getDefaultValue() ) ) {
                    butt.setSelected( true );
                }
                butt.addActionListener( getActionForwarder() );
            }
        }

        public JComponent createComponent() {
            JComponent box = new Box( BoxLayout.X_AXIS ) {
                @Override
                public void setEnabled( boolean enabled ) {
                    super.setEnabled( enabled );
                    for ( int ib = 0; ib < buttons_.length; ib++ ) {
                        buttons_[ ib ].setEnabled( enabled );
                    }
                }
            };
            for ( int i = 0; i < buttons_.length; i++ ) {
                if ( i > 0 ) {
                    box.add( Box.createHorizontalStrut( 10 ) );
                }
                box.add( buttons_[ i ] );
            }
            return box;
        }

        public T getSpecifiedValue() {
            for ( int i = 0; i < buttons_.length; i++ ) {
                if ( buttons_[ i ].isSelected() ) {
                    return options_[ i ];
                }
            }
            return null;
        }

        public void setSpecifiedValue( T value ) {
            for ( int i = 0; i < options_.length; i++ ) {
                if ( options_[ i ] == value ) {
                    buttons_[ i ].setSelected( true );
                    fireAction();
                    return;
                }
            }
        }
    }
}
