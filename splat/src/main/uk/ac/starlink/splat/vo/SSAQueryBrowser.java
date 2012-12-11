/*
 * Copyright (C) 2004 Central Laboratory of the Research Councils
 * Copyright (C) 2007-2009 Science and Technology Facilities Council
 *
 *  History:
 *     11-NOV-2004 (Peter W. Draper):
 *       Original version.
 *     23-FEB-2012 (Margarida Castro Neves, mcneves@ari.uni-heidelberg.de)
 *       Added support for customized metadata parameters.  
 *       Ported SwingWorker from jsky.util.SwingWorker to javax.swing.SwingWorker
 *     08-OCT-2012 Redesigned new GUI
 */
package uk.ac.starlink.splat.vo;


import java.awt.BorderLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
//import javax.swing.SwingWorker;

import jsky.catalog.BasicQueryArgs;
import jsky.catalog.QueryArgs;
import jsky.catalog.QueryResult;
import jsky.catalog.TableQueryResult;
import jsky.catalog.skycat.SkycatCatalog;
import jsky.catalog.skycat.SkycatConfigEntry;
import jsky.coords.Coordinates;
import jsky.coords.WorldCoords;
import jsky.util.SwingWorker;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.InputSource;

import uk.ac.starlink.splat.data.SpecDataFactory;
import uk.ac.starlink.splat.iface.HelpFrame;
import uk.ac.starlink.splat.iface.ProgressPanel;
import uk.ac.starlink.splat.iface.SpectrumIO;
import uk.ac.starlink.splat.iface.SplatBrowser;
import uk.ac.starlink.splat.iface.ToolButtonBar;
import uk.ac.starlink.splat.iface.SpectrumIO.Props;
import uk.ac.starlink.splat.iface.images.ImageHolder;
import uk.ac.starlink.splat.util.SplatCommunicator;
import uk.ac.starlink.splat.util.SplatException;
import uk.ac.starlink.splat.util.Transmitter;
import uk.ac.starlink.splat.util.Utilities;
//import uk.ac.starlink.splat.vo.SSAMetadataFrame.MetadataInputParameter;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.DescribedValue;
import uk.ac.starlink.table.RowSequence;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.TableFormatException;
import uk.ac.starlink.table.gui.StarJTable;
import uk.ac.starlink.util.ProxySetup;
import uk.ac.starlink.util.gui.BasicFileChooser;
import uk.ac.starlink.util.gui.BasicFileFilter;
import uk.ac.starlink.util.gui.ErrorDialog;
import uk.ac.starlink.util.gui.GridBagLayouter;
import uk.ac.starlink.util.gui.ProxySetupFrame;
import uk.ac.starlink.vo.DalResultXMLFilter;
import uk.ac.starlink.vo.RegResource;
import uk.ac.starlink.vo.ResolverInfo;
import uk.ac.starlink.votable.DataFormat;
import uk.ac.starlink.votable.ParamElement;
import uk.ac.starlink.votable.TableElement;
import uk.ac.starlink.votable.VOElement;
import uk.ac.starlink.votable.VOElementFactory;
import uk.ac.starlink.votable.VOSerializer;
import uk.ac.starlink.votable.VOStarTable;
import uk.ac.starlink.votable.VOTableWriter;
import javax.swing.BoxLayout;


/**
 * Display a page of controls for querying a list of  SSA servers and
 * displaying the results of those queries. The spectra returned can then be
 * selected and displayed in the main SPLAT browser.
 *
 * @author Peter W. Draper
 * @author Mark Taylor
 * @author Margarida Castro Neves 
 * @version $Id$
 *
 */
