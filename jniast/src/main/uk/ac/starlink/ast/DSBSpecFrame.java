/* ********************************************************
 * This file automatically generated by DSBSpecFrame.pl.
 *                   Do not edit.                         *
 **********************************************************/

package uk.ac.starlink.ast;


/**
 * Java interface to the AST DSBSpecFrame class
 *  - dual sideband spectral coordinate system description. 
 * A DSBSpecFrame is a specialised form of SpecFrame which represents
 * positions in a spectrum obtained using a dual sideband instrument.
 * Such an instrument produces a spectrum in which each point contains
 * contributions from two distinctly different frequencies, one from
 * the "lower side band" (LSB) and one from the "upper side band" (USB).
 * Corresponding LSB and USB frequencies are connected by the fact
 * that they are an equal distance on either side of a fixed central
 * frequency known as the "Local Oscillator" (LO) frequency.
 * <p>
 * When quoting a position within such a spectrum, it is necessary to
 * indicate whether the quoted position is the USB position or the
 * corresponding LSB position. The SideBand attribute provides this
 * indication. Another option that the SideBand attribute provides is
 * to represent a spectral position by its topocentric offset from the
 * LO frequency.
 * <p>
 * In practice, the LO frequency is specified by giving the distance
 * from the LO frequency to some "central" spectral position. Typically
 * this central position is that of some interesting spectral feature.
 * The distance from this central position to the LO frequency is known
 * as the "intermediate frequency" (IF). The value supplied for IF can
 * be a signed value in order to indicate whether the LO frequency is
 * above or below the central position.
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
 * Foundation, Inc., 51 Franklin Street,Fifth Floor, Boston, MA
 * 02110-1301, USA
 * 
 * 
 * @see  <a href='http://star-www.rl.ac.uk/cgi-bin/htxserver/sun211.htx/?xref_DSBSpecFrame'>AST DSBSpecFrame</a>  
 */
public class DSBSpecFrame extends SpecFrame {
    /** 
     * Creates a DSBSpecFrame.   
     * @throws  AstException  if an error occurred in the AST library
    */
    public DSBSpecFrame(  ) {
        construct(  );
    }
    private native void construct(  );

    /**
     * Get 
     * should the SideBand attribute be taken into account when aligning
     * this DSBSpecFrame with another DSBSpecFrame.  
     * This attribute controls how a DSBSpecFrame behaves when an attempt
     * is made to align it with another DSBSpecFrame using
     * astFindFrame or astConvert.
     * If both DSBSpecFrames have a non-zero value for AlignSideBand, the
     * value of the SideBand attribute in each DSBSpecFrame is used so that
     * alignment occurs between sidebands. That is, if one DSBSpecFrame
     * represents USB and the other represents LSB then
     * astFindFrame and astConvert
     * will recognise that the DSBSpecFrames represent different sidebands
     * and will take this into account when constructing the Mapping that
     * maps positions in one DSBSpecFrame into the other. If AlignSideBand
     * in either DSBSpecFrame is set to zero, then the values of the SideBand
     * attributes are ignored. In the above example, this would result in a
     * frequency in the first DSBSpecFrame being mapped onto the same
     * frequency in the second DSBSpecFrame, even though those frequencies
     * refer to different sidebands. In other words, if either AlignSideBand
     * attribute is zero, then the two DSBSpecFrames aligns like basic
     * SpecFrames. The default value for AlignSideBand is zero.
     * <p>
     * When astFindFrame or astConvert
     * is used on two DSBSpecFrames (potentially describing different spectral
     * coordinate systems and/or sidebands), it returns a Mapping which can be
     * used to transform a position in one DSBSpecFrame into the corresponding
     * position in the other. The Mapping is made up of the following steps in
     * the indicated order:
     * <p>
     * <br> - If both DSBSpecFrames have a value of 1 for the AlignSideBand
     * attribute, map values from the target's current sideband (given by its
     * SideBand attribute) to the observed sideband (whether USB or LSB). If
     * the target already represents the observed sideband, this step will
     * leave the values unchanged. If either of the two DSBSpecFrames have a
     * value of zero for its AlignSideBand attribute, then this step is omitted.
     * <p>
     * <br> - Map the values from the spectral system of the target to the spectral
     * system of the template. This Mapping takes into account all the
     * inherited SpecFrame attributes such as System, StdOfRest, Unit, etc.
     * <p>
     * <br> - If both DSBSpecFrames have a value of 1 for the AlignSideBand
     * attribute, map values from the result's observed sideband to the
     * result's current sideband (given by its SideBand attribute). If the
     * result already represents the observed sideband, this step will leave
     * the values unchanged. If either of the two DSBSpecFrames have a value
     * of zero for its AlignSideBand attribute, then this step is omitted.
     * 
     *
     * @return  this object's AlignSideBand attribute
     */
    public boolean getAlignSideBand() {
        return getB( "AlignSideBand" );
    }

