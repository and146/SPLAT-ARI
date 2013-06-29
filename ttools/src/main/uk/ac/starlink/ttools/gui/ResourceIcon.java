package uk.ac.starlink.ttools.gui;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Stores icons used by the ttools package.
 *
 * @author   Mark Taylor
 * @since    9 Feb 2007
 */
public class ResourceIcon {

    public static final Icon

        /* MethodBrowser icons. */
        FOLDER_NODE = readIcon( "folder_node.gif" ),
        LIBRARY_NODE = readIcon( "book_leaf.gif" ),
        FUNCTION_NODE = readIcon( "fx_leaf.gif" ),
        CONSTANT_NODE = readIcon( "c_leaf.gif" ),

        /* Plot2 icons. */
        PLOT_FUNCTION = readIcon( "sinx.gif" ),
        PLOT_CONTOUR = readIcon( "plot-contour.gif" ),
        PLOT_LINE = readIcon( "plot-line.gif" ),
        PLOT_LABEL = readIcon( "plot-label.gif" ),
        FORM_MARK = readIcon( "form-mark.gif" ),
        FORM_SIZE = readIcon( "form-size.gif" ),
        FORM_ELLIPSE = readIcon( "form-ellipse2.gif" ),
        FORM_ERROR = readIcon( "form-error.gif" ),
        FORM_VECTOR = readIcon( "form-vector.gif" ),
        FORM_LINK2 = readIcon( "form-link2.gif" ),
        FORM_LINK3 = readIcon( "form-link3.gif" ),
        MODE_FLAT = readIcon( "mode-flat.gif" ),
        MODE_AUTO = readIcon( "mode-auto.gif" ),
        MODE_DENSITY = readIcon( "mode-density.gif" ),
        MODE_TRANSPARENT = readIcon( "mode-transparent.gif" ),
        MODE_AUX = readIcon( "mode-aux.gif" ),

        /* Placeholder and terminator. */
        DO_WHAT = readIcon( "burst.gif" );

    /**
     * Private constructor prevents instantiation.
     */
    ResourceIcon() {
    }

    /**
     * Reads an icon from a filename representing a resource in this 
     * class's package.
     *
     * @param  name  image name in this package
     */
    private static Icon readIcon( String name ) {
        try {
            URL url = ResourceIcon.class.getResource( name );
            if ( url == null ) {
                return null;
            }
            InputStream in = new BufferedInputStream( url.openStream() );
            byte[] buf = new byte[ 4096 ];
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            try {
                for ( int n; ( n = in.read( buf ) ) >= 0; ) {
                    bout.write( buf, 0, n );
                }
            }
            finally {
                in.close();
                bout.close();
            }
            return new ImageIcon( bout.toByteArray() );
        }
        catch ( IOException e ) {
            return null;
        }
    }
}
