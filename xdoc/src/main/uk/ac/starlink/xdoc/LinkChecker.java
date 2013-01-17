package uk.ac.starlink.xdoc;

import java.io.File;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Checks an XHTML document to see that the links it references 
 * are valid URLs.
 *
 * @author    Mark Taylor (Starlink)
 */
public class LinkChecker {

    private final URL context;
    private final boolean attemptExternal;
    private int localFailures;
    private int extFailures;
    private Map urlNames = new HashMap();
    private static Pattern namePattern1 =
        Pattern.compile( "<a\\b[^>]*\\bname=['\"]([^'\"]*)['\"]",
                         Pattern.CASE_INSENSITIVE );
    private static Pattern namePattern2 =
        Pattern.compile( "<a\\s+name=(\\w+)>",
                         Pattern.CASE_INSENSITIVE );
    private int networkTimeout = 5;

    /**
     * Constructs a new LinkChecker with a given home context. 
     * This should be the URL of the document being checked, or at least
     * its directory, if relevant.  It is used for relative link
     * resolution. 
     *
     * @param  context  document context
     * @param  attemptExternal  true if you want to check external (http) 
     *         links; if false, only local ones will be checked
     */
    public LinkChecker( URL context, boolean attemptExternal ) {
        this.context = context;
        this.attemptExternal = attemptExternal;
    }

    /**
     * Checks whether a link string (representing an absolute URL or a URL 
     * relative to this checker's context) is valid or not.
     * If it's not, then a brief message to this effect will be logged,
     * and either the <tt>localFailures</tt> or <tt>extFailures</tt> 
     * counts will be incremented.
     *
     * @param   href  link string to check
     * @return  true iff href exists
     */
    private boolean checkURL( String href ) {
        int hashPos = href.indexOf( '#' );
        URL url;
        String frag;
        try {
            if ( hashPos < 0 ) {
                url = new URL( context, href );
                frag = null;
            }
            else {
                url = new URL( context, href.substring( 0, hashPos ) );
                frag = href.substring( hashPos + 1 );
            }
        }
        catch ( MalformedURLException e ) {
            logMessage( "Badly formed URL: " + href );
            localFailures++;
            return false;
        }

        boolean isExternal = ! "file".equals( url.getProtocol() );
        if ( isExternal && ! attemptExternal ) {
            return true;
        }
        else {
            boolean ok;
            try {
                if ( frag == null ) {
                    ok = checkUrlExists( url );
                }
                else {
                    ok = checkUrlContains( url, frag );
                }
             }
             catch ( IOException e ) {
                logMessage( "Connection failed: " + e );
                ok = false;
             }
             if ( ! ok ) {
                if ( isExternal ) {
                    extFailures++;
                    logMessage( "Bad remote link: " + href );
                }
                else {
                    logMessage( "Bad link: " + href );
                    localFailures++;
                }
            }
            return ok;
        }
    }

    /**
     * Checks whether a document at the named URL exists.
     *
     * @param  url  URL to check
     * @return  true  iff <tt>url</tt> exists
     */
    private boolean checkUrlExists( URL url ) throws IOException {
        if ( urlNames.containsKey( url ) ) {
            return true;
        }
        else {
            URLConnection conn = url.openConnection();
            boolean ok;
            if ( conn instanceof HttpURLConnection ) {
                HttpURLConnection hconn = (HttpURLConnection) conn;
                hconn.setRequestMethod( "HEAD" );
                URLConnection fconn = followRedirectsWithTimeout( hconn );
                if ( fconn instanceof HttpURLConnection ) {
                    int response =
                        ((HttpURLConnection) fconn).getResponseCode();
                    if ( response == HttpURLConnection.HTTP_OK ) {
                        ok = true;
                    }
                    else {
                        logMessage( "Response " + response + ": " + url );
                        ok = false;
                    }
                }
                else {
                    logMessage( "Non-HTTP redirect " + conn.getURL() );
                    ok = false;
                }
                fconn.getInputStream().close();
            }
            else {
                connectWithTimeout( conn );
                conn.getInputStream().close();
                ok = true;
            }
            if ( ok ) {
                urlNames.put( url, null );
            }
            return ok;
        }
    }

