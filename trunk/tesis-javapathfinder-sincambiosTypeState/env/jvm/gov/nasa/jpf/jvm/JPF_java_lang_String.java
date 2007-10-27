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

/**
 * MJI NativePeer class for java.lang.String library abstraction
 */
public class JPF_java_lang_String {
  
  public static int intern____Ljava_lang_String_2 (MJIEnv env, int robj) {
    // now this is REALLY a naive implementation of 'intern', but
    // at least we don't burn state for a Hashtable.
    // Replace this once we have a real String abstraction
    DynamicArea   da = env.getDynamicArea();
    ElementInfo   sei = da.get(robj);

    // <2do> really? with heap symmetry, can't a corresponding one be at > robj?
    //*reply:
    // this picks a canonical one, the one with the lowest objref.  if a match
    // is at a point > robj (or there is no other matching string), robj is
    // the canonical. -peterd 
    for (int i = 0; i < robj; i++) {
      ElementInfo ei = da.get(i);

      if (ei != null) {
        if (ei.equals(sei)) {
          return i;
        }
      }
    }

    return robj;
  }
}
