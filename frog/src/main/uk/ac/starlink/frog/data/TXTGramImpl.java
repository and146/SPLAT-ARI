package uk.ac.starlink.frog.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

import uk.ac.starlink.ast.Frame;
import uk.ac.starlink.ast.FrameSet;
import uk.ac.starlink.ast.LutMap;
import uk.ac.starlink.frog.util.FrogException;
import uk.ac.starlink.frog.util.FrogDebug;

/**
 *  This class provides an implementation of GramImpl to access
 *  periodograms stored in text files.
 *
 * @author Alasdair Allan
 * @version $Id$
 * @since $Date$
 * @since 01-SEP-2000
 */

public class TXTGramImpl extends GramImpl 
{
   /**
     *  Application wide debug manager
     */
    protected FrogDebug debugManager = FrogDebug.getReference();
    
//
// Implementation of abstract methods.
//
    /**
     * Create an object by opening a text file and reading its
     * content. 
     *
     * @param fileName the name of the text file.
     */
    public TXTGramImpl( String fileName ) throws FrogException
    {
        super( fileName );
        
        debugManager.print("              TXTGramImpl(" + fileName + ")");
        this.fullName = fileName;
        this.shortName = fileName;
        try {
           readFromFile( fileName );
        } catch(FrogException e) {
           throw e;
        }   
    }

    /**
     * Create an object by reading values from an existing Gram
     * object. The text file is associated (so can be a save target),
     * but not opened.
     *
     * @param fileName the name of the text file.
     */
    public TXTGramImpl( String fileName, Gram source )
    {
        super( fileName, source );
        this.fullName = fileName;
        this.shortName = fileName;
        data = source.getYData();
        coords = source.getXData();
        errors = source.getYDataErrors();
        
        // Need to copy the FrameSet or we'll annull it on saving the
        // new Gram to disk and brake the original source object
        FrameSet tmpFrame =  source.getAst().getRef();
        astref = (FrameSet) tmpFrame.copy();
    }

    /**
     * Return a copy of the gram data values.
     *
     * @return reference to the gram data values.
     */
    public double[] getData()
    {
        return data;
    }
     
   /**
     * Return a copy of the gram coord values.
     *
     * @return reference to the gram coord values.
     */
    public double[] getTime()
    {
        return coords;
    }
    
    /**
     * Return a copy of the gram data errors.
     *
     * @return reference to the gram data values.
     */
    public double[] getDataErrors()
    {
        return errors;
    }

    /**
     * Return a symbolic name.
     *
     * @return a symbolic name for the gram. For text files this
     * is based on the file name.
     */
    public String getShortName()
    {
        return shortName;
    }

    /**
     * Return the full name. This is the filename.
     *
     * @return the file name containing the gram.
     */
    public String getFullName()
    {
        return fullName;
    }

    /**
     * Return the data array dimensionality (always length of
     * gram).
     *
     * @return integer array of size 1 returning the number of data
     *                 values available.
     */
    public int[] getDims()
    {
        int dummy[] = new int[1];
        dummy[0] = data.length;
        return dummy;
    }

    /**
     * Return reference to AST frameset.
     *
     * @return reference to a raw AST frameset.
     */
    public FrameSet getAst()
    {
        return astref;
    }

    /**
     * Return the data format.
     */
    public String getDataFormat()
    {
        return "TEXT";
    }

    /**
     * Save the gram to disk-file.
     */
    public void save() throws FrogException
    {
        saveToFile( fullName );
    }

//
// Implementation specific methods and variables.
//

    /**
     * Reference to the file.
     */
    File file = null;

    /**
     * Reference to text file coordinates.
     */
    protected double[] coords = null;

    /**
     * Reference to text file data values.
     */
    protected double[] data = null;

    /**
     * Reference to text file data errors.
     */
    protected double[] errors = null;

    /**
     * Default symbolic name.
     */
    protected String shortName = "Text file";

    /**
     * File name.
     */
    protected String fullName;

    /**
     * Reference to AST frameset.
     */
    protected FrameSet astref = null;

    /**
     * Finalise object. Free any resources associated with member
     * variables.
     */
    protected void finalize() throws Throwable
    {
        coords = null;
        data = null;
        errors = null;
        data = null;
        file = null;
        super.finalize();
    }

    /**
     * Open an existing text file and read the contents.
     *
     * @param fileName diskfile name of the text file.
     */
    protected void readFromFile( String fileName ) throws FrogException
    {
        //  Check file exists.
        file = new File( fileName );
        if ( ! file.exists() && file.canRead() && file.isFile() ) {
            file = null;
            return;
        }
        try {
           readData( file );
        } catch( FrogException e ) {
        
           throw e;
        }
           
    }

    /**
     * Open an new text file and write data as its contents.
     *
     * @param fileName diskfile name of the text file.
     */
    protected void saveToFile( String fileName )
    {
        // If file exists, then we need to be able to overwrite it.
        file = new File( fileName );
        if ( file.exists() && file.isFile() && ! file.canWrite() ) {
            file = null;
            return;
        }
        writeData( file );
    }

