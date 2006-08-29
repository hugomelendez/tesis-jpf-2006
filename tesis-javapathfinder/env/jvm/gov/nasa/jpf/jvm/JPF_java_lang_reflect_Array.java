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
 * MJI NativePeer class for java.lang.reflect.Array library abstraction
 */
public class JPF_java_lang_reflect_Array {
  
  public static int getLength__Ljava_lang_Object_2__I (MJIEnv env, int clsObjRef, 
                                                    int objRef) {
    return env.getArrayLength(objRef);
  }
  
  public static int newArray__Ljava_lang_Class_2I__Ljava_lang_Object_2 (MJIEnv env, int clsRef,
                                                                        int componentTypeRef, int length) {
    ClassInfo ci = JPF_java_lang_Class.getReferredClassInfo(env, componentTypeRef);
    String clsName = ci.getName();
    
    return createNewArray( env, clsName, length);
  }
  
  static int createNewArray (MJIEnv env, String clsName, int length) {
    int aRef = MJIEnv.NULL;
    
    if ("boolean".equals(clsName)) { aRef = env.newBooleanArray(length); }
    else if ("byte".equals(clsName)) { aRef = env.newByteArray(length); }
    else if ("char".equals(clsName)) { aRef = env.newCharArray(length); }
    else if ("short".equals(clsName)) { aRef = env.newShortArray(length); }
    else if ("int".equals(clsName)) { aRef = env.newIntArray(length); }
    else if ("long".equals(clsName)) { aRef = env.newLongArray(length); }
    else if ("float".equals(clsName)) { aRef = env.newFloatArray(length); }
    else if ("double".equals(clsName)) { aRef = env.newDoubleArray(length); }
    else { aRef = env.newObjectArray(clsName, length); }
    
    return aRef;
    
  }
  
  public static int multiNewArray__Ljava_lang_Class_2_3I__Ljava_lang_Object_2 (MJIEnv env, int clsRef,
                                                                               int componentTypeRef,
                                                                               int dimArrayRef) {
    ClassInfo ci = JPF_java_lang_Class.getReferredClassInfo(env, componentTypeRef);
    String clsName = ci.getName();
    int n = env.getArrayLength(dimArrayRef);
    int i;

    clsName = Types.getTypeCode(clsName);
    
    String arrayType = "[";
    for (i=2; i<n; i++) arrayType += '[';
    arrayType += clsName;
    
    int[] dim = new int[n];
    for (i=0; i<n; i++) {
      dim[i] = env.getIntArrayElement(dimArrayRef, i);
    }
    
    int aRef = createNewMultiArray(env, arrayType, dim, 0); 
    return aRef;
  }
  
  static int createNewMultiArray (MJIEnv env, String arrayType, int[] dim, int level) {
    int aRef = MJIEnv.NULL;
    int len = dim[level];
    
    if (level < dim.length-1) {
      aRef = env.newObjectArray(arrayType, len);
    
      for (int i=0; i<len; i++) {
        int eRef = createNewMultiArray(env, arrayType.substring(1), dim, level+1);
        env.setReferenceArrayElement(aRef, i, eRef);
      }
    } else {
      aRef = createNewArray( env, arrayType, len);
    }
    
    return aRef;
  }
  
}
