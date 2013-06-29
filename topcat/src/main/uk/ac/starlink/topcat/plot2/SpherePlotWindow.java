package uk.ac.starlink.topcat.plot2;

import java.awt.Component;
import uk.ac.starlink.ttools.plot2.geom.CubeAspect;
import uk.ac.starlink.ttools.plot2.geom.CubeSurfaceFactory;
import uk.ac.starlink.ttools.plot2.geom.SpherePlotType;

/**
 * Layer plot window for 3D plots with spherical polar coordinates.
 *
 * @author   Mark Taylor
 * @since    19 Mar 2013
 */
public class SpherePlotWindow
       extends StackPlotWindow<CubeSurfaceFactory.Profile,CubeAspect> {
    private static final SpherePlotType PLOT_TYPE =
        SpherePlotType.getInstance();
    private static final SpherePlotTypeGui PLOT_GUI = new SpherePlotTypeGui();

    /**
     * Constructor.
     *
     * @param  parent  parent component
     */
    public SpherePlotWindow( Component parent ) {
        super( "Sphere2", parent, PLOT_TYPE, PLOT_GUI );
        addHelp( "SpherePlotWindow" );
    }

    /**
     * Defines GUI features specific to sphere plot.
     */
    private static class SpherePlotTypeGui
            implements PlotTypeGui<CubeSurfaceFactory.Profile,CubeAspect> {
        public AxisControl<CubeSurfaceFactory.Profile,CubeAspect>
                createAxisControl( ControlStack stack ) {
            return new CubeAxisControl( true, stack );
        }
        public PositionCoordPanel createPositionCoordPanel() {
            return new SimplePositionCoordPanel( PLOT_TYPE.getDataGeoms()[ 0 ],
                                                 true );
        }
    }
}
