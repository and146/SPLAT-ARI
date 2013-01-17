// HandlerBase.java: Simple base class for AElfred processors.
// NO WARRANTY! See README, and copyright below.
// $Id: HandlerBase.java,v 1.3 2001/07/22 22:02:05 johnr Exp $

//package com.microstar.xml;
package diva.util.aelfred;

//import com.microstar.xml.XmlHandler;
//import com.microstar.xml.XmlException;
import java.io.Reader;


/**
  * Convenience base class for AElfred handlers.
  * <p>This base class implements the XmlHandler interface with
  * (mostly empty) default handlers.  You are not required to use this,
  * but if you need to handle only a few events, you might find
  * it convenient to extend this class rather than implementing
  * the entire interface.  This example overrides only the
  * <code>charData</code> method, using the defaults for the others:
  * <pre>
  * import diva.util.aelfred.HandlerBase;
  *
  * public class MyHandler extends HandlerBase {
  *   public void charData (char ch[], int start, int length)
  *   {
  *     System.out.println("Data: " + new String (ch, start, length));
  *   }
  * }
  * </pre>
  * <p>This class is optional, but if you use it, you must also
  * include the <code>XmlException</code> class.
  * <p>Do not extend this if you are using SAX; extend
  * <code>org.xml.sax.HandlerBase</code> instead.
  * @author Copyright (c) 1998 by Microstar Software Ltd.
  * @author written by David Megginson &lt;dmeggins@microstar.com&gt;
  * @version 1.1
  * @see XmlHandler
  * @see XmlException
  * @see org.xml.sax.HandlerBase
  */
public class HandlerBase implements XmlHandler {

  /**
    * Handle the start of the document.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#startDocument
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void startDocument () 
    throws java.lang.Exception
  {
  }

  /**
    * Handle the end of the document.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#endDocument
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void endDocument ()
    throws java.lang.Exception
  {
  }

  /**
    * Resolve an external entity.
    * <p>The default implementation simply returns the supplied
    * system identifier.
    * @see diva.util.aelfred.XmlHandler#resolveEntity
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public Object resolveEntity (String publicId, String systemId) 
    throws java.lang.Exception
  {
    return null;
  }


  /**
    * Handle the start of an external entity.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#startExternalEntity
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void startExternalEntity (String systemId)
    throws java.lang.Exception
  {
  }

  /**
    * Handle the end of an external entity.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#endExternalEntity
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void endExternalEntity (String systemId)
    throws java.lang.Exception
  {
  }

  /**
    * Handle a document type declaration.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#doctypeDecl
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void doctypeDecl (String name, String publicId, String systemId)
    throws java.lang.Exception
  {
  }

  /**
    * Handle an attribute assignment.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#attribute
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void attribute (String aname, String value, boolean isSpecified)
    throws java.lang.Exception
  {
  }

  /**
    * Handle the start of an element.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#startElement
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void startElement (String elname)
    throws java.lang.Exception
  {
  }

  /**
    * Handle the end of an element.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#endElement
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void endElement (String elname)
    throws java.lang.Exception
  {
  }

  /**
    * Handle character data.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#charData
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void charData (char ch[], int start, int length)
    throws java.lang.Exception
  {
  }

  /**
    * Handle ignorable whitespace.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#ignorableWhitespace
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void ignorableWhitespace (char ch[], int start, int length)
    throws java.lang.Exception
  {
  }

  /**
    * Handle a processing instruction.
    * <p>The default implementation does nothing.
    * @see diva.util.aelfred.XmlHandler#processingInstruction
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void processingInstruction (String target, String data)
    throws java.lang.Exception
  {
  }

  /**
    * Throw an exception for a fatal error.
    * <p>The default implementation throws <code>XmlException</code>.
    * @see diva.util.aelfred.XmlHandler#error
    * @exception diva.util.aelfred.XmlException A specific parsing error.
    * @exception java.lang.Exception Derived methods may throw exceptions.
    */
  public void error (String message, String systemId, int line, int column)
    throws XmlException, java.lang.Exception
  {
    throw new XmlException(message, systemId, line, column);
  }

}




