package uk.ac.starlink.topcat;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JList;
import javax.swing.ListModel;
import javax.swing.table.TableColumnModel;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.gui.StarTableColumn;
import uk.ac.starlink.vo.AbstractAdqlExample;
import uk.ac.starlink.vo.AdqlExample;
import uk.ac.starlink.vo.TableMeta;
import uk.ac.starlink.vo.TapCapability;
import uk.ac.starlink.vo.TapCapabilityPanel;

/**
 * Provides some ADQL examples showing how TOPCAT TAP uploads work.
 *
 * @author   Mark Taylor
 * @since    9 May 2011
 */
public abstract class UploadAdqlExample extends AbstractAdqlExample {

    private final JList tcList_;

    private static final Pattern[] RADEC_UCD_REGEXES = new Pattern[] {
        Pattern.compile( "^pos.eq.ra[_;.]?(.*)", Pattern.CASE_INSENSITIVE ),
        Pattern.compile( "^pos.eq.dec[_;.]?(.*)", Pattern.CASE_INSENSITIVE ),
    };
    private static final Pattern[] RADEC_NAME_REGEXES = new Pattern[] {
        Pattern.compile( "RA_?J?(2000)?", Pattern.CASE_INSENSITIVE ),
        Pattern.compile( "DEC?L?_?J?(2000)?", Pattern.CASE_INSENSITIVE )
    };

    /**
     * Constructor.
     *
     * @param   name  example name
     * @param   description  example description
     * @param   tcList  JList of known TopcatModels
     */
    public UploadAdqlExample( String name, String description, JList tcList ) {
        super( name, description );
        tcList_ = tcList;
    }

    /**
     * Creates and returns a selection of examples for display in the TAP
     * load dialgue which illustrate how to use table uploads from TOPCAT.
     *
     * @param   tcList  JList of known TopcatModels
     */
    public static AdqlExample[] createSomeExamples( final JList tcList ) {
        return new AdqlExample[] {
            new UploadAdqlExample( "Trivial Upload",
                                   "Upload a table and query all its columns; "
                                 + "not very useful",
                                   tcList ) {
                public String getText( boolean lineBreaks, String lang,
                                       TapCapability tcap, TableMeta[] tables,
                                       TableMeta table ) {
                    if ( ! TapCapabilityPanel.canUpload( tcap ) ||
                         tcList.getModel().getSize() < 1 ) {
                        return null;
                    } 
                    return getTrivialText( lineBreaks, lang, tables, table,
                                           tcList );
                }
            },
            new UploadAdqlExample( "Upload Join",
                                   "Upload a local table and join a remote "
                                 + "table with it",
                                   tcList ) {
                public String getText( boolean lineBreaks, String lang,
                                       TapCapability tcap, TableMeta[] tables,
                                       TableMeta table ) {
                    if ( ! TapCapabilityPanel.canUpload( tcap ) ) {
                        return null;
                    } 
                    return getJoinText( lineBreaks, lang, tables, table,
                                        tcList );
                }
            },
        };
    }

    /**
     * Returns text for a trivial upload query.
     *
     * @param  lineBreaks  whether output ADQL should include multiline
     *                     formatting
     * @param  lang  ADQL language variant (e.g. "ADQL-2.0")
     * @param  tables  table metadata set
     * @param  table  currently selected table
     * @param  tcList  JList of known TopcatModels
     */
    private static String getTrivialText( boolean lineBreaks, String lang,
                                          TableMeta[] tables, TableMeta table,
                                          JList tcList ) {
        Object item = tcList.getSelectedValue();
        if ( item == null ) {
            item = tcList.getModel().getElementAt( 0 );
        }
        if ( ! ( item instanceof TopcatModel ) ) {
            return null;
        }
        TopcatModel tcModel = (TopcatModel) item;
        int tcid = tcModel.getID();
        StringBuffer sbuf = new StringBuffer();
        Breaker breaker = createBreaker( lineBreaks );
        sbuf.append( "SELECT" )
            .append( breaker.level( 1 ) )
            .append( "TOP " )
            .append( 1000 )
            .append( " *" )
            .append( breaker.level( 1 ) )
            .append( "FROM " )
            .append( "TAP_UPLOAD.t" )
            .append( tcid );
        return sbuf.toString();
    }

