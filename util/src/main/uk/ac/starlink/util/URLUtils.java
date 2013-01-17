package uk.ac.starlink.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLStreamHandlerFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides convenience methods for resolving URLs.
 * This class provides some static methods for turning strings into URLs.
 * This tends to be a bit of a pain in java, since you have to watch
 * out for MalformedURLExceptions all over and work out what the
 * context is.  The methods provided here assume that if a string 
 * looks like a URL it is one, if it doesn't it's a file name, and
 * if it's not absolute or resolved against a given context 
 * it is relative to the current directory.
 * From the point of view of a user providing text to an application,
 * or an XML document providing an href, this is nearly always 
 * what is wanted.  The strategy can lead to surprising situations
 * in the case that wacky URL protocols are used; for instance if 
 * <tt>makeURL</tt> is called on the string "gftp://host/file" and
 * no gftp handler is installed, it will be interpreted as a file-protocol
 * URL referring to the (presumably non-existent) file "gftp://host/file".
 * In this case the eventual upshot will presumably be a file-not-found
 * type error rather than a MalformedURLException type error getting
 * presented to the user.  Users of this class should be of the opinion
 * that this is not a particularly bad thing.
 * <p>
 * The <tt>systemId</tt> strings used by {@link javax.xml.transform.Source}s
 * have similar semantics to the strings which this class converts
 * to URLs or contexts.
 * <p>
 * This class assumes that the "file:" protocol is legal for URLs, 
 * and will throw AssertionErrors if this turns out not to be the case.
 * 
 * @author   Mark Taylor (Starlink)
 * @author   Norman Gray (Starlink)
 */
public class URLUtils {

    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.util" );

    /* Set up a URL representing the default context (the current directory). */
    private static URL defaultContext;
    static {
        try {
            defaultContext = new URL( "file:." );
        }
        catch ( MalformedURLException e ) {
            throw protestFileProtocolIsLegal( e );
        }
        catch ( SecurityException e ) {
            defaultContext = null;
        }
    }
    private static final Pattern FILE_URL_REGEX =
        Pattern.compile( "(file:)(/*)(.*)" );

    /**
     * Private constructor prevents instantiation.
     */
    private URLUtils() {
    }

    /**
     * Obtains a URL from a string.  If the String has the form of a URL,
     * it is turned directly into a URL.  If it does not, it is treated as
     * a filename, and turned into a file-protocol URL.  In the latter
     * case a relative or absolute filename may be used.  If it is 
     * null or a blank string (or something else equally un-filename like?)
     * then null is returned.
     *
     * @param   location   a string representing the location of a resource
     * @return  a URL representing the location of the resource
     */
    public static URL makeURL( String location ) {
        if ( location == null || location.trim().length() == 0 ) {
            return null;
        }
        try {
            return new URL( location );
        }
        catch ( MalformedURLException e ) {
            try {
                URI uri = new File( location ).toURI();
                return uri.toURL();
                //return new URL( uri.toString() );
            }
            catch ( MalformedURLException e2 ) {
                throw protestFileProtocolIsLegal( e2 );
            }
        }
        catch ( SecurityException e ) {
            try {
                return new URL( "file:" + location );
            }
            catch ( MalformedURLException e2 ) {
                throw protestFileProtocolIsLegal( e2 );
            }
        }
    }

    /**
     * Obtains a URL from a string in a given context.
     * The string <tt>context</tt> is turned into a URL as per 
     * the {@link #makeURL(String)} method, unless it is null or
     * the empty string, in which case it is treated as a reference
     * to the current directory.
     * The string <tt>location</tt> is then turned into a URL in
     * the same way as using {@link #makeURL(String)}, except that
     * if it represents a relative path it is resolved in the context
     * of <tt>context</tt>, taking its protocol and/or relative position
     * from it.
     *
     * @param   context   a string representing the context within which
     *                    <tt>location</tt> is to be resolved
     * @param   location   a string representing the location of a resource
     * @return  a URL representing the location of the resource
     */
    public static URL makeURL( String context, String location ) {
        URL contextURL;
        if ( context == null || context.trim().length() == 0 ) {
            contextURL = defaultContext;
        }
        else {
            contextURL = makeURL( context );
        }
        try {
            return new URL( contextURL, location );
        }
        catch ( MalformedURLException e ) {
            try {
                return new URL( contextURL, makeURL( location ).toString() );
            }
            catch ( MalformedURLException e2 ) {
                // can this happen??
                return makeURL( location );
            }
        }
    }

