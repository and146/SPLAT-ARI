/* ********************************************************
 * This file automatically generated by SelectorMap.pl.
 *                   Do not edit.                         *
 **********************************************************/

package uk.ac.starlink.ast;


/**
 * Java interface to the AST SelectorMap class
 *  - a Mapping that locates positions within one of a set of alternate 
 * Regions. 
 * A SelectorMap is a Mapping that identifies which Region contains 
 * a given input position.
 * <p>
 * A SelectorMap encapsulates a number of Regions that all have the same 
 * number of axes and represent the same coordinate Frame. The number of 
 * inputs (Nin attribute) of the SelectorMap equals the number of axes 
 * spanned by one of the encapsulated Region. All SelectorMaps have only 
 * a single output. SelectorMaps do not define an inverse transformation.
 * <p>
 * For each input position, the forward transformation of a SelectorMap 
 * searches through the encapsulated Regions (in the order supplied when 
 * the SelectorMap was created) until a Region is found which contains
 * the input position. The index associated with this Region is
 * returned as the SelectorMap output value (the index value is the
 * position of the Region within the list of Regions supplied when the 
 * SelectorMap was created, starting at 1 for the first Region). If an
 * input position is not contained within any Region, a value of zero is 
 * returned by the forward transformation.
 * <p>
 * If a compound Mapping contains a SelectorMap in series with its own
 * inverse, the combination of the two adjacent SelectorMaps will be 
 * replaced by a UnitMap when the compound Mapping is simplified using
 * astSimplify.
 * <p>
 * In practice, SelectorMaps are often used in conjunction with SwitchMaps.
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
 * @see  <a href='http://star-www.rl.ac.uk/cgi-bin/htxserver/sun211.htx/?xref_SelectorMap'>AST SelectorMap</a>  
 */
public class SelectorMap extends Mapping {
    /** 
     * Creates a SelectorMap.   
     * @param  regs  An array of pointers to the Regions. All the supplied Regions must 
     * relate to the same coordinate Frame. The number of axes in this
     * coordinate Frame defines the number of inputs for the SelectorMap.
     * 
     * @param  badval  The value to be returned by the forward transformation of the
     * SelectorMap for any input positions that have a bad (AST__BAD) 
     * value on any axis.
     * 
     * @throws  AstException  if an error occurred in the AST library
    */
    public SelectorMap( Region[] regs, double badval ) {
        construct( regs, badval );
    }
    private native void construct( Region[] regs, double badval );

}