    /**
     * Returns text for an upload query involving a join with a remote table.
     *
     * @param  lineBreaks  whether output ADQL should include multiline
     *                     formatting
     * @param  lang  ADQL language variant (e.g. "ADQL-2.0")
     * @param  tables  table metadata set
     * @param  table  currently selected table
     * @param  tcList  JList of known TopcatModels
     */
    private static String getJoinText( boolean lineBreaks, String lang,
                                       TableMeta[] tables, TableMeta table,
                                       JList tcJlist ) {
        TableWithCols[] rdRemotes =
            getRaDecTables( toTables( table, tables ), 1 );
        if ( rdRemotes.length == 0 ) {
            return null;
        }
        TableWithCols rdRemote = rdRemotes[ 0 ];
        ListModel tcListModel = tcJlist.getModel();
        List<TopcatModel> tcList = new ArrayList<TopcatModel>();
        Object selItem = tcJlist.getSelectedValue();
        if ( selItem instanceof TopcatModel ) {
            tcList.add( (TopcatModel) selItem );
        }
        for ( int i = 0; i < tcListModel.getSize(); i++ ) {
            Object item = tcListModel.getElementAt( i );
            if ( item instanceof TopcatModel &&
                 item != selItem ) {
                tcList.add( (TopcatModel) item );
            }
        }
        TopcatModel[] tcs = tcList.toArray( new TopcatModel[ 0 ] );
        TopcatModel localRd = null;
        String[] localCoords = null;
        for ( int i = 0; i < tcs.length; i++ ) {
            String[] crds = getRaDecDegreesNames( tcs[ i ] );
            if ( crds != null ) {
                localRd = tcs[ i ];
                localCoords = crds;
                break;
            }
        }
        if ( localRd == null ) {
            return null;
        }
        Breaker breaker = createBreaker( lineBreaks );
        String localAlias = "tc";
        String remoteAlias = "db";
        return new StringBuffer()
            .append( "SELECT" )
            .append( breaker.level( 1 ) )
            .append( "TOP " )
            .append( 1000 )
            .append( breaker.level( 1 ) )
            .append( "*" )
            .append( breaker.level( 1 ) )
            .append( "FROM " )
            .append( rdRemote.getTable().getName() )
            .append( " AS " )
            .append( remoteAlias )
            .append( breaker.level( 1 ) )
            .append( "JOIN " )
            .append( "TAP_UPLOAD.t" )
            .append( localRd.getID() )
            .append( " AS " )
            .append( localAlias )
            .append( breaker.level( 1 ) )
            .append( "ON 1=CONTAINS(POINT('ICRS', " )
            .append( remoteAlias )
            .append( "." )
            .append( rdRemote.getColumns()[ 0 ] )
            .append( ", " )
            .append( remoteAlias )
            .append( "." )
            .append( rdRemote.getColumns()[ 1 ] )
            .append( ")," )
            .append( breaker.level( 1 ) )
            .append( "              CIRCLE('ICRS', " )
            .append( localAlias )
            .append( "." )
            .append( localCoords[ 0 ] )
            .append( ", " )
            .append( localAlias )
            .append( "." )
            .append( localCoords[ 1 ] )
            .append( ", " )
            .append( "5./3600." )
            .append( "))" )
            .toString();
    }

    /**
     * Returns the names for suitable RA/Dec columns in degrees from a table.
     * If no such column pair can be found, null is returned.
     *
     * @param  tcModel   topcat table to be investigated
     * @return   2-element array with column names for RA, Dec respectively,
     *           or null if nothing suitable
     */
    private static String[] getRaDecDegreesNames( TopcatModel tcModel ) {
        TableColumnModel colModel = tcModel.getColumnModel();
        String[] coords = new String[ 2 ];
        int[] scores = new int[ 2 ];
        int ncol = colModel.getColumnCount();
        for ( int ic = 0; ic < ncol; ic++ ) {
            ColumnInfo info = ((StarTableColumn) colModel.getColumn( ic ))
                             .getColumnInfo();
            String ucd = info.getUCD();
            String unit = info.getUnitString();
            String name = info.getName();
            if ( unit == null || unit.length() == 0
                              || unit.toLowerCase().startsWith( "deg" ) ) {
                for ( int id = 0; id < 2; id++ ) {
                    int score = 0;
                    if ( ucd != null && ucd.trim().length() > 0 ) { 
                        Matcher matcher =
                            RADEC_UCD_REGEXES[ id ].matcher( ucd );
                        if ( matcher.matches() ) {
                            score = 2;
                            String trailer = matcher.group( 1 );
                            if ( trailer == null ||
                                 trailer.trim().length() == 0 ) {
                                score = 3;
                            }
                            else if ( trailer.toLowerCase().equals( "main" ) ) {
                                score = 5;
                            }
                            else if ( trailer.toLowerCase()
                                             .startsWith( "main" ) ) {
                                score = 4;
                            }
                        }
                    }
                    else if ( name != null && name.trim().length() > 0 ) {
                        Matcher matcher =
                            RADEC_NAME_REGEXES[ id ].matcher( name );
                        if ( matcher.matches() ) {
                            score = 1;
                        }
                    }
                    if ( score > scores[ id ] ) {
                        scores[ id ] = score;
                        coords[ id ] = name;
                    }
                }
            }
        }
        return scores[ 0 ] > 0 && scores[ 1 ] > 0 ? coords : null;
    }
}
