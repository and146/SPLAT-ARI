/*
 * Copyright (C) 2003 Central Laboratory of the Research Councils
 *
 *  History:
 *     12-JUL-2001 (Peter W. Draper):
 *       Original version.
 */
package uk.ac.starlink.frog.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import uk.ac.starlink.frog.util.FrogDebug;

import uk.ac.starlink.util.AsciiFileParser;

/**
 * This class provides utility methods for remote control based
 * access to FROG.
 *
 * @author Peter W. Draper
 * @version $Id$
 */
public class RemoteUtilities
{
  /**
     *  Application wide debug manager
     */
    protected FrogDebug debugManager = FrogDebug.getReference();

    /**
     *  Create an instance. Private all methods static.
     */
    private RemoteUtilities()
    {
        // Nothing to do.
    }

    /**
     * Write contact details to a file only readable by the owner
     * process and privileged local users.
     * <p>
     * These details are used to authenticate any requests.
     * <p>
     * The file contains the simple line:
     * <pre>
     *    hostname port_number cookie
     * </pre>
     * The cookie is generated by this method and returned as its
     * result.
     *
     * @param port the port number being used by a server process that
     *             is listening for connections.
     * @return the cookie for authenticating connections. This is null
     *         if a failure to write the contact file is encountered.
     *
     * @see RemoteServer
     */
    protected static String writeContactFile( int port )
    {
        String cookie = null;

        //  Open the contact file. This needs protection from prying
        //  eyes, so the next is _UNIX_ specific (TODO: something
        //  about this?).  The file is re-created every time, so
        //  connections are only available to the last instance.
        File contactFile = Utilities.getConfigFile( ".remote" );
        if ( contactFile != null ) {
            try {
                contactFile.createNewFile();
                Runtime.getRuntime().exec( "chmod 600 " +
                                           contactFile.getPath() );
            } 
            catch (Exception e) {
                // Do nothing, chmod can fail validly under Windows.
                //e.printStackTrace();
            }

            //  Add the information we want.
            try {
                PrintStream out =
                    new PrintStream( new FileOutputStream( contactFile ) );
                InetAddress addr = InetAddress.getLocalHost();
                String hexVal = Integer.toHexString((int)(Math.random()*12345));
                cookie = hexVal + addr.hashCode();
                out.println( addr.getHostName() + " " + port + " " + cookie );

            } 
            catch (Exception e) {
                //  Do nothing
            }
        }
        //  Return the cookie which should be used to authenticate any
        //  remote connections.
        return cookie;
    }

    /**
     * Parse the contact file returning its contents as an Object array.
     *
     * @return array of three Objects. These are really the hostname
     *         String, an Integer with the port number and a String
     *         containing the validation cookie. Returns null if not
     *         available.
     */
    public static Object[] readContactFile()
    {
        File contactFile = Utilities.getConfigFile( ".remote" );
        if ( contactFile == null || ! contactFile.canRead() ) {
            return null;
        }

        //  Ok file exists and we can read it, so open it and get the
        //  contents.
        AsciiFileParser reader = new AsciiFileParser( contactFile );
        String host = reader.getStringField( 0, 0 );
        Integer port = new Integer( reader.getIntegerField( 0, 1 ) );
        String cookie = reader.getStringField( 0, 2 );

        //  Construct the result.
        Object[] result = new Object[3];
        result[0] = host;
        result[1] = port;
        result[2] = cookie;
        return result;
    }

    /**
     * Find out if FROG is listening on the given hostname and port
     * and recognises the given cookie.
     *
     * @param contactDetails return from readContactFile.
     *
     * @return true if an instance of FROG was successfully contacted.
     */
    public static boolean isListening( Object[] contactDetails )
    {
        String hostname = (String) contactDetails[0];
        Integer port = (Integer) contactDetails[1];
        String cookie = (String) contactDetails[2];
        Socket socket = null;
        PrintWriter out = null;
        try {
            socket = new Socket( hostname, port.intValue() );
            out = new PrintWriter(socket.getOutputStream(), true);

            //  Send the cookie, this has no response, except closure
            //  on failure.
            out.println( cookie );
            out.close();
            socket.close();
            return true;
        } 
        catch (Exception e) {
            try {
                out.close();
                socket.close();
            } 
            catch (Exception ee) {
                // Do nothing.
            }
            return false;
        }
    }

    /**
     * Send a single beanshell command to a remote FROG beanshell
     * interpreter. Needs the hostname, port and cookie from the
     * contact file.
     *
     * @param contactDetails return from readContactFile.
     * @param command the beanshell command to execute.
     *
     * @exception Exception exception resulting from any network problems.
     * @return the string that returns from the command, null for an error.
     */
    public static String sendRemoteCommand( Object[] contactDetails,
                                            String command )
        throws Exception
    {
        String hostname = (String) contactDetails[0];
        Integer port = (Integer) contactDetails[1];
        String cookie = (String) contactDetails[2];
        String result = null;
        Socket socket = new Socket( hostname, port.intValue() );
        PrintWriter out = new PrintWriter( socket.getOutputStream(), true );

        //  Return messages are line-based.
        BufferedReader in = new BufferedReader
            ( new InputStreamReader( socket.getInputStream() ) );

        //  Send the cookie, this has no response, except closure on
        //  failure.
        out.println( cookie );
        if ( ! out.checkError() ) {

            out.println( command );
            String length = in.readLine();
            result = in.readLine();

            out.println( "bye" );
            length = in.readLine();
            String dummyValue = in.readLine();
        }
        out.close();
        socket.close();
        return result;
    }
}
