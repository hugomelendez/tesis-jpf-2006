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
 * we don't want this class! This is a hodgepodge of stuff that shouldn't be in Java, but
 * is handy for some hacks. The reason we we - very rudimentary - have it here is that
 * java.util.concurrent makes use of the atomic compare&swap which is in it.
 * The choice here was to duplicate a lot of relatively difficult code in the "right" class
 * (java.util.concurrent.locks.AbstractQueuedSynchronizer) or a small amount of straight forward
 * code in the "wrong" class (sun.misc.Unsafe). Knowing a bit about the "library chase" game,
 * we opt for the latter
 */
public class JPF_sun_misc_Unsafe {

  public  int getUnsafe____Lsun_misc_Unsafe_2 (MJIEnv env, int clsRef) {
    int objRef = env.getStaticObjectField("sun.misc.Unsafe", "singleton");
    return objRef;
  }
  
  public static long objectFieldOffset__Ljava_lang_reflect_Field_2__J (MJIEnv env, int unsafeRef, int fieldRef) {
    return fieldOffset__Ljava_lang_reflect_Field_2__I(env, unsafeRef, fieldRef);
  }

  /**
   * we don't really return an offset here, since that would be useless. What we really want is
   * to identify the corresponding FieldInfo, and that's much easier done with the Field
   * registration id
   */
  public static int fieldOffset__Ljava_lang_reflect_Field_2__I (MJIEnv env, int unsafeRef, int fieldRef) {
    //FieldInfo fi = JPF_java_lang_reflect_Field.getFieldInfo(env, fieldRef);
    //return fi.getStorageOffset();
    return env.getIntField(fieldRef, "regIdx");
  }

  public static boolean compareAndSwapObject__Ljava_lang_Object_2JLjava_lang_Object_2Ljava_lang_Object_2__Z (MJIEnv env, int unsafeRef,
                                                                                                             int oRef, long fieldOffset,
                                                                                                             int expectRef, int updateRef) {
    FieldInfo fi = JPF_java_lang_reflect_Field.getRegisteredFieldInfo((int)fieldOffset);
    ElementInfo ei = env.getElementInfo(oRef);

    if (ei.getReferenceField(fi) == expectRef) {
      ei.setReferenceField(fi, updateRef);
      return true;
    }
    return false;
  }

  public static boolean compareAndSwapInt__Ljava_lang_Object_2JII__Z (MJIEnv env, int unsafeRef, 
                                                                      int oRef, long fieldOffset, int expect, int update) {
    FieldInfo fi = JPF_java_lang_reflect_Field.getRegisteredFieldInfo((int)fieldOffset);
    ElementInfo ei = env.getElementInfo(oRef);
    
    if (ei.getIntField(fi) == expect) {
      ei.setIntField(fi, update);
      return true;
    }
    return false;
  }
  
  public static boolean compareAndSwapLong__Ljava_lang_Object_2JJJ__Z (MJIEnv env, int unsafeRef,
                                                                       int oRef, long fieldOffset, long expect, long update) {
    FieldInfo fi = JPF_java_lang_reflect_Field.getRegisteredFieldInfo((int)fieldOffset);
    ElementInfo ei = env.getElementInfo(oRef);
    
    if (ei.getLongField(fi) == expect) {
      ei.setLongField(fi, update);
      return true;
    }
    return false;
  }

  
  // these just encapsulate waits on a very private Thread field object ("permit"). wait() requires
  // owning the lock, but the trick is to not expose any lock that would cause JPF to break
  // the transition and add additional states (e.g. by synchronizing on the Unsafe object itself),
  // hence we have to do the lock/unlock explicitly from within park/unpark. In case of the
  // park(), this is slightly more complicated since it might get re-executed
  
  public static void park__ZJ__V (MJIEnv env, int unsafeRef, boolean dontknow, long timeout) {
    ThreadInfo ti = env.getThreadInfo();
    int objRef = ti.getObjectReference();
    
    int permitRef = env.getReferenceField( objRef, "permit");
    ElementInfo ei = env.getElementInfo(permitRef);

    if (ei.getBooleanField("isTaken")) { // we have to block
      ei.lock(ti); // otherwise a subsequent wait will blow up
      JPF_java_lang_Object.wait__J__V(env,permitRef,timeout);
    } else {
      if (ti.isFirstStepInsn()) {  // somebody might have notified us
        JPF_java_lang_Object.wait__J__V(env,permitRef,timeout);
        ei.unlock(ti); // simulate the accompanying MONITOR_EXIT
      }
      ei.setBooleanField("isTaken", true);
    }    
  }
  
  public static void unpark__Ljava_lang_Object_2__V (MJIEnv env, int unsafeRef, int objRef) {
    ThreadInfo ti = env.getThreadInfo();

    /*ThreadInfo t1 =*/ JPF_java_lang_Thread.getThreadInfo(env, objRef); // needed? -pcd
    int permitRef = env.getReferenceField( objRef, "permit");
    ElementInfo ei = env.getElementInfo(permitRef);
    
    ei.lock(ti);
    ei.setBooleanField("isTaken", false);
    ei.notifies(env.getSystemState(), ti);
    ei.unlock(ti);    
  }
}
