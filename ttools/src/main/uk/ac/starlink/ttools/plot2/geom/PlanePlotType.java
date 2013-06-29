package uk.ac.starlink.ttools.plot2.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.starlink.ttools.plot2.DataGeom;
import uk.ac.starlink.ttools.plot2.PlotType;
import uk.ac.starlink.ttools.plot2.Plotter;
import uk.ac.starlink.ttools.plot2.SurfaceFactory;
import uk.ac.starlink.ttools.plot2.config.StyleKeys;
import uk.ac.starlink.ttools.plot2.data.Coord;
import uk.ac.starlink.ttools.plot2.data.FloatingCoord;
import uk.ac.starlink.ttools.plot2.layer.CartesianErrorCoordSet;
import uk.ac.starlink.ttools.plot2.layer.CartesianVectorCoordSet;
import uk.ac.starlink.ttools.plot2.layer.ContourPlotter;
import uk.ac.starlink.ttools.plot2.layer.EdgeForm;
import uk.ac.starlink.ttools.plot2.layer.FunctionPlotter;
import uk.ac.starlink.ttools.plot2.layer.LinePlotter;
import uk.ac.starlink.ttools.plot2.layer.LabelPlotter;
import uk.ac.starlink.ttools.plot2.layer.MarkForm;
import uk.ac.starlink.ttools.plot2.layer.MultiPointForm;
import uk.ac.starlink.ttools.plot2.layer.PlaneEllipseCoordSet;
import uk.ac.starlink.ttools.plot2.layer.SizeForm;
import uk.ac.starlink.ttools.plot2.layer.ShapeForm;
import uk.ac.starlink.ttools.plot2.layer.ShapeMode;
import uk.ac.starlink.ttools.plot2.layer.ShapePlotter;
import uk.ac.starlink.ttools.plot2.paper.PaperTypeSelector;

/**
 * Defines the characteristics of a plot on a 2-dimensional plane.
 *
 * <p>This is a singleton class, see {@link #getInstance}.
 *
 * @author   Mark Taylor
 * @since    19 Feb 2013
 */
public class PlanePlotType implements PlotType {

    private static final SurfaceFactory SURFACE_FACTORY =
        new PlaneSurfaceFactory();
    private static final PlanePlotType INSTANCE = new PlanePlotType();
    private final DataGeom[] dataGeoms_;
    private final String[] axisNames_;

    /**
     * Private constructor for singleton.
     */
    private PlanePlotType() {
        dataGeoms_ = new DataGeom[] { PlaneDataGeom.INSTANCE };
        Coord[] coords = dataGeoms_[ 0 ].getPosCoords();
        axisNames_ = new String[ coords.length ];
        for ( int i = 0; i < coords.length; i++ ) {
            axisNames_[ i ] =
                ((FloatingCoord) coords[ i ]).getUserInfo().getName();
        };
    }

    public DataGeom[] getDataGeoms() {
        return dataGeoms_;
    }

    public Plotter[] getPlotters() {
        List<Plotter> list = new ArrayList<Plotter>();
        ShapeForm[] forms = new ShapeForm[] {
            new MarkForm(),
            new SizeForm(),
            MultiPointForm
           .createVectorForm( new CartesianVectorCoordSet( axisNames_ ), true ),
            MultiPointForm
           .createErrorForm( new CartesianErrorCoordSet( axisNames_ ),
                             StyleKeys.ERROR_SHAPE_2D ),
            MultiPointForm
           .createEllipseForm( new PlaneEllipseCoordSet(), true ),
            new EdgeForm( 1, dataGeoms_[ 0 ] ),
            new EdgeForm( 2, dataGeoms_[ 0 ] ),
        };
        Plotter[] shapePlotters =
            ShapePlotter.createShapePlotters( forms, ShapeMode.MODES_2D );
        list.addAll( Arrays.asList( shapePlotters ) );
        list.addAll( Arrays.asList( new Plotter[] {
            new LinePlotter(),
            new LabelPlotter(),
            new ContourPlotter(),
            FunctionPlotter.PLANE,
        } ) );
        return list.toArray( new Plotter[ 0 ] );
    }

    public SurfaceFactory getSurfaceFactory() {
        return SURFACE_FACTORY;
    }

    public PaperTypeSelector getPaperTypeSelector() {
        return PaperTypeSelector.SELECTOR_2D;
    }

    public String toString() {
        return "plane";
    }

    /**
     * Returns the sole instance of this class.
     *
     * @return  singleton instance
     */
    public static PlanePlotType getInstance() {
        return INSTANCE;
    }
}
