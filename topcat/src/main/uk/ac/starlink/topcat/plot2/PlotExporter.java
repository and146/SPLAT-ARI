package uk.ac.starlink.topcat.plot2;

import java.awt.Component;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import uk.ac.starlink.ttools.plot.GraphicExporter;
import uk.ac.starlink.ttools.plot.Range;
import uk.ac.starlink.ttools.plot2.AuxScale;
import uk.ac.starlink.ttools.plot2.Drawing;
import uk.ac.starlink.ttools.plot2.LayerOpt;
import uk.ac.starlink.ttools.plot2.PlotLayer;
import uk.ac.starlink.ttools.plot2.PlotPlacement;
import uk.ac.starlink.ttools.plot2.PlotUtil;
import uk.ac.starlink.ttools.plot2.Surface;
import uk.ac.starlink.ttools.plot2.data.DataStore;
import uk.ac.starlink.ttools.plot2.paper.PaperType;
import uk.ac.starlink.ttools.plot2.paper.PaperTypeSelector;
import uk.ac.starlink.util.gui.CustomComboBoxRenderer;
import uk.ac.starlink.util.gui.ErrorDialog;
import uk.ac.starlink.util.gui.ShrinkWrapper;

/**
 * Provides a GUI for exporting a plot to an external format,
 * generally to a file.
 *
 * @author   Mark Taylor
 * @since    12 Mar 2013
 */
public class PlotExporter {

    private final JFileChooser saveChooser_;
    private final JComboBox formatSelector_;
    private final JCheckBox bitmapButton_;
    private static final GraphicExporter[] EXPORTERS = createExporters();
    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.ttools.plot2" );
    private static PlotExporter instance_;

    /**
     * Constructor.
     */
    public PlotExporter() {
        saveChooser_ = new JFileChooser( "." );
        saveChooser_.setDialogTitle( "Export Plot" );
        formatSelector_ = new JComboBox( EXPORTERS );
        bitmapButton_ = new JCheckBox( "Force Bitmap" );
        CustomComboBoxRenderer renderer = new CustomComboBoxRenderer() {
            protected Object mapValue( Object value ) {
                return value instanceof GraphicExporter
                     ? ((GraphicExporter) value).getName()
                     : value;
            }
        };
        renderer.setNullRepresentation( "(auto)" );
        formatSelector_.setRenderer( renderer );
        JComponent formatBox = Box.createVerticalBox();
        formatBox.add( new LineBox( new JLabel( "File Format:" ) ) );
        formatBox.add( Box.createVerticalStrut( 5 ) );
        formatBox.add( new LineBox( new ShrinkWrapper( formatSelector_ ) ) );
        formatBox.add( Box.createVerticalStrut( 5 ) );
        formatBox.add( new LineBox( bitmapButton_ ) );
        formatBox.add( Box.createVerticalGlue() );
        saveChooser_.setAccessory( formatBox );
    }

    /**
     * Offers the user a GUI to save a plot defined by given parameters
     * in a user-chosen format.
     *
     * @param  parent   parent component for dialogue window
     * @param  placer   plot placement
     * @param  layers   plot layers
     * @param  auxRanges   layer-requested range data
     * @param  dataStore  data storage ojbect
     * @param  ptsel   paper type selector
     */
    public void exportPlot( Component parent, PlotPlacement placer,
                            PlotLayer[] layers, Map<AuxScale,Range> auxRanges,
                            DataStore dataStore, PaperTypeSelector ptsel ) {
        while ( saveChooser_.showDialog( parent, "Export Plot" )
                == JFileChooser.APPROVE_OPTION ) {
            File file = saveChooser_.getSelectedFile();
            GraphicExporter exporter = getExporter( file );
            if ( exporter == null ) {
                JOptionPane
               .showMessageDialog( parent,
                                   "Can't guess auto file format for " + file,
                                   "Save failure", JOptionPane.ERROR_MESSAGE );
            }
            else {
                LayerOpt[] opts = PaperTypeSelector.getOpts( layers );
                PaperType paperType = bitmapButton_.isSelected()
                                    ? ptsel.getPixelPaperType( opts, null )
                                    : ptsel.getVectorPaperType( opts );
                Icon icon = createPlotIcon( placer, layers, auxRanges,
                                            dataStore, paperType );
                try {
                    attemptSave( icon, file, exporter );
                    return;
                }
                catch ( Exception e ) {
                    ErrorDialog.showError( parent, "Plot Export Error", e,
                                           "Failed to export plot in "
                                         + exporter.getName() + " format to "
                                         + file );
                }
            }
        }
    }

