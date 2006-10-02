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
 * MJI NativePeer class for java.lang.System library abstraction
 */
public class JPF_java_lang_System {
  public static void setErr0__Ljava_io_PrintStream_2__V (MJIEnv env, int clsObjRef, int streamRef) {
    env.setStaticReferenceField("java.lang.System", "err", streamRef);
  }

  public static void setIn0__Ljava_io_InputStream_2__V (MJIEnv env, int clsObjRef, int streamRef) {
    env.setStaticReferenceField("java.lang.System", "in", streamRef);
  }

  public static void setOut0__Ljava_io_PrintStream_2__V (MJIEnv env, int clsObjRef, int streamRef) {
    env.setStaticReferenceField("java.lang.System", "out", streamRef);
  }

  public static void $clinit____V (MJIEnv env, int clsObjRef) {
    ThreadInfo ti = env.getThreadInfo();
    Instruction insn = ti.getPC();
    
    ClassInfo oci = ClassInfo.getClassInfo("java.io.PrintStream");
    if (insn.requiresClinitCalls(ti, oci)) {
      env.repeatInvocation();
      return;
    }
    
    ClassInfo ici = ClassInfo.getClassInfo("java.io.InputStream");
    if (insn.requiresClinitCalls(ti, ici)) {
      env.repeatInvocation();
      return;
    }

    env.setStaticReferenceField("java.lang.System", "out", env.newObject(oci));
    env.setStaticReferenceField("java.lang.System", "err", env.newObject(oci));
    
    env.setStaticReferenceField("java.lang.System", "in", env.newObject(ici));
  }

  // <2do> - now this baby really needs to be fixed. Imagine what it
  // does to an app that works with large vectors
  public static void arraycopy__Ljava_lang_Object_2ILjava_lang_Object_2II__V (MJIEnv env, int clsObjRef,
                                                                              int srcArrayRef, int srcIdx, 
                                                                              int dstArrayRef, int dstIdx,
                                                                              int length) {
    int i;
    
    if ((srcArrayRef == -1) || (dstArrayRef == -1)) {
      env.throwException("java.lang.NullPointerException");

      return;
    }

    if (!env.isArray(srcArrayRef) || !env.isArray(dstArrayRef)) {
      env.throwException("java.lang.ArrayStoreException");

      return;
    }

    int sts = env.getArrayTypeSize(srcArrayRef);
    int dts = env.getArrayTypeSize(dstArrayRef);

    if (sts != dts) {
      // not enough, real thing checks types !!
      env.throwException("java.lang.ArrayStoreException");

      return;
    }

    // ARGHH <2do> pcm - at some point, we should REALLY do this with a block
    // operation (saves lots of state, speed if arraycopy is really used)
    // we could also use the native checks in that case
    int sl = env.getArrayLength(srcArrayRef);
    int dl = env.getArrayLength(dstArrayRef);

    // <2do> - we need detail messages!
    if ((srcIdx < 0) || ((srcIdx + length) > sl)) {
      env.throwException("java.lang.ArrayIndexOutOfBoundsException");

      return;
    }

    if ((dstIdx < 0) || ((dstIdx + length) > dl)) {
      env.throwException("java.lang.ArrayIndexOutOfBoundsException");

      return;
    }

    if (sts == 1) {
      for (i = 0; i < length; i++, srcIdx++, dstIdx++) {
        int v = env.getIntArrayElement(srcArrayRef, srcIdx);
        env.setIntArrayElement(dstArrayRef, dstIdx, v);
      }
    } else {
      for (i = 0; i < length; i++, srcIdx++, dstIdx++) {
        long v = env.getLongArrayElement(srcArrayRef, srcIdx);
        env.setLongArrayElement(dstArrayRef, dstIdx, v);
      }
    }
  }

  // <2do> - this break every app which uses time delta thresholds
  // (sort of "if ((t2 - t1) > d)"). Ok, we can't deal with
  // real time, but we could at least give some SystemState dependent
  // increment
  public static long currentTimeMillis____J (MJIEnv env, int clsObjRef) {
    return 1L;
  }

  // <2do> - likewise. Java 1.5's way to measure relative time
  public static long nanoTime____J (MJIEnv env, int clsObjRef) {
    return 1L;
  }  
  
  // <2do> - now this implementation is dangerous if it's called from
  // a context where we have subsequent unchecked stack frame access
  // (the bytecodes). We better choose a 'exit' flag instead of
  // this stack suicide
  public static void exit__I__V (MJIEnv env, int clsObjRef, int ret) {
    KernelState ks = env.getKernelState();
    int         length = ks.tl.length();

    for (int i = 0; i < length; i++) {
      ThreadInfo ti = ks.tl.get(i);

      while (ti.countStackFrames() > 0) {
        ti.popFrame();
      }
    }
  }

  public static void gc____V (MJIEnv env, int clsObjRef) {
    env.getSystemState().activateGC();
  }

  public static int identityHashCode__Ljava_lang_Object_2__I (MJIEnv env, int clsObjRef, int objref) {
    return (objref ^ 0xABCD);
  }

  public static void registerNatives____V (MJIEnv env, int clsObjRef) {
    // ignore
  }
  
  /**
   * <2do> pcm - replace this with a fixed set of system properties
   */
  public static int getProperty__Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int clsObjRef,
                                                                           int keyRef) {
    int r = MJIEnv.NULL;
    
    if (keyRef != MJIEnv.NULL) {
      String k = env.getStringObject(keyRef);
      String v = System.getProperty(k);
      if (v != null) {
        r = env.newString(v);
      }
    }
    
    return r;
  }
}
