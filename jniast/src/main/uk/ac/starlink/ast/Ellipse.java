/* ********************************************************
 * This file automatically generated by Ellipse.pl.
 *                   Do not edit.                         *
 **********************************************************/

package uk.ac.starlink.ast;


/**
 * Java interface to the AST Ellipse class
 *  - an elliptical region within a 2-dimensional Frame. 
 * The Ellipse class implements a Region which represents a ellipse 
 * within a 2-dimensional Frame.
 * <h4>Licence</h4>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public Licence as
 * published by the Free Software Foundation; either version 2 of
 * the Licence, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be
 * useful,but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU General Public Licence for more details.
 * <p>
 * You should have received a copy of the GNU General Public Licence
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place,Suite 330, Boston, MA
 * 02111-1307, USA
 * 
 * 
 * @see  <a href='http://star-www.rl.ac.uk/cgi-bin/htxserver/sun211.htx/?xref_Ellipse'>AST Ellipse</a>  
 */
public class Ellipse extends Region {
    /** 
     * Create a Ellipse.   
     * This function creates a new Ellipse and optionally initialises its
     * attributes.
     * <p>
     * A Ellipse is a Region which represents a elliptical area within the
     * supplied 2-dimensional Frame.
     * <h4>Notes</h4>
     * <br> - A null Object pointer (AST__NULL) will be returned if this
     * function is invoked with the AST error status set, or if it
     * should fail for any reason.
     * @param  frame  A pointer to the Frame in which the region is defined. It must
     * have exactly 2 axes. A deep copy is taken of the supplied Frame. 
     * This means that any subsequent changes made to the Frame using the 
     * supplied pointer will have no effect the Region.
     * 
     * @param  form  Indicates how the ellipse is described by the remaining parameters.
     * A value of zero indicates that the ellipse is specified by a
     * centre position and two positions on the circumference. A value of 
     * one indicates that the ellipse is specified by its centre position, 
     * the half-lengths of its two axes, and the orientation of its first 
     * axis.
     * 
     * @param  centre  An array of 2 doubles, 
     * containing the coordinates at the centre of
     * the ellipse.
     * 
     * @param  point1  An array of 2 doubles. If "form" 
     * is zero, this array should contain the coordinates of one of the four 
     * points where an axis of the ellipse crosses the circumference of the 
     * ellipse.
     * If "form" 
     * is one, it should contain the lengths of semi-major and
     * semi-minor axes of the ellipse, given as geodesic distances
     * within the Frame.
     * 
     * @param  point2  An array of 2 doubles. If "form" 
     * is zero, this array should containing the coordinates at some other 
     * point on the circumference of the ellipse, distinct from 
     * "point1". If "form"
     * is one, the first element of this array should hold the angle
     * between the second axis of the Frame and the first ellipse axis
     * (i.e. the ellipse axis which is specified first in the 
     * "point1" 
     * array), and the second element will be ignored. The angle should be 
     * given in radians, measured positive in the same sense as rotation 
     * from the positive direction of the second Frame axis to the positive
     * direction of the first Frame axis.
     * 
     * @param  unc  An optional pointer to an existing Region which specifies the 
     * uncertainties associated with the boundary of the Box being created. 
     * The uncertainty in any point on the boundary of the Box is found by 
     * shifting the supplied "uncertainty" Region so that it is centred at 
     * the boundary point being considered. The area covered by the
     * shifted uncertainty Region then represents the uncertainty in the
     * boundary position. The uncertainty is assumed to be the same for
     * all points.
     * <p>
     * If supplied, the uncertainty Region must be of a class for which 
     * all instances are centro-symetric (e.g. Box, Circle, Ellipse, etc.) 
     * or be a Prism containing centro-symetric component Regions. A deep 
     * copy of the supplied Region will be taken, so subsequent changes to 
     * the uncertainty Region using the supplied pointer will have no 
     * effect on the created Box. Alternatively, 
     * a NULL Object pointer 
     * may be supplied, in which case a default uncertainty is used 
     * equivalent to a box 1.0E-6 of the size of the Box being created.
     * <p>
     * The uncertainty Region has two uses: 1) when the 
     * astOverlap
     * function compares two Regions for equality the uncertainty
     * Region is used to determine the tolerance on the comparison, and 2)
     * when a Region is mapped into a different coordinate system and
     * subsequently simplified (using 
     * astSimplify),
     * the uncertainties are used to determine if the transformed boundary 
     * can be accurately represented by a specific shape of Region.
     * 
     * @throws  AstException  if an error occurred in the AST library
    */
    public Ellipse( Frame frame, int form, double[] centre, double[] point1, double[] point2, Region unc ) {
        construct( frame, form, centre, point1, point2, unc );
    }
    private native void construct( Frame frame, int form, double[] centre, double[] point1, double[] point2, Region unc );

}