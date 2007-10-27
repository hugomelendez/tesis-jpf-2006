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

public class JPF_java_lang_reflect_Field {

  // the registry is rather braindead, let's hope we don't have many lookups - 
  // using Fields is fine, but creating them is not efficient until we fix this
  
  static final int NREG = 10;
  static FieldInfo[] registered = new FieldInfo[NREG];
  static int nRegistered;
  
  static int registerFieldInfo (FieldInfo fi) {
    int idx;
    
    for (idx=0; idx < nRegistered; idx++) {
      if (registered[idx] == fi) {
        return idx;
      }
    }
    
    if (idx == registered.length) {
      FieldInfo[] newReg = new FieldInfo[registered.length+NREG];
      System.arraycopy(registered, 0, newReg, 0, registered.length);
    }
    
    registered[idx] = fi;
    nRegistered++;
    return idx;
  }
  
  static FieldInfo getRegisteredFieldInfo (int idx) {
    return registered[idx];
  }
  
  static int getIntField (MJIEnv env, int objRef, int fobjRef, Class<?> fiType, String type) {
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = env.getElementInfo(fobjRef);

    // our guards (still need IllegalAccessException)
    if (ei == null) {
      env.throwException("java.lang.NullPointerException");
      return 0;      
    }
    if (!fiType.isInstance(fi)) {
      env.throwException("java.lang.IllegalArgumentException", "field type incompatible with " + type);
      return 0;
    }
    
    int val = ei.getIntField(fi);
    return val;    
  }

  static long getLongField (MJIEnv env, int objRef, int fobjRef, Class<?> fiType, String type) {
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = env.getElementInfo(fobjRef);

    // our guards (still need IllegalAccessException)
    if (ei == null) {
      env.throwException("java.lang.NullPointerException");
      return 0;      
    }
    if (!fiType.isInstance(fi)) {
      env.throwException("java.lang.IllegalArgumentException", "field type incompatible with " + type);
      return 0;
    }
    
    long val = ei.getLongField(fi);
    return val;    
  }

  static void setIntField (MJIEnv env, int objRef, int fobjRef,
                           Class<?> fiType, String type, int val) {
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = env.getElementInfo(fobjRef);

    // our guards (still need IllegalAccessException)
    if (ei == null) {
      env.throwException("java.lang.NullPointerException");
      return;      
    }
    if (!fiType.isInstance(fi)) {
      env.throwException("java.lang.IllegalArgumentException", "field type incompatible with " + type);
      return;
    }
    
    ei.setIntField(fi, val);    
  }  

  static void setLongField (MJIEnv env, int objRef, int fobjRef,
                           Class<?> fiType, String type, long val) {
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = env.getElementInfo(fobjRef);

    // our guards (still need IllegalAccessException)
    if (ei == null) {
      env.throwException("java.lang.NullPointerException");
      return;      
    }
    if (!fiType.isInstance(fi)) {
      env.throwException("java.lang.IllegalArgumentException", "field type incompatible with " + type);
      return;
    }
    
    ei.setLongField(fi, val);    
  }  
  
  public static boolean getBoolean__Ljava_lang_Object_2__Z (MJIEnv env, int objRef, int fobjRef) {
    int v = getIntField(env, objRef, fobjRef, IntegerFieldInfo.class, "boolean");
    return (v != 0) ? true : false;
  }
  public static byte getByte__Ljava_lang_Object_2__B (MJIEnv env, int objRef, int fobjRef) {
    int v = getIntField(env, objRef, fobjRef, IntegerFieldInfo.class, "byte");
    return (byte)v;
  }
  public static char getChar__Ljava_lang_Object_2__C (MJIEnv env, int objRef, int fobjRef) {
    int v = getIntField(env, objRef, fobjRef, IntegerFieldInfo.class, "char");
    return (char)v;
  }
  public static short getShort__Ljava_lang_Object_2__S (MJIEnv env, int objRef, int fobjRef) {
    int v = getIntField(env, objRef, fobjRef, IntegerFieldInfo.class, "short");
    return (short)v;
  }  
  public static int getInt__Ljava_lang_Object_2__I (MJIEnv env, int objRef, int fobjRef) {
    return getIntField(env, objRef, fobjRef, IntegerFieldInfo.class, "int");
  }
  public static long getLong__Ljava_lang_Object_2__J (MJIEnv env, int objRef, int fobjRef) {
    return getLongField(env, objRef, fobjRef, LongFieldInfo.class, "long");
  }
  public static float getFloat__Ljava_lang_Object_2__F (MJIEnv env, int objRef, int fobjRef) {
    int v = getIntField(env, objRef, fobjRef, FloatFieldInfo.class, "float");
    return Types.intToFloat(v);
  }
  public static double getDouble__Ljava_lang_Object_2__D (MJIEnv env, int objRef, int fobjRef) {
    long v = getLongField(env, objRef, fobjRef, DoubleFieldInfo.class, "double");
    return Types.longToDouble(v);
  }