    /**
     * Checks whether a named URL apparently contains an &lt;a&gt; element
     * with the given <tt>name</tt> attribute.  The HTML parsing is not
     * foolproof, so it might miss a name which is there.
     *
     * @param  url  the URL 
     * @param  frag   the sought name attribute
     * @return  true  iff <tt>url</tt> exists and apparently contains
     *          the target <tt>frag</tt>
     */
    private boolean checkUrlContains( URL url, String frag )
            throws IOException {
        if ( urlNames.get( url ) == null ) {
            Set names = new HashSet();
            URLConnection conn = url.openConnection();
            if ( conn instanceof HttpURLConnection ) {
                ((HttpURLConnection) conn).setInstanceFollowRedirects( true );
            }
            connectWithTimeout( conn );
            BufferedReader strm = new BufferedReader( 
                new InputStreamReader( conn.getInputStream() ) );
            for ( String line; ( line = strm.readLine() ) != null; ) {
                for ( Matcher matcher = namePattern1.matcher( line );
                      matcher.find(); ) {
                    names.add( matcher.group( 1 ) );
                }
                for ( Matcher matcher = namePattern2.matcher( line );
                      matcher.find(); ) {
                    names.add( matcher.group( 1 ) );
                }
            }
            strm.close();
            urlNames.put( url, names );
        }
        return ((Collection) urlNames.get( url )).contains( frag );
    }

    /**
     * Checks the result of an XML transformation to see if the links
     * in the result are OK or not.
     *
     * @param  xsltSrc  source for the XSLT stylesheet which converts to
     *         HTML or an HTML-like output format
     * @param  xmlSrc   source for the XML document which will be
     *         transformed by <tt>xsltSrc</tt> to produce the HTML to test
     * @return  true  iff all the links in the resulting XHTML document
     *          can be successfully resolved
     */
    public boolean checkLinks( Source xsltSrc, Source xmlSrc ) 
            throws TransformerException, MalformedURLException {
        return checkLinks( xsltSrc, xmlSrc, null );
    }

    /**
     * Checks the result of an XML transformation to see if the links
     * in the result are OK or not, with an optional list of parameters.
     *
     * @param  xsltSrc  source for the XSLT stylesheet which converts to
     *         HTML or an HTML-like output format
     * @param  xmlSrc   source for the XML document which will be
     *         transformed by <tt>xsltSrc</tt> to produce the HTML to test
     * @param  params   stylesheet parameter map (or null)
     * @return  true  iff all the links in the resulting XHTML document
     *          can be successfully resolved
     */
    public boolean checkLinks( Source xsltSrc, Source xmlSrc, Map params )
            throws TransformerException, MalformedURLException {
        Transformer trans = TransformerFactory.newInstance()
                           .newTransformer( xsltSrc );

        if ( params != null ) {
            for ( Iterator it = params.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry entry = (Map.Entry) it.next();
                String name = (String) entry.getKey();
                String value = (String) entry.getValue();
                trans.setParameter( name, value );
            }
        }

        trans.setOutputProperty( OutputKeys.METHOD, "xml" );
        LinkCheckHandler handler = new LinkCheckHandler();
        trans.transform( xmlSrc, new SAXResult( handler ) );
        return handler.ok;
    }

    /**
     * Sets the network timeout used for retrieving URLs.
     *
     * @param  timeoutSecs  timeout in seconds
     */
    public void setTimeout( int timeoutSecs ) {
        networkTimeout = timeoutSecs;
    }

    /**
     * Returns the network timeout used for retrieving URLs.
     *
     * @return  timeout in seconds
     */
    public int getTimeout() {
        return networkTimeout;
    }

    /**
     * Returns the total number of local link resolution failures this
     * checker has come across.  Local ones are those which correspond
     * to hrefs representing relative URLs or file-type URLs.
     *
     * @return  total number of bad local links
     */
    public int getLocalFailures() {
        return localFailures;
    }

    /**
     * Returns the total number of external link resolution failures
     * this checker has come across.  External links are ones that
     * aren't local.
     *
     * @return  total number of bad non-local links
     * @see  #getLocalFailures
     */
    public int getExternalFailures() {
        return extFailures;
    }

    /**
     * Interface through which short messages about progress can be 
     * logged.
     *
     * @param  msg  message to log
     */
    protected void logMessage( String msg ) {
        System.out.println( msg );
    }

    /**
     * Opens a URLConnection with a timeout.  
     *
     * @param  conn  URL connection
     * @throws  IOException  if the connection times out
     */
    private void connectWithTimeout( URLConnection conn ) throws IOException {
        URLConnector connector = new URLConnector( conn );
        connector.start();
        long endTime = System.currentTimeMillis() + networkTimeout * 1000;
        synchronized ( connector ) {
            while ( System.currentTimeMillis() < endTime &&
                    ! connector.isConnected() ) { 
                try {
                    connector.wait( endTime - System.currentTimeMillis() + 1 );
                }
                catch ( InterruptedException e ) {
                    throw (IOException)
                          new IOException( "Connection interrupted: " +
                                           conn.getURL() )
                         .initCause( e );
                }
            }
            if ( ! connector.isConnected() ) {
                throw new IOException( "Connection timed out: " + 
                                       conn.getURL() );
            }
        }
    }

