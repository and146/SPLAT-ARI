package uk.ac.starlink.ttools.plot2.data;

import uk.ac.starlink.table.DomainMapper;

/**
 * Coord implementation for single boolean values.
 *
 * @author   Mark Taylor
 * @since    4 Feb 2013
 */
public class BooleanCoord extends SingleCoord {

    /**
     * Constructor.
     *
     * @param   name   user-directed coordinate name
     * @param   description  user-directed coordinate description
     * @param   isRequired  true if this coordinate is required for plotting
     */
    public BooleanCoord( String name, String description, boolean isRequired ) {
        super( name, description, isRequired,
               Boolean.class, StorageType.BOOLEAN, null );
    }

    public Object userToStorage( Object[] userCoords, DomainMapper[] mappers ) {
        Object c = userCoords[ 0 ];
        return c instanceof Boolean ? (Boolean) c : Boolean.FALSE;
    }

    /**
     * Reads a boolean value from an appropriate column in the current row
     * of a given TupleSequence.
     *
     * @param  tseq  sequence positioned at a row
     * @param  icol  index of column in sequence corresponding to this Coord
     * @return  value of boolean column at the current sequence row
     */
    public boolean readBooleanCoord( TupleSequence tseq, int icol ) {
        return tseq.getBooleanValue( icol );
    }
}
