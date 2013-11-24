package uk.ac.starlink.topcat.plot2;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.topcat.ResourceIcon;
import uk.ac.starlink.topcat.ToggleButtonModel;
import uk.ac.starlink.ttools.plot.Matrices;
import uk.ac.starlink.ttools.plot2.PlotLayer;
import uk.ac.starlink.ttools.plot2.PlotUtil;
import uk.ac.starlink.ttools.plot2.SurfaceFactory;
import uk.ac.starlink.ttools.plot2.config.ConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigMap;
import uk.ac.starlink.ttools.plot2.config.ConfigMeta;
import uk.ac.starlink.ttools.plot2.config.Specifier;
import uk.ac.starlink.ttools.plot2.config.StyleKeys;
import uk.ac.starlink.ttools.plot2.data.SkyCoord;
import uk.ac.starlink.ttools.plot2.geom.Projection;
import uk.ac.starlink.ttools.plot2.geom.SkyAspect;
import uk.ac.starlink.ttools.plot2.geom.SkySurfaceFactory;
import uk.ac.starlink.ttools.plot2.geom.SkySys;
import uk.ac.starlink.vo.DoubleValueField;
import uk.ac.starlink.vo.SkyPositionEntry;

/**
 * AxisControl for sky plot.
 *
 * @author   Mark Taylor
 * @since    14 Mar 2013
 */
