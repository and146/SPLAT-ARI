package uk.ac.starlink.topcat.plot2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 * Panel which displays a list of control summaries in a list on one side,
 * and an area for showing the control interaction areas on the other side.
 * When one of the items in the summary list is selected, the detail panel
 * is filled from the content of the relevant control.
 *
 * @author   Mark Taylor
 * @since    13 Mar 2013
 */
public class ControlStackPanel extends JPanel {

    private final DefaultListModel fixListModel_;

    /**
     * Constructor.
     *
     * @param  stack   stack in which controls can be added and moved around
     */
    public ControlStackPanel( ControlStack stack ) {
        super( new BorderLayout() );
        JComponent holder = new JPanel( new BorderLayout() );
        holder.setMinimumSize( new Dimension( 200, 100 ) );

        /* Set up a list for the fixed controls. */
        fixListModel_ = new DefaultListModel();
        JList fixList = new JList( fixListModel_ );
        fixList.setCellRenderer( new FixRenderer() );

        /* Set up an object which keeps track of when a control has been
         * newly added to the stack. */
        Spotter controlSpotter = new Spotter( stack.getStackModel() );
        stack.getStackModel().addListDataListener( controlSpotter );

        /* Set up selection listeners.  Only one item from either list
         * can be selected at a time. */
        stack.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        stack.addListSelectionListener(
            new LayerControlListener( stack, fixList, holder,
                                      controlSpotter ) );
        fixList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        fixList.addListSelectionListener(
            new ControlListener( fixList, stack, holder ) );

        /* Position the components. */
        JScrollPane stackScroller = new JScrollPane( stack );
        stackScroller.setMinimumSize( new Dimension( 80, 100 ) );
        stackScroller.setPreferredSize( new Dimension( 120, 160 ) );
        stackScroller.setHorizontalScrollBarPolicy(
                          JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
        JComponent listsPanel = new JPanel( new BorderLayout() );
        listsPanel.add( fixList, BorderLayout.NORTH );
        listsPanel.add( stackScroller, BorderLayout.CENTER );
        JSplitPane splitter = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT,
                                              listsPanel, holder );
        splitter.setResizeWeight( 0.0 );
        add( splitter );
        splitter.setDividerLocation( 120 );
    }

    /**
     * Add a control to the fixed part of the stack.
     * These controls cannot be reordered or (de)activated under user control.
     *
     * @param  control  control to add
     */
    public void addFixedControl( Control control ) {
        fixListModel_.addElement( control );
    }

    /**
     * Removes a control from the fixed part of the stack.
     *
     * @param  control  control previously added
     */
    public void removeFixedControl( Control control ) {
        fixListModel_.removeElement( control );
    }

    /**
     * Configure one control to a similar state as another,
     * where possible.
     * This is done when changing selections to reduce visual jumpiness
     * in the GUI.
     *
     * @param   c0  template control
     * @param   c1  control whose state may be altered
     */
    private static void configureLike( Control c0, Control c1 ) {
        if ( c0 instanceof TabberControl && c1 instanceof TabberControl ) {
            JTabbedPane tabber0 = ((TabberControl) c0).getTabber();
            JTabbedPane tabber1 = ((TabberControl) c1).getTabber();
            int itab0 = tabber0.getSelectedIndex();
            if ( itab0 >= 0 ) {
                String title0 = tabber0.getTitleAt( itab0 );
                int itab1 = tabber1.indexOfTab( title0 );
                if ( itab1 >= 0 ) {
                    tabber1.setSelectedIndex( itab1 );
                }
            }
        }
    }

    /**
     * Selection listener for changes to the selected control.
     */
    private static class ControlListener implements ListSelectionListener {
        private final JList list1_;
        private final JList list2_;
        private final JComponent controlHolder_;

