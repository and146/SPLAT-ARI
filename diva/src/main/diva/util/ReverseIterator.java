/*
 * $Id: ReverseIterator.java,v 1.4 2002/01/12 00:06:41 johnr Exp $
 *
 * Copyright (c) 1998-2001 The Regents of the University of California.
 * All rights reserved. See the file COPYRIGHT for details.
 */
package diva.util;

import java.util.List;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A reverse-order iterator over a List.
 *
 * @author John Reekie
 * @version $Revision: 1.4 $
 */
public class ReverseIterator extends  IteratorAdapter {
  private List _list;
  private int _cursor;

  /** Construct a reverse iterator on the given list.
   */
  public ReverseIterator (List list) {
    this._list = list;
    _cursor = list.size();
  }

  /** Test if there are more elements
   */
  public boolean hasNext() {
    return _cursor > 0;
  }

  /** Return the next element.
   */
  public Object next() {
    _cursor--;
    return _list.get(_cursor);
  }
}



