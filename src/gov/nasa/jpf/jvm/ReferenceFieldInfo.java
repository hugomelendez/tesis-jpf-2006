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
package gov.nasa.jpf.jvm;

import org.apache.bcel.classfile.*;
import gov.nasa.jpf.JPFException;


/**
 * field info for object fields
 */
public class ReferenceFieldInfo extends FieldInfo {
  int init=-1;
  String sInit; // <2do> pcm - just a temporary quirk to init from string literals
                // check if there are other non-object reference inits
  
  public ReferenceFieldInfo (String name, String type, int staticAttr,
                             ConstantValue cv, ClassInfo ci, int idx, int off) {
    super(name, type, staticAttr, cv, ci, idx, off);
    init = computeInitValue(cv);
  }

  public String valueToString (Fields f) {
    int i = f.getIntValue(storageOffset);
    if (i == -1) {
      return "null";
    } else {
      return DynamicArea.getHeap().get(i).toString();
    }
  }
  
  public boolean isReference () {
    return true;
  }
  
  public boolean isArrayField () {
    return ci.isArray;
  }
  
  int computeInitValue (ConstantValue cv) {
    // <2do> pcm - check what other constants we might encounter, this is most
    // probably not just used for Strings.
    // Besides the type issue, there is an even bigger problem with identities.
    // For instance, all String refs initialized via the same string literal
    // inside a single classfile are in fact refering to the same object. This
    // means we have to keep a registry (hashtab) with string-literal created
    // String objects per ClassInfo, and use this when we assign or init
    // String references.
    // For the sake of progress, we ignore this for now, but have to come back
    // to it because it violates the VM spec
    
    if (cv == null) return -1;
    
    // here the mess starts
    //DynamicArea heap = DynamicArea.getHeap();
    String s = cv.toString();
    
    if (s.charAt(0) == '"') {
      s = s.substring(1,s.length()-1); // chop off the double quotes
      sInit = s;
      
      //init = heap.newString(s, null);  // turn literal into a string object
      // but how do we pin it down?
    } else {
      throw new JPFException ("unsupported reference initialization: " + s);
    }
    
    return -1;
  }
  
  public void initialize (Fields f) {
    int ref = init;
    if (sInit != null) {
      ref = DynamicArea.getHeap().newString(sInit, null);
    }
    f.setReferenceValue(storageOffset, ref);
  }
}
