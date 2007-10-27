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

import gov.nasa.jpf.JPFException;


/**
 * MJI NativePeer class for java.lang.Object library abstraction
 */
public class JPF_java_lang_Object {
  
  public static int getClass____Ljava_lang_Class_2 (MJIEnv env, int objref) {
    ClassInfo oci = env.getClassInfo(objref);

    return oci.getClassObjectRef();
  }


  public static int clone____Ljava_lang_Object_2 (MJIEnv env, int objref) {
    // <2do>
    return -1;
  }

  public static int hashCode____I (MJIEnv env, int objref) {
    return (objref ^ 0xABCD);
  }

  public static void registerNatives____V (MJIEnv env, int clsObjRef) {
    // let go un-noticed
  }

  public static void wait__J__V (MJIEnv env, int objref, long timeout) {
    // IllegalMonitorStateExceptions are checked in the MJIEnv methods
    ThreadInfo ti = env.getThreadInfo();    
    SystemState ss = env.getSystemState();
    ElementInfo ei = env.getElementInfo(objref);
    
    if (ti.isFirstStepInsn()) { // we already have a CG
      switch (ti.getStatus()) {
      // note that we can't get here if we are in NOTIFIED state, since we still have to
      // reacquire the lock
      case ThreadInfo.UNBLOCKED:
      case ThreadInfo.TIMEDOUT: // nobody else acquired the lock
        // thread status set by explicit notify() call
        env.lockNotified(objref);
        break;      
      case ThreadInfo.UNBLOCKED_INTERRUPTED:
        env.lockNotified(objref);
        env.throwException("java.lang.InterruptedException");
        break;

      default:
        throw new JPFException("invalid thread state: " + ti.getStatus() + 
                               " waiting on " + ei);
      }      
    } else { // first time, break the transition
      // note we pass in the timeout value, since this might determine the type of CG that is created
      env.wait(objref, timeout); // releases all locks and sets BLOCKED threads to UNBLOCKED

      ChoiceGenerator cg = ss.getSchedulerFactory().createWaitCG( ei, ti, timeout);
      assert (cg != null) : "wait of " + ti.getName() + " on: " + ei + " created no choice generator";
      ss.setNextChoiceGenerator(cg);

      env.repeatInvocation(); // so that we can still see the wait on the callstack
    }
  }

  public static final void notify____V (MJIEnv env, int objref) {
    // IllegalMonitorStateExceptions are checked in the MJIEnv methods

    ThreadInfo ti = env.getThreadInfo();
    SystemState ss = env.getSystemState();
    
    if (!ti.isFirstStepInsn()) { // first time around
      ElementInfo ei = env.getElementInfo(objref);
      
      ChoiceGenerator cg = ss.getSchedulerFactory().createNotifyCG(ei, ti);
      if (cg != null) {
        ss.setNextChoiceGenerator(cg);
        ti.skipInstructionLogging();
        env.repeatInvocation();
        return;
      }
    }
        
    // this is a bit cluttered throughout the whole system, with the actual thread
    // notification (status change) taking place in the ElementInfo
    env.notify(objref);
  }

  public static void notifyAll____V (MJIEnv env, int objref) {
    // IllegalMonitorStateExceptions are checked in the MJIEnv methods

    // usually, there is no non-determinism involved here, but
    // we might have a SchedulerFactory policy that does want to
    // break, so we have to give it a chance to interfere
    ThreadInfo ti = env.getThreadInfo();
    SystemState ss = env.getSystemState();
    
    if (!ti.isFirstStepInsn()) { // first time around
      env.notifyAll(objref); // do that before we create a CG
      
      ElementInfo ei = env.getElementInfo(objref);
      
      ChoiceGenerator cg = ss.getSchedulerFactory().createNotifyAllCG(ei, ti);
      if (cg != null) {
        ss.setNextChoiceGenerator(cg);
        ti.skipInstructionLogging();
        env.repeatInvocation();
        return;
      }
    }
  }

}