    /**
     * Takes a URLConnection and repeatedly follows 303 redirects
     * until a non-303 status is achieved.  Infinite loops are defended
     * against.
     *
     * @param  conn   initial URL connection 
     * @return   target URL connection
     *           (if no redirects, the same as <code>hconn</code>)
     */
    public URLConnection followRedirectsWithTimeout( URLConnection conn )
            throws IOException {
        if ( ! ( conn instanceof HttpURLConnection ) ) {
            return conn;
        }
        HttpURLConnection hconn = (HttpURLConnection) conn;
        String requestMethod = hconn.getRequestMethod();
        connectWithTimeout( hconn );
        Set urlSet = new HashSet<String>();
        urlSet.add( hconn.getURL() );
        for ( int code;
              ( isRedirectCode( code = hconn.getResponseCode() ) ); ) {
            URL url0 = hconn.getURL();
            String loc = hconn.getHeaderField( "Location" );
            if ( loc == null || loc.trim().length() == 0 ) {
                throw new IOException( "No Location field for " + code 
                                     + " response from " + url0 );
            }
            URL url1; 
            try {
                url1 = new URL( loc );
            }
            catch ( MalformedURLException e ) {
                throw (IOException)
                      new IOException( "Bad Location field for " + code
                                     + " response from " + url0 )
                     .initCause( e ); 
            }
            if ( ! urlSet.add( url1 ) ) {
                throw new IOException( "Recursive 3xx redirect at " + url1 );
            }
            URLConnection conn1 = url1.openConnection();
            if ( ! ( conn1 instanceof HttpURLConnection ) ) { 
                return conn1;
            }
            hconn = (HttpURLConnection) conn1;
            hconn.setRequestMethod( requestMethod );
            connectWithTimeout( hconn );
        }
        if ( hconn.getResponseCode() == 403 &&
             "HEAD".equals( hconn.getRequestMethod() ) ) {
            return followRedirectsWithTimeout( conn.getURL().openConnection() );
        }
        return hconn;
    }

    /**
     * Indicate whether an HTTP response code indicates an HTTP redirect
     * that should contain a Location header.
     *
     * @param  response code
     * @return true for redircts
     */
    private static boolean isRedirectCode( int code ) {
        return code == 301
            || code == 302
            || code == 303
            || code == 307;
    }

    /**
     * Checks the links of the result of a given transformation to XHTML
     * (or an HTML-like result).
     * For any link which fails to resolve correctly in the transformation
     * result, a short warning message is output during processing.
     * At the end, a summary of any bad links is also output.
     * There will be an error status exit (1) if any of the <em>local</em>
     * links fail to resolve; if the only bad links are ones corresponding
     * to non-local (hrefs than don't start with "#" or "file:") then
     * although warnings are logged, the exit status is zero.
     * <p>
     * Usage:  LinkChecker stylesheet xmldoc
     *
     * @param  args  arguments
     */
    public static void main( String[] args ) 
            throws MalformedURLException, TransformerException {
        String usage = "Usage: LinkChecker [-noext] [-param name value ...] "
                     + "stylesheet [xmldoc]";

        /* Process flags. */
        List argList = new ArrayList( Arrays.asList( args ) );
        Map params = new HashMap();
        boolean attemptExternal = true;
        while ( argList.size() > 0 &&
                ((String) argList.get( 0 )).startsWith( "-" ) ) {
            String flag = (String) argList.remove( 0 );
            if ( flag.startsWith( "-param" ) && argList.size() >= 2 ) {
                params.put( argList.remove( 0 ), argList.remove( 0 ) );
            }
            else if ( flag.startsWith( "-noext" ) ) {
                attemptExternal = false;
            }
            else {
                System.err.println( usage );
                System.exit( 1 );
            }
        }

        /* Check arguments. */
        if ( argList.size() < 1 || argList.size() > 2 ) {
            System.err.println( usage );
            System.exit( 1 );
        }

        /* Get stylesheet source. */
        File stylesheet = new File( (String) argList.get( 0 ) );
        if ( ! stylesheet.isFile() ) {
            System.err.println( "No stylesheet " + stylesheet );
            System.exit( 1 );
        }
        Source styleSrc = new StreamSource( stylesheet );

        /* Get document source. */
        Source docSrc;
        if ( args.length > 1 ) {
            docSrc = new StreamSource( (String) argList.get( 1 ) );
        }
        else {
            docSrc = new StreamSource( System.in );
        }

        try {
            LinkChecker checker =
                new LinkChecker( new File( "." ).toURL(), attemptExternal );
            boolean ok = checker.checkLinks( styleSrc, docSrc, params );
            int localFailures = checker.getLocalFailures();
            int extFailures = checker.getExternalFailures();
            if ( extFailures > 0 ) {
                System.out.println( extFailures +
                                    " external link resolution errors" );
            }
            if ( localFailures > 0  ) {
                System.out.println( localFailures + 
                                    " local link resolution errors" );
            }
            System.exit( localFailures == 0 ? 0 : 1 );
        }
        catch ( TransformerException e ) {
            System.err.println( e );
            System.exit( 1 );
        }
        catch ( MalformedURLException e ) {
            System.err.println( e );
            System.exit( 1 );
        }
    }