    /**
     * Read in the data from the file.
     *
     * @param file File object.
     */
    protected void readData( File file ) throws FrogException
    {
        debugManager.print("                TXTGramImp readData()");

        //  Get a BufferedReader to read the file line-by-line. Note
        //  we are avoiding using StreamTokenizer directly, and doing
        //  our own parsing, as this doesn't deal with floating point
        //  values very well.
        FileInputStream f = null;
        BufferedReader r = null;
        try {
            f = new FileInputStream( file );
            r = new BufferedReader( new InputStreamReader( f ) );
        } catch ( Exception e ) {
            e.printStackTrace();
            return;
        }

        //  Storage of all values go into ArrayList vectors, until we
        //  know the exact sizes required.
        ArrayList[] vec = new ArrayList[3];
        vec[0] = new ArrayList();
        vec[1] = new ArrayList();
        vec[2] = new ArrayList();

        //  Read file input until end of file occurs.
        String raw = null;
        String clean = null;
        int nlines = 0;
        int nwords = 0;
        try {
            while ( ( raw = r.readLine() ) != null ) {

                //  Skip blank and comment lines.
                if ( raw.length() == 0 || raw.charAt(0) == '!' ||
                     raw.charAt(0) == '#' ) {
                    continue;
                    //  TODO: restore shortname etc?
                } else {

                    // Read at least two floating numbers from line
                    // and no more than 3.
                    StringTokenizer st = new StringTokenizer( raw );
                    int count = Math.min( st.countTokens(), 3 );
                    nwords = Math.max( count, nwords );
                    
                    for ( int i = 0; i < count; i++ ) {
                        vec[i].add( new Double( st.nextToken() ) );
                    }
                    for ( int i = count; i < 3; i++ ) {
                        vec[i].add( new Double( 0.0 ) );
                    }
                    nlines++;
                }
            }
        } catch ( IOException e ) {
            e.printStackTrace();
            try {
                r.close();
                f.close();
            } catch ( Exception ex ) {
                ex.printStackTrace();
            }
            return;
        }
        try {
            r.close();
            f.close();
        } catch (Exception e) {
            //  Do nothing.
        }

        //  Create memory needed to store these coordinates.
        data = new double[nlines];
        coords = new double[nlines];
        if ( nwords == 3 ) {
            errors = new double[nlines];
        }

        //  Now copy data into arrays and record the data range.
        try {
          if ( nwords == 3 ) {
              for ( int i = 0; i < nlines; i++ ) {
                  coords[i] = ((Double)vec[0].get(i)).doubleValue();
                  data[i] = ((Double)vec[1].get(i)).doubleValue();
                  errors[i] = ((Double)vec[2].get(i)).doubleValue();
              }
          } else {
              for ( int i = 0; i < nlines; i++ ) {
                  coords[i] = ((Double)vec[0].get(i)).doubleValue();
                  data[i] = ((Double)vec[1].get(i)).doubleValue();
              }
          }

          debugManager.print( "                Inital Data Read");
          debugManager.print( "                ----------------");
          debugManager.print( "                coords.length = " +
                              coords.length);

          if (nwords == 3 ) {
             for ( int i = 0; i < coords.length; i++ ) {
               debugManager.print( "                " + i + ": " + 
                    coords[i] + "    " + data[i]  + "    " + errors[i] );
             }       
          } else {
             for ( int i = 0; i < coords.length; i++ ) {
               debugManager.print( "                " + i + ": " + 
                                   coords[i] + "    " + data[i] );         
          
             }
          }   
          
        } catch (Exception e ) {
            debugManager.print( 
               "                Problem reading " + file );
            debugManager.print( 
               "                Throwing FrogException..." );   
               
            FrogException fe = new FrogException( 
                               "Problem reading data from " + file );
            fe.initCause( e );
            throw fe;  
        }
        
        //  Create the AST frameset that describes the data-coordinate
        //  relationship.
       createAst();
    }

    /**
     * Create an AST frameset that relates the gram coordinates to
     * data value positions.
     */
    protected void createAst()
    {
        debugManager.print("                createAst()");

        //  Create two simple frames, one for the indices of the data
        //  counts and one for the coordinates (timestamp).
        Frame baseframe = new Frame( 1 );
        baseframe.set( "Label(1)=Power" );
        Frame currentframe = new Frame( 1 );
        currentframe.set( "Label(1)=Frequency" );

        //  Create an AST lutmap that relates the index of the data
        //  counts to the coordinates.

        LutMap lutmap = new LutMap( coords, 1.0, 1.0 );

        //  Now create a frameset and add all these to it.
        astref = new FrameSet( baseframe );
        astref.addFrame( 1, lutmap, currentframe );
    }

    /**
     * Write gram data to the file.
     *
     * @param file File object.
     */
    protected void writeData( File file )
    {
        //  Get a BufferedWriter to write the file line-by-line.
        FileOutputStream f = null;
        BufferedWriter r = null;
        try {
            f = new FileOutputStream( file );
            r = new BufferedWriter( new OutputStreamWriter( f ) );
        } catch ( Exception e ) {
            e.printStackTrace();
            return;
        }


        // Add a header to the file.
        try {
            r.write( "# File created by Frog \n" );
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Now write the data.
        if ( errors == null ) {
            for ( int i = 0; i < data.length; i++ ) {
                try { 
                    r.write( coords[i] + " " + data[i] + "\n" );
                    r.newLine();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            for ( int i = 0; i < data.length; i++ ) {
                try {
                    r.write(coords[i] + " " + data[i] + " " + errors[i] + "\n");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            r.newLine();
            r.close();
            f.close();
        } catch (Exception e) {
            //  Do nothing.
        }
    }
}
