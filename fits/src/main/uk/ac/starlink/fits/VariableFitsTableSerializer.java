package uk.ac.starlink.fits;

import java.io.BufferedOutputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import nom.tam.fits.HeaderCardException;
import nom.tam.util.Cursor;
import uk.ac.starlink.table.ByteStore;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StoragePolicy;
import uk.ac.starlink.table.Tables;

/**
 * FitsTableSerializer which can write variable array-valued columns
 * using the 'P' or 'Q' TFORM formatting characters.
 *
 * @author   Mark Taylor
 * @since    10 Jul 2008
 */
public class VariableFitsTableSerializer extends StandardFitsTableSerializer {

    private final StoragePolicy storagePolicy_;
    private final boolean allowSignedByte_;

    /** 
     * Constructor.
     *
     * @param  table  table to write
     * @param  storagePolicy  policy for acquiring byte array scratch buffers
     * @param  allowSignedByte  if true, bytes written as FITS signed bytes
     *         (TZERO=-128), if false bytes written as signed shorts
     */
    public VariableFitsTableSerializer( StarTable table,
                                        StoragePolicy storagePolicy,
                                        boolean allowSignedByte )
            throws IOException {
        super( allowSignedByte );
        storagePolicy_ = storagePolicy;
        allowSignedByte_ = allowSignedByte;
        init( table );
        set64BitMode( getPCount() > Integer.MAX_VALUE );
    }

    /**
     * Sets whether this serializer should use
     * the 'P' descriptor (32-bit addressing into the heap) or
     * the 'Q' descriptor (64-bit addressing into the heap)
     * for variable-length array columns.
     * Normally Q is only used if the heap is larger than 2^31.
     *
     * @param  useQ  true for Q, false for P
     */
    public void set64BitMode( boolean useQ ) {
        PQMode pqMode = useQ ? PQMode.Q
                             : PQMode.P;
        VariableArrayColumnWriter[] vcws = getVariableArrayColumnWriters();
        for ( int iv = 0; iv < vcws.length; iv++ ) {
            vcws[ iv ].setPQMode( pqMode );
        }
    }

    public Header getHeader() throws HeaderCardException {
        Header hdr = super.getHeader();
        long pcount = getPCount();
        long theap = hdr.getIntValue( "NAXIS1" ) * hdr.getIntValue( "NAXIS2" );
        theap = ( ( theap + 2880 - 1 ) / 2880 ) * 2880;

        /* Header manipulation methods leave a bit to be desired.
         * It is a bit fiddly to make sure these cards go in the right place. */
        final List cardList = new ArrayList();
        assert hdr.containsKey( "PCOUNT" );
        assert hdr.containsKey( "GCOUNT" );
        hdr.removeCard( "THEAP" );
        assert hdr.containsKey( "NAXIS2" );
        for ( Iterator it = hdr.iterator(); it.hasNext(); ) {
            HeaderCard card = (HeaderCard) it.next();
            String key = card.getKey();
            if ( "PCOUNT".equals( key ) ) {
                cardList.add( new HeaderCard( "PCOUNT", pcount, "heap size" ) );
            }
            else if ( "TFIELDS".equals( key ) ) {
                cardList.add( card );
                cardList.add( new HeaderCard( "THEAP", theap,
                                              "heap start (block aligned)" ) );
            }
            else {
                cardList.add( card );
            }
        }
        return new Header() {
            {
                for ( Iterator it = cardList.iterator(); it.hasNext(); ) {
                    addLine( (HeaderCard) it.next() );
                }
            }
        };
    }

    /**
     * Returns an array of all the ColumnWriters used by this class
     * which are instances of VariableArrayColumnWriter.
     *
     * @return   array of variable column writers in use
     */
    private VariableArrayColumnWriter[] getVariableArrayColumnWriters() {
        ColumnWriter[] colWriters = getColumnWriters();
        List vcwList = new ArrayList();
        for ( int icol = 0; icol < colWriters.length; icol++ ) {
            if ( colWriters[ icol ] instanceof VariableArrayColumnWriter ) {
                vcwList.add( colWriters[ icol ] );
            }
        }
        return (VariableArrayColumnWriter[])
               vcwList.toArray( new VariableArrayColumnWriter[ 0 ] );
    }

    /**
     * Returns the number of bytes which will be written to the heap 
     * containing variable array data.
     *
     * @return   heap size
     */
    private long getPCount() {
        long pcount = 0L;
        VariableArrayColumnWriter[] vcws = getVariableArrayColumnWriters();
        for ( int iv = 0; iv < vcws.length; iv++ ) {
            VariableArrayColumnWriter vcw = vcws[ iv ];
            pcount += vcw.totalElements_ * vcw.arrayWriter_.getByteCount();
        }
        return pcount;
    }

    public void writeData( DataOutput out ) throws IOException {
        VariableArrayColumnWriter[] vcws = getVariableArrayColumnWriters();
        ByteStore byteStore = storagePolicy_.makeByteStore();
        int bufsiz = 64 * 1024;
        DataOutputStream dataOut =
            new DataOutputStream(
                new BufferedOutputStream( byteStore.getOutputStream(),
                                          bufsiz ) );
        for ( int iv = 0; iv < vcws.length; iv++ ) {
            vcws[ iv ].setDataOutput( dataOut );
        }
        try {

            /* Write the fixed-size table data.  Note this pads to a 
             * 2880-byte block. */
            super.writeData( out );
            dataOut.flush();
            byteStore.copy( toStream( out ) );
        }
        finally {
            byteStore.close();
        }
        int over = (int) ( getPCount() % 2880 );
        if ( over > 0 ) {
            out.write( new byte[ 2880 - over ] );
        }
        for ( int iv = 0; iv < vcws.length; iv++ ) {
            vcws[ iv ].setDataOutput( null );
        }
    }

