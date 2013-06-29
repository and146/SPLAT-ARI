package uk.ac.starlink.topcat.plot2;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import uk.ac.starlink.topcat.ResourceIcon;
import uk.ac.starlink.topcat.ToggleButtonModel;
import uk.ac.starlink.ttools.plot2.Captioner;
import uk.ac.starlink.ttools.plot2.LegendEntry;
import uk.ac.starlink.ttools.plot2.LegendIcon;
import uk.ac.starlink.ttools.plot2.config.StyleKeys;

/**
 * Control for defining legend characteristics.
 *
 * @author   Mark Taylor
 * @since    13 Mar 2013
 */
public class LegendControl extends TabberControl {

    /* This class could perhaps be written in a more generic way
     * (subclassing ConfigControl). */

    private final ControlStackModel stackModel_;
    private final Configger configger_;
    private final ToggleButtonModel visibleModel_;
    private final ToggleButtonModel opaqueModel_;
    private final ToggleButtonModel borderModel_;
    private final ToggleButtonModel insideModel_;
    private final SquarePusher pusher_;
    private boolean everMadeVisible_;
    private boolean everMadeInvisible_;

    /**
     * Constructor.
     *
     * @param   stackModel   model containing layer controls
     * @param   configger   config source containing some plot-wide config,
     *                      specifically captioner style
     */
    public LegendControl( ControlStackModel stackModel, Configger configger ) {
        super( "Legend", ResourceIcon.LEGEND );
        stackModel_ = stackModel;
        configger_ = configger;
        final ActionListener forwarder = getActionForwarder();

        /* Set up control for selecting whether legend is visible at all. */
        visibleModel_ = new ToggleButtonModel( "Show Legend", null,
                                               "Whether to display legend "
                                             + "near plot" );
        visibleModel_.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                boolean isVis = visibleModel_.isSelected();
                if ( isVis ) {
                    everMadeVisible_ = true;
                }
                else {
                    everMadeInvisible_ = true;
                }
                forwarder.actionPerformed( evt );
            }
        } );

        /* Set up control for background opacity. */
        opaqueModel_ =
            new ToggleButtonModel( "Opaque", null,
                                   "Whether background is opaque white, "
                                 + "or transparent" );
        opaqueModel_.addActionListener( forwarder );
        opaqueModel_.setSelected( true );

        /* Set up control for whether to draw line border. */
        borderModel_ =
            new ToggleButtonModel( "Border", null,
                                   "Whether to draw border around legend box" );
        borderModel_.setSelected( true );
        borderModel_.addActionListener( forwarder );

        /* Set up control for internal/external legend. */
        insideModel_ =
            new ToggleButtonModel( "Internal", null,
                                   "Draw legend within the plot bounds"
                                 + " or outside them" );
        insideModel_.addChangeListener( new ChangeListener() {
            public void stateChanged( ChangeEvent evt ) {
                pusher_.setEnabled( insideModel_.isSelected() );
            }
        } );
        insideModel_.addActionListener( forwarder );

        /* Set up control for internal legend position. */
        pusher_ = new SquarePusher();
        pusher_.setEnabled( insideModel_.isSelected() );
        pusher_.addActionListener( forwarder );

        /* Update visibility defaults based on how many entries the legend
         * would have - it's not very useful if it only has one entry.
         * But once it's appeared once keep it, because it's more distracting
         * to have it keep appearing and disappearing. */
        stackModel_.addPlotActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent evt ) {
                if ( getLegendEntries().length > 1 &&
                     ! everMadeInvisible_ ) {
                    visibleModel_.setSelected( true );
                }
            }
        } );

        /* Style panel. */
        JComponent styleBox = Box.createVerticalBox();
        styleBox.add( new LineBox( visibleModel_.createCheckBox() ) );
        styleBox.add( new LineBox( opaqueModel_.createCheckBox() ) );
        styleBox.add( new LineBox( borderModel_.createCheckBox() ) );
        addControlTab( "Style", styleBox, true );

        /* Position panel. */
        JComponent posBox = new JPanel( new BorderLayout() );
        JComponent topBox = Box.createVerticalBox();
        JRadioButton[] butts =
            insideModel_.createRadioButtons( "External", "Internal" );
        topBox.add( new LineBox( butts[ 0 ] ) );
        topBox.add( new LineBox( butts[ 1 ] ) );
        posBox.add( topBox, BorderLayout.NORTH );
        posBox.add( pusher_, BorderLayout.CENTER );
        addControlTab( "Position", posBox, false );
    }

    /**
     * Returns the legend icon for the current state of the stack model.
     *
     * @return  legend icon, or null if not visible
     */
    public Icon getLegendIcon() {
        if ( visibleModel_.isSelected() ) {
            LegendEntry[] entries = getLegendEntries();
            if ( entries.length == 0 ) {
                return null;
            }
            else {
                Captioner captioner =
                    StyleKeys.createCaptioner( configger_.getConfig() );
                boolean border = borderModel_.isSelected();
                Color bgColor = opaqueModel_.isSelected() ? Color.WHITE : null;
                return new LegendIcon( entries, captioner, border, bgColor );
            }
        }
        else {
            return null;
        }
    }

    /**
     * Returns the requested legend fractional position.
     *
     * @return   2-element array giving x, y fractional positions for legend
     *           (each in range 0..1), or null for absent or external legend
     */
    public float[] getLegendPosition() {
        return insideModel_.isSelected()
             ? new float[] { pusher_.getXPosition(), pusher_.getYPosition() }
             : null;
    }

    /**
     * Returns a list of legend entries for the current state of the stack.
     *
     * @return   legend entries
     */
    private LegendEntry[] getLegendEntries() {
        List<LegendEntry> entryList = new ArrayList<LegendEntry>();
        LayerControl[] controls = stackModel_.getActiveLayerControls();
        for ( int ic = 0; ic < controls.length; ic++ ) {
            entryList.addAll( Arrays.asList( controls[ ic ]
                                            .getLegendEntries() ) );
        }
        return entryList.toArray( new LegendEntry[ 0 ] );
    }
}
