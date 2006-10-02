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
package gov.nasa.jpf.search;

import gov.nasa.jpf.Property;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.jvm.JVM;

import java.io.PrintWriter;

/**
 * simple list abstracting a set of Properties. This version cannot deal with
 * reporting multiple failures 
 */
public class PropertyMulticaster implements Property {
  Property head, tail;
  boolean headFailed, tailFailed;
  
  public static Property add (Property oldProperty, Property newProperty) {
    if (newProperty == null) {
      return oldProperty;
    }
    if (oldProperty == null) {
      return newProperty;
    }
    
    // we store in the order of registration, multiple entries are allowed
    // (but generally useless)
    return new PropertyMulticaster(oldProperty, newProperty);
  }
  
  public static Property remove (Property oldProperty, Property removeProperty){
    if (oldProperty == removeProperty) {
      return null;
    }
    if (oldProperty instanceof PropertyMulticaster){
      return ((PropertyMulticaster)oldProperty).remove( removeProperty);
    }
    
    return oldProperty;
  }
  
  protected Property remove (Property p) {
    if (p == head) {
      return tail;
    }
    if (p == tail){
      return head;
    }
    
    if (head instanceof PropertyMulticaster) {
      if (tail instanceof PropertyMulticaster){
        return new PropertyMulticaster( ((PropertyMulticaster)head).remove(p), 
                                        ((PropertyMulticaster)tail).remove(p));
      } else {
        return new PropertyMulticaster( ((PropertyMulticaster)head).remove(p), tail);
      }
    } else if (tail instanceof PropertyMulticaster) {
      return new PropertyMulticaster( head, ((PropertyMulticaster)tail).remove(p));
    }
    
    return this;
  }
  
  public PropertyMulticaster (Property h, Property t) {
    head = h;
    tail = t;
  }
  
  public boolean check (Search search, JVM vm) {
    if (!head.check(search, vm)){
      headFailed = true;
      return false;
    }
    if (!tail.check(search, vm)){
      tailFailed = true;
      return false;
    }
    
    return true;
  }

  public String getErrorMessage () {
    if (headFailed) {
      return head.getErrorMessage();
    } else if (tailFailed){
      return tail.getErrorMessage();
    }
    
    return null;
  }

  public void printOn (PrintWriter pw) {
    if (headFailed){
      head.printOn(pw);
    } else if (tailFailed) {
      tail.printOn(pw);
    }
  }
}