    ColumnWriter createColumnWriter( ColumnInfo cinfo, int[] shape,
                                     boolean varShape, int eSize,
                                     int maxEls, long totalEls,
                                     boolean nullableInt ) {
        Class clazz = cinfo.getContentClass();
        if ( ! varShape || clazz == String.class || clazz == String[].class ) {
            return super.createColumnWriter( cinfo, shape, varShape, eSize,
                                             maxEls, totalEls, nullableInt );
        }
        else {
            assert clazz.isArray();
            ArrayWriter aw =
                ArrayWriter.createArrayWriter( cinfo.getContentClass(),
                                               allowSignedByte_ );
            return new VariableArrayColumnWriter( aw, maxEls, totalEls );
        }
    }

    /**
     * Gets an OutputStream based on a given DataOutput.
     *
     * @param   dataOut  data output object
     * @return   stream which writes to the same place as <code>dataOut</code>
     */
    private static OutputStream toStream( final DataOutput dataOut ) {
        if ( dataOut instanceof OutputStream ) {
            return (OutputStream) dataOut;
        }
        else {
            return new OutputStream() {
                public void write( int b ) throws IOException {
                    dataOut.write( b );
                }
                public void write( byte[] buf ) throws IOException {
                    dataOut.write( buf );
                }
                public void write( byte[] buf, int off, int leng )
                        throws IOException {
                    dataOut.write( buf, off, leng );
                }
            };
        }
    }

    /**
     * ColumnWriter which writes array-valued elements using the
     * BINTABLE conventions for variable-sized arrays.
     */
    private static class VariableArrayColumnWriter implements ColumnWriter {

        private final ArrayWriter arrayWriter_;
        private final int maxElements_;
        private final long totalElements_;
        private PQMode pqMode_;
        private DataOutputStream dataOut_;

        /**
         * Constructor.
         *
         * @param  arrayWriter   array writer for a specific data type
         */
        VariableArrayColumnWriter( ArrayWriter arrayWriter, int maxElements,
                                   long totalElements ) {
            arrayWriter_ = arrayWriter;
            maxElements_ = maxElements;
            totalElements_ = totalElements;
        }

        /**
         * Sets the 32/64-bit mode used by this writer.  Must be called
         * before use.
         */
        public void setPQMode( PQMode pqMode ) {
            pqMode_ = pqMode;
        }

        /**
         * Sets the byte store to which the actual array data is written
         * by this serializer.  Must be called before use.
         *
         * @param  byteStore  byte store
         */
        public void setDataOutput( DataOutputStream dataOut ) {
            dataOut_ = dataOut;
        }

        public void writeValue( DataOutput out, Object value )
                throws IOException {
            int leng = value == null ? 0 : Array.getLength( value );
            pqMode_.writeInteger( out, leng );
            pqMode_.writeInteger( out, leng == 0 ? 0
                                                 : dataOut_.size() );
            for ( int i = 0; i < leng; i++ ) {
                arrayWriter_.writeElement( dataOut_, value, i );
            }
        }

        public char getFormatChar() {
            return arrayWriter_.getFormatChar();
        }

        public String getFormat() {
            return new StringBuffer()
                  .append( pqMode_.getFormatChar() )
                  .append( arrayWriter_.getFormatChar() )
                  .append( '(' )
                  .append( maxElements_ )
                  .append( ')' )
                  .toString();
        }

        public int getLength() {
            return 2 * pqMode_.getIntegerLength();
        }

        public int[] getDims() {
            return new int[] { -1 };
        }

        public double getZero() {
            return arrayWriter_.getZero();
        }

        public double getScale() {
            return 1.0;
        }

        public Number getBadNumber() {
            return null;
        }
    }

    /**
     * Parameterises whether 'P' or 'Q' descriptor is used to write 
     * variable-length arrays.
     */
    private static abstract class PQMode {
        private final char formatChar_;
        private final int intLength_;

        /** 32-bit mode. */
        public static final PQMode P = new PQMode( 'P', 4 ) {
            public void writeInteger( DataOutput out, long value )
                    throws IOException {
                out.writeInt( Tables.checkedLongToInt( value ) );
            }
        };

        /** 64-bit mode. */
        public static final PQMode Q = new PQMode( 'Q', 8 ) {
            public void writeInteger( DataOutput out, long value )
                    throws IOException {
                out.writeLong( value );
            }
        };

        /**
         * Constructor.
         *
         * @param  formatChar  TFORM character
         * @param  intLength  number of bytes in an integer
         */
        private PQMode( char formatChar, int intLength ) {
            formatChar_ = formatChar;
            intLength_ = intLength;
        }

        /**
         * Writes an integer to an output stream.
         *
         * @param   out  output stream
         * @param  value  integer value to write
         */
        public abstract void writeInteger( DataOutput out, long value )
                throws IOException;

        /**
         * Returns the TFORM character for this object.
         *
         * @return  P or Q
         */
        public char getFormatChar() {
            return formatChar_;
        }

        /**
         * Returns the number of bytes per integer written.
         *
         * @return  byte count
         */
        public int getIntegerLength() {
            return intLength_;
        }
    }
}
