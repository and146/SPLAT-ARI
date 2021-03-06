package uk.ac.starlink.ttools.plot2;

import java.awt.Color;

/**
 * Defines characteristics of a plot layer that may enable plotting
 * optimisations.
 *
 * @author   Mark Taylor
 * @since    12 Feb 2013
 */
@Equality
public class LayerOpt {
    private final Color singleColor_;
    private final boolean opaque_;

    /** Indicates opaque multicoloured layer. */
    public static LayerOpt OPAQUE = new LayerOpt( null, true );

    /** Indicates layer with no known optimisation assumptions. */
    public static LayerOpt NO_SPECIAL = new LayerOpt( null, false );

    /**
     * Constructs a LayerOpt with explicit options.
     *
     * @param   singleColor  colour if only one is used by the layer, else null
     * @param   opaque   true if only opaque pixels are generated by the layer
     */
    public LayerOpt( Color singleColor, boolean opaque ) {
        singleColor_ = singleColor;
        opaque_ = opaque;
    }

    /**
     * Returns a colour if the only painting done by this layer is
     * in a single colour.  Different alphas are permitted, but not
     * different RGB values.
     *
     * @return  single colour, or null
     */
    public Color getSingleColor() {
        return singleColor_;
    }

    /**
     * Indicates whether it's safe to assume that all drawing is opaque.
     *
     * @return  true if no transparency is used
     */
    public boolean isOpaque() {
        return opaque_;
    }

    @Override
    public int hashCode() {
        int code = 2301;
        code = 23 * code + PlotUtil.hashCode( singleColor_ );
        code = 23 * code + ( opaque_ ? 3 : 7 );
        return code;
    }

    @Override
    public boolean equals( Object o ) {
        if ( o instanceof LayerOpt ) {
            LayerOpt other = (LayerOpt) o;
            return PlotUtil.equals( this.singleColor_, other.singleColor_ )
                && this.opaque_ == other.opaque_;
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "color: " + singleColor_ + "; "
             + "opaque: " + opaque_;
    }
}
