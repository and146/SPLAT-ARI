package uk.ac.starlink.ttools.votlint;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.xml.sax.Locator;
import uk.ac.starlink.votable.VOTableVersion;

/**
 * Context for a VOTLint process.  This is the object which knows most of
 * the available global information about the parse.  It also provides
 * facilities for reporting log information about the parse to the user.
 * 
 * @author   Mark Taylor (Starlink)
 */
public class VotLintContext {

    private final VOTableVersion version_;
    private final boolean validate_;
    private final boolean debug_;
    private final PrintStream out_;
    private final Map idMap_;
    private final Map refMap_;
    private final Map msgMap_;
    private final Map namespaceMap_;
    private Locator locator_;
    private int errCount_;

    /** Maximum number of identical error messages which will be logged. */
    public final static int MAX_REPEAT = 4;

    /**
     * Constructor.
     *
     * @param  version  version of VOTable for which the parse will be done
     * @param  validate  if true, validation will be performed against
     *                   the appropriate DTD/schema
     * @param  debug  if true, a stack trace will be output with each
     *                log message
     * @param  out    output stream to which messages will be written
     */
    public VotLintContext( VOTableVersion version, boolean validate,
                           boolean debug, PrintStream out ) {
        version_ = version;
        validate_ = validate;
        debug_ = debug;
        out_ = out;
        idMap_ = new HashMap();
        refMap_ = new HashMap();
        msgMap_ = new HashMap();
        namespaceMap_ = new HashMap();
    }

    /**
     * Returns the version of VOTable this context is parsing.
     *
     * @return   version object
     */
    public VOTableVersion getVersion() {
        return version_;
    }

    /**
     * Indicates whether this lint is validating.
     *
     * @return   true for validating lint
     */
    public boolean isValidating() {
        return validate_;
    }

    /**
     * Returns whether we are in debug mode.
     *
     * @return   true if debugging output is on
     */
    public boolean isDebug() {
        return debug_;
    }

    /**
     * Sets the SAX document locator for this parse.
     *
     * @param  locator   locator
     */
    public void setLocator( Locator locator ) {
        locator_ = locator;
    }

    /**
     * Returns the SAX document locator for this parse.
     *
     * @return   locator
     */
    public Locator getLocator() {
        return locator_;
    }

    /**
     * Returns the output stream to which messages will be written.
     *
     * @return   output stream
     */
    public PrintStream getOutput() {
        return out_;
    }

    /**
     * Returns prefix->namespaceURI map for the xmlns namespaces currently
     * in scope.
     */
    public Map getNamespaceMap() {
        return namespaceMap_;
    }

    /**
     * Register the fact that an XML ID-type attribute has been seen on an
     * element.
     *
     * @param   id  ID value
     * @param   handler  element labelled <tt>id</tt>
     */
    public void registerID( String id, ElementHandler handler ) {

        /* Check this one isn't already taken. */
        if ( idMap_.containsKey( id ) ) {
            ElementRef ref = (ElementRef) idMap_.get( id );
            error( "ID " + id + " already defined " + ref );
        }

        /* If not, keep a record of it. */
        else {
            idMap_.put( id, handler.getRef() );
        }

        /* If we've seen a reference to this one already, process the
         * link now and remove it from the pending list. */
        if ( refMap_.containsKey( id ) ) {
            UncheckedReference unref = 
                (UncheckedReference) refMap_.remove( id );
            ElementRef to = (ElementRef) idMap_.get( id );
            unref.checkLink( to );
        }
    }

    /**
     * Register the fact that an XML IDREF-type attribute has been seen on an
     * element.
     *
     * @param   id  ID value
     * @param   from  the element on which the ref has been seen
     * @param   checker  the checker which knows how to check links of this
     *          type
     */
    public void registerRef( String id, ElementRef from, RefChecker checker ) {

        /* Construct an object describing the from-to reference arc. */
        UncheckedReference unref = new UncheckedReference( id, from, checker );

        /* If we've already seen the corresponding ID, do the checking now. */
        if ( idMap_.containsKey( id ) ) {
            ElementRef to = (ElementRef) idMap_.get( id );
            unref.checkLink( to );
        }

        /* Otherwise, remember the check needs to be done for processing
         * later. */
        else {
            refMap_.put( id, unref );
        }
    }

