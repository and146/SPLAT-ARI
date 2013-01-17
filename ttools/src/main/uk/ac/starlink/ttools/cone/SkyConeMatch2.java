package uk.ac.starlink.ttools.cone;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.JoinFixAction;
import uk.ac.starlink.table.join.PairMode;
import uk.ac.starlink.task.BooleanParameter;
import uk.ac.starlink.task.ChoiceParameter;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.IntegerParameter;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.ParameterValueException;
import uk.ac.starlink.task.TaskException;
import uk.ac.starlink.task.UsageException;
import uk.ac.starlink.ttools.task.ChoiceMode;
import uk.ac.starlink.ttools.task.JoinFixActionParameter;
import uk.ac.starlink.ttools.task.LineTableEnvironment;
import uk.ac.starlink.ttools.task.SingleMapperTask;
import uk.ac.starlink.ttools.task.TableProducer;

/**
 * Crossmatcher which works by performing one cone-search type query 
 * for each row of an input table on an external service of some kind.
 * This is not <i>prima facie</i> a very efficient way of doing a
 * cross match, but if the external service represents a table which 
 * is too large or otherwise unfeasible to access as one term in a
 * normal cone search it's about the only way to do it.
 *
 * @author   Mark Taylor
 * @since    9 Aug 2007
 */
public abstract class SkyConeMatch2 extends SingleMapperTask {

    private final Coner coner_;
    private final int parallelWarnThreshold_;
    private final Parameter raParam_;
    private final Parameter decParam_;
    private final Parameter srParam_;
    private final Parameter copycolsParam_;
    private final ChoiceParameter modeParam_;
    private final Parameter distcolParam_;
    private final BooleanParameter ostreamParam_;
    private final IntegerParameter parallelParam_;
    private final ConeErrorPolicyParameter erractParam_;
    private final JoinFixActionParameter fixcolsParam_;
    private final Parameter insuffixParam_;
    private final Parameter conesuffixParam_;
    private final BooleanParameter usefootParam_;
    private final IntegerParameter nsideParam_;
    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.ttools.cone" );

