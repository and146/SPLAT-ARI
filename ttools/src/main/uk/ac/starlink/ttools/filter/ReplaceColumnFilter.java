package uk.ac.starlink.ttools.filter;

import java.io.IOException;
import java.util.Iterator;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.ColumnPermutedStarTable;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.ttools.jel.ColumnIdentifier;

public class ReplaceColumnFilter extends BasicFilter {

    public ReplaceColumnFilter() {
        super( "replacecol",
               "[-name <name>] [-units <units>] [-ucd <ucd>] " +
               "[-utype <utype>]\n" +
               "[-desc <descrip>]\n" +
               "<col-id> <expr>" );
    }

    protected String[] getDescriptionLines() {
        return new String[] {
            "<p>Replaces the content of a column with the value of an",
            "algebraic expression.",
            "The old values are discarded in favour of the result of",
            "evaluating <code>&lt;expr&gt;</code>.",
            "You can specify the metadata for the new column using the",
            "<code>-name</code>, <code>-units</code>, <code>-ucd</code>,",
            "<code>-utype</code>",
            "and <code>-desc</code> flags; for any of these items which you",
            "do not specify, they will take the values from the column",
            "being replaced.",
            "</p>",
            "<p>It is legal to reference the replaced column in the",
            "expression,",
            "so for example \"<code>replacecol pixsize pixsize*2</code>\"",
            "just multiplies the values in column <code>pixsize</code> by 2.",
            "</p>",
            explainSyntax( new String[] { "col-id", "expr", } ),
        };
    }

    public ProcessingStep createStep( Iterator argIt ) throws ArgException {
        String colId = null;
        String expr = null;
        String rename = null;
        String units = null;
        String ucd = null;
        String utype = null;
        String description = null;
        while ( argIt.hasNext() && ( colId == null || expr == null ) ) {
            String arg = (String) argIt.next();
            if ( arg.equals( "-name" ) && argIt.hasNext() ) {
                argIt.remove();
                rename = (String) argIt.next();
                argIt.remove();
            }
            else if ( arg.equals( "-units" ) && argIt.hasNext() ) {
                argIt.remove();
                units = (String) argIt.next();
                argIt.remove();
            }
            else if ( arg.equals( "-ucd" ) && argIt.hasNext() ) {
                argIt.remove();
                ucd = (String) argIt.next();
                argIt.remove();
            }
            else if ( arg.equals( "-utype" ) && argIt.hasNext() ) {
                argIt.remove();
                utype = (String) argIt.next();
                argIt.remove();
            }
            else if ( arg.equals( "-desc" ) && argIt.hasNext() ) {
                argIt.remove();
                description = (String) argIt.next();
                argIt.remove();
            }
            else if ( colId == null ) {
                argIt.remove();
                colId = arg;
            }
            else if ( expr == null ) {
                argIt.remove();
                expr = arg;
            }
        }
        if ( colId != null && expr != null ) {
            return new ReplaceColumnStep( colId, expr, rename,
                                          units, ucd, utype, description );
        }
        else {
            throw new ArgException( "Bad " + getName() + " specification" );
        }
    }

    private static class ReplaceColumnStep implements ProcessingStep {

        final String colId_;
        final String expr_;
        final String name_;
        final String units_;
        final String ucd_;
        final String utype_;
        final String description_;

        ReplaceColumnStep( String colId, String expr, String name, String units,
                           String ucd, String utype, String description ) {
            colId_ = colId;
            expr_ = expr;
            name_ = name;
            units_ = units;
            ucd_ = ucd;
            utype_ = utype;
            description_ = description;
        }

        public StarTable wrap( StarTable base ) throws IOException {

            /* Identify the column to be replaced. */
            int icol = new ColumnIdentifier( base )
                      .getColumnIndex( colId_ );

            /* Add the new column, using the same metadata as the old one. */
            ColumnInfo cinfo = new ColumnInfo( base.getColumnInfo( icol ) );

            /* Modify metadata items which have been explicitly specified
             * in the filter stage. */
            if ( name_ != null ) {
                cinfo.setName( name_ );
            }
            if ( units_ != null ) {
                cinfo.setUnitString( units_ );
            }
            if ( ucd_ != null ) {
                cinfo.setUCD( ucd_ );
            }
            if ( utype_ != null ) {
                cinfo.setUtype( utype_ );
            }
            if ( description_ != null ) {
                cinfo.setDescription( description_ );
            }

            /* Invalidate metadata assertions which may be no longer true. */
            cinfo.setNullable( true );
            cinfo.setElementSize( -1 );
            cinfo.setShape( new int[] { -1 } );

            /* Create a column supplement with the new column. */
            ColumnSupplement jelSup =
                new JELColumnSupplement( base, expr_, cinfo );

            /* Add the new column just after the one it's replacing. */
            StarTable addTable = new AddColumnsTable( base, jelSup, icol );

            /* Delete the old column. */
            StarTable removed =
                ColumnPermutedStarTable
               .deleteColumns( addTable, new int[] { icol + 1 } );

            /* Return the result. */
            return removed;
        }
    }
}
