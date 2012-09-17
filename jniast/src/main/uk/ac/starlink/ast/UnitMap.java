/* ********************************************************
 * This file automatically generated by UnitMap.pl.
 *                   Do not edit.                         *
 **********************************************************/

package uk.ac.starlink.ast;


/**
 * Java interface to the AST UnitMap class
 *  - unit (null) Mapping. 
 * A UnitMap is a unit (null) Mapping that has no effect on the
 * coordinates supplied to it. They are simply copied. This can be
 * useful if a Mapping is required (e.g. to pass to another
 * function) but you do not want it to have any effect.
 * The Nin and Nout attributes of a UnitMap are always equal and
 * are specified when it is created.
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
 * @see  <a href='http://star-www.rl.ac.uk/cgi-bin/htxserver/sun211.htx/?xref_UnitMap'>AST UnitMap</a>  
 */
public class UnitMap extends Mapping {
    /** 
     * Creates a UnitMap.   
     * @param  ncoord  The number of input and output coordinates (these numbers are
     * necessarily the same).
     * 
     * @throws  AstException  if an error occurred in the AST library
    */
    public UnitMap( int ncoord ) {
        construct( ncoord );
    }
    private native void construct( int ncoord );

}