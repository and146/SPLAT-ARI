package uk.ac.starlink.votable;

import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.Source;
import uk.ac.starlink.table.AbstractStarTable;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DefaultValueInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.ReaderRowSequence;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.ValueInfo;

/**
 * A {@link uk.ac.starlink.table.StarTable} implementation based on a VOTable.
 *
 * @author   Mark Taylor (Starlink)
 */
public class VOStarTable extends AbstractStarTable {

    private Table votable;
    private TabularData tdata;
    private List params;
    private ColumnInfo[] colinfos;

    /* Auxiliary metadata. */
    private final static ValueInfo idInfo = new DefaultValueInfo(
        "ID", String.class, "VOTable ID attribute" );
    private final static ValueInfo datatypeInfo = new DefaultValueInfo(
        "Datatype", String.class, "VOTable data type name" );
    private final static ValueInfo widthInfo = new DefaultValueInfo(
        "Width", Integer.class, "VOTable width attribute" );
    private final static ValueInfo precisionInfo = new DefaultValueInfo(
        "Precision", Double.class, "VOTable precision attribute" );
    private final static ValueInfo typeInfo = new DefaultValueInfo(
        "Type", String.class, "VOTable type attribute" );
    private final static List auxDataInfos = Arrays.asList( new ValueInfo[] {
        idInfo, datatypeInfo, widthInfo, precisionInfo, typeInfo,
    } );

    /**
     * Construct a VOStarTable from a VOTable <tt>Table</tt> object.
     *
     * @param  votable  Table VOElement
     */
    public VOStarTable( Table votable ) {
        this.votable = votable;
        this.tdata = votable.getData();
        int ncol = tdata.getColumnCount();
        colinfos = new ColumnInfo[ ncol ];
    }

    public int getColumnCount() {
        return tdata.getColumnCount();
    }

    public long getRowCount() {
        return tdata.getRowCount();
    }

    public boolean isRandom() {
        return tdata.isRandom();
    }

    public ColumnInfo getColumnInfo( int icol ) {

        /* Lazily construct the columninfo object. */
        if ( colinfos[ icol ] == null ) {
            Field field = votable.getField( icol );
            ColumnInfo cinfo = new ColumnInfo( getValueInfo( field ) );

            /* Set up auxiliary metadata for this column according to the
             * attributes that the FIELD element has. */
            List auxdata = cinfo.getAuxData();

            String id = field.getAttribute( "ID" );
            if ( id != null ) {
                auxdata.add( new DescribedValue( idInfo, id ) );
            }

            String datatype = field.getAttribute( "datatype" );
            if ( datatype != null ) {
                auxdata.add( new DescribedValue( datatypeInfo, datatype ) );
            }

            String width = field.getAttribute( "width" );
            if ( width != null ) {
                try {
                    int wval = Integer.parseInt( width );
                    auxdata.add( new DescribedValue( widthInfo,
                                                     new Integer( wval ) ) );
                }
                catch ( NumberFormatException e ) {
                }
            }

            String precision = field.getAttribute( "precision" );
            if ( precision != null ) {
                try {
                    double precval = Double.parseDouble( precision );
                    auxdata.add( new DescribedValue( precisionInfo,
                                                     new Double( precval ) ) );
                }
                catch ( NumberFormatException e ) {
                }
            }

            String type = field.getAttribute( "type" );
            if ( type != null ) {
                auxdata.add( new DescribedValue( typeInfo, type ) );
            }

            colinfos[ icol ] = cinfo;
        }
        return colinfos[ icol ];
    }

    public List getParameters() {

        /* Lazily construct parameter list. */
        if ( params == null ) {
            params = new ArrayList();
            String description = votable.getDescription();
            if ( description != null ) {
                DefaultValueInfo descInfo = 
                    new DefaultValueInfo( "Description", String.class );
                params.add( new DescribedValue( descInfo, description ) );
            }

            VOElement parent = votable.getParent();
            if ( parent != null && parent.getTagName().equals( "RESOURCE" ) ) {
                VOElement[] paramels = parent.getChildrenByName( "PARAM" );
                for ( int i = 0; i < paramels.length; i++ ) {
                    Param pel = (Param) paramels[ i ];
                    DescribedValue dval = 
                        new DescribedValue( getValueInfo( pel ),
                                            pel.getObject() );
                    params.add( new DescribedValue( getValueInfo( pel ),
                                                    pel.getObject() ) );
                }
            }
        }
        return params;
    }

    public String getName() {
        return votable.getName();
    }

    public List getColumnAuxDataInfos() {
        return auxDataInfos;
    }

    public RowSequence getRowSequence() throws IOException {
        final RowStepper rstep = tdata.getRowStepper();
        return new ReaderRowSequence() {
            protected Object[] readRow() throws IOException {
                return rstep.nextRow();
            }
        };
    }

    public Object[] getRow( long lrow ) throws IOException {
        if ( isRandom() ) {
            return tdata.getRow( lrow );
        }
        else {
            throw new UnsupportedOperationException();
        }
    }

    public Object getCell( long lrow, int icol ) throws IOException {
        if ( isRandom() ) {
            return tdata.getCell( lrow, icol );
        }
        else {
            throw new UnsupportedOperationException();
        }
    }
    

    /**
     * Returns a ValueInfo object suitable for holding the values in a
     * VOTable Field (or Param) object.  The datatype, array shape and
     * other metadata in the returned object are taken from the 
     * relevant bits of the supplied field.
     *
     * @param   field  the Field object for which the ValueInfo is to be
     *          constructed
     * @return  a ValueInfo suitable for <tt>field</tt>
     */
    private static ValueInfo getValueInfo( Field field ) {
        Decoder decoder = field.getDecoder();
        Class clazz = decoder.getContentClass();
        String name = field.getHandle();
        long[] shapel = decoder.getDecodedShape();
        DefaultValueInfo info = new DefaultValueInfo( name, clazz );
        info.setDescription( field.getDescription() );
        info.setUnitString( field.getUnit() );
        info.setUCD( field.getUcd() );
        info.setShape( ( shapel == null || shapel.length == 0 ) 
                            ? null
                            : Decoder.longsToInts( shapel ) );
        info.setElementSize( decoder.getElementSize() );
        return info;
    }

}
