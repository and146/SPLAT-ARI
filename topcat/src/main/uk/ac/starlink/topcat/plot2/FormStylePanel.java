package uk.ac.starlink.topcat.plot2;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListModel;
import javax.swing.border.BevelBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import uk.ac.starlink.ttools.plot2.PlotUtil;
import uk.ac.starlink.ttools.plot2.Plotter;
import uk.ac.starlink.ttools.plot2.config.ConfigKey;
import uk.ac.starlink.ttools.plot2.config.ConfigMap;
import uk.ac.starlink.topcat.ActionForwarder;
import uk.ac.starlink.topcat.AuxWindow;
import uk.ac.starlink.topcat.RowSubset;
import uk.ac.starlink.topcat.TopcatModel;
import uk.ac.starlink.util.IconUtils;
import uk.ac.starlink.util.gui.ShrinkWrapper;

/**
 * GUI component for acquiring style information for a plot for
 * each row subset of a particular table.
 * One part of the panel allows selection of global (per-table) style
 * configuration, and another part allows selection of subset-specific
 * overrides.
 *
 * @author   Mark Taylor
 * @since    15 Mar 2013
 */
public class FormStylePanel extends JPanel {

    private final Configger plotConfigger_;
    private final Factory<Plotter> plotterFact_;
    private final SubsetConfigManager subManager_;
    private final ConfigSpecifier globalSpecifier_;
    private final ActionForwarder forwarder_;
    private final Map<RowSubset,ConfigMap> subsetConfigs_;
    private final JLabel iconLabel_;
    private final JComponent subsetSpecifierHolder_;
    private final JComboBox subsetSelector_;
    private final ConfigSpecifier subsetSpecifier_;

    /**
     * Constructor.
     *
     * @param  keys  style configuration keys that this panel is to acquire
     * @param  plotConfigger   global config defaults
     * @param  plotterFact   obtains on demand the plotter for which this
     *                       panel is acquiring style information
     * @param  subManager   provides per-subset defaults for some config keys
     * @param  tcModel   topcat model whose subsets are being configured
     */
    public FormStylePanel( ConfigKey[] keys, Configger plotConfigger,
                           Factory<Plotter> plotterFact,
                           SubsetConfigManager subManager,
                           TopcatModel tcModel ) {
        setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );
        plotConfigger_ = plotConfigger;
        plotterFact_ = plotterFact;
        subManager_ = subManager;
        forwarder_ = new ActionForwarder();

        /* Set up specifier for global keys. */
        globalSpecifier_ =
            new OptionalConfigSpecifier( keys, subManager.getConfigKeys(),
                                         "By Subset" );

        /* Ensure that any change to the global key specifier results in
         * an immediate change to the current state of the per-subset
         * configs. */
        globalSpecifier_
           .addActionListener( new GlobalChangeListener( globalSpecifier_ ) );

        /* Ensure that any change to the subset manager specifiers results in
         * an immediate change to the current state of the appropriate
         * per-subset configs. */
        subManager.addActionListener( new SubChangeListener( subManager ) );

        /* Place global specifier component. */
        JComponent globalComp = globalSpecifier_.getComponent();
        globalComp.setBorder( AuxWindow.makeTitledBorder( "Global Style" ) );
        add( globalComp );

        /* Place subset specifier components. */
        iconLabel_ = new JLabel();
        iconLabel_.setBorder( BorderFactory
                             .createBevelBorder( BevelBorder.RAISED ) );
        Dimension iconSize = new Dimension( 24, 24 );
        iconLabel_.setPreferredSize( iconSize );
        iconLabel_.setMinimumSize( iconSize );
        iconLabel_.setMaximumSize( iconSize );
        subsetSpecifierHolder_ = Box.createVerticalBox();

