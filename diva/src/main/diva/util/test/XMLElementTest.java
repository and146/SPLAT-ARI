/*
 * $Id: XMLElementTest.java,v 1.5 2001/11/30 02:45:55 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util.test;

import diva.util.jester.*;
import diva.util.xml.*;

import java.awt.*;
import java.awt.geom.*;
import java.util.*;

/**
 * A test suite for XmlElement
 *
 * @author John Reekie (johnr@eecs.berkeley.edu)
 * @version $Revision: 1.5 $
 */
public class XMLElementTest extends TestSuite {

    /** Constructor
     */
    public XMLElementTest (TestHarness harness) {
        setTestHarness(harness);
    }

    /**
     * runSuite()
     */
    public void runSuite () {
        testConstructor();
        testAttributes();
        testElements();
    }

    ///////////////////////////////////////////////////////////////////
    //// Test methods

    /** Test construction of XmlElement
     */
    public void testConstructor () {
        runTestCase(new TestCase("XmlElement constructor") {
            XmlElement elt;
            public void run () throws Exception {
                TreeMap attrs = new TreeMap();
                attrs.put("name0", "value0");
                attrs.put("name1", "value1");
                elt = new XmlElement("element", attrs);
            }
            public void check () throws TestFailedException {
                String result = "<element name0=\"value0\" name1=\"value1\"></element>\n";
                assertEquals(result, elt.toString(),
                        result  + " != " + elt.toString());
            }
        });
    }

    /** Test attribute setting, getting, and removing
     */
    public void testAttributes () {
        runTestCase(new TestCase("XmlElement attributes") {
            XmlElement elt;
            public void run () throws Exception {
                elt = new XmlElement("element");
                elt.setAttribute("name0", "value0");
                elt.setAttribute("name1", "value1");
            }
            public void check () throws TestFailedException {
                String result = "<element name0=\"value0\" name1=\"value1\"></element>\n";
                assertEquals(result, elt.toString(), result  + " != " + elt.toString());

                assertEquals("value0", elt.getAttribute("name0"), "Attribute name0");
                assertEquals("value1", elt.getAttribute("name1"), "Attribute name1");

                elt.setAttribute("name0", "value2");
                assertEquals("value2", elt.getAttribute("name0"), "Attribute name0 after setting");

                result = "<element name1=\"value1\"></element>\n";
                elt.removeAttribute("name0");
                assertEquals(result, elt.toString(), result  + " != " + elt.toString());
            }
        });
    }

    /** Test children manipulation
     */
    public void testElements () {
        runTestCase(new TestCase("XmlElement children") {
            XmlElement elt0, elt1,elt2;

            public void init () throws Exception {    
                elt0 = new XmlElement("element0");
                elt1 = new XmlElement("element1");
                elt2 = new XmlElement("element2");
            }
            public void run () throws Exception {    
                elt0.addElement(elt1);
                elt1.addElement(elt2);
            }
            public void check () throws TestFailedException {
                String result =
"<element0>\n<element1>\n<element2></element2>\n</element1>\n</element0>\n";
                assertEquals(result, elt0.toString(), result  + " != " + elt0.toString());

                assertExpr(elt0.containsElement(elt1), "elt0.containsElement(elt1)");
                assertExpr(elt1.containsElement(elt2), "elt1.containsElement(elt2)");

                assertExpr(!elt1.containsElement(elt0), "!elt1.containsElement(elt0)");
                assertExpr(!elt2.containsElement(elt1), "!elt2.containsElement(elt1)");

                assertExpr(!elt0.containsElement(elt2), "!elt0.containsElement(elt2)");
                assertExpr(!elt2.containsElement(elt0), "!elt2.containsElement(elt0)");

                // No go ahead and remove some stuff
                result = "<element0></element0>\n";
                elt0.removeElement(elt1);
                assertEquals(result, elt0.toString(), result  + " != " + elt0.toString());
                assertExpr(!elt0.containsElement(elt1), "!elt0.containsElement(elt1)");
            }
        });
    }

    //////////////////////////////////////////////////////////// 
    ////  main

    /** Create a default test harness and
     * run all tests on it.
     */
    public static void main (String argv[]) {
        new XMLElementTest(new TestHarness()).run();
    }
}


