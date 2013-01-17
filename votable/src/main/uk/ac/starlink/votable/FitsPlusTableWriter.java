package uk.ac.starlink.votable;

import java.io.IOException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCardException;
import uk.ac.starlink.fits.FitsTableSerializer;
import uk.ac.starlink.fits.StandardFitsTableSerializer;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableWriter;

/**
 * TableWriter which writes table data into the first extension of a FITS file,
 * Unlike {@link uk.ac.starlink.fits.FitsTableWriter} however, the
 * primary extension is not left contentless, instead it gets the
 * text of a DATA-less VOTable written into it.  This VOTable describes
 * the metadata of the table, as if the DATA element contained a FITS
 * element referencing the first extension HDU of the file.
 * Tables stored in this format have all the rich metadata associated
 * with VOTables, and benefit from the compactness of FITS tables,
 * without the considerable disdvantage of being split into two files.
 *
 * <p>The header cards in the primary HDU look like this:
 * <pre>
 *     SIMPLE  =                    T / Standard FITS format
 *     BITPIX  =                    8 / Character data
 *     NAXIS   =                    1 / Text string
 *     NAXIS1  =                 nnnn / Number of characters
 *     VOTMETA =                    T / Table metadata in VOTABLE format
 *     EXTEND  =                    T / There are standard extensions
 * </pre>
 * the VOTMETA card in particular marking that this HDU contains VOTable
 * metadata.
 *
 * @author   Mark Taylor (Starlink)
 * @since    26 Aug 2004
 */
public class FitsPlusTableWriter extends VOTableFitsTableWriter {

    public FitsPlusTableWriter() {
        super( "fits-plus" );
    }

    /**
     * Returns true if <tt>location</tt> ends with something like ".fit"
     * or ".fits".
     *
     * @param  location  filename
     * @return true if it sounds like a fits file
     */
    public boolean looksLikeFile( String location ) {
        int dotPos = location.lastIndexOf( '.' );
        if ( dotPos > 0 ) {
            String exten = location.substring( dotPos + 1 ).toLowerCase();
            if ( exten.startsWith( "fit" ) ) {
                return true;
            }
        }
        return false;
    }

    protected void customisePrimaryHeader( Header hdr )
            throws HeaderCardException {
        hdr.addValue( "VOTMETA", true, "Table metadata in VOTable format" );
    }

    protected boolean isMagic( int icard, String key, String value ) {
        switch ( icard ) {
            case 4:
                return "VOTMETA".equals( key ) && "T".equals( value );
            default:
                return super.isMagic( icard, key, value );
        }
    }

    protected FitsTableSerializer createSerializer( StarTable table ) 
            throws IOException {
        return new StandardFitsTableSerializer( table, false );
    }

    /**
     * Returns a list of FITS-plus table writers with variant values of
     * attributes.
     * In fact this just returns two functionally identical instances
     * but with different format names: one is "fits" and the other is
     * "fits-plus".
     *
     * @return  table writers
     */
    public static StarTableWriter[] getStarTableWriters() {
        FitsPlusTableWriter w1 = new FitsPlusTableWriter();
        FitsPlusTableWriter w2 = new FitsPlusTableWriter();
        w1.setFormatName( "fits" );
        return new StarTableWriter[] { w1, w2 };
    }
}
