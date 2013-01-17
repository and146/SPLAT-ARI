/*
 * $Id: CompositeBuilder.java,v 1.1 2002/05/26 07:39:59 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.compat.xml;

import java.util.Iterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.FileReader;
import java.io.Reader;

/**
 * CompositeBuilder is a non-validating parser that uses other
 * builders to parse and generate XML files from arbitary collections
 * of objects.  (FIXME - more documentation here)
 *
 * @author Michael Shilman (michaels@eecs.berkeley.edu)
 * @version $Revision: 1.1 $
 * @rating Red
 */
public class CompositeBuilder extends AbstractXmlBuilder {
    /**
     * The public identity of the RCL dtd file.
     */
    public static final String PUBLIC_ID = "-//UC Berkeley//DTD builder 1//EN";

    /**
     * The URL where the DTD is stored.
     */
    public static final String DTD_URL = "http://www.gigascale.org/diva/dtd/builder.dtd";

    /**
     * The DTD for builder declarations.
     */
    public static final String DTD_1 =
    "<!ELEMENT builderDecls (builder*)><!ATTLIST builderDecls ref CDATA #IMPLIED><!ATTLIST builder name CDATA #REQUIRED class CDATA #REQUIRED builder CDATA #REQUIRED>";
    
    /**
     * Indicates a recognizer class
     */
    public static final String CLASS_TAG = "class";
    
    /**
     * Indicates the tag of a recognizer
     */
    public static final String TAG_TAG = "tag";

    /**
     * Indicates a builder for a recognizer
     */
    public static final String BUILDER_TAG = "builder";

    /**
     * Indicates a group of builder declarations
     */
    public static final String BUILDER_DECLS_TAG = "builderDecls";
    
    /**
     * Store builder declarations as [tag, (class, builder)] for
     * [class, (class, builder)] aliases.
     */
    private HashMap _builders = new HashMap();

    /** Add all of the builder declarations in the given
     * XML document to the builder map.
     */
    public void addBuilderDecls(Reader in) throws Exception {
        XmlDocument doc = new XmlDocument();
        doc.setDTDPublicID(PUBLIC_ID);
        doc.setDTD(DTD_1);
        XmlReader reader = new XmlReader();
        reader.parse(doc, in);
        if(reader.getErrorCount() > 0) {
            throw new Exception("errors encountered during parsing");
        }
        for(Iterator i = doc.getRoot().elements(); i.hasNext(); ) {
            XmlElement builder = (XmlElement)i.next();
            String[] val = new String[2];
            val[0] = builder.getAttribute(CLASS_TAG);
            val[1] = builder.getAttribute(BUILDER_TAG);
            _builders.put(builder.getAttribute(TAG_TAG), val);
            _builders.put(builder.getAttribute(CLASS_TAG), val);
            debug("Adding: " + builder.getAttribute(TAG_TAG)
                    + "=>" + builder.getAttribute(BUILDER_TAG));
        }
    }

    private void debug(String out) {
        System.out.println(out);
    }

    /** Build an object based on the XML element by looking up the
     * appropriate builder and calling that builder on the element.
     */
    public Object build(XmlElement elt, String type) throws Exception {
        String[] val = (String[])_builders.get(type);
        if(val == null) {
            if(getDelegate() == null) {
                String err = "Unknown type: " + type;
                throw new Exception(err);
            }
            return getDelegate().build(elt, type);
        }
        XmlBuilder builder = (XmlBuilder)(this.getClass().forName(val[1]).newInstance());
        builder.setDelegate(this);
        return builder.build(elt, val[0]);
    }

    /** Build an XML element based on given object by looking up
     * the appropriate builder based on the object's class name
     * and calling that builder's generate method on the object.
     */
    public XmlElement generate(Object in) throws Exception {
        String[] val = (String[])_builders.get(in.getClass().getName());
        if(val == null) {
            if(getDelegate() == null) {
                String err = "Unknown type: " + in.getClass().getName();
                throw new Exception(err);
            }
            return getDelegate().generate(in);
        }
        XmlBuilder builder = (XmlBuilder)(this.getClass().forName(val[1]).newInstance());
        builder.setDelegate(this);
        return builder.generate(in);
    }
    
    /**
     * Simple test of this class.
     */
    public static void main (String args[]) throws Exception {
        if (args.length != 2) {
            System.err.println("java CompositeBuilder <builderDeclsURI> <fileURI>");
            System.exit(1);
        } else {
            XmlDocument doc = new XmlDocument();
            XmlReader reader = new XmlReader();
            CompositeBuilder builder = new CompositeBuilder();
            builder.addBuilderDecls(new FileReader(args[0]));
            reader.parse(doc, new FileReader(args[1]));
            System.out.println("out = " + builder.build(doc.getRoot(), doc.getRoot().getType()));
        }
    }
}