public class SSAQueryBrowser
extends JFrame
implements ActionListener, MouseListener, DocumentListener, PropertyChangeListener
{
    // Logger.
    private static Logger logger =  Logger.getLogger( "uk.ac.starlink.splat.vo.SSAQueryBrowser" );

    /** UI preferences. */
    protected static Preferences prefs =
            Preferences.userNodeForPackage( SSAQueryBrowser.class );

    /** Initial window size and location */
    private static final Rectangle defaultWindowLocation =
            new Rectangle( 0, 0, 600, 600 );

    /** The object holding the list of servers that we should use for SSA
     *  queries. */
    private SSAServerList serverList = null;

    /** The instance of SPLAT we're associated with. */
    private SplatBrowser browser = null;

    /** The SpecDataFactory.*/
    private SpecDataFactory specDataFactory = SpecDataFactory.getInstance();

    /** File chooser used for saving and restoring queries. */
    protected BasicFileChooser fileChooser = null;

    /** Content pane of frame */
    protected JPanel contentPane = null;

    /** Centre panel */
    protected JPanel centrePanel = null;

    /** Servers panel */
    protected JPanel leftPanel = null;
    private JPanel leftPanel_1;
    
    
    /** Query panel */
    protected JPanel queryPanel = null;

    /** Basic Query panel */
    protected JPanel basicQueryPanel = null;

    /** Customized Query panel */
    protected JPanel customQueryPanel = null;

    /** Object name */
    protected JTextField nameField = null;

    /** Resolve object name button */
    protected JButton nameLookup = null;

    /** Download and display selected spectra */
    protected JButton displaySelectedButton = null;

    /** Download and display all spectra */
    protected JButton displayAllButton = null;

    /** Download selected spectra */
    protected JButton downloadSelectedButton = null;

    /** Download all spectra */
    protected JButton downloadAllButton = null;

    /** Deselect spectra in visible table */
    protected JButton deselectVisibleButton = null;

    /** Deselect all spectra in all tables */
    protected JButton deselectAllButton = null;

    /** Display basic search parameters */
    protected JRadioButton  basicSearchButton;

    /** Display extended search parameters */
    protected JRadioButton  customSearchButton;


    /** Make the query to all known servers */
    protected JButton goButton = null;
    
    /** update/display query string */
    JButton showQueryButton = null;

    /** Allows user to customize search parameters */
    protected JButton customButton = null;

    /** Central RA */
    protected JTextField raField = null;

    /** Central Dec */
    protected JTextField decField = null;

    /** Region radius */
    protected JTextField radiusField = null;

    /** Lower limit for BAND */
    protected JTextField lowerBandField = null;

    /** Upper limits for BAND */
    protected JTextField upperBandField = null;

    /** Lower limit for TIME */
    protected JTextField lowerTimeField = null;

    /** Upper limits for TIME */
    protected JTextField upperTimeField = null;

    /** The query text */
    protected JTextArea queryText = null;
    
    /** The extended query text */
    protected String extendedQueryText = null;
    

    /** ButtonGroup for the format selection */
    protected ButtonGroup formatGroup = null;
    protected JComboBox formatList = null;

    /** ButtonGroup for the FLUXCALIB selection */
    protected ButtonGroup fluxCalibGroup = null;
    protected JComboBox flcalibList = null;
    

    /** ButtonGroup for the WAVECALIB selection */
    protected ButtonGroup waveCalibGroup = null;
    protected JComboBox wlcalibList = null;

    /** Tabbed pane showing the query results tables */
    protected JTabbedPane resultsPane = null;

    /** The list of StarJTables in use */
    protected ArrayList<StarJTable> starJTables = null;

    /** NED name resolver catalogue */
    protected SkycatCatalog nedCatalogue = null;

    /** SIMBAD name resolver catalogue */
    protected SkycatCatalog simbadCatalogue = null;

    /** The current name resolver, if using Skycat method */
    protected SkycatCatalog resolverCatalogue = null;

    /** The proxy server dialog */
    protected ProxySetupFrame proxyWindow = null;

    /** The SSA servers window */
   // protected SSAServerFrame serverWindow = null;

    /** The Button for adding optional parameters to the query */
    protected JButton addParamButton = null;
    /** The Button for removing optional parameters from the optional parameters list */
    protected JButton removeParamButton = null;
    
    /** The list of all input parameters read from the servers */
   // protected static SSAMetadataFrame metaFrame = null;
    protected static SSAMetadataPanel metaPanel = null;

    /** Make sure the proxy environment is setup */
    static {
        ProxySetup.getInstance().restore();
    }

    /* The Query text that will be displayed */
    private SSAQuery queryLine;
    
    /** The list of all input parameters read from the servers as a hash map */
    private static HashMap<String, MetadataInputParameter> metaParam=null;  
    
    /** The list of all input parameters read from the servers as a hash map */
    private static HashMap< JTextField, String > queryMetaParam=null; 
    
    /** the authenticator for access control **/
    private static SSAPAuthenticator authenticator;
    
    /** two way mapping servers-metadata parameters **/
    private static ServerParamRelation serverParam;
    
    /** the serverlist as a tree **/
    private SSAServerTree tree;
    
    /** progress frame **/
    private ProgressPanelFrame oldProgressFrame = null;

    

    private boolean isLookup = false;
    
    static ProgressPanelFrame progressFrame = null;


    /**
     * Create an instance.
     */
    public SSAQueryBrowser( SSAServerList serverList, SplatBrowser browser )
    {
        this.serverList = serverList;
        this.browser = browser;
        authenticator = new SSAPAuthenticator();
        Authenticator.setDefault(authenticator);
        specDataFactory.setAuthenticator(authenticator);
        serverParam = new ServerParamRelation();
        queryMetaParam = new HashMap< JTextField, String >();
        if (serverList != null) customParameters();
       
        initUI();
        initMenusAndToolbar();
        initFrame(); 
        
    }

    public SSAPAuthenticator getAuthenticator() {
        return authenticator;
    }
    /**
     * Create and display the UI components.
     */
    private void initUI()
    {
        contentPane = (JPanel) getContentPane();
        //contentPane.setLayout( new BorderLayout() );
       // contentPane.setLayout( new GridLayout(1,2,3,3) );
        contentPane.setLayout( new BoxLayout(contentPane, BoxLayout.X_AXIS) );
        centrePanel = new JPanel( new BorderLayout() );
        leftPanel_1 = new JPanel( );
     //   leftPanel_1.setPreferredSize(new Dimension(100,600));
     //   centrePanel.setPreferredSize(new Dimension(500,700));
        contentPane.add( leftPanel_1);//, BorderLayout.LINE_START);
        contentPane.add( centrePanel);//, BorderLayout.LINE_END );
  
        initServerComponents();
        
        
        initQueryComponents();
        initResultsComponent();
        setDefaultNameServers();
    }


    public void initServerComponents()
    {
        leftPanel_1.setLayout(new BoxLayout(leftPanel_1, BoxLayout.Y_AXIS));

        tree=new SSAServerTree( serverList, serverParam );   
        leftPanel_1.add(tree);
        //serverPanel.add(tree); 
       // leftPanel_1.add(serverPanel);
       // tree.adaptTreeHeight( leftPanel.getHeight());
     //   JPanel paramPanel = new JPanel();
       // paramPanel.setBorder ( BorderFactory.createTitledBorder( "SSAP Parameters" ) );
        //metaPanel = new SSAMetadataPanel();
      //  paramPanel.add(metaPanel);
      //  leftPanel.add(paramPanel);
        //tree.setSize(getMaximumSize());
    }
    /**
     * Initialise the menu bar, action bar and related actions.
     */
    private void initMenusAndToolbar()
    {
        //  Add the menuBar.
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar( menuBar );

        //  Create the toolbar.
       // ToolButtonBar toolBar = new ToolButtonBar( contentPane );
        JPanel actionBarContainer = new JPanel();

        //  Get icons.
        ImageIcon closeImage =
                new ImageIcon( ImageHolder.class.getResource( "close.gif" ) );
        ImageIcon saveImage =
                new ImageIcon( ImageHolder.class.getResource( "savefile.gif" ) );
        ImageIcon readImage =
                new ImageIcon( ImageHolder.class.getResource( "openfile.gif" ) );
        ImageIcon helpImage =
                new ImageIcon( ImageHolder.class.getResource( "help.gif" ) );
        ImageIcon ssaImage =
                new ImageIcon( ImageHolder.class.getResource("ssapservers.gif") );

        //  Create the File menu.
        JMenu fileMenu = new JMenu( "File" );
        fileMenu.setMnemonic( KeyEvent.VK_F );
        menuBar.add( fileMenu );

        //  Add options to save and restore the query result.
        LocalAction saveAction = new LocalAction( LocalAction.SAVE,
                "Save query results",
                saveImage,
                "Save results of query " +
                "to disk file" );

        fileMenu.add( saveAction );
        JButton saveButton = new JButton( saveAction );
        actionBarContainer.add( saveButton );

        LocalAction readAction = new LocalAction( LocalAction.READ,
                "Restore query results",
                readImage,
                "Read results of a " +
                        "previous query back " +
                "from disk file" );
        fileMenu.add( readAction );
        JButton readButton = new JButton( readAction );
        actionBarContainer.add( readButton );

        //  Add an action to close the window.
        LocalAction closeAction = new LocalAction( LocalAction.CLOSE,
                "Close", closeImage,
                "Close window",
                "control W" );
        fileMenu.add( closeAction ).setMnemonic( KeyEvent.VK_C );

        JButton closeButton = new JButton( closeAction );
        actionBarContainer.add( closeButton );

        //  Create the options menu.
        JMenu optionsMenu = new JMenu( "Options" );
        optionsMenu.setMnemonic( KeyEvent.VK_O );
        menuBar.add( optionsMenu );

        LocalAction proxyAction = new LocalAction( LocalAction.PROXY,
                "Configure connection " +
                "proxy..." );
        optionsMenu.add( proxyAction );

 /*       //  Add item to control the use of SSA servers.
        LocalAction serverAction = new LocalAction(LocalAction.SERVER,
                "Configure SSAP servers...",
                ssaImage,
                "Configure SSAP servers" );
        optionsMenu.add( serverAction );
    //    toolBar.add( serverAction );

        //  SSAP version 1 format control, wavelength calibation and
        //  flux calibration options.
        initFormatOptions( optionsMenu );
        initWaveCalibOptions( optionsMenu );
        initFluxCalibOptions( optionsMenu );
*/
        //  Create a menu containing all the name resolvers.
        JMenu resolverMenu = new JMenu( "Resolver" );
        resolverMenu.setMnemonic( KeyEvent.VK_R );
        menuBar.add( resolverMenu );

        ButtonGroup bg = new ButtonGroup();

        JRadioButtonMenuItem jrbmi = new JRadioButtonMenuItem();
        resolverMenu.add( jrbmi );
        jrbmi.setSelected( false );
        bg.add( jrbmi );
        jrbmi.setAction( new ResolverAction( "SIMBAD via CADC",
                simbadCatalogue ) );
        jrbmi.setToolTipText( "SIMBAD service served by CADC" );

        jrbmi = new JRadioButtonMenuItem();
        resolverMenu.add( jrbmi );
        jrbmi.setSelected( false );
        bg.add( jrbmi );
        jrbmi.setAction( new ResolverAction( "NED via ESO", nedCatalogue ) );
        jrbmi.setToolTipText( "NED catalogue served by ESO" );

        jrbmi = new JRadioButtonMenuItem();
        resolverMenu.add( jrbmi );
        jrbmi.setSelected( true );
        bg.add( jrbmi );
        jrbmi.setAction( new ResolverAction( "CDS Sesame", null  ) );
        resolverCatalogue = null;
        jrbmi.setToolTipText
        ( "CDS Sesame service queries SIMBAD, NED and Vizier" );


        //  Create a menu for inter-client communications.
        JMenu interopMenu = new JMenu( "Interop" );
        interopMenu.setMnemonic( KeyEvent.VK_I );
        menuBar.add( interopMenu );

        //  Need an actions to transmit and broadcast the current
        //  results table.
        SplatCommunicator communicator = browser.getCommunicator();

        // Add table transmit options.
        Transmitter transmitter = communicator.createTableTransmitter( this );
        interopMenu.add( transmitter.getBroadcastAction() )
        .setMnemonic( KeyEvent.VK_B );
        interopMenu.add( transmitter.createSendMenu() )
        .setMnemonic( KeyEvent.VK_T );

        //  Create the Help menu.
        HelpFrame.createButtonHelpMenu( "ssa-window", "Help on window", menuBar, null /*toolBar*/ );

        //  ActionBar goes at bottom.
        //contentPane.add( actionBarContainer, BorderLayout.SOUTH );
        centrePanel.add( actionBarContainer, BorderLayout.SOUTH );
    }

    /**
     * Initialise frame properties (disposal, title, menus etc.).
     */
    private void initFrame()
    {
        setTitle( Utilities.getTitle( "Query VO for Spectra" ) );
        setDefaultCloseOperation( JFrame.HIDE_ON_CLOSE );
        Utilities.setFrameLocation( this, defaultWindowLocation, prefs,
                "SSAQueryBrowser" );
        setVisible( true );
    }

    /**
     * Populate the north part of center window with the basic query
     * components.
     */
    private void initQueryComponents()
    {
        queryLine = new SSAQuery("<SERVER>");
        queryPanel = new JPanel();
        JPanel queryParamPanel = new JPanel();
       // queryParamPanel.setPreferredSize(new Dimension(300, 200));
       /// customQueryPanel = new JPanel();
        queryPanel.setBorder ( BorderFactory.createTitledBorder( "Search parameters:" ) );
        queryPanel.setLayout( new BorderLayout());
      // customScrollPanel = new JScrollPane( customQueryPanel );
        queryParamPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill=GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 0;
        
        JPanel simpleQueryPanel = new JPanel();
     //   simpleQueryPanel.setPreferredSize(new Dimension(100,200));
        simpleQueryPanel.setBorder(BorderFactory.createTitledBorder("Simple Query"));
        queryParamPanel.add(simpleQueryPanel, c);
        
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0.5;
        c.gridx = 1;
        c.gridy = 0;
       
        JPanel optionalQueryPanel = new JPanel();
        optionalQueryPanel.setBorder(BorderFactory.createTitledBorder("Optional Parameters"));
  //      optionalQueryPanel.setPreferredSize(new Dimension(300,200));
        queryParamPanel.add(optionalQueryPanel, c);
        
       
        
      
        // The simple query panel
        //------------------------
        
       // GridBagLayouter layouter =  new GridBagLayouter( queryPanel, GridBagLayouter.SCHEME3 /*SCHEME4*/ );
        GridBagLayouter layouter =  new GridBagLayouter( simpleQueryPanel, GridBagLayouter.SCHEME4 /*SCHEME4*/ );


        //  Object name. Arrange for a resolver to look up the coordinates of
        //  the object name, when return or the lookup button are pressed.
        JLabel nameLabel = new JLabel( "Object:" );
        nameField = new JTextField( 10 );
        nameField.setFocusable(true);
        nameField.setToolTipText( "Enter the name of an object " +
                " -- press return to get coordinates" );
        nameField.addActionListener( this );
        nameField.getDocument().putProperty("owner", nameField); //set the owner
        nameField.getDocument().addDocumentListener( this );
      //  nameField.addMouseListener( this );
        
        nameLookup = new JButton( "Lookup" );
        nameLookup.addActionListener( this );
        nameLookup.setToolTipText( "Press to get coordinates of Object" );
        
        JPanel objPanel = new JPanel( new GridBagLayout());
        GridBagConstraints gbcs = new GridBagConstraints();
        gbcs.weightx = 1.0;
        gbcs.fill = GridBagConstraints.HORIZONTAL;
        gbcs.ipadx = 15;
        objPanel.add(nameField, gbcs);
        objPanel.add(nameLookup, gbcs);
        layouter.add( nameLabel, false );
     
        layouter.add(objPanel, true);
 
        layouter.eatLine();

       
        //  RA and Dec fields. We're free-formatting on these (decimal degrees
        //  not required).

        JLabel raLabel = new JLabel( "RA:" );
        raField = new JTextField( 10 );
        raField.addActionListener(this);
        raField.getDocument().putProperty("owner", raField); //set the owner
        raField.getDocument().addDocumentListener( this );

        JLabel decLabel = new JLabel( "Dec:" );
        decField = new JTextField( 10 );
        decField.addActionListener(this);
        decField.getDocument().putProperty("owner", decField); //set the owner
        decField.getDocument().addDocumentListener( this );

        JPanel posPanel = new JPanel( new GridBagLayout() );
        GridBagConstraints gbc = new GridBagConstraints();
     
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        posPanel.add( raField, gbc );
        gbc.weightx=0.0;
        gbc.fill = GridBagConstraints.NONE;
        posPanel.add(decLabel, gbc);
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        posPanel.add(decField, gbc);
        
        layouter.add( raLabel, false );
        layouter.add(posPanel,true);
        decField.setToolTipText( "Enter the Dec of field centre, decimal degrees or dd:mm:ss.ss" );
        raField.setToolTipText( "Enter the RA of field centre, decimal degrees or hh:mm:ss.ss" );

    


        //  Radius field.
        JLabel radiusLabel = new JLabel( "Radius:" );
        radiusField = new JTextField( "10.0", 10 );
        queryLine.setRadius(10.0);
        radiusField.addActionListener( this );
        layouter.add( radiusLabel, false );
        layouter.add( radiusField, true );
        radiusField.setToolTipText( "Enter radius of field to search" +
                " from given centre, arcminutes" );
        radiusField.addActionListener( this );
        radiusField.getDocument().putProperty("owner", radiusField); //set the owner
        radiusField.getDocument().addDocumentListener( this );


        //  Band fields.
        JLabel bandLabel = new JLabel( "Band:" );
        lowerBandField = new JTextField( 10 );
        lowerBandField.addActionListener( this );
        lowerBandField.getDocument().putProperty("owner", lowerBandField); //set the owner
        lowerBandField.getDocument().addDocumentListener( this );

        upperBandField = new JTextField( 10 );
        upperBandField.addActionListener( this );
        upperBandField.getDocument().putProperty("owner", upperBandField); //set the owner
        upperBandField.getDocument().addDocumentListener( this );


        JPanel bandPanel = new JPanel( new GridBagLayout() );
        GridBagConstraints gbc2 = new GridBagConstraints();

        gbc2.weightx = 1.0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        bandPanel.add( lowerBandField, gbc2 );

     
        gbc2.weightx = 0.0;
        gbc2.fill = GridBagConstraints.NONE;
        bandPanel.add( new JLabel( "/" ), gbc2 );

 
        gbc2.weightx = 1.0;
        gbc2.fill = GridBagConstraints.HORIZONTAL;
        bandPanel.add( upperBandField, gbc2 );

        layouter.add( bandLabel, false );
        layouter.add( bandPanel, true );
        lowerBandField.setToolTipText( "Lower limit, or single include " +
                "value, for spectral band, in meters" );
        upperBandField.setToolTipText
        ( "Upper limit for spectral band, in meters" );

        //  Time fields, note this shares a line with the band fields.
        JLabel timeLabel = new JLabel( "Time:" );
        lowerTimeField = new JTextField( 15 );
        lowerTimeField.addActionListener( this );
        lowerTimeField.getDocument().putProperty("owner", lowerTimeField); //set the owner
        lowerTimeField.getDocument().addDocumentListener( this );

        upperTimeField = new JTextField( 15 );
        upperTimeField.addActionListener( this );
        upperTimeField.getDocument().putProperty("owner", upperTimeField); //set the owner
        upperTimeField.getDocument().addDocumentListener( this );

        JPanel timePanel = new JPanel( new GridBagLayout() );

        GridBagConstraints gbc4 = new GridBagConstraints();
        gbc4.weightx = 1.0;
        gbc4.fill = GridBagConstraints.HORIZONTAL;
        timePanel.add( lowerTimeField, gbc4 );

        GridBagConstraints gbc5 = new GridBagConstraints();
        gbc5.weightx = 0.0;
        gbc5.fill = GridBagConstraints.NONE;
        timePanel.add( new JLabel( "/" ), gbc5 );

        GridBagConstraints gbc6 = new GridBagConstraints();
        gbc6.weightx = 1.0;
        gbc6.fill = GridBagConstraints.HORIZONTAL;
        timePanel.add( upperTimeField, gbc6 );

        layouter.add( timeLabel, false );
        layouter.add( timePanel, true );
        lowerTimeField.setToolTipText( "Lower limit, or single include " +
                "value for time coverage, " +
                "ISO 8601 format " +
                "(e.g 2008-10-15T20:48Z)" );
        upperTimeField.setToolTipText( "Upper limit for time coverage, " +
                "in ISO 8601 format " +
                "(e.g 2008-10-15T20:48Z)" );
        
        // format and calibration options:
        JPanel calibOptions = new JPanel(new GridLayout(3,2));
        
        // Formats
        String[] formats =  { "None", "ALL", "COMPLIANT", "votable", "fits", "xml", "native" };
        formatList = new JComboBox(formats);
        formatList.setSelectedIndex(0);
        formatList.addActionListener(this);
        calibOptions.add(new JLabel("Query Format:"), false);
        calibOptions.add( formatList, true);
        
        //Wavelength Calibration
        String[] wlcalibs = { "None", "any", "absolute", "relative" };
        wlcalibList = new JComboBox(wlcalibs);
        wlcalibList.setSelectedIndex(0);
        wlcalibList.addActionListener(this);
        calibOptions.add(new JLabel( "Wavelength calibration:"), false);
        calibOptions.add( wlcalibList, true);
        
        //Flux Calibration
        String[] flcalibs = { "None", "any", "absolute", "relative", "normalized" };
        flcalibList = new JComboBox(flcalibs);
        flcalibList.setSelectedIndex(0);
        flcalibList.addActionListener(this);
        calibOptions.add(new JLabel( "Flux calibration:"), false);
        calibOptions.add( flcalibList, true);
        layouter.add(calibOptions, true);
     
        layouter.eatSpare();
        
        
        // The optional query parameter (metadata) Panel
        //----------------------------------------------
     
        optionalQueryPanel.setLayout( new BoxLayout(optionalQueryPanel, BoxLayout.PAGE_AXIS) /*BorderLayout()*/);
       metaPanel = new SSAMetadataPanel();
       metaPanel.addPropertyChangeListener(this);
       metaPanel.setVisible(false);
      // layouter.add(metaPanel, true);
    //   metaPanel.setTableWidth(200);
       optionalQueryPanel.add(metaPanel);//, BorderLayout.NORTH);

    //   queryTextPanel.add(new JLabel("Query:"));
        JPanel paramButtonsPanel = new JPanel( );
        addParamButton = new JButton("Add Parameter");
        addParamButton.addActionListener( this );
        addParamButton.setToolTipText("Add Optional Parameters");
       // optionalQueryPanel.add(addParamButton, BorderLayout.WEST);
        removeParamButton = new JButton("Remove Parameter");
        removeParamButton.addActionListener( this );
        removeParamButton.setToolTipText("Remove selected Parameters");
    //    optionalQueryPanel.add(removeParamButton, BorderLayout.EAST);
        paramButtonsPanel.add( addParamButton );
        paramButtonsPanel.add( removeParamButton );
        optionalQueryPanel.add(paramButtonsPanel, BorderLayout.SOUTH);
       
        queryPanel.add(queryParamPanel, BorderLayout.NORTH);
        
        // the query string display Panel
        //-------------------------------
        goButton = new JButton( "    SEND QUERY    " );
        goButton.setBackground(Color.green);
        goButton.addActionListener( this );
      
  //      showQueryButton = new JButton( "Query: ");
  //      showQueryButton.setToolTipText("show/update query string");
  //      showQueryButton.addActionListener( this );
        JPanel sendQueryPanel = new JPanel(new BorderLayout());
        sendQueryPanel.add(new JLabel("Query:"), BorderLayout.LINE_START);
  //      sendQueryPanel.add(showQueryButton, BorderLayout.LINE_START);
        queryText = new JTextArea(2,30);
        queryText.setEditable(false);
        sendQueryPanel.add(queryText);
        queryText.setLineWrap(true);     
        sendQueryPanel.add(goButton, BorderLayout.LINE_END);
       
        
        queryPanel.add( sendQueryPanel, BorderLayout.SOUTH);
       
        centrePanel.add( queryPanel, BorderLayout.NORTH );
        
        // add query text to query text area
        updateQueryText();
    }

    /**
     * Make the results component. This is mainly JTabbedPane containing a
     * JTable for each set of results (the tables are realized later) and
     * a button to display the selected spectra.
     */
    private void initResultsComponent()
    {
        JPanel resultsPanel = new JPanel( new BorderLayout() );
        resultsPanel.setBorder
        ( BorderFactory.createTitledBorder( "Query results:" ) );
        resultsPanel.setToolTipText( "Results of query to the current list "+
                "of SSAP servers. One table per server" );

        resultsPane = new JTabbedPane();
        resultsPanel.add( resultsPane, BorderLayout.NORTH );

        JPanel controlPanel = new JPanel( new BorderLayout() );
        JPanel controlPanel1 = new JPanel();
        JPanel controlPanel2 = new JPanel();

        //  Download and display.
        displaySelectedButton = new JButton( "Display selected" );
        displaySelectedButton.addActionListener( this );
        displaySelectedButton.setToolTipText
        ( "Download and display all spectra selected in all tables" );
        controlPanel1.add( displaySelectedButton );


        displayAllButton = new JButton( "Display all" );
        displayAllButton.addActionListener( this );
        displayAllButton.setToolTipText
        ( "Download and display all spectra in all tables" );
        controlPanel1.add( displayAllButton );

        //  Just download.
        downloadSelectedButton = new JButton( "Download selected" );
        downloadSelectedButton.addActionListener( this );
        downloadSelectedButton.setToolTipText
        ( "Download all spectra selected in all tables");
        controlPanel1.add( downloadSelectedButton );

        downloadAllButton = new JButton( "Download all" );
        downloadAllButton.addActionListener( this );
        downloadAllButton.setToolTipText
        ( "Download all spectra in all tables");
        controlPanel1.add( downloadAllButton );


        //  Deselect
        deselectVisibleButton = new JButton( "Deselect" );
        deselectVisibleButton.addActionListener( this );
        deselectVisibleButton.setToolTipText
        ( "Deselect all spectra in displayed table" );
        controlPanel2.add( deselectVisibleButton );

        deselectAllButton = new JButton( "Deselect all" );
        deselectAllButton.addActionListener( this );
        deselectAllButton.setToolTipText
        ( "Deselect all spectra in all tables" );
        controlPanel2.add( deselectAllButton );

        controlPanel.add( controlPanel1, BorderLayout.NORTH );
        controlPanel.add( controlPanel2, BorderLayout.SOUTH );
        resultsPanel.add( controlPanel, BorderLayout.SOUTH );
        centrePanel.add( resultsPanel, BorderLayout.CENTER );
    }

    /**
     * Initialise the SSAP version 1 data formats. Don't want
     * one of these by default.
     */
    protected void initFormatOptions( JMenu optionsMenu )
    {
        JMenu formatMenu = new JMenu( "Query format" );
        String[] names =
            { "None", "ALL", "COMPLIANT", "votable", "fits", "xml", "native" };
        JRadioButtonMenuItem item;
        formatGroup = new ButtonGroup();
        for ( int i = 0; i < names.length; i++ ) {
            item = new JRadioButtonMenuItem( names[i] );
            if ( i == 0 ) {
                item.setSelected( true );
            }
            item.setActionCommand( names[i] );
            formatGroup.add( item );
            formatMenu.add( item );
        }
        optionsMenu.add( formatMenu );
    }

    /**
     * Initialise the SSAP version 1 wavecalib formats. Don't want
     * one of these by default.
     */
    protected void initWaveCalibOptions( JMenu optionsMenu )
    {
        JMenu calibMenu = new JMenu( "Wavelength calibration" );
        String[] names =
            { "None", "any", "absolute", "relative" };
        JRadioButtonMenuItem item;
        waveCalibGroup = new ButtonGroup();
        for ( int i = 0; i < names.length; i++ ) {
            item = new JRadioButtonMenuItem( names[i] );
            if ( i == 0 ) {
                item.setSelected( true );
            }
            item.setActionCommand( names[i] );
            waveCalibGroup.add( item );
            calibMenu.add( item );
        }
        optionsMenu.add( calibMenu );
    }

    /**
     * Initialise the SSAP version 1 fluxcalib formats. Don't want
     * one of these by default.
     */
    protected void initFluxCalibOptions( JMenu optionsMenu )
    {
        JMenu calibMenu = new JMenu( "Flux calibration" );
        String[] names =
            { "None", "any", "absolute", "relative", "normalized" };
        JRadioButtonMenuItem item;
        fluxCalibGroup = new ButtonGroup();
        for ( int i = 0; i < names.length; i++ ) {
            item = new JRadioButtonMenuItem( names[i] );
            if ( i == 0 ) {
                item.setSelected( true );
            }
            item.setActionCommand( names[i] );
            fluxCalibGroup.add( item );
            calibMenu.add( item );
        }
        optionsMenu.add( calibMenu );
    }

    /**
     * Arrange to resolve the object name into coordinates.
     */
    protected void resolveName()
    {
        String name = nameField.getText().trim();
        if ( name != null && name.length() > 0 ) {

            QueryArgs qargs = null;
            if ( resolverCatalogue != null ) {
                //  Skycat resolver.
                qargs = new BasicQueryArgs( resolverCatalogue );

                // If objectName has spaces we should protect them.
                name = name.replaceAll( " ", "%20" );
                qargs.setId( name );
            }
            final QueryArgs queryArgs = qargs;
            final String objectName = name;

            Thread thread = new Thread( "Name server" )
            {
                public void run()
                {
                    try {
                        if ( queryArgs == null ) {
                            ResolverInfo info =
                                    ResolverInfo.resolve( objectName );
                            WorldCoords coords =
                                    new WorldCoords( info.getRaDegrees(),
                                            info.getDecDegrees() );
                            String[] radec = coords.format();
                            raField.setText( radec[0] );
                            decField.setText( radec[1] );
                         //   queryLine.setPosition(radec[0], radec[1]);
                        }
                        else {
                            QueryResult r =
                                    resolverCatalogue.query( queryArgs );
                            if ( r instanceof TableQueryResult ) {
                                Coordinates coords =
                                        ((TableQueryResult) r).getCoordinates( 0 );
                                if ( coords instanceof WorldCoords ) {
                                    //  Enter values into RA and Dec fields.
                                    String[] radec =
                                            ((WorldCoords) coords).format();
                                    raField.setText( radec[0] );
                                    decField.setText( radec[1] );
                                    // 
                           //         queryLine.setPosition(radec[0], radec[1]);
                                }
                            }
                        }
                    }
                    catch (Exception e) {
                        ErrorDialog.showError( null, e );
                    }
                }
            };

            //  Start the nameserver.
            raField.setText( "" );
            decField.setText( "" );
            thread.start();
        }
    }

    /**
     * Setup the default name servers (SIMBAD and NED) to use to resolve
     * astronomical object names. Note these are just those used in JSky.
     * A better implementation should reuse the JSky classes.
     * <p>
     * XXX refactor these into an XML file external to the application.
     * Maybe switch to the CDS Sesame webservice.
     */
    private void setDefaultNameServers()
    {
        Properties p1 = new Properties();
        p1.setProperty( "serv_type", "namesvr" );
        p1.setProperty( "long_name", "SIMBAD Names via CADC" );
        p1.setProperty( "short_name", "simbad_ns@cadc" );
        p1.setProperty
        ( "url",
                "http://cadcwww.dao.nrc.ca/cadcbin/sim-server?&o=%id" );
        SkycatConfigEntry entry = new SkycatConfigEntry( p1 );
        simbadCatalogue = new SkycatCatalog( entry );

        Properties p2 = new Properties();
        p2.setProperty( "serv_type", "namesvr" );
        p2.setProperty( "long_name", "NED Names" );
        p2.setProperty( "short_name", "ned@eso" );
        p2.setProperty
        ( "url",
                "http://archive.eso.org/skycat/servers/ned-server?&o=%id" );
        entry = new SkycatConfigEntry( p2 );
        nedCatalogue = new SkycatCatalog( entry );
    }

    /**
     * Perform the query to all the currently selected servers.
     */
    public void doQuery()
    {
        //  Get the position. Allow the object name to be passed using
        //  TARGETNAME, useful for solar system objects.
        String ra = raField.getText();
        String dec = decField.getText();
        String objectName = nameField.getText().trim();
        if ( ra == null || ra.length() == 0 ||
                dec == null || dec.length() == 0 ) {

            //  Check for an object name. Need one or the other.
            if ( objectName != null && objectName.length() > 0 ) {
                ra = null;
                dec = null;
            }
            else {
                int n = JOptionPane.showConfirmDialog( this,
                        "You have not supplied " +
                                "a search centre or object " +
                                "name, do you want to proceed?",
                                "No RA or Dec",
                                JOptionPane.YES_NO_OPTION );
                if ( n == JOptionPane.NO_OPTION ) {
                    return;
                }

                //  To be clear.
                ra = null;
                dec = null;
                objectName = null;
            }
        }

        //  And the radius.
        String radiusText = radiusField.getText();
        double radius=0;// = 10.0;
        if ( radiusText != null && radiusText.length() > 0 ) {
            try {
                radius = Double.parseDouble( radiusText );
            }
            catch (NumberFormatException e) {
                ErrorDialog.showError( this, "Cannot understand radius value", e );
                return;
            }
        }

       

       // queryLine.setQueryParameters(ra, dec, objectName, radiusText, lowerBandField.getText(), upperBandField.getText(), lowerTimeField.getText(), upperTimeField.getText(), 
       //                             waveCalibGroup.getSelection().getActionCommand(), fluxCalibGroup.getSelection().getActionCommand(), formatGroup.getSelection().getActionCommand());
        try {
            queryLine.setPosition(ra, dec);
        } catch (NumberFormatException e) {
            ErrorDialog.showError( this, "Cannot understand this value", e );
            return;
        }
        queryLine.setRadius( radius );
        queryLine.setBand(lowerBandField.getText(), upperBandField.getText());
        queryLine.setTime(lowerTimeField.getText(), upperTimeField.getText());
        queryLine.setWaveCalib(wlcalibList.getSelectedItem().toString());
        queryLine.setFluxCalib(flcalibList.getSelectedItem().toString());
        queryLine.setFormat(formatList.getSelectedItem().toString());
        
        //  Create a stack of all queries to perform.
        ArrayList<SSAQuery> queryList = new ArrayList<SSAQuery>();
        Iterator i = serverList.getIterator();
        SSAPRegResource server = null;
        while( i.hasNext() ) {
            server = (SSAPRegResource) i.next();
            if (serverList.isServerSelected(server.getShortName())) {
            
            SSAQuery ssaQuery =  new SSAQuery(queryLine);//new SSAQuery( server );
            ssaQuery.setServer( server) ; //Parameters(queryLine); // copy the query parameters to the new query
 /*           ssaQuery.setTargetName( objectName );
            ssaQuery.setPosition( ra, dec );
            ssaQuery.setRadius( radius );
            ssaQuery.setBand( lowerBand, upperBand );
            ssaQuery.setTime( lowerTime, upperTime );
            ssaQuery.setFormat( format );
            ssaQuery.setWaveCalib( waveCalib );
            ssaQuery.setFluxCalib( fluxCalib );*/
            queryList.add( ssaQuery );
            }
        }

        // Now actually do the queries, these are performed in a separate
        // Thread so we avoid locking the interface.
        if ( queryList.size() > 0 ) {
            processQueryList( queryList );
        }
        else {
            JOptionPane.showMessageDialog( this,
                    "There are no SSA servers currently selected",
                    "No SSA servers", JOptionPane.ERROR_MESSAGE );
        }
    }

    /**
     * Process a list of URL queries to SSA servers and display the
     * results. All processing is performed in a background Thread.
     */
    protected void processQueryList( ArrayList<SSAQuery> queryList )
    {
        //  final ArrayList localQueryList = queryList;
        makeResultsDisplay( null );
        
        if (progressFrame != null) {
            progressFrame.closeWindowEvent();
            progressFrame=null;
        }
       // final ProgressPanelFrame 
        progressFrame = new ProgressPanelFrame( "Querying SSAP servers" );
        

        Iterator<SSAQuery> i = queryList.iterator();
        while ( i.hasNext() ) {
            final SSAQuery ssaQuery = i.next();
            final ProgressPanel progressPanel = new ProgressPanel( "Querying: " + ssaQuery.getDescription());
            progressFrame.addProgressPanel( progressPanel );

            final SwingWorker worker = new SwingWorker()
            {
                boolean interrupted = false;
                public Object construct() 
                {
                    progressPanel.start();
                    try {
                        runProcessQuery( ssaQuery, progressPanel );
                    }
                    catch (InterruptedException e) {
                        interrupted = true;
                    }
                    return null;
                }

                public void finished()
                {
                    progressPanel.stop();
                    //  Display the results.
                    if ( ! interrupted ) {
                        addResultsDisplay( ssaQuery );
                    }
                }
            };

            progressPanel.addActionListener( new ActionListener()
            {
                public void actionPerformed( ActionEvent e )
                {
                    if ( worker != null ) {
                         worker.interrupt();
                    }
                }
            });

            worker.start();  
        }
    }

    /**
     * Do a query to an SSAP server.
     */
    private void runProcessQuery( SSAQuery ssaQuery,
            ProgressPanel progressPanel )
                    throws InterruptedException
                    {
        boolean failed = false;
        boolean overflow = false;

        //  We just download VOTables, so avoid attempting to build the other
        //  formats.
        StarTable starTable = null;
        URL queryURL = null;

        // int j = 0;

        try {
            queryURL = ssaQuery.getQueryURL();
          

            // check if more parameters have been added
            // Not very nice... should think of a better way to do that
            //
        //    if (metaFrame != null) {
            //       String extendedQuery=metaFrame.getParamsQueryString();
            String extendedQuery = null;
    //        if (querystring != null)
                extendedQuery=metaPanel.getParamsQueryString();
            logger.info( "Extended Query string " + extendedQuery );
            if (extendedQuery != null && extendedQuery.length() > 0) 
            {
                String newURL = queryURL.toString() + extendedQuery;
                logger.info( "Query string " + newURL );
                queryURL = new URL(newURL);
            }
            //     }
        }   
        catch ( MalformedURLException mue ) {
            progressPanel.logMessage( mue.getMessage() );
            logger.info( "Malformed URL "+queryURL );
            failed = true;
        }
        catch ( UnsupportedEncodingException uee) {
            progressPanel.logMessage( uee.getMessage() );
            logger.info( "URL Encoding Exception "+queryURL );
            failed = true;
        } 
        //   accessControl = new HashMap<String,String>;
        //    accessControl.addPropertyChangeListener(this);

        logger.info( "Querying: " + queryURL );

        progressPanel.logMessage( ssaQuery.getBaseURL() );
    
    
        //  Do the query and get the result as a StarTable. Uses this
        //  method for efficiency as non-result tables are ignored.
        try {
            InputSource inSrc = new InputSource( queryURL.openStream() );
            
            // inSrc.setSystemId( ssaQuery.getBaseURL() );
            inSrc.setSystemId( queryURL.toString());
            VOElementFactory vofact = new VOElementFactory();

            starTable = DalResultXMLFilter.getDalResultTable( vofact, inSrc );

            //  Check parameter QUERY_STATUS, this should be set to OK
            //  when the query
            String queryOK = null;
            String queryDescription = null;
            try {
                queryOK = starTable
                        .getParameterByName( "QUERY_STATUS" )
                        .getValueAsString( 100 );
                queryDescription = starTable
                        .getParameterByName( "QUERY_STATUS" )
                        .getInfo().getDescription();
            }
            catch (NullPointerException ne) {
                // Whoops, that's not good, but see what we can do.
                queryOK = "FAILED";
            }
            if ( "OK".equalsIgnoreCase( queryOK ) ) {
                ssaQuery.setStarTable( starTable );
                progressPanel.logMessage( "Done" );
            }
            else if ("OVERFLOW".equalsIgnoreCase( queryOK ) ) {
                ssaQuery.setStarTable( starTable );
                progressPanel.logMessage( queryDescription );
                logger.info( queryDescription);
                overflow=true;
            }
            else {
                //  Some problem with the service, report that.
                progressPanel.logMessage( "Query failed: " + queryOK );
                logger.info( "Query failed: " + queryOK );
                if ( queryDescription != null ) {
                    progressPanel.logMessage( queryDescription );
                    logger.info( queryDescription);
                }
                failed = true;
            }

            //  Dump query results as VOTables.
            //uk.ac.starlink.table.StarTableOutput sto =
            //    new uk.ac.starlink.table.StarTableOutput();
            //sto.writeStarTable( starTable,
            //                    "votable" + j + ".xml", null);
            //j++;
        }
        catch (TableFormatException te) {
            progressPanel.logMessage( te.getMessage() );
            logger.info( ssaQuery.getDescription() + ": " + te.getMessage() );
            failed = true;
        }
        catch (IOException ie) {
            progressPanel.logMessage( ie.getMessage() );
            logger.info( ssaQuery.getDescription() + ": " + ie.getMessage() );
            failed = true;
        }
        catch (Exception ge) {
            //  General exception.
            progressPanel.logMessage( ge.getMessage() );
            logger.info( ssaQuery.getDescription() + ": " + ge.getMessage() );
            failed = true;
        } 

        if ( Thread.interrupted() ) {
            throw new InterruptedException();
        }
        if ( ! failed && ! overflow ) {
            progressPanel.logMessage( "Completed download" );
        }
} //runProcessQUery

    /**
     * Display the results of the queries to the SSA servers. The results can
     * be either a list of SSAQuery instances, or the StarTables themselves
     * (usually these are from a disk-file restoration, not a live query).
     */
    protected void makeResultsDisplay( ArrayList<VOStarTable> tableList )
    {
        if ( starJTables == null ) {
            starJTables = new ArrayList<StarJTable>();
        }

        //  Remove existing tables.
        resultsPane.removeAll();
        starJTables.clear();

        if ( tableList != null ) {
            Iterator<VOStarTable> i = tableList.iterator();
            while ( i.hasNext() ) {
                addResultsDisplay( i.next() );
            }
        }
    }

    protected void addResultsDisplay( Object next )
    {
        DescribedValue dValue = null;
        JScrollPane scrollPane = null;
        SSAQuery ssaQuery = null;
        StarJTable table = null;
        StarTable starTable = null;
        String shortName = null;

        if ( next instanceof SSAQuery && next != null ) {
            ssaQuery = (SSAQuery) next;
            starTable = ssaQuery.getStarTable();
            shortName = ssaQuery.getDescription();
        }
        else if ( next instanceof StarTable) {
            starTable = (StarTable) next;
            dValue = starTable.getParameterByName( "ShortName" );
            if ( dValue == null ) {
                shortName = starTable.getName();
            }
            else {
                shortName = (String)dValue.getValue();
            }
        }
        else {
            logger.info( "Couldn't handle: " + next );
        }
        if ( starTable != null ) {
            //  Check if table has rows, if not skip.
            int nrows = (int) starTable.getRowCount();
            if (  nrows > 0 ) {
                table = new StarJTable( starTable, true );
                scrollPane = new JScrollPane( table );
                resultsPane.addTab( shortName, scrollPane );
                starJTables.add( table );

                //  Set widths of columns.
                if ( nrows > 1 ) {
                    nrows = ( nrows > 5 ) ? 5 : nrows;
                    table.configureColumnWidths( 200, nrows );
                }

                //  Double click on row means load just that spectrum.
                table.addMouseListener( this );
            }
        }
    }

    /**
     * Deselect all spectra in the visible table, or deselect all tables.
     */
    protected void deselectSpectra( boolean all )
    {
        if ( all ) {
            //  Visit all the tabbed StarJTables.
            Iterator<StarJTable> i = starJTables.iterator();
            while ( i.hasNext() ) {
                i.next().clearSelection();
            }
        }
        else {
            Component c = resultsPane.getSelectedComponent();
            if ( c != null ) {
                JScrollPane sp = (JScrollPane) c;
                StarJTable table = (StarJTable ) sp.getViewport().getView();
                table.clearSelection();
            }
        }
    }



    /**
     * Get the main SPLAT browser to download and display spectra.
     * <p>
     * Can either display all the spectra, just the selected spectra, or the
     * spectrum from a row in a given table. If table is null, then the
     * selected parameter determines the behaviour of all or just the selected
     * spectra.
     */
    protected void displaySpectra( boolean selected, boolean display,
            StarJTable table, int row )
    {
        //  List of all spectra to be loaded and their data formats and short
        //  names etc.
        ArrayList<Props> specList = new ArrayList<Props>();

        if ( table == null ) {
            //  Visit all the tabbed StarJTables.
            Iterator<StarJTable> i = starJTables.iterator();
            while ( i.hasNext() ) {
                extractSpectraFromTable( i.next(), specList,
                        selected, -1 );
            }
        }
        else {
            extractSpectraFromTable( table, specList, selected, row );
        }

        //  If we have no spectra complain and stop.
        if ( specList.size() == 0 ) {
            String mess;
            if ( selected ) {
                mess = "There are no spectra selected";
            }
            else {
                mess = "No spectra available";
            }
            JOptionPane.showMessageDialog( this, mess, "No spectra",
                    JOptionPane.ERROR_MESSAGE );
            return;
        }

        //  And load and display...
        SpectrumIO.Props[] propList = new SpectrumIO.Props[specList.size()];
        specList.toArray( propList );
        
        // check for authentication
        for (int p=0; p<propList.length; p++ ) {
            URL url=null;
            try {
                 url = new URL(propList[p].getSpectrum());
            } catch (MalformedURLException mue) {
                logger.info(mue.getMessage());
            }
        }
        

        browser.threadLoadSpectra( propList, display );
        browser.toFront();
    }

    /**
     * Extract all the links to spectra for downloading, plus the associated
     * information available in the VOTable. Each set of spectral information
     * is used to populated a SpectrumIO.Prop object that is added to the
     * specList list.
     * <p>
     * Can return the selected spectra, if requested, otherwise all spectra
     * are returned or if a row value other than -1 is given just one row.
     * @throws SplatException 
     */
    private void extractSpectraFromTable( StarJTable starJTable,
            ArrayList<Props> specList,
            boolean selected,
            int row )
    {
        int[] selection = null;

        //  Check for a selection if required, otherwise we're using the given
        //  row.
        if ( selected && row == -1 ) {
            selection = starJTable.getSelectedRows();
        }
        else if ( row != -1 ) {
            selection = new int[1];
            selection[0] = row;
        }

        // Only do this if we're processing all rows or we have a selection.
        if ( selection == null || selection.length > 0 ) {
            StarTable starTable = starJTable.getStarTable();

            //  Check for a column that contains links to the actual data
            //  (XXX these could be XML links to data within this
            //  document). The signature for this is an UCD of DATA_LINK,
            //  or a UTYPE of Access.Reference.
            int ncol = starTable.getColumnCount();
            int linkcol = -1;
            int typecol = -1;
            int namecol = -1;
            int axescol = -1;
            int specaxiscol = -1;
            int fluxaxiscol = -1;
            int unitscol = -1;
            int specunitscol = -1;
            int fluxunitscol = -1;
            int fluxerrorcol = -1;
            ColumnInfo colInfo;
            String ucd;
            String utype;
            for( int k = 0; k < ncol; k++ ) {
                colInfo = starTable.getColumnInfo( k );
                ucd = colInfo.getUCD();

                //  Old-style UCDs for backwards compatibility.
                if ( ucd != null ) {
                    ucd = ucd.toLowerCase();
                    if ( ucd.equals( "data_link" ) ) {
                        linkcol = k;
                    }
                    else if ( ucd.equals( "vox:spectrum_format" ) ) {
                        typecol = k;
                    }
                    else if ( ucd.equals( "vox:image_title" ) ) {
                        namecol = k;
                    }
                    else if ( ucd.equals( "vox:spectrum_axes" ) ) {
                        axescol = k;
                    }
                    else if ( ucd.equals( "vox:spectrum_units" ) ) {
                        unitscol = k;
                    }
                }

                //  Version 1.0 utypes. XXX not sure if axes names
                //  are in columns or are really parameters. Assume
                //  these work like the old-style scheme and appear in
                //  the columns.
                utype = colInfo.getUtype();
                if ( utype != null ) {
                    utype = utype.toLowerCase();
                    if ( utype.endsWith( "access.reference" ) ) {
                        linkcol = k;
                    }
                    else if ( utype.endsWith( "access.format" ) ) {
                        typecol = k;
                    }
                    else if ( utype.endsWith( "target.name" ) ) {
                        namecol = k;
                    }
                    else if ( utype.endsWith( "char.spectralaxis.name" ) ) {
                        specaxiscol = k;
                    }
                    else if ( utype.endsWith( "char.spectralaxis.unit" ) ) {
                        specunitscol = k;
                    }
                    else if ( utype.endsWith( "char.fluxaxis.name" ) ) {
                        fluxaxiscol = k;
                    }
                    else if ( utype.endsWith( "char.fluxaxis.accuracy.staterror" ) ) {
                        fluxerrorcol = k;
                    }
                    else if ( utype.endsWith( "char.fluxaxis.unit" ) ) {
                        fluxunitscol = k;
                    }
                }
            }

            //  If we have a DATA_LINK column, gather the URLs it contains
            //  that are appropriate.
            if ( linkcol != -1 ) {
                RowSequence rseq = null;
                SpectrumIO.Props props = null;
                String value = null;
                String[] axes;
                String[] units;
                try {
                    if ( ! selected && selection == null ) {
                        //  Using all rows.
                        rseq = starTable.getRowSequence();
                        while ( rseq.next() ) {
                            value = ( (String) rseq.getCell( linkcol ).toString() );
                            value = value.trim();
                            props = new SpectrumIO.Props( value );
                            if ( typecol != -1 ) {
                                value = ((String)rseq.getCell( typecol ).toString() );
                                if ( value != null ) {
                                    value = value.trim();
                                    props.setType
                                    ( specDataFactory
                                            .mimeToSPLATType( value ) );
                                }
                            }
                            if ( namecol != -1 ) {
                                value = ( (String)rseq.getCell( namecol ).toString() );
                                if ( value != null ) {
                                    value = value.trim();
                                    props.setShortName( value );
                                }
                            }

                            if ( axescol != -1 ) {

                                //  Old style column names.
                                value = ( (String)rseq.getCell( axescol ).toString() );
                                if ( value != null ) {
                                    value = value.trim();
                                    axes = value.split("\\s");
                                    props.setCoordColumn( axes[0] );
                                    props.setDataColumn( axes[1] );
                                    if ( axes.length == 3 ) {
                                        props.setErrorColumn( axes[2] );
                                    }
                                }
                            }
                            else {

                                //  Version 1.0 style.
                                if ( specaxiscol != -1 ) {
                                    value = (String)rseq.getCell(specaxiscol).toString();
                                    props.setCoordColumn( value );
                                }
                                if ( fluxaxiscol != -1 ) {
                                    value = (String)rseq.getCell(fluxaxiscol).toString();
                                    props.setDataColumn( value );
                                }
                                if ( fluxerrorcol != -1 ) {
                                    value = (String)rseq.getCell(fluxerrorcol).toString();
                                    props.setErrorColumn( value );
                                }
                            }

                            if ( unitscol != -1 ) {

                                //  Old style column names.
                                value = ( (String)rseq.getCell( unitscol ).toString() );
                                if ( value != null ) {
                                    value = value.trim();
                                    units = value.split("\\s");
                                    props.setCoordUnits( units[0] );
                                    props.setDataUnits( units[1] );
                                    //  Error must have same units as data.
                                }
                            }
                            else {

                                //  Version 1.0 style.
                                if ( specunitscol != -1 ) {
                                    value = (String)rseq.getCell(specunitscol).toString();
                                    props.setCoordUnits( value );
                                }
                                if ( fluxunitscol != -1 ) {
                                    value = (String)rseq.getCell(fluxunitscol).toString();
                                    props.setDataUnits( value );
                                }
                            }
                            specList.add( props );
                        }
                    }
                    else {
                        //  Just using selected rows. To do this we step
                        //  through the table and check if that row is the
                        //  next one in the selection (the selection is
                        //  sorted).
                        rseq = starTable.getRowSequence();
                        int k = 0; // Table row
                        int l = 0; // selection index
                        while ( rseq.next() ) {
                            if ( k == selection[l] ) {

                                // Store this one as matches selection.
                                if (rseq.getCell( linkcol ) != null)                                      
                                    value = ( (String)rseq.getCell( linkcol ).toString() );
                                if (value != null ) {         
                                    value = value.trim();
                                    props = new SpectrumIO.Props( value );
                                } 
                                if ( typecol != -1 ) {
                                    value = null;
                                    Object obj = rseq.getCell(typecol);
                                    if (obj != null) 
                                        value =((String)rseq.getCell(typecol).toString());
                                    if ( value != null ) {
                                        value = value.trim();
                                        props.setType
                                        ( specDataFactory
                                                .mimeToSPLATType( value ) );
                                    }
                                }
                                if ( namecol != -1 ) {
                                    value = null;
                                    Object obj = rseq.getCell(namecol);
                                    if (obj != null) 
                                    value = ((String)rseq.getCell( namecol ).toString());
                                    if ( value != null ) {
                                        value = value.trim();
                                        props.setShortName( value );
                                    }
                                }
                                if ( axescol != -1 ) {
                                    value = null;
                                    Object obj = rseq.getCell(axescol);
                                    if (obj != null) 
                                        value = ((String)obj.toString());
                                    
                                    if (value != null ) {
                                         value = value.trim();
                                        axes = value.split("\\s");
                                        props.setCoordColumn( axes[0] );
                                        props.setDataColumn( axes[1] );
                                    }
                                }
                                if ( unitscol != -1 ) {
                                    value = null;
                                    Object obj = rseq.getCell(unitscol);
                                    if (obj != null) 
                                        value = ((String)rseq.getCell(unitscol).toString());
                                    if ( value != null ) {
                                        units = value.split("\\s");
                                        props.setCoordUnits( units[0] );
                                        props.setDataUnits( units[1] );
                                    }
                                }
                                specList.add( props );

                                //  Move to next selection.
                                l++;
                                if ( l >= selection.length ) {
                                    break;
                                }
                            }
                            k++;
                        }
                    }
                }
                catch (IOException ie) {
                    ie.printStackTrace();
                }
                catch (NullPointerException ee) {
                    ErrorDialog.showError( this, "Failed to parse query results file", ee );
                }
                finally {
                    try {
                        if ( rseq != null ) {
                            rseq.close();
                        }
                    }
                    catch (IOException iie) {
                        // Ignore.
                    }
                }
            }
        }
    }

    /**
     *  Restore a set of previous query results that have been written to a
     *  VOTable. The file name is obtained interactively.
     */
    public void readQueryFromFile()
    {
        initFileChooser();
        int result = fileChooser.showOpenDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            File file = fileChooser.getSelectedFile();
            try {
                readQuery( file );
            }
            catch (SplatException e) {
                ErrorDialog.showError( this, e );
            }
        }
    }

    /**
     *  Restore a set of query results from a File. The File should have the
     *  results written previously as a VOTable, with a RESOURCE containing
     *  the various query results as TABLEs.
     */
    protected void readQuery( File file )
            throws SplatException
            {
        VOElement rootElement = null;
        try {
            rootElement = new VOElementFactory().makeVOElement( file );
        }
        catch (Exception e) {
            throw new SplatException( "Failed to open query results file", e );
        }

        //  First element should be a RESOURCE.
        VOElement[] resource = rootElement.getChildren();
        VOStarTable table = null;
        ArrayList<VOStarTable> tableList = new ArrayList<VOStarTable>();
        String tagName = null;
        for ( int i = 0; i < resource.length; i++ ) {
            tagName = resource[i].getTagName();
            if ( "RESOURCE".equals( tagName ) ) {

                //  Look for the TABLEs.
                VOElement child[] = resource[i].getChildren();
                for ( int j = 0; j < child.length; j++ ) {
                    tagName = child[j].getTagName();
                    if ( "TABLE".equals( tagName ) ) {
                        try {
                            table = new VOStarTable( (TableElement) child[j] );
                        }
                        catch (IOException e) {
                            throw new SplatException( "Failed to read query result", e );
                        }
                        tableList.add( table );
                    }
                }
            }
        }
        if ( tableList.size() > 0 ) {
            makeResultsDisplay( tableList );
        }
        else {
            throw new SplatException( "No query results found" );
        }
            }

    /**
     *  Interactively get a file name and save current query results to it as
     *  a VOTable.
     */
    public void saveQueryToFile()
    {
        if ( starJTables == null || starJTables.size() == 0 ) {
            JOptionPane.showMessageDialog( this,
                    "There are no queries to save",
                    "No queries", JOptionPane.ERROR_MESSAGE );
            return;
        }

        initFileChooser();
        int result = fileChooser.showSaveDialog( this );
        if ( result == JFileChooser.APPROVE_OPTION ) {
            File file = fileChooser.getSelectedFile();
            try {
                saveQuery( file );
            }
            catch (SplatException e) {
                ErrorDialog.showError( this, e );
            }
        }
    }

    /**
     *  Save current query to a File, writing the results as a VOTable.
     */
    protected void saveQuery( File file )
            throws SplatException
            {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter( new FileWriter( file ) );
        }
        catch (IOException e) {
            throw new SplatException( e );
        }
        saveQuery( writer );
        try {
            writer.close();
        }
        catch (IOException e) {
            throw new SplatException( e );
        }
            }

    /**
     *  Save current query results to a BufferedWriter. The resulting document
     *  is a VOTable with a RESOURCE that contains a TABLE for each set of
     *  query results.
     */
    protected void saveQuery( BufferedWriter writer )
            throws SplatException
            {
        String xmlDec = VOTableWriter.DEFAULT_XML_DECLARATION;
        try {
            writer.write( xmlDec );
            writer.newLine();
            writer.write( "<VOTABLE version=\"1.1\"" );
            writer.newLine();
            writer.write( "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" );
            writer.newLine();
            writer.write( "xsi:schemaLocation=\"http://www.ivoa.net/xml/VOTable/v1.1\"" );
            writer.newLine();
            writer.write( "xmlns=\"http://www.ivoa.net/xml/VOTable/v1.1\">" );
            writer.newLine();
            writer.write( "<RESOURCE>" );
            writer.newLine();

            StarJTable starJTable = null;
            StarTable table = null;
            VOSerializer serializer = null;
            Iterator<StarJTable> i = starJTables.iterator();
            while ( i.hasNext() ) {
                starJTable = i.next();
                table = starJTable.getStarTable();

                //  Write <TABLE> element. First need to remove FIELD
                //  IDS. These are no longer unique for the whole document.
                int n = table.getColumnCount();
                for ( int j = 0; j < n; j++ ) {
                    ColumnInfo ci = table.getColumnInfo( j );
                    ci.setAuxDatum( new DescribedValue( VOStarTable.ID_INFO, null ) );
                }
                serializer = VOSerializer.makeSerializer( DataFormat.TABLEDATA, table );
                serializer.writeInlineTableElement( writer );
            }
            writer.write( "</RESOURCE>" );
            writer.newLine();
            writer.write( "</VOTABLE>" );
            writer.newLine();
            writer.close();
        }
        catch (IOException e) {
            throw new SplatException( "Failed to save queries", e );
        }
            }

    /**
     * Return a StarTable of the currently selected tab of query results.
     */
    public StarTable getCurrentTable()
    {
        if ( starJTables != null && starJTables.size() > 0 ) {
            int index = resultsPane.getSelectedIndex();
            if ( index > -1 ) {
                StarJTable jTable = starJTables.get( index );
                return jTable.getStarTable();
            }
        }
        return null;
    }

    /**
     * Initialise the file chooser to have the necessary filters.
     */
    protected void initFileChooser()
    {
        if ( fileChooser == null ) {
            fileChooser = new BasicFileChooser( false );
            fileChooser.setMultiSelectionEnabled( false );

            //  Add a filter for XML files.
            BasicFileFilter xmlFileFilter =
                    new BasicFileFilter( "xml", "XML files" );
            fileChooser.addChoosableFileFilter( xmlFileFilter );

            //  But allow all files as well.
            fileChooser.addChoosableFileFilter
            ( fileChooser.getAcceptAllFileFilter() );
        }
    }

    /**
     * Set the proxy server and port.
     */
    protected void showProxyDialog()
    {
        if ( proxyWindow == null ) {
            ProxySetupFrame.restore( null );
            proxyWindow = new ProxySetupFrame();
        }
        proxyWindow.setVisible( true );
    }

    /**
     *  Close the window.
     */
    protected void closeWindowEvent()
    {
        Utilities.saveFrameLocation( this, prefs, "SSAQueryBrowser" );
        this.dispose();
    }

    /**
     * Configure the SSA servers.
     *
    protected void showServerWindow()
    {
        if ( serverWindow == null ) {
            serverWindow = new SSAServerFrame( serverList );
        }
        serverWindow.setVisible( true );
    }
    */
    public static void main( String[] args )
    {
        try {
            SSAQueryBrowser b =
                    new SSAQueryBrowser( new SSAServerList(), null );
            b.pack();
            b.setVisible( true );
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    //
    // ActionListener interface.
    //
    public void actionPerformed( ActionEvent e )
    {
        Object source = e.getSource();
        String cmd = e.getActionCommand();

        if ( source.equals( goButton ) ) {
            {
                doQuery();
               
            }        
            return;
        } 
      /*  if ( source.equals( showQueryButton ) ) {
            {
                try {
                    queryText.setText(queryLine.getQueryURLText()+ extendedQueryText);
                } catch (UnsupportedEncodingException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }        
            return;
        } */
        if ( source.equals( nameLookup ) /*|| source.equals( nameField ) */) {
            resolveName();
            isLookup=true;
           // queryLine.setPosition(raField.getText(), decField.getText());
            //updateQueryText();
            //try {
            //    String q = queryLine.getQueryURLText();
           //     queryText.setText(q);
         //   } catch (UnsupportedEncodingException e1) {
          //      // TODO Auto-generated catch block
           //     e1.printStackTrace();
          //  }
            return;
        } 
           
        if (source.equals(raField) || source.equals(decField) || source.equals(nameField)) {

                //  Get the position. Allow the object name to be passed using
                //  TARGETNAME, useful for solar system objects.
                String ra = raField.getText();
                String dec = decField.getText();
                String objectName = nameField.getText();
                if ( ra == null || ra.length() == 0 ||
                        dec == null || dec.length() == 0 ) {

                    //  Check for an object name. Need one or the other.
                    if ( objectName != null && objectName.length() > 0 ) {
                        ra = null;
                        dec = null;
                    }
                    else { 
                        //  To be clear.
                        ra = null;
                        dec = null;
                        objectName = null;
                    }
                }
                try {
                    queryLine.setPosition( ra, dec );
                    } catch (NumberFormatException nfe) {
                        ErrorDialog.showError( this, "Cannot understand this value", nfe );
                        return;
                    }
                queryLine.setTargetName(objectName);
                try {
                    queryText.setText(queryLine.getQueryURLText());
                } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                return;   
            }
            if ( source.equals( radiusField )  ) {
            
                String radiusText = radiusField.getText();
                double radius = 0.0;
                if ( radiusText != null && radiusText.length() > 0 ) {
                    try {
                        radius = Double.parseDouble( radiusText );
                    }
                    catch (NumberFormatException e1) {
                        ErrorDialog.showError( this, "Cannot understand radius value", e1);                         
                        return;
                    }
                }
                queryLine.setRadius(radius);
            
                try {
                    queryText.setText(queryLine.getQueryURLText());
                } catch (UnsupportedEncodingException e1) {
                // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                return;            
            }
            
            //  Spectral bandpass. These should be in meters. XXX allow other
            //  units and do the conversion.
            if (  source.equals( lowerBandField ) || source.equals( upperBandField )) {
                 
                String lowerBand = lowerBandField.getText();
                if ( "".equals( lowerBand ) ) {
                    lowerBand = null;
                }
                String upperBand = upperBandField.getText();
                if ( "".equals( upperBand ) ) {
                    upperBand = null;
                }
                  queryLine.setBand(lowerBand, upperBand);
                  try {
                      queryText.setText(queryLine.getQueryURLText());
                  } catch (UnsupportedEncodingException e1) {
                  // TODO Auto-generated catch block
                      e1.printStackTrace();
                  }
                  return;            
            }
           
            if (  source.equals( upperTimeField ) || source.equals( lowerTimeField ))  {
                
                String lowerTime = lowerTimeField.getText();
                if ( "".equals( lowerTime ) ) {
                    lowerTime = null;
                }
                String upperTime = upperTimeField.getText();
                if ( "".equals( upperTime ) ) {
                    upperTime = null;
                }
                  queryLine.setTime(lowerTime, upperTime);
                  try {
                      queryText.setText(queryLine.getQueryURLText());
                  } catch (UnsupportedEncodingException e1) {
                  // TODO Auto-generated catch block
                      e1.printStackTrace();
                  }
                  return;            
            }
            if ( source.equals(formatList)) {
                    queryLine.setFormat(formatList.getSelectedItem().toString());
                    try {
                        queryText.setText(queryLine.getQueryURLText());
                    } catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                
                return;     
            }
            if ( source.equals(wlcalibList)) {
                
                    queryLine.setWaveCalib(wlcalibList.getSelectedItem().toString());
                    try {
                        queryText.setText(queryLine.getQueryURLText());
                    } catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                
                return;     
            }
            if ( source.equals(flcalibList)) {
            
                    queryLine.setFluxCalib(flcalibList.getSelectedItem().toString());
                    try {
                        queryText.setText(queryLine.getQueryURLText());
                    } catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                
                return;     
            }

        if ( (source.getClass() == JTextField.class) && queryMetaParam.get(source) != null) {
        //    queryLine.setParam()
        }
                                 
        if ( source.equals( addParamButton ) ) 
        {
            //tree.setParamMap(serverParam);
            addParameter();
            if (extendedQueryText != null && extendedQueryText.length() > 0) 
            {
                try {
                    queryText.setText(queryLine.getQueryURLText() + extendedQueryText );
                } catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
          
            return;
        } 
        if ( source.equals( removeParamButton ) ) 
        {
            // de-select and update query text
            
            metaPanel.removeSelectedMetadata();
        /*    if (extendedQueryText != null && extendedQueryText.length() > 0) 
            {
                try {
                    queryText.setText(queryLine.getQueryURLText() + extendedQueryText );
                } catch (UnsupportedEncodingException e1) {
                        // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
         */ 
            return;
        } 

        if ( source.equals( customButton ) ) {
   //         if (metaFrame == null) {
                customParameters();
   //         } else {
   //             metaFrame.openWindow();
    //        }
            return;
        }

        if ( source.equals( displaySelectedButton ) ) {
            displaySpectra( true, true, null, -1 );
            return;
        }
        if ( source.equals( displayAllButton ) ) {
            displaySpectra( false, true, null, -1 );
            return;
        }

        if ( source.equals( downloadSelectedButton ) ) {
            displaySpectra( true, false, null, -1 );
            return;
        }
        if ( source.equals( downloadAllButton ) ) {
            displaySpectra( false, false, null, -1 );
            return;
        }

        if ( source.equals( deselectVisibleButton ) ) {
            deselectSpectra( false );
            return;
        }
        if ( source.equals( deselectAllButton ) ) {
            deselectSpectra( true );
            return;
        }

    }

    /**
     * Event listener to trigger a metadata update. Triggered by SSAMetadataFrame
     * the table has been updated
     */
    public void propertyChange(PropertyChangeEvent pvt)
    {
       updateQueryText();        
    }
    
    private void updateQueryText() {
        
        if (metaPanel != null)
            extendedQueryText=metaPanel.getParamsQueryString(); 
      
            try {
                queryText.setText(queryLine.getQueryURLText() + extendedQueryText);
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }           
    }

    /**
     * addParameter
     * Adds an optional metadata parameter to the query.
     * Allows the user to select a list of parameters, according to the selected list of servers
     * update the parameter table
     */
    private void addParameter() 
    {
        // check serverlist (selected servers!!)
        ArrayList<String> parameters = new ArrayList();
        Iterator srv=serverList.getIterator();
        while ( srv.hasNext() ) {
            SSAPRegResource server = (SSAPRegResource) srv.next();
            if (serverList.isServerSelected(server.getShortName())) {
                ArrayList<String> serverpars = serverParam.getParams(server.getShortName());
                if (serverpars != null )
                    parameters.addAll(serverpars);
            }
        }
        // make the list unique 
        HashSet<String> hs = new HashSet<String>();
        hs.addAll(parameters);
        parameters.clear();
        parameters.addAll(hs);
        Collections.sort(parameters);
        
        // remove the INPUT: Prefix from the parameters
        for (int i=0; i<parameters.size(); i++) {
           // String param = parameters.get(i).substring(6); // INPUT: = 6 characters
            parameters.set(i,parameters.get(i).substring(6));// INPUT: = 6 characters
        }
        
         
        Object selectedValue = JOptionPane.showInputDialog(this, "Supported Parameters", "Input", JOptionPane.INFORMATION_MESSAGE, null, 
                (Object[]) parameters.toArray(), null);
        if (selectedValue != null) {
            
            metaPanel.addRow(metaParam.get("INPUT:"+selectedValue.toString()), false); // INPUT: has to be added again
    //        metaPanel.
            metaPanel.setVisible(true);
        }
        metaPanel.repaint();
       // queryMetaParam.put(txtf, selectedValue.toString());
      //  JLabel label = new JLabel(selectedValue.toString() + ":");
      //  customQueryPanel.add(label); queryPanel.add(txtf);
       // customQueryPanel.repaint();
      
    }
    /**
     * Customize metadata parameters
     * Query metadata from known servers to retrieve all possible parameters. Every
     * query is performed in a different thread, which will perform the query and
     * update the parameter table
     */
    private void customParameters() 
    {
        int nrServers = serverList.getSize();
        final WorkQueue workQueue = new WorkQueue(nrServers);

        metaParam = new HashMap<String, MetadataInputParameter>();
     
/*
        if ( metaFrame == null ) {
            metaFrame = new SSAMetadataFrame( metaParam );
            metaFrame.addPropertyChangeListener(this);
        } else
            metaFrame.updateMetadata( metaParam );
*/
        final ProgressPanelFrame metadataProgressFrame = new ProgressPanelFrame( "Querying Metadata" );

        Iterator i = serverList.getIterator();

        while( i.hasNext() ) {

            final SSAPRegResource server = (SSAPRegResource) i.next();
            final ProgressPanel metadataProgressPanel = new ProgressPanel( "Querying: " + server.getShortName());
            metadataProgressFrame.addProgressPanel( metadataProgressPanel );
            //final MetadataQueryWorker queryWorker  = new MetadataQueryWorker(server, workQueue);
            final MetadataQueryWorker queryWorker  = new MetadataQueryWorker(workQueue, server, metadataProgressPanel);
            queryWorker.start();
            final MetadataProcessWorker processWorker  = new MetadataProcessWorker(workQueue);           
            processWorker.start();

        }// while


        /*
         *  XXX to do eventually: create the parameter frame only after the first parameter has been found. After all threads are
         *  done, show a dialog message in case no parameters have been found.
         */
       // tree.setParamMap(serverParam);
    } // customParameters

    /**
     *  Metadata Query worker class
     *  makes the query, adds resulting metadata to the queue, notify the metadata process worker 
     *  
     * @author mm
     *
     */

    class MetadataQueryWorker extends SwingWorker 
    {
        URL queryURL=null;
        SSAPRegResource server=null;
        ParamElement [] metadata = null;
        WorkQueue workQueue=null;
        ProgressPanel progressPanel = null;

        /**
         * Constructor
         * @param queue  the work queue
         * @param server the server which to query for metadata
         */
        public MetadataQueryWorker(WorkQueue queue, SSAPRegResource server) {
            this.server=server;
            this.workQueue=queue;
        }
        /**
         * Constructor
         * @param queue  the work queue
         * @param server the server which to query for metadata
         * @param panel  the progress panel
         */
        public MetadataQueryWorker(WorkQueue queue, SSAPRegResource server,ProgressPanel panel) {
            this.server=server;
            this.workQueue=queue;
            this.progressPanel=panel;
        }

        public Object construct()
        {   
            final SSAMetadataParser ssaMetaParser = new SSAMetadataParser( server );    
               if (progressPanel != null)
                progressPanel.start();            

            try {
                queryURL = ssaMetaParser.getQueryURL();
                logger.info( "Querying metadata from " + queryURL + " contact:" + server.getContact() );
            } catch (MalformedURLException e) {
                if (progressPanel != null)
                    progressPanel.logMessage("Malformed URL");
                queryURL=null;
            }
            if ( queryURL != null ) 
            {
                try {                 
                    if (progressPanel != null)
                        metadata = ssaMetaParser.queryMetadata( queryURL, progressPanel );
                    else
                        metadata = ssaMetaParser.queryMetadata( queryURL );

                } catch (InterruptedException e) {
                    if (progressPanel != null)
                        progressPanel.logMessage("Interrupted");
                    metadata = null;
                }
                catch (Exception e) {
                    if (progressPanel != null)
                        progressPanel.logMessage("Other Exception"); // this should not happen
                    e.printStackTrace();
                    metadata = null;
                }
            } else
                metadata = null;

            if (progressPanel != null)
                progressPanel.stop();  
            // add results to the queue
            workQueue.setServer(server);
            workQueue.addWork(metadata);
            return null;
        } //doinbackground

        public void finished()
        {
            //  display the final status of the query
            if ( metadata != null ) {
                // adds parameter information into the metaParam hash
                logger.info("RESPONSE "+queryURL+"returned "+ metadata.length +"parameters ");
                progressPanel.logMessage( metadata.length +"  input parameters found");
            } else {
                logger.info( "RESPONSE No input parameters loaded from " + queryURL );                
            }
            if (progressPanel != null)
                progressPanel.stop();  

        } // done

    };

    /**
     *  Metadata Process worker class
     *  makes the query, adds resulting metadata to the queue, notify the metadata process worker 
     *  
     * @author Margarida Castro Neves
     *
     */

    class MetadataProcessWorker extends SwingWorker 
    {
        WorkQueue workQueue=null;

        public MetadataProcessWorker( WorkQueue queue) {          
            this.workQueue=queue;
        }
        public Object construct()
        {
            // progressPanel.start();
            try {
                ParamElement [] data = workQueue.getWork();
                if ( data != null)
                    processMetadata(data, workQueue.getServer());
            } 
            catch (InterruptedException e) {
            }
            catch (Exception e) {
            }

            return null;
        } //construct


        public void finished()
        {
        } // done

    };



    /**
     * queue that receives information from the QueryWorker threads
     * and process them
     */
    class WorkQueue {
        LinkedList<Object> queue = new LinkedList<Object>();
        int workedItems = 0;
        int maxItems=0;
        SSAPRegResource server;

        public WorkQueue( int total ) {
            maxItems = total;
        }

        // add work to the queue
        public synchronized void addWork(Object o) {
            //  logger.info( "ADDWORK " + workedItems);
            queue.addLast(o);
            this.notify();
        }

        // takes the work from the queue as soon as it's not empty
        public synchronized ParamElement[] getWork() throws InterruptedException {
            //  logger.info( "GETWORK " + workedItems + " " + maxItems);
            if (workedItems >= maxItems) return null;
            while (queue.isEmpty()) {
                this.wait();
            }
            ParamElement [] data = (ParamElement[]) queue.removeFirst();     
            workedItems++;
            return (data);
        }
        
        public void setServer( SSAPRegResource server ) {
            this.server=server;
        }
        public SSAPRegResource getServer(  ) {
            return this.server;
        }
    } // WorkerQueue


    /**
     * adds the parameters to a hashmap. Every parameter should be unique, 
     * and a counter shows how many servers support each parameter  
     * Exclude the parameters that are already included in the main menues of splat query browser
     * @param - metadata the parameters read from all servers 
     * 
     */
    private void processMetadata( ParamElement[] metadata, SSAPRegResource server) {

        int i=0;
        //boolean changed = false;

        while ( i < metadata.length ) {
            String paramName = metadata[i].getName();
            if (! paramName.equalsIgnoreCase("INPUT:POS") &&        // these parameters should be entered in the main browser
                    ! paramName.equalsIgnoreCase("INPUT:SIZE") &&
                    ! paramName.equalsIgnoreCase("INPUT:BAND") &&
                    ! paramName.equalsIgnoreCase("INPUT:TIME") && 
                    ! paramName.equalsIgnoreCase("INPUT:FORMAT") && 
                    ! paramName.equalsIgnoreCase("INPUT:WAVECALIB") && 
                    ! paramName.equalsIgnoreCase("INPUT:FLUXCALIB") && 
                    ! paramName.equalsIgnoreCase("INPUT:TARGETNAME") ) 
            {
                addRelation( server.getShortName(), paramName);
                MetadataInputParameter mip = new MetadataInputParameter(metadata[i]);
                addMetaParam(mip);    // call static synchronized method to change the data with mutual exclusion             
            } // if
            i++;
        } // while

    }//processMetadata
    private synchronized static void addRelation( String server, String param ) {
        
        serverParam.addRelation(server, param);

    }

    /*
     * Adds parameters to the hash and to the parameter table
     * 
     * @param mip - the metadata parameter object
     */
    private synchronized static void addMetaParam( MetadataInputParameter mip) {

        if (! metaParam.containsKey(mip.getName())) 
        {
            metaParam.put( mip.getName(), mip );
            // add new element to the parameter table
         //   metaPanel.addRow(mip); //!! frame

        } else {
            // increase counter of existing hash member
            mip = metaParam.get(mip.getName());
            mip.increase();
            metaParam.put( mip.getName(), mip );
            // update nr server column in the table
    //        metaPanel.setNrServers(mip.getCounter(), mip.getName()); // !! Frame
        }
        
    }
    //
    // MouseListener interface. Double clicks display the clicked spectrum.
    //
    public void mousePressed( MouseEvent e ) {}
    public void mouseReleased( MouseEvent e ) {}
    public void mouseEntered( MouseEvent e ) {}
    public void mouseExited( MouseEvent e ) {}
    public void mouseClicked( MouseEvent e )
    {
       
        //requestFocusInWindow();
     //   if (e.getSource().getClass() == StarTable.class ) {

            if ( e.getClickCount() == 2 ) {
                StarJTable table = (StarJTable) e.getSource();
                Point p = e.getPoint();
                int row = table.rowAtPoint( p );
                displaySpectra( false, true, table, row );
            }
      //  }
    }

    //
    //  Action for switching name resolvers.
    //
    class ResolverAction
    extends AbstractAction
    {
        SkycatCatalog resolver = null;
        public ResolverAction( String name, SkycatCatalog resolver )
        {
            super( name );
            this.resolver = resolver;
        }
        public void actionPerformed( ActionEvent e )
        {
            resolverCatalogue = resolver;
        }
    }

    //
    // LocalAction to encapsulate all trivial local Actions into one class.
    //
    class LocalAction
    extends AbstractAction
    {
        //  Types of action.
        public static final int PROXY = 0;
        public static final int SERVER = 1;
        public static final int SAVE = 2;
        public static final int READ = 3;
        public static final int CLOSE = 4;

        //  The type of this instance.
        private int actionType = PROXY;

        public LocalAction( int actionType, String name )
        {
            super( name );
            this.actionType = actionType;
        }

        public LocalAction( int actionType, String name, Icon icon,
                String help )
        {
            super( name, icon );
            putValue( SHORT_DESCRIPTION, help );
            this.actionType = actionType;
        }

        public LocalAction( int actionType, String name, Icon icon,
                String help, String accel )
        {
            this( actionType, name, icon, help );
            putValue( ACCELERATOR_KEY, KeyStroke.getKeyStroke( accel ) );
        }

        public void actionPerformed( ActionEvent ae )
        {
            switch ( actionType )
            {
                case PROXY: {
                    showProxyDialog();
                    break;
                }
                case SERVER: {
                 //   showServerWindow();
                    break;
                }
                case SAVE: {
                    saveQueryToFile();
                    break;
                }
                case READ: {
                    readQueryFromFile();
                    break;
                }
                case CLOSE: {
                    closeWindowEvent();
                    break;
                }
            }
        }
    }


 
    /**
     * A Metadata input parameter class, to contain the parameter element and the number of servers that
     * accept this parameter
     */

    public class MetadataInputParameter
    {
        ParamElement param;
        String paramName = null;
        String description = null;
        String unit = null;
        String value = null;
        String nullValue = null;
        String[] options;
        //ValuesElement values = null;
        String datatype = null;
        String min=null;    
        String max=null;


        int counter;

        MetadataInputParameter(ParamElement param) {

            this.param = param; // TO DO  decide if it is really necessary to keep this object here...
            counter=1; 
            paramName = param.getName();
            description = param.getDescription();
            if (description == null)
                description = "";
            datatype = param.getDatatype();
            value = param.getValue();
            //if (value.equals("NaN") || value.contains("null") || value.contains("NULL") || value.contains("Null"))
            //    value = "";
            unit = param.getUnit();

            /* To be done later after some questions have been cleared:
             * what to do if different servers have parameter with same name and different default values, units and limits?
             * 
             * 
    		ValuesElement values = param.getActualValues(); // or legal values??
    		if  (values != null) {
    		        options = values.getOptions();
    		        max = values.getMaximum();
    		        min = values.getMinimum();
    		        nullValue=values.getNull();
    		}
             */	    		
        }
        protected int getCounter() {
            return counter;
        }
        protected String getName() {
            return paramName;
        }
        protected String getValue() {
            return value;
        }
        protected String getDescription() {
            return description;
        }
        protected String getDatatype() {
            return datatype;
        }
        protected String getUnit() {
            return unit;
        }
        /*
    	protected void setDescription(String description) {
    		 this.description = description;
    	}
    	protected void setDatatype(String datatype) {
   		 this.datatype = datatype;
    	}
    	protected void setUnit(String unit) {
      		 this.datatype = unit;
       	}
         */
        protected void increase() {
            counter++;
        }
    } // MetadataInputParameter


    public void changedUpdate(DocumentEvent de) {
        // Plain text components don't fire these events.
    }

    public void insertUpdate(DocumentEvent de) {
        
        //get the owner of this document
        Object owner = de.getDocument().getProperty("owner");
        if(owner != null){
            if (owner == nameField ) {
                queryLine.setTargetName(nameField.getText());
            }
            if (owner == radiusField ) {
                String radiusText = radiusField.getText();
                double radius = 0.0;
                if ( radiusText != null && radiusText.length() > 0 ) {
                    try {
                        radius = Double.parseDouble( radiusText );
                    }
                    catch (NumberFormatException e1) {
                        ErrorDialog.showError( this, "Cannot understand radius value", e1);                         
                        return;
                    }
                }
                queryLine.setRadius(radius);
            }
            if (owner == raField || owner == decField ) {
                if (raField.getText().length() > 0 && decField.getText().length() > 0 )
                    try {
                            queryLine.setPosition(raField.getText(), decField.getText());
                    } catch (NumberFormatException nfe) {
                        ErrorDialog.showError( this, "Invalid coordinate format", nfe);
                        return;
                    }
            }
           
            if (owner == upperBandField || owner == lowerBandField ) {
                queryLine.setBand(lowerBandField.getText(), upperBandField.getText());
            }
            
            if (owner == upperTimeField || owner == lowerTimeField) {
                queryLine.setTime(lowerTimeField.getText(), upperTimeField.getText());
            }
            
        }
        updateQueryText();
        
    }

    public void removeUpdate(DocumentEvent de) {
        Object owner = de.getDocument().getProperty("owner");
        
        if(owner != null){
            if (owner == nameField ) {
                nameField.setForeground(Color.black);
                queryLine.setTargetName(nameField.getText());
                // when name is changed, remove current coordinates.
                queryLine.setPosition(0, 0);
                raField.setText("");
                decField.setText("");
            }
            if (owner == raField || owner == decField ) {
                
                try {
                        queryLine.setPosition(0, 0);
                    } catch (NumberFormatException nfe) {
                        ErrorDialog.showError( this, "Invalid coordinate format", nfe);
                        return;
                    }
                if  ( isLookup ) {
                    isLookup=false;
                    nameField.setForeground(Color.black);
                } else  {     // coordinate manually changed ( not through lookup)
                   //queryLine.setTargetName("");
                   nameField.setForeground(Color.gray);
                }
                //nameField.setText("");
            }
        }
        updateQueryText();
        
    }
}