    /**
     * Set 
     * should the SideBand attribute be taken into account when aligning
     * this DSBSpecFrame with another DSBSpecFrame.  
     * This attribute controls how a DSBSpecFrame behaves when an attempt
     * is made to align it with another DSBSpecFrame using
     * astFindFrame or astConvert.
     * If both DSBSpecFrames have a non-zero value for AlignSideBand, the
     * value of the SideBand attribute in each DSBSpecFrame is used so that
     * alignment occurs between sidebands. That is, if one DSBSpecFrame
     * represents USB and the other represents LSB then
     * astFindFrame and astConvert
     * will recognise that the DSBSpecFrames represent different sidebands
     * and will take this into account when constructing the Mapping that
     * maps positions in one DSBSpecFrame into the other. If AlignSideBand
     * in either DSBSpecFrame is set to zero, then the values of the SideBand
     * attributes are ignored. In the above example, this would result in a
     * frequency in the first DSBSpecFrame being mapped onto the same
     * frequency in the second DSBSpecFrame, even though those frequencies
     * refer to different sidebands. In other words, if either AlignSideBand
     * attribute is zero, then the two DSBSpecFrames aligns like basic
     * SpecFrames. The default value for AlignSideBand is zero.
     * <p>
     * When astFindFrame or astConvert
     * is used on two DSBSpecFrames (potentially describing different spectral
     * coordinate systems and/or sidebands), it returns a Mapping which can be
     * used to transform a position in one DSBSpecFrame into the corresponding
     * position in the other. The Mapping is made up of the following steps in
     * the indicated order:
     * <p>
     * <br> - If both DSBSpecFrames have a value of 1 for the AlignSideBand
     * attribute, map values from the target's current sideband (given by its
     * SideBand attribute) to the observed sideband (whether USB or LSB). If
     * the target already represents the observed sideband, this step will
     * leave the values unchanged. If either of the two DSBSpecFrames have a
     * value of zero for its AlignSideBand attribute, then this step is omitted.
     * <p>
     * <br> - Map the values from the spectral system of the target to the spectral
     * system of the template. This Mapping takes into account all the
     * inherited SpecFrame attributes such as System, StdOfRest, Unit, etc.
     * <p>
     * <br> - If both DSBSpecFrames have a value of 1 for the AlignSideBand
     * attribute, map values from the result's observed sideband to the
     * result's current sideband (given by its SideBand attribute). If the
     * result already represents the observed sideband, this step will leave
     * the values unchanged. If either of the two DSBSpecFrames have a value
     * of zero for its AlignSideBand attribute, then this step is omitted.
     * 
     *
     * @param  alignSideBand   the AlignSideBand attribute of this object
     */
    public void setAlignSideBand( boolean alignSideBand ) {
       setB( "AlignSideBand", alignSideBand );
    }

