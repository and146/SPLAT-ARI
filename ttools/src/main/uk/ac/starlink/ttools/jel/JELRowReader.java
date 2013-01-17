package uk.ac.starlink.ttools.jel;

import gnu.jel.CompiledExpression;
import gnu.jel.DVMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * An object which is able to read cell values by column name or number.
 * The values are got using the {@link #evaluate} method.
 * <p>
 * This class currently deals with columns of all the primitive types, 
 * objects of type {@link java.lang.String} or {@link java.util.Date},
 * and arrays of any of these.  
 * Anything else is treated as an <tt>Object</tt> or <tt>Object[]</tt>.
 * It could be extended to deal with more if necessary.
 * <p>
 * Expressions of the following types are understood:
 * <dl>
 * <dt>"null":
 * <dd>the <tt>null</tt> value (this is not provided as part of the JEL 
 *     engine).
 *
 * <dt>"NULL":
 * <dd>if this expression is evaluated at any point in the expression 
 *     evaluation, then the result of the whole evaluation will be 
 *     <tt>null</tt>.  This has the same effect as throwing a 
 *     <tt>NullPointerException</tt> during evaluation.
 *     The NULL token is syntactically of type <tt>byte</tt>, which can
 *     be promoted implicitly to any numeric value; this means it can be
 *     used anywhere a primitive (other than <tt>boolean</tt>) can be used.
 *
 * <dt>Column $ID identifiers:
 * <dd>The letter '$' followed by the 1-based index of the column refers
 *     to the contents of that column in the current row (as a primitive,
 *     if applicable).
 * 
 * <dt>Column names:
 * <dd>The name of a column (case-insensitive) refers to the contents of
 *     that column in the current row (as a primitive, if applicable) - 
 *     this can only work if the column name is a legal java identifier.
 *
 * <dt>Null queries:
 * <dd>The string {@link #NULL_QUERY_PREFIX} followed by a value identifier
 *     (column name, column $ID or parameter identifier - see above)
 *     returns a boolean value which is <tt>true</tt> iff the corresponding
 *     value (at the current row, if applicable) has a blank value.
 * </dl>
 * 
 * @author   Mark Taylor (Starlink)
 */
public abstract class JELRowReader extends DVMap {

    private final Object[] args_;
    private List constantList_;
    private boolean isNullExpression_;

    /**
     * The string which, when prefixed to a column identifier, indicates
     * that the null-ness of the column should be queried.
     */
    public static final String NULL_QUERY_PREFIX = "NULL_";

    /** Prefix identifying a unique column identifier. */
    public static final char COLUMN_ID_CHAR = '$';

    /** Constant which returns a boolean True. */
    private final Constant TRUE_CONST = new FixedConstant( Boolean.TRUE );

    /** Constant which returns a boolean False. */
    private final Constant FALSE_CONST = new FixedConstant( Boolean.FALSE );

    /** Constant which returns a null Object. */
    private final Constant NULL_CONST = new FixedConstant( null, Object.class );

    /** Constant which effectively returns a null primitive. */
    private final Constant NULL_EXPRESSION_CONST = new Constant() {
        private final Byte b0 = new Byte( (byte) 0 );
        public Class getContentClass() {
            return Byte.class;
        }
        public Object getValue() {
            foundNull();
            return b0;
        }
    };

    /**
     * Constructor.
     */
    public JELRowReader() {
        args_ = new Object[] { this };
        constantList_ = new ArrayList();
    }

    /**
     * Returns the column index of a column in the row given its name.
     * If <code>name</code> does not refer to any known column, return -1.
     *
     * @param  name   column name
     * @return  column index, or -1
     */
    protected abstract int getColumnIndexByName( String name );

    /**
     * Returns a constant value for this reader given its name.
     *
     * @param  name  constant name
     * @return  constant, or null
     */
    protected abstract Constant getConstantByName( String name );

    /**
     * Indicates whether the value in a given column is null.
     *
     * @param   icol  column index
     * @return   true if value at icol is null
     */
    protected abstract boolean isBlank( int icol );

    /**
     * Returns the class of values returned by a given column.
     *
     * @param  icol   column index
     * @return  value class
     */
    protected abstract Class getColumnClass( int icol );

    /**
     * Returns a boolean value for a cell of the current row.
     * Will only be called if the relevant column is declared boolean.
     * Must call {@link #foundNull} (and return any value) if the result
     * is null.
     *
     * @param   icol   column index
     * @return  value
     */
    protected abstract boolean getBooleanColumnValue( int icol );

    /**
     * Returns a byte value for a cell of the current row.
     * Will only be called if the relevant column is declared byte.
     * Must call {@link #foundNull} (and return any value) if the result
     * is null.
     *
     * @param   icol   column index
     * @return  value
     */
    protected abstract byte getByteColumnValue( int icol );

    /**
     * Returns a char value for a cell of the current row.
     * Will only be called if the relevant column is declared char.
     * Must call {@link #foundNull} (and return any value) if the result
     * is null.
     *
     * @param   icol   column index
     * @return  value
     */
    protected abstract char getCharColumnValue( int icol );

    /**
     * Returns a short value for a cell of the current row.
     * Will only be called if the relevant column is declared short.
     * Must call {@link #foundNull} (and return any value) if the result
     * is null.
     *
     * @param   icol   column index
     * @return  value
     */
    protected abstract short getShortColumnValue( int icol );

    /**
     * Returns a int value for a cell of the current row.
     * Will only be called if the relevant column is declared int.
     * Must call {@link #foundNull} (and return any value) if the result
     * is null.
     *
     * @param   icol   column index
     * @return  value
     */
    protected abstract int getIntColumnValue( int icol );

    /**
     * Returns a long value for a cell of the current row.
     * Will only be called if the relevant column is declared long.
     * Must call {@link #foundNull} (and return any value) if the result
     * is null.
     *
     * @param   icol   column index
     * @return  value
     */
    protected abstract long getLongColumnValue( int icol );

    /**
     * Returns a float value for a cell of the current row.
     * Will only be called if the relevant column is declared float.
     * Must call {@link #foundNull} (and return any value) if the result
     * is null.
     *
     * @param   icol   column index
     * @return  value
     */
    protected abstract float getFloatColumnValue( int icol );

    /**
     * Returns a double value for a cell of the current row.
     * Will only be called if the relevant column is declared double.
     * Must call {@link #foundNull} (and return any value) if the result
     * is null.
     *
     * @param   icol   column index
     * @return  value
     */
    protected abstract double getDoubleColumnValue( int icol );

    /**
     * Returns an Object value for a cell of the current row.
     *
     * @param   icol   column index
     * @return  value
     */
    protected abstract Object getObjectColumnValue( int icol );

    /**
     * Must be called by any of the <code>getObjectColumnValue</code> methods
     * which wants to return a <code>null</code> but has to return a 
     * primitive instead.
     */
    protected void foundNull() {
        isNullExpression_ = true;
    }

    /**
     * Returns the a special quantity which corresponds to
     * a given name, or null if it isn't a special.
     * Specials are much like constants but they are checked for earlier.
     *
     * <p>The current specials are:
     * <ul>
     * <li>"null" returns the <tt>null</tt> value (this is not built in
     *     to the JEL evaluator)
     * <li>"NULL" flags that an attempt has been made to evaluate a 
     *     primitive with no value, and thus invalidates the rest of the
     *     evaluation
     * </ul>
     *
     * @param   name  special name
     * @return  special, or null
     */
    protected Constant getSpecialByName( String name ) {
        if ( name.equals( "null" ) ) {
            return NULL_CONST;
        }
        else if ( name.equals( "NULL" ) ) {
            return NULL_EXPRESSION_CONST;
        }
        else if ( getNullConstantIndex( name ) >= 0 ) {
            Object value = ((NamedConstant)
                            constantList_.get( getNullConstantIndex( name ) ))
                          .getValue();
            return value == null ? TRUE_CONST
                                 : FALSE_CONST;
        }
        else {
            return null;
        }
    }

    /**
     * Evaluates a given compiled expression at the current row.
     * The returned value is wrapped up as an object if the result of
     * the expression is a primitive.
     *
     * @param  compEx  compiled expression
     * @return   expression value at current row
     */
    public synchronized Object evaluate( CompiledExpression compEx )
             throws Throwable {
         try {
             isNullExpression_ = false;
             Object result = compEx.evaluate( args_ );
             return isNullExpression_ ? null : result;
         }
         catch ( NullPointerException e ) {
             return null;
         }
    }

    /**
     * Evaluates a given compiled expression at the given row under the 
     * assumption that the expression represents a numeric value.
     * The returned value is a double.  If a null value was encountered
     * during evaluation, a NaN is returned.
     *
     * @param  compEx  numeric-valued compiled expression
     * @return   expression value at current row
     */
    public synchronized double evaluateDouble( CompiledExpression compEx )
             throws Throwable {
        try {
            isNullExpression_ = false;
            double result = compEx.evaluate_double( args_ );
            return isNullExpression_ ? Double.NaN : result;
        }
        catch ( NullPointerException e ) {
            return Double.NaN;
        }
    }

    /**
     * Returns the type name of the quantity which is referenced in 
     * expressions with a given name.  The significance of this return
     * value is that it appears in the names of the 
     * corresponding <tt>getXXXProperty</tt> methods in this class.
     *
     * @param   name  the variable name
     * @return  the corresponding method name fragment
     * @see   "JEL manual"
     */
    public String getTypeName( String name ) {

        /* See if it's a known special, and treat it specially if so. */
        int ispecial = getSpecialIndex( name );
        if ( ispecial >= 0 ) {
            return getTypeName( ((NamedConstant) constantList_.get( ispecial ))
                               .getContentClass() );
        }

        /* See if it's a null column indicator, and return a Boolean value type
         * if so. */
        int inul = getNullColumnIndex( name );
        if ( inul >= 0 ) {
            return "Boolean";
        }

        /* See if it's a known column, and get the return value type if so. */
        int icol = getColumnIndex( name );
        if ( icol >= 0 ) {
            return getTypeName( getColumnClass( icol ) );
        }

        /* See if it's a null constant indicator, and return a Boolean value
         * type if so. */
        int inulconst = getNullConstantIndex( name );
        if ( inulconst >= 0 ) {
            return "Boolean";
        }

        /* See if it's a known constant, and treat it specially if so. */
        int iconst = getConstantIndex( name );
        if ( iconst >= 0 ) {
            return getTypeName( ((NamedConstant) constantList_.get( iconst ))
                               .getContentClass() );
        }

        /* If we haven't got it yet, we don't know what it is. */
        return null;
    }

    /**
     * Turns a value specification into a constant object which can be
     * used at evaluation time to reference a particular quantity to
     * evaluate.  Currently this routine returns 
     * <ul>
     * <li>a non-negative <tt>Integer</tt> object (the column index) if 
     *     <tt>name</tt> appears to reference a known column 
     * <li>a negative <tt>Integer</tt> object (-1-constIndex) if
     *     <tt>name</tt> appears to reference a known constant
     * <li>a <tt>Long</tt> object if it is a null query on a known column
     * <li><tt>null</tt> otherwise
     * </ul>
     * The different integral types are only used to separate the namespaces,
     * there is no other significance in these types.
     * <p>
     * This method is only called at expression compilation time, not
     * evaluation time, so it doesn't need to be particularly fast.
     *
     * @param  name  the name of the variable-like object to evaluate
     * @return  a numeric object corresponding to an object which we
     *          know how to evaluate
     * @see    "JEL manual"
     */
    public Object translate( String name ) {

        /* See if it corresponds to a special value. */
        int ispecial = getSpecialIndex( name );
        if ( ispecial >= 0 ) {
            return new Integer( -1 - ispecial );
        }

        /* See if it corresponds to a null column indicator. */
        int inul = getNullColumnIndex( name );
        if ( inul >= 0 ) {
            return new Long( inul );
        }

        /* See if it corresponds to a column. */
        int icol = getColumnIndex( name );
        if ( icol >= 0 ) {
            return new Integer( icol );
        }

        /* See if it corresponds to a constant value. */
        int iconst = getConstantIndex( name );
        if ( iconst >= 0 ) {
            return new Integer( -1 - iconst );
        }

        /* It is an error if the column name doesn't exist, since the
         * variable substitution isn't going to come from anywhere else. */
        return null;
    }

    /**
     * Returns the column index in the table model which corresponds to 
     * a given name.  The current formats are
     * <ul>
     * <li> column name (case insensitive, first occurrence used)
     * <li> "$"+(index+1) (so first column would be "$1")
     * </ul>
     * Note that the name '$0' is reserved for the special index column.
     * <p>
     * Note this method is only called during expression compilation,
     * so it doesn't need to be particularly efficient.
     *
     * @param  name  column identifier
     * @return  column index, or -1 if the column was not known
     */
    public final int getColumnIndex( String name ) {

        /* Blank name, unknown column. */
        if ( name.length() == 0 ) {
            return -1;
        }

        /* Try the '$' + number format. */
        if ( name.charAt( 0 ) == COLUMN_ID_CHAR ) {
            try {
                int icol = Integer.parseInt( name.substring( 1 ) ) - 1;
                if ( icol >= 0 ) {
                    return icol;
                }
            }
            catch ( NumberFormatException e ) {
                // no good
            }
        }

        /* Try class-specific name identification. */
        int icol = getColumnIndexByName( name );
        if ( icol >=0 ) {
            return icol;
        }

        /* It's not a column. */
        return -1;
    }

    /**
     * Returns the column index in the table model which corresponds to
     * the column name as a null query.  If <tt>name</tt> has the form
     * "{@link #NULL_QUERY_PREFIX}<i>column-name</i>" where 
     * <i>column-name</i> is as 
     * recognised by the {@link #getColumnIndex} method, then the return
     * value will be the index of the column corresponding to 
     * <i>column-name</i>.
     * Otherwise (if it doesn't start with the NULL_QUERY_PREFIX string or 
     * the <tt>name</tt> part
     * doesn't correspond to a known column) the value -1 will be returned.
     * <p>
     * Note this method is only called during expression compilation,
     * so it doesn't need to be particularly efficient.
     *
     * @param   name  null-query column identifier
     * @return  column index for which <tt>name</tt> is a null-query, 
     *          or -1
     */
    private int getNullColumnIndex( String name ) {
        String colname = stripPrefix( name, NULL_QUERY_PREFIX );
        if ( colname != null ) {
            return getColumnIndex( colname );
        }
        return -1;
    }

    /**
     * Returns the index into this reader's list of known special constants
     * which corresponds to a given name.  Matching is case insensitive.
     * <p>
     * Note this method is only called during expression compilation,
     * so it doesn't need to be particularly efficient.
     *
     * @param   name  constant name
     * @return  index into constants list, or -1 if no such constant was found
     */
    private int getSpecialIndex( String name ) {

        /* Search the existing list of known constants for a constant with
         * this name that has been encountered before.
         * If one is found return the index. */
        for ( int i = 0; i < constantList_.size(); i++ ) {
            String cname = ((NamedConstant) constantList_.get( i )).getName();
            if ( cname.equals( name ) ) {
                return i;
            }
        }

        /* Otherwise see if we can identify it from the name. */
        Constant konst = getSpecialByName( name );
        if ( konst != null ) {
            constantList_.add( new NamedConstant( name, konst ) );
            return constantList_.size() - 1;
        }

        /* No luck. */
        return -1;
    }

    /**
     * Returns the index into this reader's list of known constants which
     * corresponds to a given name.  Special and normal constants are searched.
     * Matching is case insensitive.
     * <p>
     * Note this method is only called during expression compilation,
     * so it doesn't need to be particularly efficient.
     *
     * @param   name  constant name
     * @return  index into constants list, or -1 if no such constant was found
     */
    private int getConstantIndex( String name ) {

        /* See if it's a special. */
        int iconst = getSpecialIndex( name );
        if ( iconst >= 0 ) {
            return iconst;
        }
       
        /* If not search in normal constants. */
        Constant konst = getConstantByName( name );
        if ( konst != null ) {
            constantList_.add( new NamedConstant( name, konst ) );
            return constantList_.size() - 1;
        }

        /* No luck. */
        return -1;
    }

    /**
     * Returns the constant index which corresponds to the given name as a 
     * null query.
     *
     * @param  name  null-query identifier
     * @return  constant index for which <code>name</code> is a null-query
     *          or -1
     * @see  #getNullColumnIndex
     * @see  #getConstantIndex
     */
    private int getNullConstantIndex( String name ) {
        String constname = stripPrefix( name, NULL_QUERY_PREFIX );
        if ( constname != null ) {
            return getConstantIndex( constname );
        }
        return -1;
    }

    /**
     * Return the value of a constant with a given index.
     *
     * @param  iconst  constant index as returned by {@link #getConstantIndex}
     * @return  value as an Object
     */
    private Object getConstantValue( int iconst ) {
        return ((NamedConstant) constantList_.get( iconst )).getValue();
    }

    /**
     * Indicates whether the cell at the current row in a given column
     * has a blank value.  This is the case if the value is the
     * java <tt>null</tt> reference, or if it is a Float or Double
     * with a NaN value.
     *
     * @param  inul column index (as a <tt>long</tt>)
     * @return whether the cell is null
     */
    public boolean getBooleanProperty( long inul ) {
        return isBlank( (int) inul );
    }

    /*
     * Methods for returning the actual column values.  
     * These must be of the form 'getXXXProperty(int)', where XXX is one
     * of the strings returned by the getTypeName method.
     * 
     * Those methods which return primitives check explicitly for null
     * values and call {@link #foundNull} while returning a dummy value.
     * It would be a bit simpler for these methods to throw a 
     * NullPointerException instead, which would percolate up to be 
     * thrown from the evaluate method, but handling it like that
     * can cause a big performance hit (exceptions are expensive).
     */

    public boolean getBooleanProperty( int id ) {
        return id >= 0
             ? getBooleanColumnValue( id )
             : getBooleanValue( (Boolean) getConstantValue( -1 - id ) );
    }
    public byte getByteProperty( int id ) {
        return id >= 0
             ? getByteColumnValue( id )
             : getByteValue( (Byte) getConstantValue( -1 - id ) );
    }
    public char getCharProperty( int id ) {
        return id >= 0
             ? getCharColumnValue( id )
             : getCharValue( (Character) getConstantValue( -1 - id ) );
    }
    public short getShortProperty( int id ) {
        return id >= 0
             ? getShortColumnValue( id )
             : getShortValue( (Short) getConstantValue( -1 - id ) );
    }
    public int getIntProperty( int id ) {
        return id >= 0
             ? getIntColumnValue( id )
             : getIntValue( (Integer) getConstantValue( -1 - id ) );
    }
    public long getLongProperty( int id ) {
        return id >= 0
             ? getLongColumnValue( id )
             : getLongValue( (Long) getConstantValue( -1 - id ) );
    }
    public float getFloatProperty( int id ) {
        return id >= 0
             ? getFloatColumnValue( id )
             : getFloatValue( (Float) getConstantValue( -1 - id ) );
    }
    public double getDoubleProperty( int id ) {
        return id >= 0
             ? getDoubleColumnValue( id )
             : getDoubleValue( (Double) getConstantValue( -1 - id ) );
    }
    public Object getObjectProperty( int id ) {
        return id >= 0
             ? getObjectColumnValue( id )
             : getConstantValue( -1 - id );
    }
    public Number getNumberProperty( int id ) {
        return id >= 0
             ? (Number) getObjectColumnValue( id )
             : (Number) getConstantValue( -1 - id );
    }
    public String getStringProperty( int id ) {
        return id >= 0
             ? (String) getObjectColumnValue( id )
             : (String) getConstantValue( -1 - id );
    }
    public boolean[] getBooleanArrayProperty( int id ) {
        return id >= 0
             ? (boolean[]) getObjectColumnValue( id )
             : (boolean[]) getConstantValue( -1 - id );
    }
    public byte[] getByteArrayProperty( int id ) {
        return id >= 0
             ? (byte[]) getObjectColumnValue( id )
             : (byte[]) getConstantValue( -1 - id );
    }
    public char[] getCharArrayProperty( int id ) {
        return id >= 0
             ? (char[]) getObjectColumnValue( id )
             : (char[]) getConstantValue( -1 - id );
    }
    public short[] getShortArrayProperty( int id ) {
        return id >= 0
             ? (short[]) getObjectColumnValue( id )
             : (short[]) getConstantValue( -1 - id );
    }
    public int[] getIntArrayProperty( int id ) {
        return id >= 0
             ? (int[]) getObjectColumnValue( id )
             : (int[]) getConstantValue( -1 - id );
    }
    public float[] getFloatArrayProperty( int id ) {
        return id >= 0
             ? (float[]) getObjectColumnValue( id )
             : (float[]) getConstantValue( -1 - id );
    }
    public double[] getDoubleArrayProperty( int id ) {
        return id >= 0
             ? (double[]) getObjectColumnValue( id )
             : (double[]) getConstantValue( -1 - id );
    }
    public Object[] getObjectArrayProperty( int id ) {
        return id >= 0
             ? (Object[]) getObjectColumnValue( id )
             : (Object[]) getConstantValue( -1 - id );
    }
    public String[] getStringArrayProperty( int id ) {
        return id >= 0
             ? (String[]) getObjectColumnValue( id )
             : (String[]) getConstantValue( -1 - id );
    }
    public Date[] getDateArrayProperty( int id ) {
        return id >= 0
             ? (Date[]) getObjectColumnValue( id )
             : (Date[]) getConstantValue( -1 - id );
    }

    /*
     * Utility methods to turn wrapper types into primitive ones. 
     * Special handling is done for null values.
     */

    public boolean getBooleanValue( Boolean value ) {
        if ( value == null ) {
            foundNull();
            return false;
        }
        else {
            return value.booleanValue();
        }
    }
    public byte getByteValue( Byte value ) {
        if ( value == null ) {
            foundNull();
            return (byte) 0;
        }
        else {
            return value.byteValue();
        }
    }
    public char getCharValue( Character value ) {
        if ( value == null ) {
            foundNull();
            return (char) 0;
        }
        else {
            return value.charValue();
        }
    }
    public short getShortValue( Short value ) {
        if ( value == null ) {
            foundNull();
            return (short) 0;
        }
        else {
            return value.shortValue();
        }
    }
    public int getIntValue( Integer value ) {
        if ( value == null ) {
            foundNull();
            return 0;
        }
        else {
            return value.intValue();
        }
    }
    public long getLongValue( Long value ) {
        if ( value == null ) {
            foundNull();
            return 0L;
        }
        else {
            return value.longValue();
        }
    }
    public float getFloatValue( Float value ) {
        return value == null ? Float.NaN : value.floatValue();
    }
    public double getDoubleValue( Double value ) {
        return value == null ? Double.NaN : value.doubleValue();
    }

    /**
     * Returns the type name corresponding to a given class.
     * The significance of this return value is that it appears in
     * the names of the corresponding <tt>getXXXProperty</tt>
     * methods in this class.
     *
     * @param   name  the value class
     * @return  the corresponding method name fragment
     * @see   "JEL manual"
     */
    private static String getTypeName( Class clazz ) {
        if ( clazz.equals( Boolean.class ) ) {
            return "Boolean";
        }
        else if ( clazz.equals( boolean[].class ) ) {
            return "BooleanArray";
        }
        else if ( clazz.equals( Byte.class ) ) {
            return "Byte";
        }
        else if ( clazz.equals( byte[].class ) ) {
            return "ByteArray";
        }
        else if ( clazz.equals( Character.class ) ) {
            return "Char";
        }
        else if ( clazz.equals( char[].class ) ) {
            return "CharArray";
        }
        else if ( clazz.equals( Short.class ) ) {
            return "Short";
        }
        else if ( clazz.equals( short[].class ) ) {
            return "ShortArray";
        }
        else if ( clazz.equals( Integer.class ) ) {
            return "Int";
        }
        else if ( clazz.equals( int[].class ) ) {
            return "IntArray";
        }
        else if ( clazz.equals( Long.class ) ) {
            return "Long";
        }
        else if ( clazz.equals( long[].class ) ) {
            return "LongArray";
        }
        else if ( clazz.equals( Float.class ) ) {
            return "Float";
        }
        else if ( clazz.equals( float[].class ) ) {
            return "FloatArray";
        }
        else if ( clazz.equals( Double.class ) ) {
            return "Double";
        }
        else if ( clazz.equals( double[].class ) ) {
            return "DoubleArray";
        }
        else if ( clazz.equals( Number.class ) ) {
            return "Number";
        }
        else if ( clazz.equals( String.class ) ) {
            return "String";
        }
        else if ( clazz.equals( String[].class ) ) {
            return "StringArray";
        }
        else if ( clazz.equals( Date.class ) ) {
            return "Date";
        }
        else if ( clazz.equals( Date[].class ) ) {
            return "DateArray";
        }
        else if ( clazz.getComponentType() != null ) {
            return "ObjectArray";
        }
        else {
            return "Object";
        }
    }

    /**
     * Takes a token and strips a given prefix from it, returning the 
     * remainder.  If the given <code>name</code> does not begin with
     * <code>prefix</code> (or if it is exactly equal to it), 
     * then <code>null</code> is returned.
     *
     * @param    name  token which may begin with <code>prefix</code>
     * @param    prefix   maybe matches the start of <code>name</code>
     * @return   <code>name</code> minux <code>prefix</code>,
     *           or <code>null</code>
     * @see   #NULL_QUERY_PREFIX
     */
    public static String stripPrefix( String name, String prefix ) {
        if ( name != null &&
             name.length() > prefix.length() &&
             name.toLowerCase().startsWith( prefix.toLowerCase() ) ) {
            return name.substring( prefix.length() );
        }
        else {
            return null;
        }
    }

    /**
     * Utility class which associates a name with a Constant object.
     */
    private static class NamedConstant {
        private final String name_;
        private final Constant konst_;

        /**
         * Constructor.
         *
         * @param   name  key
         * @param   konst  constant object
         */
        NamedConstant( String name, Constant konst ) {
            name_ = name;
            konst_ = konst;
        }

        public String getName() {
            return name_;
        }

        public Class getContentClass() {
            return konst_.getContentClass();
        }

        public Object getValue() {
            return konst_.getValue();
        }
    }
}
