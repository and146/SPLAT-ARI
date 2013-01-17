package uk.ac.starlink.votable;

import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.xml.XMLConstants;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

/**
 * Provides characteristics for a given version of the VOTable standard.
 * An instance of this class is passed to a {@link VOTableWriter} to 
 * indicate what version of the standard should be followed when generating
 * VOTable output.
 *
 * @author   Mark Taylor
 * @since    15 Nov 2012
 */
public abstract class VOTableVersion {

    /** VOTable 1.0. */
    public static final VOTableVersion V10;

    /** VOTable 1.1. */
    public static final VOTableVersion V11;

    /** VOTable 1.2. */
    public static final VOTableVersion V12;

    /** VOTable 1.3. */
    public static final VOTableVersion V13;

    private static final Map<String,VOTableVersion> VERSION_MAP =
        Collections.unmodifiableMap( createMap( new VOTableVersion[] {
            V10 = new VersionLike10( "1.0" ),
            V11 = new VersionLike11( "1.1" ),
            V12 = new VersionLike12( "1.2" ),
            V13 = new VersionLike13( "1.3" ),
        } ) );

    /** 
     * Default VOTable version number which output will conform to
     * if not otherwise specified ({@value}).
     */
    public static final String DEFAULT_VERSION_STRING = "1.1";

    /**
     * System property name whose value gives the default VOTable version
     * written by instances of this class if no version is given explicitly.
     * The property is named {@value} and if it is not supplied the
     * version defaults to the value of {@link #DEFAULT_VERSION_STRING}
     * (={@value #DEFAULT_VERSION_STRING}).
     */
    public static final String VOTABLE_VERSION_PROP = "votable.version";

    private static final Logger logger_ =
        Logger.getLogger( "uk.ac.starlink.votable" );

    private final String versionNumber_;

    /**
     * Constructor.
     *
     * @param   versionNumber  the number (like "1.1") identifying this version
     */
    protected VOTableVersion( String versionNumber ) {
        versionNumber_ = versionNumber;
    }

    /**
     * Returns the version number for this version.
     *
     * @return  version number (like "1.1")
     */
    public String getVersionNumber() {
        return versionNumber_;
    }

    /**
     * Returns the XML namespace in which the VOTable elements reside.
     *
     * @return  VOTable XML namespace, or null
     */
    public abstract String getXmlNamespace();

    /**
     * Returns the URL of the VOTable schema corresponding to this version.
     *
     * @return  VOTable schema, or null
     */
    public abstract String getSchemaLocation();

    /**
     * Returns a schema which may be used to validate document instances
     * of this VOTable version.
     * Will return non-null iff {@link #getSchemaLocation} returns non-null.
     *
     * @return  validation schema, or null
     */
    public abstract Schema getSchema();

    /**
     * Returns the text of the DOCTYPE XML declaration for this version.
     *
     * @return  doctype declaration, or null
     */
    public abstract String getDoctypeDeclaration();

    /**
     * Returns a URL from which the DTD can be retrieved.
     * This is not the canonical DTD url, but a pointer to a local resource.
     * Will return non-null iff {@link #getDoctypeDeclaration} returns non-null.
     *
     * @return  local URL from which the DTD can be retrieved, or null
     */
    public abstract URL getDtdUrl();
   
    /**
     * Returns version number.
     */
    @Override
    public String toString() {
        return versionNumber_;
    }

    /**
     * Indicates whether this version permits an empty TD element to represent
     * a null value for <em>all</em> data types.
     *
     * @return  true iff empty TD elements are always permitted
     */
    public abstract boolean allowEmptyTd();

    /**
     * Indicates whether the BINARY2 serialization format is defined by
     * this version.
     *
     * @return  true iff BINARY2 is allowed
     */
    public abstract boolean allowBinary2();

    /**
     * Indicates whether the xtype attribute is permitted on FIELD elements
     * etc in this version.
     *
     * @return  true iff xtype attribute is allowed
     */
    public abstract boolean allowXtype();

    /**
     * Returns a number->version map for all known versions.
     * The map keys are version number strings like "1.1".
     * The order of entries in this map is in ascending order
     * of version number.
     *
     * @return   version map
     */
    public static Map<String,VOTableVersion> getKnownVersions() {
        return VERSION_MAP;
    }