    /**
     * Returns an Error which can be thrown when you can't make a URL even
     * though you know you're using the "file:" protocol.  Although this
     * is permitted by the URL class, we consider ourselves to be on
     * an irretrievably broken system if it happens.
     */
    private static AssertionError 
            protestFileProtocolIsLegal( MalformedURLException e ) {
        AssertionError ae = 
            new AssertionError( "Illegal \"file:\" protocol in URL??" );
        ae.initCause( e );
        return ae;
    }

    /**
     * Turns a URL into a URI.
     *
     * <p>Since URIs are syntactically and semantically a superset of
     * URLs, this conversion should not cause any errors.  If,
     * however, the input URL is malformed in rather extreme ways,
     * then the URI construction will fail.  These ways include (but
     * are not necesssarily limited to) the features discussed in
     * {@link java.net.URI#URI(String,String,String,String,String)},
     * namely that a scheme is present, but with a relative path, or
     * that it has a registry-based authority part.
     *
     * <p>Because of the way the class does the conversion, the method
     * will itself resolve some malformations of URLs.  You should not rely
     * on this, however, firstly because the method might in principle
     * change, but mostly because you should avoid creating such
     * malformed URLs in the first place.
     *
     * <p>The most common source of malformed URLs is that of
     * <code>file</code> URLs which have inadequately escaped
     * (windows) drive letters or spaces in the name: such URLs should
     * be constructed using the {@link java.io.File#toURI} or {@link
     * java.io.File#toURL} methods.  Such URLs will be escaped by
     * this method.
     *
     * @param url a URL to be converted.  If this is null, then the
     *        method returns null 
     * @return the input URL as a URI, or null if the input was null
     * @throws MalformedURLException if the URI cannot be constructed
     *        because the input URL turns out to be malformed
     */
    public static URI urlToUri( URL url ) 
            throws MalformedURLException {
        /*
         * Weaknesses: this method doesn't cope with URIs which have
         * a scheme plus a relative path, or registry-based authorities.
         * Ought it to?  In the absence of specific use-cases,
         * probably not, but we should note that this might be
         * reasonable and be prepared to revisit it.
         */

        if (url == null)
            return null;

        try {
            return new URI(url.getProtocol(),
                           url.getAuthority(),
                           url.getPath(),
                           url.getQuery(),
                           url.getRef() // ie, fragment
                           );
        } catch (java.net.URISyntaxException e) {
            // The input URL was malformed, so indicate that
            MalformedURLException newEx
                    = new MalformedURLException("URL " + url
                                                + " was malformed");
            newEx.initCause(e);
            throw newEx;
        }
    }

    /**
     * Constructs a legal URL for a given File.
     * Unlike java, this gives you a URL which conforms to RFC1738 and
     * looks like "<code>file://localhost/abs-path</code>" rather than 
     * "<code>file:abs-or-rel-path</code>".
     *
     * @param   file   file
     * @return   URL
     * @see   "RFC 1738"
     */
    public static URL makeFileURL( File file ) {
        try {
            return fixURL( file.toURI().toURL() );
        }
        catch ( MalformedURLException e ) {
            throw new AssertionError();
        }
    }

    /**
     * Fixes file: URLs which don't have enough slashes in them.
     * Java generates invalid URLs of the form 
     * "<code>file:abs-or-rel-path</code>"
     * when it should generate "<code>file://localhost/abs-path</code>".
     *
     * @param   url  input URL
     * @return  fixed URL
     * @see   "RFC 1738"
     */
    public static URL fixURL( URL url ) {
        Matcher matcher = FILE_URL_REGEX.matcher( url.toString() );
        if ( matcher.matches() ) {
            String scheme = matcher.group( 1 );
            String slashes = matcher.group( 2 );
            String path = matcher.group( 3 );
            assert "file:".equals( scheme );
            try {
                switch ( slashes.length() ) {
                    case 0:
                        return fixURL( new File( path ).getAbsoluteFile()
                                                       .toURI().toURL() );
                    case 1:
                        return new URL( scheme + "//localhost" 
                                               + slashes + path );
                    case 2:
                        return url;
                    default:
                        return url;
                }
            }
            catch ( MalformedURLException e ) {
                throw new AssertionError( e );
            }
        }
        else {
            return url;
        }
    }

