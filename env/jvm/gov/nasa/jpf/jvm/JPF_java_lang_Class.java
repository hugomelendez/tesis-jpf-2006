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

import gov.nasa.jpf.jvm.bytecode.Instruction;


/**
 * MJI NativePeer class for java.lang.Class library abstraction
 */
public class JPF_java_lang_Class {
  public static boolean isArray____Z (MJIEnv env, int robj) {
    return getReferredClassInfo(env, robj).isArray();
  }

  public static int getComponentType____Ljava_lang_Class_2 (MJIEnv env, int robj) {
    if (isArray____Z(env, robj)) {
      ThreadInfo ti = env.getThreadInfo();
      Instruction insn = ti.getPC();
      ClassInfo ci = getReferredClassInfo(env, robj).getComponentClassInfo();

      if (insn.requiresClinitCalls(ti, ci)) {
        env.repeatInvocation();
        return MJIEnv.NULL;
      }

      return ci.getClassObjectRef();
    }

    return MJIEnv.NULL;
  }

  public static boolean isInstance__Ljava_lang_Object_2__Z (MJIEnv env, int robj,
                                                         int r1) {
    ElementInfo sei = env.getClassElementInfo(robj);
    ClassInfo   ci = sei.getClassInfo();
    ClassInfo   ciOther = env.getClassInfo(r1);

    return (ciOther.instanceOf(ci.getName()));
  }

  public static boolean isAssignableFrom__Ljava_lang_Class_2__Z (MJIEnv env, int rcls,
                                                              int r1) {
    ElementInfo sei1 = env.getClassElementInfo(rcls);
    ClassInfo   ci1 = sei1.getClassInfo();

    ElementInfo sei2 = env.getClassElementInfo(r1);
    ClassInfo   ci2 = sei2.getClassInfo();
    
    return ci2.instanceOf( ci1.getName());
  }
  
  public static int getPrimitiveClass__Ljava_lang_String_2__Ljava_lang_Class_2 (MJIEnv env,
                                                            int rcls, int stringRef) {
    String clsName = env.getStringObject(stringRef);

    // we don't really have to check for a valid class name here, since
    // this is a package default method that just gets called from
    // the clinit of box classes
    // note this does NOT return the box class (e.g. java.lang.Integer), which
    // is a normal, functional class, but a primitive class (e.g. 'int') that
    // is rather a strange beast (not even Object derived)
    StaticArea        sa = env.getStaticArea();
    StaticElementInfo ei = sa.get(clsName);
    int               cref = ei.getClassObjectRef();
    env.setBooleanField(cref, "isPrimitive", true);

    return cref;
  }

  public static boolean desiredAssertionStatus____Z (MJIEnv env, int robj) {
    ClassInfo ci = getReferredClassInfo(env,robj);
    return ci.areAssertionsEnabled();
  }
  
  public static int forName__Ljava_lang_String_2__Ljava_lang_Class_2 (MJIEnv env,
                                                                       int rcls,
                                                                       int stringRef) {
    ThreadInfo ti = env.getThreadInfo();
    Instruction insn = ti.getPC();
    String            clsName = env.getStringObject(stringRef);
    ClassInfo         ci = ClassInfo.getClassInfo(clsName);
    
    if (insn.requiresClinitCalls(ti, ci)) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }

    StaticElementInfo ei = env.getStaticArea().get(clsName);
    int               ref = ei.getClassObjectRef();