    /**
     * Goes through all the unresolved IDREF->ID arcs and reports them.
     * This is done at the end of the parse.
     */
    public void reportUncheckedRefs() {
        for ( Iterator it = refMap_.keySet().iterator(); it.hasNext(); ) {
            String id = (String) it.next();
            UncheckedReference unref = (UncheckedReference) refMap_.get( id );
            it.remove();
            ElementRef from = unref.from_;
            error( "ID " + id + " referenced from " + from + " never found" );
        }
    }

    /**
     * Write an informative message to the user.
     *
     * @param  msg  message
     */
    public void info( String msg ) {
        message( "INFO", msg, null );
    }

    /**
     * Write a warning message to the user. 
     *
     * @param  msg  message
     */
    public void warning( String msg ) {
        message( "WARNING", msg, null );
    }

    /**
     * Write an error message to the user.
     *
     * @param  msg  message
     */
    public void error( String msg ) {
        errCount_++;
        message( "ERROR", msg, null );
    }

    /**
     * Dispatches a message to the user.  Context information, such as
     * position in the parse and message severity, are output as well
     * as the text itself.  Additionally an effort is made not to write
     * the same message millions of times.  Either the <tt>msg</tt>
     * or the <tt>e</tt> arguments, but not both, may be null.
     *
     * @param  type  indication of message severity
     * @param  msg   specific message content
     * @param  e     throwable associated with this message
     */
    public void message( String type, String msg, Throwable e ) {

        /* Fill in the message from the throwable if necessary. */
        if ( msg == null && e != null ) {
            msg = e.getMessage();
            if ( msg == null ) {
                msg = e.toString();
            }
        }

        /* See how many times (if any) we have output this same message 
         * before now.  If it's more than a certain threshold, don't
         * bother to do it again. */
        int repeat = msgMap_.containsKey( msg )
                   ? ((Integer) msgMap_.get( msg )).intValue()
                   : 0;
        msgMap_.put( msg, new Integer( repeat + 1 ) );
        if ( repeat < MAX_REPEAT ) {

            /* Construct the text to output. */
            StringBuffer sbuf = new StringBuffer()
                .append( type );
            int il = -1;
            int ic = -1;
            if ( locator_ != null ) {
                ic = locator_.getColumnNumber();
                il = locator_.getLineNumber();
            }
            if ( il > 0 ) {
                sbuf.append( " (l." )
                    .append( il );
                if ( ic > 0 ) {
                    sbuf.append( ", c." )
                        .append( ic );
                }
                sbuf.append( ")" );
            }
            sbuf.append( ": " )
                .append( msg );
            if ( repeat == MAX_REPEAT - 1 ) {
                sbuf.append( " (more...)" );
            }
            String text = sbuf.toString();

            /* Output the message. */
            out_.println( text );
            if ( debug_ ) {
                if ( e == null ) {
                    e = new VotLintException( msg );
                    e.fillInStackTrace();
                }
                e.printStackTrace( out_ );
                out_.println();
            }
        }
    }

    /**
     * Helper class which encapsulates an IDREF which hasn't yet been 
     * matched up with its ID.
     */
    private class UncheckedReference {
        final String id_;
        final ElementRef from_;
        final RefChecker refChecker_;

        /**
         * Constructor.
         *
         * @param  id  XML ID
         * @param  from   the element with the IDREF value of <tt>id</tt>
         * @param  refChecker  object which knows about constraints on the ID
         *         and the element it points to
         */
        UncheckedReference( String id, ElementRef from,
                            RefChecker refChecker ) {
            id_ = id;
            from_ = from;
            refChecker_ = refChecker;
        }

        /**
         * Performs the check on a resolved IDREF->ID arc
         *
         * @param  to  element which the IDREF points to
         */
        void checkLink( ElementRef to ) {
            refChecker_.checkLink( VotLintContext.this, id_, from_, to );
        }
    }
}
