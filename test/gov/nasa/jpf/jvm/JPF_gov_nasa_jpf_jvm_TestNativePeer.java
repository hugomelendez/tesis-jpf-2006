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
 * native peer class for unit testing MJI
 */
public class JPF_gov_nasa_jpf_jvm_TestNativePeer {
  public static void $clinit (MJIEnv env, int rcls) {
    env.setStaticIntField(rcls, "sdata", 42);
  }

  public static void $init__I__V (MJIEnv env, int robj, int i) {
    env.setIntField(robj, "idata", i);
  }

  public static int nativeCreate2DimIntArray__II___3_3I (MJIEnv env, int robj, int size1,
                                              int size2) {
    int ar = env.newObjectArray("[I", size1);

    for (int i = 0; i < size1; i++) {
      int ea = env.newIntArray(size2);

      if (i == 1) {
        env.setIntArrayElement(ea, 1, 42);
      }

      env.setReferenceArrayElement(ar, i, ea);
    }

    return ar;
  }

  // check if the non-mangled name lookup works
  public static int nativeCreateIntArray (MJIEnv env, int robj, int size) {
    int ar = env.newIntArray(size);

    env.setIntArrayElement(ar, 1, 1);

    return ar;
  }

  public static int nativeCreateStringArray (MJIEnv env, int robj, int size) {
    int ar = env.newObjectArray("Ljava/lang/String;", size);

    env.setReferenceArrayElement(ar, 1, env.newString("one"));

    return ar;
  }

  public static void nativeException____V (MJIEnv env, int robj) {
    env.throwException("java.lang.UnsupportedOperationException", "caught me");
  }

  @SuppressWarnings("null")
  public static int nativeCrash (MJIEnv env, int robj) {
    String s = null;
    return s.length();
  }
  
  public static int nativeInstanceMethod (MJIEnv env, int robj, double d,
                                          char c, boolean b, int i) {
    if ((d == 2.0) && (c == '?') && b) {
      return i + 2;
    }

    return 0;
  }

  public static long nativeStaticMethod__JLjava_lang_String_2__J (MJIEnv env, int rcls, long l,
                                                                  int stringRef) {
    String s = env.getStringObject(stringRef);

    if ("Blah".equals(s)) {
      return l + 2;
    }

    return 0;
  }
}