    /**
     * Get 
     * the central position of interest in a dual sideband spectrum.  
     * This attribute specifies the central position of interest in a dual
     * sideband spectrum. Its sole use is to determine the local oscillator
     * frequency (the frequency which marks the boundary between the lower
     * and upper sidebands). See the description of the IF (intermediate
     * frequency) attribute for details of how the local oscillator frequency
     * is calculated. The sideband containing this central position is
     * referred to as the "observed" sideband, and the other sideband as
     * the "image" sideband.
     * <p>
     * The value is accessed as a position in the spectral system
     * represented by the SpecFrame attributes inherited by this class, but
     * is stored internally as topocentric frequency. Thus, if the System
     * attribute of the DSBSpecFrame is set to "VRAD", the Unit attribute
     * set to "m/s" and the StdOfRest attribute set to "LSRK", then values
     * for the DSBCentre attribute should be supplied as radio velocity in
     * units of "m/s" relative to the kinematic LSR (alternative units may
     * be used by appending a suitable units string to the end of the value).
     * This value is then converted to topocentric frequency and stored. If
     * (say) the Unit attribute is subsequently changed to "km/s" before
     * retrieving the current value of the DSBCentre attribute, the stored
     * topocentric frequency will be converted back to LSRK radio velocity,
     * this time in units of "km/s", before being returned.
     * <p>
     * The default value for this attribute is 30 GHz.
     * <h4>Note</h4>
     * <br> - The attributes which define the transformation to or from topocentric
     * frequency should be assigned their correct values before accessing
     * this attribute. These potentially include System, Unit, StdOfRest,
     * ObsLon, ObsLat, ObsAlt, Epoch, RefRA, RefDec and RestFreq.
     * 
     *
     * @return  this object's DsbCentre attribute
     */
    public double getDsbCentre() {
        return getD( "DsbCentre" );
    }

    /**
     * Set 
     * the central position of interest in a dual sideband spectrum.  
     * This attribute specifies the central position of interest in a dual
     * sideband spectrum. Its sole use is to determine the local oscillator
     * frequency (the frequency which marks the boundary between the lower
     * and upper sidebands). See the description of the IF (intermediate
     * frequency) attribute for details of how the local oscillator frequency
     * is calculated. The sideband containing this central position is
     * referred to as the "observed" sideband, and the other sideband as
     * the "image" sideband.
     * <p>
     * The value is accessed as a position in the spectral system
     * represented by the SpecFrame attributes inherited by this class, but
     * is stored internally as topocentric frequency. Thus, if the System
     * attribute of the DSBSpecFrame is set to "VRAD", the Unit attribute
     * set to "m/s" and the StdOfRest attribute set to "LSRK", then values
     * for the DSBCentre attribute should be supplied as radio velocity in
     * units of "m/s" relative to the kinematic LSR (alternative units may
     * be used by appending a suitable units string to the end of the value).
     * This value is then converted to topocentric frequency and stored. If
     * (say) the Unit attribute is subsequently changed to "km/s" before
     * retrieving the current value of the DSBCentre attribute, the stored
     * topocentric frequency will be converted back to LSRK radio velocity,
     * this time in units of "km/s", before being returned.
     * <p>
     * The default value for this attribute is 30 GHz.
     * <h4>Note</h4>
     * <br> - The attributes which define the transformation to or from topocentric
     * frequency should be assigned their correct values before accessing
     * this attribute. These potentially include System, Unit, StdOfRest,
     * ObsLon, ObsLat, ObsAlt, Epoch, RefRA, RefDec and RestFreq.
     * 
     *
     * @param  dsbCentre   the DsbCentre attribute of this object
     */
    public void setDsbCentre( double dsbCentre ) {
       setD( "DsbCentre", dsbCentre );
    }

    /**
     * Get 
     * the intermediate frequency in a dual sideband spectrum.  
     * This attribute specifies the (topocentric) intermediate frequency in
     * a dual sideband spectrum. Its sole use is to determine the local
     * oscillator (LO) frequency (the frequency which marks the boundary
     * between the lower and upper sidebands). The LO frequency is
     * equal to the sum of the centre frequency and the intermediate
     * frequency. Here, the "centre frequency" is the topocentric
     * frequency in Hz corresponding to the current value of the DSBCentre
     * attribute. The value of the IF attribute may be positive or
     * negative: a positive value results in the LO frequency being above
     * the central frequency, whilst a negative IF value results in the LO
     * frequency being below the central frequency. The sign of the IF
     * attribute value determines the default value for the SideBand
     * attribute.
     * <p>
     * When setting a new value for this attribute, the units in which the
     * frequency value is supplied may be indicated by appending a suitable
     * string to the end of the formatted value. If the units are not
     * specified, then the supplied value is assumed to be in units of GHz.
     * For instance, the following strings all result in an IF of 4 GHz being
     * used: "4.0", "4.0 GHz", "4.0E9 Hz", etc.
     * <p>
     * When getting the value of this attribute, the returned value is
     * always in units of GHz. The default value for this attribute is 4 GHz.
     * 
     *
     * @return  this object's If attribute
     */
    public double getIf() {
        return getD( "If" );
    }