        /**
         * Constructor.
         *
         * @param  list1  the list whose selections this object is listening to
         * @param  list2  the other list; if a selection is made on list1,
         *                the selection will be cleared on list2 so that
         *                only a single selection is present on either of
         *                the lists
         * @param  controlHolder  component containing control GUI panel;
         *                        on selection this will be populated with
         *                        the content panel for the selected control
         */
        ControlListener( JList list1, JList list2, JComponent controlHolder ) {
            list1_ = list1;
            list2_ = list2;
            controlHolder_ = controlHolder;
        }

        public void valueChanged( ListSelectionEvent evt ) {
            if ( ! evt.getValueIsAdjusting() ) {
                controlHolder_.removeAll();
                Control control = (Control) list1_.getSelectedValue();
                if ( control != null ) {
                    list2_.clearSelection();
                    adjustControl( control );
                    controlHolder_.add( control.getPanel() );
                }
                controlHolder_.revalidate();
                controlHolder_.repaint();
            }
        }

        /**
         * Called on a control when it has been selected.
         * This implementation does nothing, but it may be overridden by
         * subclasses.
         *
         * @param  control   newly selected control
         */
        void adjustControl( Control control ) {
            // no-op
        }
    }

    /**
     * Selection listener for changes to the control stack.
     */
    private static class LayerControlListener extends ControlListener {

        final Spotter spotter_;
        Control lastControl_;

        /**
         * Constructor.
         *
         * @param  list1  this list
         * @param  list2  other list
         * @param  holder   control content pane holder
         * @param  spotter  object that keeps track of when a control has
         *                  been just added
         */
        LayerControlListener( JList list1, JList list2, JComponent holder,
                              Spotter spotter ) {
            super( list1, list2, holder );
            spotter_ = spotter;
        }

        @Override
        void adjustControl( Control control ) {
            if ( control != null ) {
                if ( ! spotter_.isNew( control ) && lastControl_ != null ) {
                    configureLike( lastControl_, control );
                }
                lastControl_ = control;
            }
        }
    }

    /**
     * Listener that keeps track of when a control is first added to the stack.
     */
    private static class Spotter implements ListDataListener {
        private final ListModel model_;
        private final Map<Object,Boolean> items_;

        /**
         * Constructor.
         *
         * @param  model  stack model
         */
        Spotter( ListModel model ) {
            model_ = model;
            items_ = new HashMap<Object,Boolean>();
            update();
        }

        /**
         * Returns true only the first time that this method is called on
         * a given list item.
         *
         * @param   item  item to query
         * @return   true iff this method has never been called
         *                on <code>item</code> before
         */
        public boolean isNew( Object item ) {
            Boolean isNew = items_.get( item );
            if ( Boolean.FALSE.equals( isNew ) ) {
                return false;
            }
            else {
                assert isNew != null;
                items_.put( item, Boolean.FALSE );
                return true;
            }
        }

        private void update() {
            Set<Object> itemSet = new HashSet<Object>();
            for ( int i = 0; i < model_.getSize(); i++ ) {
                itemSet.add( model_.getElementAt( i ) );
            }

            /* Discard any that no longer exist. */
            items_.keySet().retainAll( itemSet );

            /* Work out which ones we haven't seen before. */
            itemSet.removeAll( items_.keySet() );
            for ( Object item : itemSet ) {
                items_.put( item, Boolean.TRUE );
            }
        }

        public void contentsChanged( ListDataEvent evt ) {
            update();
        }
        public void intervalAdded( ListDataEvent evt ) {
            update();
        }
        public void intervalRemoved( ListDataEvent evt ) {
            update();
        }
    }

    /**
     * List cell renderer for the fixed control items.
     */
    private static class FixRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent( JList list, Object value,
                                                       int index, boolean isSel,
                                                       boolean hasFocus ) {
            super.getListCellRendererComponent( list, value, index, isSel,
                                                hasFocus );
            if ( value instanceof Control ) {
                Control control = (Control) value;
                setText( control.getControlLabel() );
                setIcon( control.getControlIcon() );
            }
            return this;
        }
    }
}
