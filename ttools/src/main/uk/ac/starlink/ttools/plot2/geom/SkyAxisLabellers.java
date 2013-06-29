package uk.ac.starlink.ttools.plot2.geom;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import uk.ac.starlink.ttools.plot2.Captioner;

/**
 * Implementation class containing SkyAxisLabeller implementations.
 *
 * @author   Mark Taylor
 * @since    21 Feb 2013
 */
public class SkyAxisLabellers {

    /**
     * Private constructor prevents instantiation.
     */
    private SkyAxisLabellers() {
    }

    /** Labeller implentation that does no drawing. */
    public static SkyAxisLabeller NONE = new SkyAxisLabeller() {
        public String getName() {
            return "None";
        }
        public AxisAnnotation createAxisAnnotation( GridLiner gridLiner,
                                                    Captioner captioner ) {
            return new AxisAnnotation() {
                public Insets getPadding( boolean withScroll ) {
                    return new Insets( 0, 0, 0, 0 );
                }
                public void drawLabels( Graphics g ) {
                }
            };
        }
    };

    /**
     * Basic labeller implementation.  Grid lines are drawn OK,
     * but not much effort is made to position axis labels sensibly.
     */
    public static SkyAxisLabeller LAME = new SkyAxisLabeller() {
        public String getName() {
            return "Basic";
        }
        public AxisAnnotation createAxisAnnotation( final GridLiner gridLiner,
                                                   final Captioner captioner ) {
            return new AxisAnnotation() {
                public Insets getPadding( boolean withScroll ) {
                    return new Insets( 0, 0, 0, 0 );
                }
                public void drawLabels( Graphics g ) {
                    Graphics2D g2 = (Graphics2D) g;
                    double[][][] lines = gridLiner.getLines();
                    String[] labels = gridLiner.getLabels();
                    int nl = labels.length;
                    for ( int il = 0; il < nl; il++ ) {
                        double[][] line = lines[ il ];
                        String label = labels[ il ];
                        double[] seg0 = line[ 0 ];
                        double[] segN = line[ line.length - 1 ];
                        double px = seg0[ 0 ];
                        double py = seg0[ 1 ];
                        g2.translate( px, py );
                        captioner.drawCaption( label, g2 );
                        g2.translate( -px, -py );
                    }
                }
            };
        }
    };

    /** Labeller implementation that draws labels outside the plot bounds. */
    public static SkyAxisLabeller EXTERNAL =
            new TickSkyAxisLabeller( "External" ) {
        protected SkyTick[] calculateTicks( double[][][] lines, String[] labels,
                                            Rectangle plotBounds ) {
            List<SkyTick> tickList = new ArrayList<SkyTick>();
            int nl = labels.length;
            for ( int il = 0; il < nl; il++ ) {
                SkyTick tick = createExternalTick( labels[ il ], lines[ il ],
                                                   plotBounds );
                if ( tick != null ) {
                    tickList.add( tick );
                }
            }
            return tickList.toArray( new SkyTick[ 0 ] );
        }
    };

    /** Labeller implementation that draws labels inside the plot bounds. */
    public static SkyAxisLabeller INTERNAL =
            new TickSkyAxisLabeller( "Internal" ) {
        protected SkyTick[] calculateTicks( double[][][] lines, String[] labels,
                                            Rectangle plotBounds ) {
            List<SkyTick> tickList = new ArrayList<SkyTick>();
            int nl = labels.length;
            for ( int il = 0; il < nl; il++ ) {
                SkyTick tick = createInternalTick( labels[ il ], lines[ il ] );
                if ( tick != null ) {
                    tickList.add( tick );
                }
            }
            return tickList.toArray( new SkyTick[ 0 ] );
        }
    };

    /**
     * Labeller implementation that draws labels outside the plot bounds
     * unless they don't appear, in which case it draws them inside.
     * Doesn't necessarily end up looking as sensible as it sounds.
     */ 
    public static SkyAxisLabeller HYBRID =
            new TickSkyAxisLabeller( "Hybrid" ) {
        protected SkyTick[] calculateTicks( double[][][] lines, String[] labels,
                                            Rectangle plotBounds ) {
            List<SkyTick> tickList = new ArrayList<SkyTick>();
            int nl = labels.length;
            for ( int il = 0; il < nl; il++ ) {
                String label = labels[ il ];
                double[][] line = lines[ il ];
                SkyTick tick = createExternalTick( label, line, plotBounds );
                if ( tick == null ) {
                    tick = createInternalTick( label, line );
                }
                if ( tick != null ) {
                    tickList.add( tick );
                }
            }
            return tickList.toArray( new SkyTick[ 0 ] );
        }
    };
 

    /**
     * Returns a list of the known SkyAxisLabeller instances.
     * The first element is null, which is interpreted as auto mode.
     *
     * @return  list of sky axis labellers
     */
    public static SkyAxisLabeller[] getKnownLabellers() {
        return new SkyAxisLabeller[] {
            null, EXTERNAL, INTERNAL, LAME, HYBRID, NONE,
        };
    }

    /**
     * Returns the axis mode to use if choosing one automatically based
     * on plot characteristics.
     *
     * @param   skyFillsBounds  true if the sky region of the plane
     *                          fills all or most of the plotting region;
     *                          false if there are significant non-sky parts
     * @return   suitable axis labeller
     */
    public static SkyAxisLabeller getAutoLabeller( boolean skyFillsBounds ) {
        return skyFillsBounds ? EXTERNAL : INTERNAL;
    }
}