    /**
     * Set 
     * the intermediate frequency in a dual sideband spectrum.  
     * This attribute specifies the (topocentric) intermediate frequency in
     * a dual sideband spectrum. Its sole use is to determine the local
     * oscillator (LO) frequency (the frequency which marks the boundary
     * between the lower and upper sidebands). The LO frequency is
     * equal to the sum of the centre frequency and the intermediate
     * frequency. Here, the "centre frequency" is the topocentric
     * frequency in Hz corresponding to the current value of the DSBCentre
     * attribute. The value of the IF attribute may be positive or
     * negative: a positive value results in the LO frequency being above
     * the central frequency, whilst a negative IF value results in the LO
     * frequency being below the central frequency. The sign of the IF
     * attribute value determines the default value for the SideBand
     * attribute.
     * <p>
     * When setting a new value for this attribute, the units in which the
     * frequency value is supplied may be indicated by appending a suitable
     * string to the end of the formatted value. If the units are not
     * specified, then the supplied value is assumed to be in units of GHz.
     * For instance, the following strings all result in an IF of 4 GHz being
     * used: "4.0", "4.0 GHz", "4.0E9 Hz", etc.
     * <p>
     * When getting the value of this attribute, the returned value is
     * always in units of GHz. The default value for this attribute is 4 GHz.
     * 
     *
     * @param  ifreq   the If attribute of this object
     */
    public void setIf( double ifreq ) {
       setD( "If", ifreq );
    }

    /**
     * Get 
     * indicates which sideband a dual sideband spectrum represents.  
     * This attribute indicates whether the DSBSpecFrame currently
     * represents its lower or upper sideband, or an offset from the local
     * oscillator frequency. When querying the current value, the returned
     * string is always one of "usb" (for upper sideband), "lsb" (for lower
     * sideband), or "lo" (for offset from the local oscillator frequency).
     * When setting a new value, any of the strings "lsb", "usb", "observed",
     * "image" or "lo" may be supplied (case insensitive). The "observed"
     * sideband is which ever sideband (upper or lower) contains the central
     * spectral position given by attribute DSBCentre, and the "image"
     * sideband is the other sideband. It is the sign of the IF attribute
     * which determines if the observed sideband is the upper or lower
     * sideband. The default value for SideBand is the observed sideband.
     * 
     *
     * @return  this object's SideBand attribute
     */
    public String getSideBand() {
        return getC( "SideBand" );
    }

    /**
     * Set 
     * indicates which sideband a dual sideband spectrum represents.  
     * This attribute indicates whether the DSBSpecFrame currently
     * represents its lower or upper sideband, or an offset from the local
     * oscillator frequency. When querying the current value, the returned
     * string is always one of "usb" (for upper sideband), "lsb" (for lower
     * sideband), or "lo" (for offset from the local oscillator frequency).
     * When setting a new value, any of the strings "lsb", "usb", "observed",
     * "image" or "lo" may be supplied (case insensitive). The "observed"
     * sideband is which ever sideband (upper or lower) contains the central
     * spectral position given by attribute DSBCentre, and the "image"
     * sideband is the other sideband. It is the sign of the IF attribute
     * which determines if the observed sideband is the upper or lower
     * sideband. The default value for SideBand is the observed sideband.
     * 
     *
     * @param  sideBand   the SideBand attribute of this object
     */
    public void setSideBand( String sideBand ) {
       setC( "SideBand", sideBand );
    }

}
