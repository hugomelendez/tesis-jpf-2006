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

import gov.nasa.jpf.jvm.MJIEnv;

/**
 * MJI NativePeer class for java.lang.StringBuffer library abstraction
 */
public class JPF_java_lang_StringBuffer {
  
  static boolean hasSharedField = false; // Java 1.4 has, 1.5 doesn't
  
  public static void $clinit____V (MJIEnv env, int clsObjRef) {
    // apparently, Java 1.5 has changed the implementation of class
    // StringBuffer so that it doesn't use the 'shared' state anymore
    // (which was a performance hack to avoid copying the char array
    // data when creating String objects from subsequently unmodified
    // StringBuffers
    // adding this little extra logic here also serves the purpose of
    // avoiding a native ObjectStreamClass method which is called during
    // the static StringBuffer init
    ClassInfo ci = env.getClassInfo();
    if (ci.getInstanceField("shared") != null) {
      hasSharedField = true;
    }
  }
  
  static int appendString (MJIEnv env, int objref, String s) {
    int slen = s.length();
    int aref = env.getReferenceField(objref, "value");
    int alen = env.getArrayLength(aref);
    int count = env.getIntField(objref, "count");
    int i, j;
    int n = count + slen;
    
    if (n < alen) {
      for (i=count, j=0; i<n; i++, j++) {
        env.setCharArrayElement(aref, i, s.charAt(j));
      }
    } else {
      int m = 3 * alen / 2;
      if (m < n) {
        m = n;
      }
      int arefNew = env.newCharArray(m);
      for (i=0; i<count; i++) {
        env.setCharArrayElement(arefNew, i, env.getCharArrayElement(aref, i));
      }
      for (j=0; i<n; i++, j++) {
        env.setCharArrayElement(arefNew, i, s.charAt(j));
      }
      env.setReferenceField(objref, "value", arefNew);
    }
    
    if (hasSharedField) {
      env.setBooleanField(objref, "shared", false);
    }
    env.setIntField(objref, "count", n);
    
    return objref;
  }

/*
  public static int append__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, int sbref) {
    int vref = env.getReferenceField(sbref, "value");
    int sbCount = env.getIntField(sbref, "count");

    // how braindead, how lazy
    char[] b = env.getCharArrayObject(vref);
    String s = new String(b, 0, sbCount);
    
    return appendString(env, objref, s);
  }
*/
  public static int append__Ljava_lang_String_2__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, int sref) {
    String s = env.getStringObject(sref);
    
    return appendString(env, objref, s);
  }
  
  public static int append__I__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, int i) {
    String s = Integer.toString(i);
    
    return appendString(env, objref, s);
  }

  public static int append__F__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, float f) {
    String s = Float.toString(f);
    
    return appendString(env, objref, s);
  }

  public static int append__D__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, double d) {
    String s = Double.toString(d);
    
    return appendString(env, objref, s);
  }
  
  public static int append__J__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, long l) {
    String s = Long.toString(l);
    
    return appendString(env, objref, s);
  }

  public static int append__Z__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, boolean b) {
    String s = b ? "true" : "false";
    
    return appendString(env, objref, s);
  }
 
/*
  public static int append__B__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, byte b) {
    return append__C__Ljava_lang_StringBuffer_2(env, objref, (char)b);
  }
*/
 
  public static int append__C__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, char c) {
    int aref = env.getReferenceField(objref, "value");
    int alen = env.getArrayLength(aref);
    int count = env.getIntField(objref, "count");
    int n = count +1;
    
    if (n < alen) {
      env.setCharArrayElement(aref, count, c);
    } else {
      int m = 3 * alen / 2;
      int arefNew = env.newCharArray(m);
      for (int i=0; i<count; i++) {
        env.setCharArrayElement(arefNew, i, env.getCharArrayElement(aref, i));
      }
      env.setCharArrayElement(arefNew, count, c);
      env.setReferenceField(objref, "value", arefNew);
    }
    
    if (hasSharedField) {
      env.setBooleanField(objref, "shared", false);
    }
    env.setIntField(objref, "count", n);
    
    return objref;
    
  }
}

