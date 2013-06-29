package uk.ac.starlink.ttools.plot2.geom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import uk.ac.starlink.ttools.plot2.DataGeom;
import uk.ac.starlink.ttools.plot2.PlotType;
import uk.ac.starlink.ttools.plot2.Plotter;
import uk.ac.starlink.ttools.plot2.SurfaceFactory;
import uk.ac.starlink.ttools.plot2.layer.ContourPlotter;
import uk.ac.starlink.ttools.plot2.layer.LabelPlotter;
import uk.ac.starlink.ttools.plot2.layer.MarkForm;
import uk.ac.starlink.ttools.plot2.layer.SizeForm;
import uk.ac.starlink.ttools.plot2.layer.ShapeForm;
import uk.ac.starlink.ttools.plot2.layer.ShapeMode;
import uk.ac.starlink.ttools.plot2.layer.ShapePlotter;
import uk.ac.starlink.ttools.plot2.paper.PaperTypeSelector;

/**
 * Defines the characteristics of plot in 3-dimensional isotropic space.
 * It's like {@link CubePlotType}, but intended for viewing
 * sky data with additional radial coordinates.
 * It differs from SkyPlotType in that radial coordinates are allowed;
 * and differs from CubePlotType in that coords are expected to be
 * in Lon/Lat/Radius.
 *
 * <p>This is a singleton class, see {@link #getInstance}.
 *
 * @author   Mark Taylor
 * @since    20 Feb 2013
 */
public class SpherePlotType implements PlotType {

    private static final SurfaceFactory SURFACE_FACTORY =
        new CubeSurfaceFactory( true );
    private static final SpherePlotType INSTANCE = new SpherePlotType();
    private final DataGeom[] dataGeoms_;

    /**
     * Private singleton constructor.
     */
    private SpherePlotType() {
        dataGeoms_ = new DataGeom[] { SphereDataGeom.INSTANCE };
    }

    public DataGeom[] getDataGeoms() {
        return dataGeoms_;
    }

    public Plotter[] getPlotters() {
        List<Plotter> list = new ArrayList<Plotter>();
        ShapeForm[] forms = new ShapeForm[] {
            new MarkForm(),
            new SizeForm(),
        };
        Plotter[] shapePlotters =
            ShapePlotter.createShapePlotters( forms, ShapeMode.MODES_3D );
        list.addAll( Arrays.asList( shapePlotters ) );
        list.addAll( Arrays.asList( new Plotter[] {
            new LabelPlotter(),
            new ContourPlotter(),
        } ) );
        return list.toArray( new Plotter[ 0 ] );
    }

    public SurfaceFactory getSurfaceFactory() {
        return SURFACE_FACTORY;
    }

    public PaperTypeSelector getPaperTypeSelector() {
        return PaperTypeSelector.SELECTOR_3D;
    }

    public String toString() {
        return "sphere";
    }

    /**
     * Returns the sole instance of this class.
     *
     * @return  singleton instance
     */
    public static SpherePlotType getInstance() {
        return INSTANCE;
    }
}
