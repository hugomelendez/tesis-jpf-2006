//
// Copyright (C) 2006 United States Government as represented by the
// Administrator of the National Aeronautics and Space Administration
// (NASA).  All Rights Reserved.
// 
// This software is distributed under the NASA Open Source Agreement
// (NOSA), version 1.3.  The NOSA has been approved by the Open Source
// Initiative.  See the file NOSA-1.3-JPF at the top of the distribution
// directory tree for the complete NOSA document.
// 
// THE SUBJECT SOFTWARE IS PROVIDED "AS IS" WITHOUT ANY WARRANTY OF ANY
// KIND, EITHER EXPRESSED, IMPLIED, OR STATUTORY, INCLUDING, BUT NOT
// LIMITED TO, ANY WARRANTY THAT THE SUBJECT SOFTWARE WILL CONFORM TO
// SPECIFICATIONS, ANY IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR
// A PARTICULAR PURPOSE, OR FREEDOM FROM INFRINGEMENT, ANY WARRANTY THAT
// THE SUBJECT SOFTWARE WILL BE ERROR FREE, OR ANY WARRANTY THAT
// DOCUMENTATION, IF PROVIDED, WILL CONFORM TO THE SUBJECT SOFTWARE.
//
package gov.nasa.jpf.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Misc {
  public static int hashCode(Object o) {
    return o == null ? 0 : o.hashCode();
  }
  
  public static boolean equal(Object a, Object b) {
    if (a == b) {
      return true;
    } else if (a == null || b == null) {
      // only one could be null
      return false;
    } else {
      return a.equals(b); 
    }
  }
  
  @SuppressWarnings("unchecked")
  public static <E> Iterator<E> emptyIterator() {
    return (Iterator<E>) emptyIterator;
  }
  
  @SuppressWarnings("unchecked")
  public static <E> Iterable<E> emptyIterable() {
    return (Iterable<E>) emptyIterable;
  }
  
  public static <E> Iterable<E> iterableFromIterator(Iterator<E> iter) {
    return new Iteratorable<E>(iter);
  }
  
  public static final Object[] emptyObjectArray = new Object[] {};
  
  public static final Iterator<?> emptyIterator = new Iterator<Object>() {
    public boolean hasNext () { return false; }
    public Object next () { throw new NoSuchElementException(); }
    public void remove () { throw new NoSuchElementException(); }
  };
  
  public static final Iterable<?> emptyIterable = new Iterable<Object>() {
    @SuppressWarnings("unchecked")
    public Iterator<Object> iterator () {
      return (Iterator<Object>) emptyIterator;
    }
  };
  
  /*=================== PRIVATE STUFF ===================*/
  
  private static final class Iteratorable<E> implements Iterable<E> {
    Iterator<E> iter;
    
    public Iteratorable(Iterator<E> iter) {
      this.iter = iter;
    }
    
    public Iterator<E> iterator () {
      Iterator<E> ret = iter;
      iter = null;
      return ret;
    }
  }
}