    /**
     * Attempts to determine whether two URLs refer to the same resource.
     * Not likely to be foolproof, but slightly smarter than using 
     * <code>equals</code>.
     *
     * @param  url1  first URL
     * @param  url2  second URL
     * @return   true if <code>url1</code> and <code>url2</code> appear to
     *           refer to the same resource
     */
    public static boolean sameResource( URL url1, URL url2 ) {
        if ( url1 == null && url2 == null ) {
            return true;
        }
        else if ( url1 == null || url2 == null ) {
            return false;
        }
        else if ( url1.equals( url2 ) ) {
            return true;
        }
        else if ( url1.getProtocol().equals( "file" ) &&
                  url2.getProtocol().equals( "file" ) ) {
            String[] strings = { url1.toString(), url2.toString() };
            for ( int i = 0; i < 2; i++ ) {
                strings[ i ] =
                    strings[ i ].replaceFirst( "^file:/*(localhost)?/*", "" );
            }
            return strings[ 0 ].equals( strings[ 1 ] );
        }
        else {
            return false;
        }
    }

    /**
     * Locates the local file, if any, represented by a URL.
     * If the URL string uses the "file:" protocol, and has no query or anchor
     * parts, the filename will be extracted and the corresponding file
     * returned.  Otherwise, null is returned.
     *
     * @param   url  URL string
     * @return   local file referenced by <code>url</code>, or null
     */
    public static File urlToFile( String url ) {
        URL u;
        try {
            u = new URL( url );
        }
        catch ( MalformedURLException e ) {
            return null;
        }
        if ( u.getProtocol().equals( "file" ) && u.getRef() == null
                                              && u.getQuery() == null ) {
            String path = u.getPath();
            try {

                /* Careful.  URLDecoder does almost what we want, but not
                 * quite - it replaces "+" with " ", which is appropriate
                 * for application/x-www-form-urlencoded, but not for
                 * normal URL decoding (RFC1738).  The %xy decoding is
                 * as required.  So by converting + to its hex-escaped
                 * form before using URLDecoder we get what we need. 
                 * An alternative would be to do the hex decoding by hand. */
                path = URLDecoder.decode( path.replace( "+", "%2B" ) );
            }
            catch ( IllegalArgumentException e ) {
                // probably a badly-formed URL - try with the undecoded form
            }
            String filename = File.separatorChar == '/'
                            ? path
                            : path.replace( '/', File.separatorChar );
            return new File( filename );
        }
        else {
            return null;
        }
    }

    /**
     * Attempts to install additional URL protocol handlers suitable 
     * for astronomy applications.  Currently installs handlers which 
     * can supply MySpace connections using either "<code>ivo:</code>" or 
     * "<code>myspace:</code>" protocols.
     */
    public static void installCustomHandlers() {

        /* See if the system property which customises URL protocol handling
         * has been set.  If so, don't attempt to mess about further with
         * the configuration. */
        String pkgProp = "java.protocol.handler.pkgs";
        boolean hasPkgProp;
        try {
            hasPkgProp = System.getProperty( pkgProp, "" ).length() > 0;
        }
        catch ( SecurityException e ) {
            hasPkgProp = false;
        }
        if ( hasPkgProp ) {
            logger_.config( pkgProp + " is set - don't further configure " 
                          + "URL protocol handlers" );
            return;
        }

        /* Set up a handler factory which deals with myspace.  This is
         * equivalent to setting the java.protocol.handler.pkgs system
         * property to "uk.ac.starlink.astrogrid.protocols", but the 
         * latter can only be done before starting up the JVM. */
        Map handlerMap = new HashMap();
        String[] protos = new String[] { "ivo", "myspace", };
        for ( int i = 0; i < protos.length; i++ ) {
            String proto = protos[ i ];
            handlerMap.put( proto,
                            "uk.ac.starlink.astrogrid.protocols."
                            + proto + ".Handler" );
        }
        URLStreamHandlerFactory fact = 
            new CustomURLStreamHandlerFactory( handlerMap );

        /* Attempt to install the custom handler. */
        try {
            URL.setURLStreamHandlerFactory( fact );
            logger_.config( "Set up URL custom protocol handlers " +
                            Arrays.asList( protos ) );
        }
        catch ( Throwable e ) {
            logger_.warning( "Can't set custom URL protocol handlers: " + e );
        }
    }
}
