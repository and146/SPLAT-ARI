package uk.ac.starlink.ttools.task;

import java.util.Iterator;
import uk.ac.starlink.task.Environment;
import uk.ac.starlink.task.Parameter;
import uk.ac.starlink.task.TaskException;

/**
 * TableMapper which does the work for the tpipe command. 
 * It can stack several processing filters to edit the table.
 *
 * @author   Mark Taylor
 * @since    16 Aug 2005
 */
public class TablePipe extends SingleMapperTask {

    public TablePipe() {
        super( "Performs pipeline processing on a table", new ChoiceMode(),
               false, true );
        int nfilter = 0;
        for ( Iterator it = getParameterList().iterator(); it.hasNext(); ) {
            Parameter param = (Parameter) it.next();
            if ( param instanceof FilterParameter ) {
                assert param.getName().equals( "icmd" );
                param.setName( "cmd" );
                nfilter++;
            }
        }
        assert nfilter == 1;
    }

    public TableProducer createProducer( Environment env )
            throws TaskException {
        return createInputProducer( env );
    }
}