    /**
     * Returns the version instance used by default for output in this JVM.
     * By default this is determined by the value of the
     * {@link #DEFAULT_VERSION_STRING} constant, but it can be
     * overridden by use of the {@link #VOTABLE_VERSION_PROP}
     * ({@value #VOTABLE_VERSION_PROP}) system property.
     *
     * @return  default VOTable version for output
     */
    public static VOTableVersion getDefaultVersion() {
        String vnum = DEFAULT_VERSION_STRING;
        try {
            vnum = System.getProperty( VOTABLE_VERSION_PROP, vnum );
            if ( ! VERSION_MAP.containsKey( vnum ) ) {
                logger_.warning( "Unknown VOTable version \"" + vnum
                               + "\" - use default " + DEFAULT_VERSION_STRING );
                vnum = DEFAULT_VERSION_STRING;
            }
        }
        catch ( SecurityException e ) {
        }
        VOTableVersion version = VERSION_MAP.get( vnum );
        assert version != null;
        return version;
    }

    /**
     * Creates a map of names to versions from a list of versions.
     *
     * @param  versions  array of versions
     * @return  name->version map
     */
    private static Map<String,VOTableVersion>
            createMap( VOTableVersion[] versions ) {
        Map<String,VOTableVersion> map =
            new LinkedHashMap<String,VOTableVersion>();
        for ( int i = 0; i < versions.length; i++ ) {
            VOTableVersion vers = versions[ i ];
            map.put( vers.getVersionNumber(), vers );
        }
        return map;
    }

    /**
     * VOTable 1.0-like version instance.
     */
    private static class VersionLike10 extends VOTableVersion {

        /**
         * Constructor.
         *
         * @param  version   version number
         */
        VersionLike10( String version ) {
            super( version );
        }

        public String getXmlNamespace() {
            return null;
        }
        public String getSchemaLocation() {
            return null;
        }
        public String getDoctypeDeclaration() {
            return "<!DOCTYPE VOTABLE SYSTEM "
                 + "\"http://us-vo.org/xml/VOTable.dtd\">";
        }
        public URL getDtdUrl() {
            return VOTableVersion.class
                  .getResource( "/uk/ac/starlink/util/text/VOTable.dtd" );
        }
        public boolean allowXtype() {
            return false;
        }
        public boolean allowEmptyTd() {
            return false;
        }
        public boolean allowBinary2() {
            return false;
        }
        public Schema getSchema() {
            return null;
        }
    }

    /**
     * VOTable 1.1-like version instance.
     */
    private static class VersionLike11 extends VersionLike10 {
        private boolean schemaTried_;
        private Schema schema_;

        /**
         * Constructor.
         *
         * @param  version   version number
         */
        VersionLike11( String version ) {
            super( version );
        }

        @Override
        public String getXmlNamespace() {
            return "http://www.ivoa.net/xml/VOTable/v" + getVersionNumber();
        }
        @Override
        public String getSchemaLocation() {
            return getXmlNamespace();
        }
        @Override
        public String getDoctypeDeclaration() {
            return null;
        }
        @Override
        public URL getDtdUrl() {
            return null;
        }
        @Override
        public synchronized Schema getSchema() {
            if ( ! schemaTried_ ) {
                schemaTried_ = true;
                String loc = "/uk/ac/starlink/util/text/VOTable"
                           + getVersionNumber() + ".xsd";
                URL surl = VOTableVersion.class.getResource( loc );
                if ( surl == null ) {
                    logger_.warning( "No VOTable schema found at " + loc );
                }
                else {
                    try {
                        schema_ =
                            SchemaFactory
                           .newInstance( XMLConstants.W3C_XML_SCHEMA_NS_URI )
                           .newSchema( surl );
                    }
                    catch ( Exception e ) {
                        logger_.warning( "Failed to initialize schema from "
                                       + surl + " - " + e );
                    }
                }
            }
            return schema_;
        }
    }

    /**
     * VOTable 1.2-like version instance.
     */
    private static class VersionLike12 extends VersionLike11 {

        /**
         * Constructor.
         *
         * @param  version   version number
         */
        VersionLike12( String version ) {
            super( version );
        }

        @Override 
        public boolean allowXtype() {
            return true;
        }
    }

    /**
     * VOTable 1.3-like version instance.
     */
    private static class VersionLike13 extends VersionLike12 {

        /**
         * Constructor.
         *
         * @param  version   version number
         */
        VersionLike13( String version ) {
            super( version );
        }

        @Override
        public boolean allowEmptyTd() {
            return true;
        }
        @Override
        public boolean allowBinary2() {
            return true;
        }
    }
}