  public static void setBoolean__Ljava_lang_Object_2Z__V (MJIEnv env, int objRef, int fobjRef,
                                                          boolean val) {
    setIntField( env, objRef, fobjRef, IntegerFieldInfo.class, "boolean", val ? 1 : 0);
  }
  public static void setByte__Ljava_lang_Object_2B__V (MJIEnv env, int objRef, int fobjRef,
                                                          byte val) {
    setIntField( env, objRef, fobjRef, IntegerFieldInfo.class, "byte", val);
  }
  public static void setChar__Ljava_lang_Object_2C__V (MJIEnv env, int objRef, int fobjRef,
                                                       char val) {
    setIntField( env, objRef, fobjRef, IntegerFieldInfo.class, "char", val);
  }
  public static void setShort__Ljava_lang_Object_2S__V (MJIEnv env, int objRef, int fobjRef,
                                                       short val) {
    setIntField( env, objRef, fobjRef, IntegerFieldInfo.class, "short", val);
  }  
  public static void setInt__Ljava_lang_Object_2I__V (MJIEnv env, int objRef, int fobjRef,
                                                      int val) {
    setIntField( env, objRef, fobjRef, IntegerFieldInfo.class, "int", val);
  }
  public static void setLong__Ljava_lang_Object_2J__V (MJIEnv env, int objRef, int fobjRef,
                                                       long val) {
    setLongField( env, objRef, fobjRef, LongFieldInfo.class, "long", val);
  }
  public static void setFloat__Ljava_lang_Object_2F__V (MJIEnv env, int objRef, int fobjRef,
                                                        float val) {
    setIntField( env, objRef, fobjRef, FloatFieldInfo.class, "float", Types.floatToInt(val));
  }
  public static void setDouble__Ljava_lang_Object_2D__V (MJIEnv env, int objRef, int fobjRef,
                                                         double val) {
    setLongField( env, objRef, fobjRef, DoubleFieldInfo.class, "double", Types.doubleToLong(val));
  }

  
  
  public static int get__Ljava_lang_Object_2__Ljava_lang_Object_2 (MJIEnv env, int objRef, int fobjRef) {
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = env.getElementInfo(fobjRef);

    // our guards (still need IllegalAccessException)
    if (ei == null) {
      env.throwException("java.lang.NullPointerException");
      return 0;      
    }
    
    // <2do> that's wrong - primitive types need to be boxed
    if (!(fi instanceof ReferenceFieldInfo)) {
      env.throwException("java.lang.IllegalArgumentException", "field type not Object");
      return MJIEnv.NULL;
    }
    
    int val = ei.getIntField(fi); // we internally store it as int
    return val;
    
  }
  

  
  public static int getName____Ljava_lang_String_2 (MJIEnv env, int objRef) {
    FieldInfo fi = getFieldInfo(env, objRef);
    
    int nameRef = env.getReferenceField( objRef, "name");
    if (nameRef == -1) {
      nameRef = env.newString(fi.getName());
      env.setReferenceField(objRef, "name", nameRef);
    }
   
    return nameRef;
  }
  
  static FieldInfo getFieldInfo (MJIEnv env, int objRef) {
    int fidx = env.getIntField( objRef, "regIdx");
    assert ((fidx >= 0) || (fidx < nRegistered)) : "illegal FieldInfo request: " + fidx + ", " + nRegistered;
    
    return registered[fidx];
  }
}