    /**
     * Constructor.
     *
     * @param  purpose  one-line description of the purpose of the task
     * @param  coner   object which provides the sky cone search service
     * @param  allowParallel  if true, provide parameters for selecting
     *         multi-threaded operation
     * @param  parallelWarnThreshold  values of the parallelism over this
     *         value result in a warning through the logging system;
     *         &lt;=0 means no warnings;
     *         ignored if <code>allowParallel</code> is false
     */
    public SkyConeMatch2( String purpose, Coner coner, boolean allowParallel,
                          int parallelWarnThreshold ) {
        super( purpose, new ChoiceMode(), true, true );
        coner_ = coner;
        parallelWarnThreshold_ = parallelWarnThreshold;
        List paramList = new ArrayList();
        String system = coner.getSkySystem();
        String sysParen;
        String inSys;
        if ( system == null || system.length() == 0 ) {
            sysParen = "";
            inSys = "";
        }
        else {
            sysParen = " (" + system + ")";
            inSys = " in the " + system + " coordinate system";
        }
    
        raParam_ = new Parameter( "ra" );
        raParam_.setUsage( "<expr>" );
        raParam_.setPrompt( "Right Ascension expression in degrees"
                          + sysParen );
        raParam_.setDescription( new String[] {
            "<p>Expression which evaluates to the right ascension in degrees"
            + inSys,
            "for the request at each row of the input table.",
            "This will usually be the name or ID of a column in the",
            "input table, or a function involving one.",
            "</p>",
        } );
        paramList.add( raParam_ );

        decParam_ = new Parameter( "dec" );
        decParam_.setUsage( "<expr>" );
        decParam_.setPrompt( "Declination expression in degrees"
                           + sysParen );
        decParam_.setDescription( new String[] {
            "<p>Expression which evaluates to the declination in degrees"
            + inSys,
            "for the request at each row of the input table.",
            "This will usually be the name or ID of a column in the",
            "input table, or a function involving one.",
            "</p>",
        } );
        paramList.add( decParam_ );

        srParam_ = new Parameter( "sr" );
        srParam_.setUsage( "<expr>" );
        srParam_.setPrompt( "Search radius in degrees" );
        srParam_.setDescription( new String[] {
            "<p>Expression which evaluates to the search radius in degrees",
            "for the request at each row of the input table.",
            "This will often be a constant numerical value, but may be",
            "the name or ID of a column in the input table,",
            "or a function involving one.",
            "</p>",
        } );
        paramList.add( srParam_ );

        /* Permit "best1" as an undocumented alternative to "best", since
         * it has the meaning of best1 in the pair match tasks. */
        modeParam_ = new ChoiceParameter( "find", new String[] {
            "best", "all", "each",
        } ) {
            @Override
            public void setValueFromString( Environment env, String value )
                    throws TaskException {
                if ( "best1".equalsIgnoreCase( value ) ) {
                    value = "best";
                }
                super.setValueFromString( env, value );
            }
        };
        modeParam_.setDefault( "all" );
        modeParam_.setPrompt( "Type of match to perform" );
        modeParam_.setDescription( new String[] {
            "<p>Determines which matches are retained.",
            "<ul>",
            "<li><code>best</code>:",
                "Only the matching query table row closest to",
                "the input table row will be output.",
                "Input table rows with no matches will be omitted.",
                "(Note this corresponds to the",
                "<code>" + PairMode.BEST1.toString().toLowerCase() + "</code>",
                "option in the pair matching commands, and <code>best1</code>",
                "is a permitted alias).",
                "</li>",
            "<li><code>all</code>:",
                "All query table rows which match",
                "the input table row will be output.",
                "Input table rows with no matches will be omitted.",
                "</li>",
            "<li><code>each</code>:",
                "There will be one output table row for each input table row.",
                "If matches are found, the closest one from the query table",
                "will be output, and in the case of no matches,",
                "the query table columns will be blank.",
                "</li>",
            "</ul>",
            "</p>",
        } );
        paramList.add( modeParam_ );

        usefootParam_ = new BooleanParameter( "usefoot" );
        usefootParam_.setPrompt( "Use service footprint if available?" );
        usefootParam_.setDescription( new String[] {
            "<p>Determines whether an attempt will be made to restrict",
            "searches in accordance with available footprint information.",
            "If this is set true, then before any of the per-row queries",
            "are performed, an attempt may be made to acquire footprint",
            "information about the servce.",
            "If such information can be obtained, then queries which",
            "fall outside the footprint, and hence which are known to",
            "yield no results, are skipped.  This can speed up the search",
            "considerably.",
            "</p>",
            "<p>Currently, the only footprints available are those",
            "provided by the CDS MOC (Multi-Order Coverage map) service,",
            "which covers VizieR and a few other cone search services.",
            "</p>",
        } );
        usefootParam_.setDefault( Boolean.TRUE.toString() );
        paramList.add( usefootParam_ );

        nsideParam_ = new IntegerParameter( "footnside" );
        nsideParam_.setPrompt( "HEALPix Nside for footprints" );
        nsideParam_.setDescription( new String[] {
            "<p>Determines the HEALPix Nside parameter for use with the MOC",
            "footprint service.",
            "This tuning parameter determines the resolution of the footprint",
            "if available.",
            "Larger values give better resolution, hence a better chance of",
            "avoiding unnecessary queries, but processing them takes longer",
            "and retrieving and storing them is more expensive.",
            "</p>",
            "<p>The value must be a power of 2,",
            "and at the time of writing, the MOC service will not supply",
            "footprints at resolutions greater than nside=512,",
            "so it should be &lt;=512.",
            "</p>",
            "<p>Only used if <code>" + usefootParam_.getName()
                                     + "=true</code>.",
            "</p>",
        } );
        nsideParam_.setMinimum( 1 );
        nsideParam_.setDefault( Integer.toString( MocServiceFootprint
                                                 .getServiceNside() ) );
        paramList.add( nsideParam_ );

        copycolsParam_ = new Parameter( "copycols" );
        copycolsParam_.setUsage( "<colid-list>" );
        copycolsParam_.setNullPermitted( true );
        copycolsParam_.setDefault( "*" );
        copycolsParam_.setPrompt( "Columns to be copied from input table" );
        copycolsParam_.setDescription( new String[] {
            "<p>List of columns from the input table which are to be copied",
            "to the output table.",
            "Each column identified here will be prepended to the",
            "columns of the combined output table,",
            "and its value for each row taken from the input table row",
            "which provided the parameters of the query which produced it.",
            "See <ref id='colid-list'/> for list syntax.",
            "The default setting is \"<code>*</code>\", which means that",
            "all columns from the input table are included in the output.",
            "</p>",
        } );
        paramList.add( copycolsParam_ );

        distcolParam_ = new Parameter( "scorecol" );
        distcolParam_.setNullPermitted( true );
        distcolParam_.setDefault( "Separation" );
        distcolParam_.setPrompt( "Angular distance output column name" );
        distcolParam_.setUsage( "<col-name>" );
        distcolParam_.setDescription( new String[] {
            "<p>Gives the name of a column in the output table to contain",
            "the distance between the requested central position and the",
            "actual position of the returned row.",
            "The distance returned is an angular distance in degrees.",
            "If a null value is chosen, no distance column will appear",
            "in the output table.",
            "</p>",
        } );
        paramList.add( distcolParam_ );

        parallelParam_ = new IntegerParameter( "parallel" );
        parallelParam_.setDefault( "1" );
        parallelParam_.setPrompt( "Number of queries to make in parallel" );
        parallelParam_.setUsage( "<n>" );
        parallelParam_.setMinimum( 1 );
        String warnText = parallelWarnThreshold_ > 1
            ? "This command does not impose any maximum value, " +
              "but if a value &gt;" + parallelWarnThreshold_ +
              " is submitted a warning will be issued."
            : "";
        parallelParam_.setDescription( new String[] {
            "<p>Allows multiple cone searches to be performed concurrently.",
            "If set to the default value, 1, the cone query corresponding",
            "to the first row of the input table will be dispatched,",
            "when that is completed the query corresponding to the",
            "second row will be dispatched, and so on.",
            "If set to <code>&lt;n&gt;</code>, then queries will be overlapped",
            "in such a way that up to approximately <code>&lt;n&gt;</code>",
            "may be running at any one time.",
            "</p>",
            "<p>Whether increasing <code>&lt;n&gt;</code> is a good idea,",
            "and what might be a sensible maximum value, depends on the",
            "characteristics of the service being queried.",
            "In particular, setting it to too large a number may overload",
            "the service resulting in some combination of failed queries,",
            "ultimately slower runtimes, and unpopularity with server admins.",
            warnText,
            "</p>",
        } );
        if ( allowParallel ) {
            paramList.add( parallelParam_ );
        }

        erractParam_ = new ConeErrorPolicyParameter( "erract" );
        paramList.add( erractParam_ );

        ostreamParam_ = new BooleanParameter( "ostream" );
        ostreamParam_.setDefault( "false" );
        ostreamParam_.setPrompt( "Whether output will be strictly streamed" );
        ostreamParam_.setDescription( new String[] {
            "<p>If set true, this will cause the operation to stream on",
            "output, so that the output table is built up as the results",
            "are obtained from the cone search service.",
            "The disadvantage of this is that some output modes and formats",
            "need multiple passes through the data to work, so depending",
            "on the output destination, the operation may fail if this is set.",
            "Use with care (or be prepared for the operation to fail).",
            "</p>",
        } );
        paramList.add( ostreamParam_ );

        fixcolsParam_ = new JoinFixActionParameter( "fixcols" );
        insuffixParam_ =
            fixcolsParam_.createSuffixParameter( "suffix0",
                                                 "the input table", "_0" );
        conesuffixParam_ =
            fixcolsParam_.createSuffixParameter( "suffix1",
                                                 "the cone result table",
                                                 "_1" );
        paramList.add( fixcolsParam_ );
        paramList.add( insuffixParam_ );
        paramList.add( conesuffixParam_ );

        getParameterList().addAll( paramList );
        getParameterList().addAll( Arrays.asList( coner.getParameters() ) );
    }