public class SkyAxisControl
             extends AxisControl<SkySurfaceFactory.Profile,SkyAspect> {

    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.ttools.SkyAxisControl" );

    /**
     * Constructor.
     */
    public SkyAxisControl() {
        super( new SkySurfaceFactory() );
        SurfaceFactory surfFact = getSurfaceFactory();

        /* Projection specifier. */
        final ConfigSpecifier projSpecifier =
                new ConfigSpecifier( new ConfigKey[] {
            SkySurfaceFactory.PROJECTION_KEY,
            SkySurfaceFactory.REFLECT_KEY,
            SkySurfaceFactory.VIEWSYS_KEY,
        } );
        addSpecifierTab( "Projection", projSpecifier );

        /* Navigator specifier. */
        addNavigatorTab();

        /* Field of view specifier. */
        addAspectConfigTab( "FOV", new FieldOfViewSpecifier( surfFact
                                                            .getAspectKeys() ) {
            protected SkySys getViewSystem() {
                return projSpecifier.getSpecifiedValue()
                                    .get( SkySurfaceFactory.VIEWSYS_KEY );
            }
        } );

        /* Grid appearance specifier. */
        addSpecifierTab( "Grid", new ConfigSpecifier( new ConfigKey[] {
            SkySurfaceFactory.GRID_KEY,
            SkySurfaceFactory.SEX_KEY,
            SkySurfaceFactory.CROWD_KEY,
            SkySurfaceFactory.AXISLABELLER_KEY,
        } ) );

        /* Font specifier. */
        addSpecifierTab( "Font",
                         new ConfigSpecifier( StyleKeys.getCaptionerKeys() ) );

        assert assertHasKeys( surfFact.getProfileKeys() );
    }

    /**
     * Currently the axis lock value is ignored,
     * (see the <code>clearRange</code> method),
     * so return null here.
     */
    @Override
    public ToggleButtonModel getAxisLockModel() {
        return null;
    }

    /**
     * Specifier which allows the user to enter a field of view centre and
     * radius to fix the sky aspect.
     */
    private static abstract class FieldOfViewSpecifier
                                  implements Specifier<ConfigMap> {
        private final SkyPositionEntry entry_;
        private final DoubleValueField radiusField_;

        /**
         * Constructor.
         *
         * @param  keys  aspect keys for surface factory
         */
        FieldOfViewSpecifier( ConfigKey[] keys ) {

            /* Check these are what we're expecting. */
            assert new HashSet<ConfigKey>( Arrays.asList( new ConfigKey[] {
                       SkySurfaceFactory.LON_KEY,
                       SkySurfaceFactory.LAT_KEY,
                       SkySurfaceFactory.FOV_RADIUS_KEY,
                    } ) )
                   .equals( new HashSet<ConfigKey>( Arrays.asList( keys ) ) );

            /* Set up an entry GUI for a sky position.  This includes a
             * name resolver.  Add a radius field. */
            entry_ = new SkyPositionEntry( "J2000" );
            ConfigMeta rMeta = SkySurfaceFactory.FOV_RADIUS_KEY.getMeta();
            DefaultValueInfo radiusInfo =
                new DefaultValueInfo( rMeta.getLongName(), Double.class,
                                      "Approximate field of view radius" );
            radiusField_ = DoubleValueField.makeSizeDegreesField( radiusInfo );
            radiusField_.setValue( SkySurfaceFactory.FOV_RADIUS_KEY
                                                    .getDefaultValue() );
            entry_.addField( radiusField_ );
        }

        public JComponent getComponent() {
            return entry_;
        }

        public void addActionListener( ActionListener listener ) {
            entry_.addActionListener( listener );
        }

        public void removeActionListener( ActionListener listener ) {
            entry_.removeActionListener( listener );
        }

        public ConfigMap getSpecifiedValue() {

            /* Read fieds from GUI. */
            double raDeg = getFieldValue( entry_.getRaDegreesField() );
            double decDeg = getFieldValue( entry_.getDecDegreesField() );
            double radiusDeg = getFieldValue( radiusField_ );

            /* Turn them into lon/lat coords in the current view system. */
            double[] eqVec3 = SkyCoord.lonLatDegreesToDouble3( raDeg, decDeg );
            double[] fromEq = Matrices.invert( getViewSystem().toEquatorial() );
            double[] viewVec3 = Matrices.mvMult( fromEq, eqVec3 );
            double latRad = Math.PI * 0.5 - Math.acos( viewVec3[ 2 ] );
            double lonRad = Math.atan2( viewVec3[ 1 ], viewVec3[ 0 ] );

            /* Dump them into a config map and return it. */
            ConfigMap config = new ConfigMap();
            putMapValue( config, SkySurfaceFactory.LON_KEY,
                         Math.toDegrees( lonRad ) );
            putMapValue( config, SkySurfaceFactory.LAT_KEY,
                         Math.toDegrees( latRad ) );
            putMapValue( config, SkySurfaceFactory.FOV_RADIUS_KEY, radiusDeg );
            return config;
        }

        public void setSpecifiedValue( ConfigMap configMap ) {
            try {
                entry_.getRaDegreesField()
                      .setValue( configMap.get( SkySurfaceFactory.LON_KEY )
                                .doubleValue() );
                entry_.getDecDegreesField()
                      .setValue( configMap.get( SkySurfaceFactory.LAT_KEY )
                                .doubleValue() );

                /* It would be nice to fill in the radius field from the
                 * existing zoom value.  This may be feasible, via
                 * SkyAxisControl.getAspect() but it's tricky because
                 * the aspect (a SkyAspect) has zoom which is related
                 * in a non-trivial way to the FOV radius. */
                radiusField_.setValue( configMap.get( SkySurfaceFactory
                                                     .FOV_RADIUS_KEY )
                                      .doubleValue() );
            }
            catch ( RuntimeException e ) {
                logger_.warning( e.getMessage() );
            }
            entry_.getResolveField().setText( "" );
        }

        public boolean isXFill() {
            return true;
        }

        /**
         * Determines the current sky view system for the purpose of
         * populating the config map with a sky position.
         *
         * @return  sky view system
         */
        protected abstract SkySys getViewSystem();

        /**
         * Utility method to read a DoubleValueField without error.
         *
         * @param  field
         * @return  field value, or NaN
         */
        private double getFieldValue( DoubleValueField field ) {
            try {
                return field.getValue();
            }
            catch ( IllegalArgumentException e ) {
                return Double.NaN;
            }
        }

        /**
         * Utility method to put a value into a config map if not NaN.
         *
         * @param  map  config map
         * @param  key  config key
         * @param  value  value to add if finite
         */
        private void putMapValue( ConfigMap map, ConfigKey<Double> key,
                                  double value ) {
            if ( ! Double.isNaN( value ) ) {
                map.put( key, value );
            }
        }

    }

    /**
     * Returns the sky view system currently selected for this control.
     *
     * @return  view system
     */
    public SkySys getViewSystem() {
        return getConfig().get( SkySurfaceFactory.VIEWSYS_KEY );
    }

    protected boolean clearRange( SkySurfaceFactory.Profile oldProfile,
                                  SkySurfaceFactory.Profile newProfile,
                                  PlotLayer[] oldLayers, PlotLayer[] newLayers,
                                  boolean lock ) {
        return ! oldProfile.getProjection().equals( newProfile.getProjection() )
            || ! oldProfile.getViewSystem().equals( newProfile.getViewSystem() )
            || oldProfile.isReflected() != newProfile.isReflected();
    }
}
