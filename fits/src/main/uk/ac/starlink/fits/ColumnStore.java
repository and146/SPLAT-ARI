package uk.ac.starlink.fits;

import java.io.DataOutput;
import java.io.IOException;
import nom.tam.fits.Header;
import nom.tam.fits.HeaderCardException;

/**
 * Defines an object which can provide serial storage and serial 
 * retrieval of a sequence of homogeneous values.
 *
 * <p>Calls on this object must be in the following sequence:
 * <ol>
 * <li>Zero or more calls to {@link #storeValue}</li>
 * <li>One call to {@link #endStores}</li>
 * <li>Zero or more calls to
 *     {@link #getDataLength}, {@link #streamData}, {@link #addHeaderInfo}</li>
 * <li>Optionally, a call to {@link #dispose}</li>
 * </ol>
 *
 * @author   Mark Taylor
 * @since    21 Jun 2006
 */
interface ColumnStore {

    /**
     * Stores the next value in sequence.
     *
     * @param  value  value to store
     */
    void storeValue( Object value ) throws IOException;

    /**
     * Declares that all the data have been stored,
     * and no more calls of {@link #storeValue} will be made.
     */
    void endStores() throws IOException;

    /**
     * Returns the number of bytes which will be written by 
     * {@link #streamData}.
     */
    long getDataLength();

    /**
     * Writes out all the values which have been stored in this object
     * to the given output stream.
     *
     * @param  out  destination stream
     */
    void streamData( DataOutput out ) throws IOException;

    /**
     * Modifies (typically, adds cards to) a FITS header in a way 
     * which describes the column data which is output by {@link #streamData}.
     *
     * @param  hdr   header to modify
     * @param  icol  index (1-based) of the column represented by this store
     */
    void addHeaderInfo( Header hdr, int icol ) throws HeaderCardException;

    /**
     * Releases any resources associated with this object.
     */
    void dispose() throws IOException;
}
