package uk.ac.starlink.treeview;

import java.awt.HeadlessException;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFrame;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;
import junit.framework.TestCase;
import uk.ac.starlink.datanode.nodes.DataNode;
import uk.ac.starlink.datanode.nodes.NoSuchDataException;
import uk.ac.starlink.datanode.tree.DataNodeJTree;
import uk.ac.starlink.datanode.tree.DataNodeTreeModel;

public class ModelTest extends TestCase {

    static {
        Logger.getLogger( "uk.ac.starlink.table" ).setLevel( Level.WARNING );
        Logger.getLogger( "uk.ac.starlink.fits" ).setLevel( Level.WARNING );
    }

    public void setUp() {
        String basedir = System.getProperty( "ant.basedir" );
        System.setProperty( "uk.ac.starlink.treeview.demodir",
                            basedir + File.separator +
                            "etc" + File.separator +
                            "treeview" + File.separator +
                            "demo" );
    }

    public ModelTest( String name ) {
        super( name );
    }

    public void testModel() throws NoSuchDataException, InterruptedException {
        try {
            final DataNode root = new DemoDataNode();
            DataNodeTreeModel model = new DataNodeTreeModel( root );
            assertEquals( model.getRoot(), root );
            JFrame window = new JFrame();
            final DataNodeJTree jtree = new DataNodeJTree( model );
            window.getContentPane().add( jtree );
            window.pack();
            window.setSize( 400, 1000 );
            window.setVisible( true );

            // doesn't work (doesn't appear to display) - why not??
            Thread expander = jtree.recursiveExpand( root );
            expander.join();
            // assertEquals( 253, model.getNodeCount() - 1 );
            assertEquals( model.getRoot(), root );
            Thread.currentThread().sleep( 4000 );
        }
        catch ( HeadlessException e ) {
            System.out.println( "Headless environment - no GUI test" );
        }
    }
}