        /* Set up a selector which will allow access to per-subset configs. */
        subsetSelector_ =
            new JComboBox( new Plus1ListModel( tcModel.getSubsets(), null ) );
        subsetSelector_.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                Object item = subsetSelector_.getSelectedItem();
                RowSubset subset = item instanceof RowSubset
                                 ? (RowSubset) item
                                 : null;
                if ( subset != null ) {
                    restoreConfig( subset );
                }
                updateLegendIcon();
                setSubsetSpecifierActive( subset != null );
            }
        } );

        /* Set up a per-subset specifier which can be configured for
         * different subsets. */
        subsetSpecifier_ = new ConfigSpecifier( keys );
        subsetSpecifier_.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                Object item = subsetSelector_.getSelectedItem();
                if ( item instanceof RowSubset ) {
                    saveConfig( (RowSubset) item );
                }
                updateLegendIcon();
                forwarder_.actionPerformed( evt );
            }
        } );
        subsetConfigs_ = new HashMap<RowSubset,ConfigMap>();
        subsetSelector_.setSelectedItem( null );

        /* Place components. */
        JComponent subsetPanel = Box.createVerticalBox();
        JComponent subsetLine = Box.createHorizontalBox();
        subsetLine.add( new JLabel( "Subset: " ) );
        subsetLine.add( new ShrinkWrapper( subsetSelector_ ) );
        subsetLine.add( Box.createHorizontalStrut( 10 ) );
        subsetLine.add( iconLabel_ );
        subsetLine.add( Box.createHorizontalGlue() );
        subsetPanel.add( subsetLine );
        subsetPanel.add( Box.createVerticalStrut( 5 ) );
        subsetPanel.add( subsetSpecifierHolder_ );
        subsetPanel.setBorder( AuxWindow.makeTitledBorder( "Subset Styles" ) );
        add( subsetPanel );
    }

    /**
     * Adds a listener which will be notified when there is a change to
     * any of this panel's configuration.
     *
     * @param listener  listener to add
     */
    public void addActionListener( ActionListener listener ) {
        forwarder_.addActionListener( listener );
    }

    /**
     * Removes a listener previously added.
     *
     * @param   listener   listener to remove
     */
    public void removeActionListener( ActionListener listener ) {
        forwarder_.removeActionListener( listener );
    }

    /**
     * Returns the configuration for one of this panel's row subsets.
     * This is a combination of global and per-subset selected items.
     *
     * @param   subset  row subset
     * @return   style configuration
     */
    public ConfigMap getConfig( RowSubset subset ) {
        ConfigMap config = getDefaultSubsetConfig( subset );
        ConfigMap subMap = subsetConfigs_.get( subset );
        if ( subMap != null ) {
            config.putAll( subMap );
        }
        return config;
    }

    /**
     * Stores the current state of the per-subset specifier component
     * as the value for a given subset.
     *
     * @param  subset  row subset
     */
    private void saveConfig( RowSubset subset ) {
        subsetConfigs_.put( subset, subsetSpecifier_.getSpecifiedValue() );
    }

    /**
     * Sets the state of the per-subset specifier component to the value
     * stored for a given subset.  A default configuration is created lazily
     * if no value has previously been stored for the subset.
     *
     * @param  row subset
     */
    private void restoreConfig( RowSubset subset ) {

        /* Lazily create an entry if no config has explicitly been saved
         * for subset. */
        if ( ! subsetConfigs_.containsKey( subset ) ) {
            ConfigMap config = getDefaultSubsetConfig( subset );
            config.keySet().retainAll( Arrays.asList( subsetSpecifier_
                                                     .getConfigKeys() ) );
            subsetConfigs_.put( subset, config );
        }

        /* Retrieve config value for subset and restore the GUI from it. */
        ConfigMap config = subsetConfigs_.get( subset );
        subsetSpecifier_.setSpecifiedValue( subsetConfigs_.get( subset ) );
    }

    /**
     * Returns the default config values for a given subset.
     * This is not affected by user actions in this component.
     *
     * @param  subset  row subset
     * @return   default config
     */
    private ConfigMap getDefaultSubsetConfig( RowSubset subset ) {
        ConfigMap config = plotConfigger_.getConfig();
        config.putAll( subManager_.getConfigger( subset ).getConfig() );
        config.putAll( globalSpecifier_.getSpecifiedValue() );
        return config;
    }

    /**
     * Configures the per-subset specifier to be capable of user interaction
     * or not.
     * Currently, the components are removed from the GUI when inactive.
     * 
     * @param  isActive  whether per-subset specifier will be usable
     */
    private void setSubsetSpecifierActive( boolean isActive ) {
        subsetSpecifierHolder_.removeAll();
        if ( isActive ) {
            subsetSpecifierHolder_.add( subsetSpecifier_.getComponent() );
        }
        subsetSpecifierHolder_.revalidate();
    }

    /**
     * There is space for a little icon near the per-subset specifier.
     * This updates it to make sure it shows the right thing, which
     * will change according to which subset is being configured and
     * the state of its configuration.
     */
    private void updateLegendIcon() {
        final Icon icon;
        if ( subsetSelector_.getSelectedItem() instanceof RowSubset ) {
            ConfigMap config = subsetSpecifier_.getSpecifiedValue();
            icon = plotterFact_.getItem().createStyle( config ).getLegendIcon();
        }
        else {
            icon = IconUtils.emptyIcon( 24, 24 );
        }
        iconLabel_.setIcon( icon );
    }

    /**
     * Listens to changes in the global style config panel.
     * Any changes made explicitly to it are written in to the per-subset
     * config records, thus overwriting them.
     */
    private class GlobalChangeListener implements ActionListener {
        private final ConfigSpecifier configSpecifier_;
        private ConfigMap lastConfig_;
 
        /**
         * Constructor.
         *
         * @param  configSpecifier   specifier we are listening to
         */
        GlobalChangeListener( ConfigSpecifier configSpecifier ) {
            configSpecifier_ = configSpecifier;
            lastConfig_ = configSpecifier_.getSpecifiedValue();
        }

        public void actionPerformed( ActionEvent evt ) {

            /* Find out what config items have just changed. */
            ConfigMap config = configSpecifier_.getSpecifiedValue();
            Set<ConfigKey<?>> changeSet = new HashSet<ConfigKey<?>>();
            for ( ConfigKey<?> key : config.keySet() ) {
                if ( ! PlotUtil.equals( config.get( key ),
                                        lastConfig_.get( key ) ) ) {
                    changeSet.add( key );
                }
            }
            lastConfig_ = config;

            /* Where applicable write those into the stored per-subset
             * records. */
            if ( ! changeSet.isEmpty() ) {
                ConfigMap changeMap = new ConfigMap( config );
                changeMap.keySet().retainAll( changeSet );
                for ( RowSubset rset : subsetConfigs_.keySet() ) {
                    ConfigMap savedConfig = subsetConfigs_.get( rset );
                    savedConfig.putAll( changeMap );
                    if ( rset == subsetSelector_.getSelectedItem() ) {
                        subsetSpecifier_.setSpecifiedValue( savedConfig );
                    }
                }
            }

            /* Notify listeners. */
            forwarder_.actionPerformed( evt );
        }
    }

    /**
     * Listens for changes in the subset manager configuration.
     * Any relevant changes made explicitly to it are written in to
     * the per-subset config records, thus overwriting them.
     */
    private class SubChangeListener implements ActionListener {
        private final SubsetConfigManager subManager_;
        private Map<RowSubset,ConfigMap> lastConfigs_;

        /**
         * Constructor.
         *
         * @param  subManager   subset config manager we are listening to
         */
        SubChangeListener( SubsetConfigManager subManager ) {
            subManager_ = subManager;
            lastConfigs_ = new HashMap<RowSubset,ConfigMap>();
        }

        public void actionPerformed( ActionEvent evt ) {
            boolean changed = false;

            /* Iterate over known subsets. */
            for ( RowSubset rset : subsetConfigs_.keySet() ) {

                /* Work out what config items have changed for the current
                 * row subset since last time. */
                ConfigMap lastConfig = lastConfigs_.get( rset );
                if ( lastConfig == null ) {
                    lastConfig = new ConfigMap();
                }
                ConfigMap config =
                    subManager_.getConfigger( rset ).getConfig();
                Set<ConfigKey<?>> changeSet = new HashSet<ConfigKey<?>>();
                for ( ConfigKey<?> key : config.keySet() ) {
                    if ( ! PlotUtil.equals( config.get( key ),
                                            lastConfig.get( key ) ) ) {
                        changeSet.add( key );
                    }
                }
                lastConfigs_.put( rset, config );

                /* Where applicable write those changes into the stored
                 * per-subset records. */
                if ( ! changeSet.isEmpty() ) {
                    ConfigMap changeMap = new ConfigMap( config );
                    changeMap.keySet().retainAll( changeSet );
                    ConfigMap savedConfig = subsetConfigs_.get( rset );
                    savedConfig.putAll( changeMap );
                    if ( rset == subsetSelector_.getSelectedItem() ) {
                        subsetSpecifier_.setSpecifiedValue( savedConfig );
                    }
                }
                changed = true;
            }

            /* Notify listeners if anything actually happened. */
            if ( changed ) {
                forwarder_.actionPerformed( evt );
            }
        }
    }

    /**
     * Wrapper ComboBoxModel which just adds an entry before all the
     * existing ones.  It is backed by a live base model, and any changes
     * to that will be reflected here.
     */
    private static class Plus1ListModel extends AbstractListModel
                                        implements ComboBoxModel {
        private final ListModel baseModel_;
        private final Object item0_;
        private Object selectedItem_;

        /**
         * Constructor.
         *
         * @param  baseModel   base list model
         * @param  item0  entry to insert first in the list
         */
        Plus1ListModel( ListModel baseModel, Object item0 ) {
            baseModel_ = baseModel;
            item0_ = item0;
            baseModel.addListDataListener( new ListDataListener() {
                public void contentsChanged( ListDataEvent evt ) {
                    fireContentsChanged( evt.getSource(),
                                         evt.getIndex0(), evt.getIndex1() );
                }
                public void intervalAdded( ListDataEvent evt ) {
                    fireIntervalAdded( evt.getSource(),
                                       evt.getIndex0(), evt.getIndex1() );
                }
                public void intervalRemoved( ListDataEvent evt ) {
                    fireIntervalRemoved( evt.getSource(),
                                         evt.getIndex0(), evt.getIndex1() );
                }
            } );
        }

        public int getSize() {
            return baseModel_.getSize() + 1;
        }

        public Object getElementAt( int ix ) {
            return ix == 0 ? item0_ : baseModel_.getElementAt( ix - 1 );
        }

        public void setSelectedItem( Object item ) {
            selectedItem_ = item;
        }

        public Object getSelectedItem() {
            return selectedItem_;
        }
    }
}