    /**
     * Helper class which defines a thread that tries to open a URLConnection.
     * Construct a URLConnector for a URLConnection which is not already
     * open, call {@link #start} on it, and call {@link #isConnected}
     * later to see if the connection has been established.  If the
     * connection attempt succeeds, or if it fails with an exception,
     * a {@link #notifyAll} will be called on this object.
     */
    private final class URLConnector extends Thread {
        final URLConnection connection;
        boolean connected;
        IOException connectException;

        /**
         * Constructs a new URLConnector that will try to open a given
         * connection.
         *
         * @param  connection  connection to open
         */
        URLConnector( URLConnection connection ) {
            this.connection = connection;
        }

        /**
         * Attempts to connect, and calls {@link #notifyAll} when the attempt
         * has finished in success or failure.
         */
        public void run() {
            try {
                connection.connect();

                if ( connection instanceof HttpURLConnection ) {
                    HttpURLConnection hconn = (HttpURLConnection) connection;
                    hconn.getResponseCode(); // can also be time-consuming
                }
                synchronized ( this ) {
                    connected = true;
                    notifyAll();
                }
            }
            catch ( IOException e ) {
                synchronized ( this ) {
                    connectException = e;
                    notifyAll();
                }
            }
        }

        /**
         * Indicates whether the connection has been opened yet.
         * There are three possibilities:
         * <ol>
         * <li>false return - still trying
         * <li>true return - connection has been opened successfully
         * <li>IOException - attempt failed (at some time in the past), given up
         * </ol>
         *
         * @return  true iff the connection has been opened
         * @throws  IOException  if the attempt failed
         */
        public synchronized boolean isConnected() throws IOException {
            if ( connectException != null ) {
                throw connectException;
            }
            else {
                return connected;
            }
        }
    }


    /**
     * An instance of this class processes the XHTML as a SAX stream,
     * performing callbacks when it comes across events which correspond
     * to interesting elements in the document.
     */
    private class LinkCheckHandler extends DefaultHandler {

        boolean ok = true;
        private Set namesSeen = new HashSet();
        private List namesReferenced = new ArrayList();

        public void startElement( String namespaceURI, String localName,
                                  String qName, Attributes atts )
                throws SAXException {

            if ( qName.equalsIgnoreCase( "a" ) ) {
                String name = atts.getValue( "name" );
                if ( name != null ) {
                    namesSeen.add( name );
                }
                String href = atts.getValue( "href" );
                if ( href != null ) {
                    if ( href.startsWith( "#" ) ) {
                        namesReferenced.add( href.substring( 1 ) );
                    }
                    else if ( href.startsWith( "mailto:" ) ) {
                        // ignore
                    }
                    else {
                        if ( ! checkURL( href ) ) {
                            ok = false;
                        }
                    }
                }
            }
            else if ( qName.equalsIgnoreCase( "img" ) ) {
                String src = atts.getValue( "src" );
                if ( src != null ) {
                    if ( ! checkURL( src ) ) {
                        ok = false;
                    }
                }
            }
        }

        public void endDocument() throws SAXException {
            for ( Iterator it = namesReferenced.iterator(); it.hasNext(); ) {
                String name = (String) it.next();
                if ( ! namesSeen.contains( name ) ) {
                    localFailures++;
                    ok = false;
                    logMessage( "Bad link: #" + name );
                }
            }
        }
    }
}
