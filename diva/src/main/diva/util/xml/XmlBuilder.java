/*
 * $Id: XmlBuilder.java,v 1.6 2001/07/22 22:02:13 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.xml;

/**
 * An XmlBuilder is an interface that can be implemented by classes
 * that convert between XmlElements and some internal
 * data representation. The main reason for doing so is to allow
 * other builders to "reuse" parts of an XML DTD. For example,
 * we could have a builder that builds Java2D objects, such as
 * "line" and "polygon". Then some other builder, that for example
 * builds libraries of graphical icons, can use an instance of the
 * Java2D builder internally -- if it does not recognize the type
 * of an XML element, it calls the Java2D builder to see if it can
 * get a low-level graphical object.
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision: 1.6 $
 * @rating Red
 */
public interface XmlBuilder {
    /** Given an XmlElement, create and return an internal representtion
     * of it. Implementors should also provide a more
     * type-specific version of this method:
     * <pre>
     *   public Graph build (XmlELement elt, String type);
     * </pre> 
     */
    public Object build (XmlElement elt, String type) throws Exception;

    /** Delegate builders can be used to build/generate for objects
     * that are unknown by the current builder, as might be the
     * case in a hierarchy of heterogeneous objects.
     *
     * @see diva.util.xml.CompositeBuilder
     */
    public void setDelegate(XmlBuilder child);

    /** Given an object, produce an XML representation of it.
     */
    public XmlElement generate (Object obj) throws Exception;
}