    public TableProducer createProducer( Environment env )
            throws TaskException {

        /* Interrogate environment for parameter values. */
        coner_.configureParams( env, srParam_ );
        boolean distFilter = coner_.useDistanceFilter( env );
        String copyColIdList = copycolsParam_.stringValue( env );
        String raString = raParam_.stringValue( env );
        String decString = decParam_.stringValue( env );
        String srString = srParam_.stringValue( env );
        boolean ostream = ostreamParam_.booleanValue( env );
        int parallelism = parallelParam_.intValue( env );
        if ( parallelWarnThreshold_ > 1 &&
             parallelism > parallelWarnThreshold_ ) {
            String msg = new StringBuffer()
                .append( parallelParam_.getName() )
                .append( "=" )
                .append( parallelism )
                .append( " - high value might overload server" )
                .toString();
            logger_.warning( msg );
        }
        ConeErrorPolicy erract = erractParam_.policyValue( env );
        if ( erract == ConeErrorPolicy.ABORT ) {
            String advice = "Cone search failed - try other values of "
                          + erractParam_.getName() + " parameter?";
            erract = ConeErrorPolicy.addAdvice( erract, advice );
        }
        String distanceCol = distcolParam_.stringValue( env );
        boolean bestOnly;
        boolean includeBlanks;
        String mode = modeParam_.stringValue( env );
        if ( mode.toLowerCase().equals( "best" ) ) {
            bestOnly = true;
            includeBlanks = false;
        }
        else if ( mode.toLowerCase().equals( "all" ) ) {
            bestOnly = false;
            includeBlanks = false;
        }
        else if ( mode.toLowerCase().equals( "each" ) ) {
            bestOnly = true;
            includeBlanks = true;
        }
        else {
            throw new UsageException( "Unknown value of " +
                                      modeParam_.getName() + "??" );
        }
        TableProducer inProd = createInputProducer( env );
        ConeSearcher coneSearcher =
            erract.adjustConeSearcher( coner_.createSearcher( env, bestOnly ) );
        final Footprint footprint;
        if ( usefootParam_.booleanValue( env ) ) {
            footprint = coner_.getFootprint( env );
            int nside = nsideParam_.intValue( env );
            if ( nside != MocServiceFootprint.getServiceNside() ) {
                try {
                    MocServiceFootprint.setServiceNside( nside );
                }
                catch ( IllegalArgumentException e ) {
                    throw new ParameterValueException( nsideParam_,
                                                       e.getMessage(), e );
                }
            }
        }
        else {
            footprint = null;
        }
        JoinFixAction inFixAct =
            fixcolsParam_.getJoinFixAction( env, insuffixParam_ );
        JoinFixAction coneFixAct =
            fixcolsParam_.getJoinFixAction( env, conesuffixParam_ );
        QuerySequenceFactory qsFact =
            new JELQuerySequenceFactory( raString, decString, srString );

        /* Return a table producer using these values. */
        ConeMatcher coneMatcher =
            new ConeMatcher( coneSearcher, inProd, qsFact, bestOnly, footprint,
                             includeBlanks, distFilter, parallelism,
                             copyColIdList, distanceCol, inFixAct, coneFixAct );
        coneMatcher.setStreamOutput( ostream );
        return coneMatcher;
    }
}