    return ref;
  }

  /**
   * this is an example of a native method issuing direct calls - otherwise known
   * as a round trip.
   * We don't have to deal with class init here anymore, since this is called
   * via the class object of the class to instantiate
   */
  public static int newInstance____Ljava_lang_Object_2 (MJIEnv env, int robj) {
    ClassInfo ci = getReferredClassInfo(env,robj);   // what are we
    ThreadInfo ti = env.getThreadInfo();
    Instruction insn = ti.getPC();
    int objRef = MJIEnv.NULL;
        
    // this is a java.lang.Class instance method, so the class we are instantiating
    // must already be initialized (either by Class.forName() or accessing the
    // .class field
    
    if (!ti.isResumedInstruction(insn)) {
      objRef = env.getDynamicArea().newObject(ci, ti);  // create the thing
    
      MethodInfo mi = ci.getMethod("<init>()V", true);
      if (mi != null) { // Oops - direct call required
        // <2do> that's still overly simplistic - leave alone SecurityManager, we have to deal with
        // IllegalAccessException - if the class or its nullary constructor is not accessible.
        // InstantiationException - if this Class represents an abstract class, interface,
        //                          array class, a primitive type, or has no nullary ctor
        // ExceptionInInitializerError - if the initialization  provoked by this method fails.
        
        MethodInfo stub = mi.createDirectCallStub("[init]");
        DirectCallStackFrame frame = new DirectCallStackFrame(stub, insn);
        frame.push( objRef, true);
        // Hmm, we borrow the DirectCallStackFrame to cache the object ref
        // (don't try that with a normal StackFrame)
        frame.dup();
        ti.pushFrame(frame);
        env.repeatInvocation();
        return MJIEnv.NULL;
      }
        
    } else { // it was resumed after we had to direct call the default ctor
      objRef = ti.getReturnedDirectCall().pop();
    }
    
    return objRef;
  }
  
  public static int getSuperclass____Ljava_lang_Class_2 (MJIEnv env, int robj) {
    ClassInfo ci = getReferredClassInfo(env, robj);
    ClassInfo sci = ci.getSuperClass();
    if (sci != null) {
      return sci.getClassObjectRef();
    } else {
      return MJIEnv.NULL;
    }
  }

  static int getMethod (MJIEnv env, int clsRef, int nameRef, int argTypesRef,
                        boolean isRecursiveLookup) {
    ClassInfo ci = getReferredClassInfo(env, clsRef);
    String mname = env.getStringObject(nameRef);
    
    StringBuffer sb = new StringBuffer(mname);
    sb.append('(');
    int nParams = env.getArrayLength(argTypesRef);
    for (int i=0; i<nParams; i++) {
      int cRef = env.getReferenceArrayElement(argTypesRef, i);
      ClassInfo cit = getReferredClassInfo(env, cRef);
      String tname = cit.getName();
      String tcode = tname;
      tcode = Types.getTypeCode(tcode);
      sb.append(tcode);
    }
    sb.append(')');
    String fullMthName = sb.toString();

    MethodInfo mi = ci.getReflectionMethod(fullMthName, isRecursiveLookup);
    if (mi == null) {
      env.throwException("java.lang.NoSuchMethodException");
      return MJIEnv.NULL;
      
    } else {
      ThreadInfo ti = env.getThreadInfo();
      Instruction insn = ti.getPC();
      ClassInfo mci = ClassInfo.getClassInfo("java.lang.reflect.Method");
      
      if (insn.requiresClinitCalls(ti, mci)) {
        env.repeatInvocation();
        return MJIEnv.NULL;
      }
      
      int regIdx = JPF_java_lang_reflect_Method.registerMethodInfo(mi);
      int eidx = env.newObject(mci);
      ElementInfo ei = env.getElementInfo(eidx);
      
      ei.setIntField("regIdx", regIdx);
      return eidx;      
    }
  }

  public static int getDeclaredMethod__Ljava_lang_String_2_3Ljava_lang_Class_2__Ljava_lang_reflect_Method_2 (MJIEnv env, int clsRef,
                                                                                                     int nameRef, int argTypesRef) {
    return getMethod(env, clsRef, nameRef, argTypesRef, false);
  }

  public static int getMethod__Ljava_lang_String_2_3Ljava_lang_Class_2__Ljava_lang_reflect_Method_2 (MJIEnv env, int clsRef,
                                                                                                     int nameRef, int argTypesRef) {
    return getMethod(env, clsRef, nameRef, argTypesRef, true);
  }

  
  static int getField (MJIEnv env, int clsRef, int nameRef, boolean isRecursiveLookup) {
    ClassInfo ci = getReferredClassInfo(env, clsRef);
    String fname = env.getStringObject(nameRef);
    FieldInfo fi = null;
    
    if (isRecursiveLookup) {
      fi = ci.getInstanceField(fname);
      if (fi == null) {
        fi = ci.getStaticField(fname);
      }      
    } else {
        fi = ci.getDeclaredInstanceField(fname);
        if (fi == null) {
          fi = ci.getDeclaredStaticField(fname);
        }
    }
    
    if (fi == null) {      
      env.throwException("java.lang.NoSuchFieldException", ci.getName() + '.' + fname);
      return MJIEnv.NULL;
      
    } else {
      ThreadInfo ti = env.getThreadInfo();
      Instruction insn = ti.getPC();
      ClassInfo fci = ClassInfo.getClassInfo("java.lang.reflect.Field");
      
      if (insn.requiresClinitCalls(ti, fci)) {
        env.repeatInvocation();
        return MJIEnv.NULL;
      }
      
      int regIdx = JPF_java_lang_reflect_Field.registerFieldInfo(fi);
      int eidx = env.newObject(fci);
      ElementInfo ei = env.getElementInfo(eidx);
      
      ei.setIntField("regIdx", regIdx);
      return eidx;
    }
  }
  
  public static int getDeclaredField__Ljava_lang_String_2__Ljava_lang_reflect_Field_2 (MJIEnv env, int clsRef, int nameRef) {
    return getField(env,clsRef,nameRef, false);
  }  
 
  public static int getField__Ljava_lang_String_2__Ljava_lang_reflect_Field_2 (MJIEnv env, int clsRef, int nameRef) {
    return getField(env,clsRef,nameRef, true);    
  }


  
  static ClassInfo getReferredClassInfo (MJIEnv env, int robj) {
    // this is only the ElementInfo for the java.lang.Class object
    /*ElementInfo ei =*/ env.getElementInfo(robj); // needed? -pcd

    // get the ClassInfo it refers to
    int         idx = env.getIntField(robj, "cref");
    StaticArea  sa = env.getStaticArea();
    ElementInfo sei = sa.get(idx);

    return sei.getClassInfo();
  }
  
}