    /**
     * Attempts to write a given icon to a file in a particular
     * graphics format.
     *
     * @param  icon   image to paint
     * @param  file   destination file
     * @param  exporter   output graphics format handler
     * @throws  IOException  in case of write error
     */
    public void attemptSave( final Icon icon, File file,
                             GraphicExporter exporter )
            throws IOException {
        OutputStream out =
            new BufferedOutputStream( new FileOutputStream( file ) );
        try {
            exporter.exportGraphic( PlotUtil.toPicture( icon ), out );
        }
        finally {
            out.close();
        }
    }

    /**
     * Returns a single instance of this class.
     * You don't have to use it as a singleton, but doing it like that
     * allows it to retain current directory for output file etc. 
     *
     * @return  shared instance
     */
    public static PlotExporter getInstance() {
        if ( instance_ == null ) {
            instance_ = new PlotExporter();
        }
        return instance_;
    }

    /**
     * Returns a graphics output format handler to use for a given file.
     * If a handler is explicitly selected in the GUI that will be returned,
     * but otherwise (auto mode) the filename will be examined to see if
     * one or other of the available handlers looks likely.
     * If none can be found/guessed, null is returned.
     *
     * @param    file   destination file
     * @return   appropriate exporter, or null
     */
    private GraphicExporter getExporter( File file ) {
        Object fmtobj = formatSelector_.getSelectedItem();
        if ( fmtobj instanceof GraphicExporter ) {
            return (GraphicExporter) fmtobj;
        }
        assert fmtobj == null;
        String fname = file.getName();
        for ( int ie = 0; ie < EXPORTERS.length; ie++ ) {
            GraphicExporter exporter = EXPORTERS[ ie ];
            String[] suffixes = exporter == null ? new String[ 0 ]
                                                 : exporter.getFileSuffixes();
            for ( int is = 0; is < suffixes.length; is++ ) {
                if ( fname.toLowerCase()
                          .endsWith( suffixes[ is ].toLowerCase() ) ) {
                    return exporter;
                }
            }
        }
        return null;
    }

    /**
     * Return the icon which will paint a plot.
     *
     * @param  placer   plot placement
     * @param  layers   plot layers
     * @param  auxRanges   layer-requested range data
     * @param  dataStore  data storage ojbect
     * @param  paperType  paper type
     */
    private static Icon createPlotIcon( PlotPlacement placer,
                                        PlotLayer[] layers,
                                        Map<AuxScale,Range> auxRanges,
                                        DataStore dataStore,
                                        PaperType paperType ) {
        Surface surface = placer.getSurface();
        int nl = layers.length;
        logger_.info( "Layers: " + nl + ", Paper: " + paperType );
        Drawing[] drawings = new Drawing[ nl ];
        Object[] plans = new Object[ nl ];
        long t1 = System.currentTimeMillis();
        for ( int il = 0; il < nl; il++ ) {
            drawings[ il ] =
                layers[ il ].createDrawing( surface, auxRanges, paperType );
            plans[ il ] = drawings[ il ].calculatePlan( plans, dataStore );
        }
        PlotUtil.logTime( logger_, "Plans", t1 );
        Icon dataIcon =
             paperType.createDataIcon( surface, drawings, plans, dataStore,
                                       false );
        return placer.createPlotIcon( dataIcon );
    }

    /**
     * Returns the default list of available graphics output format handlers.
     *
     * @return   exporter list
     */
    private static GraphicExporter[] createExporters() {
        return PlotUtil.arrayConcat(
            new GraphicExporter[] { null },
            GraphicExporter.getKnownExporters( PlotUtil.LATEX_PDF_EXPORTER ) );
    }
}
