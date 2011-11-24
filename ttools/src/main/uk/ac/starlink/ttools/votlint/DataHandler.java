package uk.ac.starlink.ttools.votlint;

/**
 * Element handler for DATA elements.
 *
 * @author   Mark Taylor (Starlink)
 * @since    7 Apr 2005
 */
public class DataHandler extends ElementHandler {

    private FieldHandler[] fields_;

    public void startElement() {

        /* Acquire the list of parsers which the parent TABLE element has
         * accumulated and store them for the convenience of child 
         * elements which actually need to do the encoding. */
        TableHandler table =
            (TableHandler) getAncestry().getAncestor( TableHandler.class );
        if ( table != null ) {
            fields_ = (FieldHandler[])
                      table.getFields().toArray( new FieldHandler[ 0 ] );
            if ( fields_.length == 0 ) {
                error( "There are no columns in this table!" );
            }
        }
        else {
            fields_ = new FieldHandler[ 0 ];
            error( getName() + " outside TABLE" );
        }
    }

    /**
     * Returns the FieldHandler object for a given column.
     *
     * @param  icol  column index
     * @return   field handler for column <tt>icol</tt>,
     *           or null if that column doesn't exist
     */
    public FieldHandler getField( int icol ) {
        return icol < fields_.length ? fields_[ icol ] : null;
    }

    /**
     * Returns the number of columns in this DATA's table.
     *
     * @return   column count
     */
    public int getColumnCount() {
        return fields_.length;
    }
}
