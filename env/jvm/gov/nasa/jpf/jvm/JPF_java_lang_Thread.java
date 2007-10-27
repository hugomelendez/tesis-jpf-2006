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

import gov.nasa.jpf.jvm.bytecode.RUNSTART;


/**
 * MJI NativePeer class for java.lang.Thread library abstraction
 */
public class JPF_java_lang_Thread {
  
  public static boolean isAlive____Z (MJIEnv env, int objref) {
    return getThreadInfo(env, objref).isAlive();
  }

  public static void setDaemon0__Z__V (MJIEnv env, int objref, boolean isDaemon) {
    ThreadInfo ti = getThreadInfo(env, objref);
    ti.setDaemon(isDaemon);
  }

  public static boolean isInterrupted____Z (MJIEnv env, int objref) {
    return getThreadInfo(env, objref).isInterrupted();
  }

  public static void setName0__Ljava_lang_String_2__V (MJIEnv env, int objref, int nameRef) {
    // it bails if you try to set a null name
    if (nameRef == -1) {
      env.throwException("java.lang.IllegalArgumentException");

      return;
    }

    // we have to intercept this to cache the name as a Java object
    // (to be stored in ThreadData)
    // luckily enough, it's copied into the java.lang.Thread object
    // as a char[], i.e. does not have to preserve identity
    // Note the nastiness in here - the java.lang.Thread object is only used
    // to get the initial values into ThreadData, and gets inconsistent
    // if this method is called (just works because the 'name' field is only
    // directly accessed from within the Thread ctors)
    ThreadInfo ti = getThreadInfo(env, objref);
    ti.setName(env.getStringObject(nameRef));
  }

  public static void setPriority0__I__V (MJIEnv env, int objref, int prio) {
    // again, we have to cache this in ThreadData for performance reasons
    ThreadInfo ti = getThreadInfo(env, objref);
    ti.setPriority(prio);
  }

  public static int countStackFrames____I (MJIEnv env, int objref) {
    return getThreadInfo(env, objref).countStackFrames();
  }

  public static int currentThread____Ljava_lang_Thread_2 (MJIEnv env, int clsObjRef) {
    ThreadInfo ti = env.getThreadInfo();

    return ti.getObjectReference();
  }

  public static boolean holdsLock__Ljava_lang_Object_2__Z (MJIEnv env, int clsObjRef, int objref) {
    ThreadInfo  ti = env.getThreadInfo();
    ElementInfo ei = env.getElementInfo(objref);

    return ei.isLockedBy(ti);
  }

  /**
   * This method is the common initializer for all Thread ctors, and the only
   * single location where we can init our ThreadInfo, but it is PRIVATE
   */

  // wow, that's almost like C++
  public static void init0__Ljava_lang_ThreadGroup_2Ljava_lang_Runnable_2Ljava_lang_String_2J__V (MJIEnv env,
                                                                                                  int objref, 
                                                                                                  int rGroup,
                                                                                                  int rRunnable,
                                                                                                  int rName,
                                                                                                  long stackSize) {
    ThreadInfo newThread = createThreadInfo(env, objref);
    newThread.init(rGroup, rRunnable, rName, stackSize, true);
  }

  public static void interrupt____V (MJIEnv env, int objref) {
    ThreadInfo interruptedThread = getThreadInfo( env, objref);
    interruptedThread.interrupt();
  }
  
  public static void sleep__JI__V (MJIEnv env, int clsObjRef, long millis, int nanos) {
    ThreadInfo ti = env.getThreadInfo();
    SystemState ss = env.getSystemState();
    ChoiceGenerator cg = ss.getChoiceGenerator();
    
    if (!ti.isFirstStepInsn()) { // first time we see this (may be the only time)
      // nothing to do, we are good to go
    } else {
      cg = ss.getSchedulerFactory().createThreadSleepCG( ti, millis, nanos);
      if (cg != null) {
        ss.setNextChoiceGenerator(cg);
        env.repeatInvocation();
      }      
    }
  }
  
  public static void start____V (MJIEnv env, int objref) {
    ThreadInfo ti = env.getThreadInfo();
    SystemState ss = env.getSystemState();
    ChoiceGenerator cg = ss.getChoiceGenerator();
    JVM vm = ti.getVM();
    
    if (!ti.isFirstStepInsn()) { // first time we see this (may be the only time)
      
      ThreadInfo newThread = getThreadInfo(env, objref);
      // check if this thread was already started. If it's still running, this
      // is a IllegalThreadStateException. If it already terminated, it just gets
      // silently ignored in Java 1.4, but the 1.5 spec explicitly marks this
      // as illegal, so we adopt this by throwing an IllegalThreadState, too
      if (newThread.getStatus() != ThreadInfo.NEW) {
        env.throwException("java.lang.IllegalThreadStateException");
        return;
      }
      
      // Outch - that's bad. we have to dig this out from the innards
      // of the java.lang.Thread class
      int target = newThread.getTarget();
      
      if (target == -1) {
        // note that we don't set the 'target' field, since java.lang.Thread doesn't
        target = objref;
      }
      
      // better late than never
      newThread.setTarget(target);
      
      // we don't do this during thread creation because the thread isn't in
      // the GC root set before it actually starts to execute. Until then,
      // it's just an ordinary object
      
      vm.notifyThreadStarted(newThread);
      
      ElementInfo ei = env.getElementInfo(target);
      ClassInfo   ci = ei.getClassInfo();
      MethodInfo  run = ci.getMethod("run()V", true);
      
      StackFrame runFrame = new StackFrame(run,target);
      // the first insn should be our own, to prevent confusion with potential
      // CGs (like Verify.getXX())
      runFrame.setPC(new RUNSTART(run));
      newThread.pushFrame(runFrame);
      
      
      if (run.isSynchronized && !ei.canLock(newThread)) {
        ei.block(newThread); // this sets the status to BLOCKED
      } else {
        newThread.setStatus( ThreadInfo.RUNNING);
      }
      
      // <2do> now that we have another runnable, we should re-compute
      // reachability so that subsequent potential breaks work correctly
      if (newThread.usePor()){ // means we use on-the-fly POR
        //env.getSystemState().activateGC();
        env.getDynamicArea().analyzeHeap(false); // sledgehammer mark
      }
      
      // now we have a new thread, create a CG for scheduling it
      cg = ss.getSchedulerFactory().createThreadStartCG( newThread);
      if (cg != null) {
        ss.setNextChoiceGenerator(cg);
        env.repeatInvocation();
      }
    } else {
      // don't show the start() twice, this is just here as a resume point
      ti.skipInstructionLogging();
    }
  }

  public static void yield____V (MJIEnv env, int clsObjRef) {
    ThreadInfo ti = env.getThreadInfo();
    SystemState ss = env.getSystemState();
    ChoiceGenerator cg = ss.getChoiceGenerator();
    
    if (!ti.isFirstStepInsn()) { // first time we see this (may be the only time)
      cg = ss.getSchedulerFactory().createThreadYieldCG( ti);
      if (cg != null) {
        ss.setNextChoiceGenerator(cg);
        env.repeatInvocation();
      }      
    } else {
      // nothing to do, this was just a forced reschedule
    }
  }

  public static long getId____J (MJIEnv env, int objref) {
    // doc says it only has to be valid and unique during lifetime of thread, hence we just use
    // the ThreadList index
    ThreadInfo ti = getThreadInfo(env, objref);
    return ti.getIndex();
  }

  public int getState0____I (MJIEnv env, int objref) {
    // return the state index with respect to one of the public Thread.States
    ThreadInfo ti = getThreadInfo(env, objref);
    int s = ti.getStatus();

    if (s == ThreadInfo.NEW) return 1;
    if (s == ThreadInfo.RUNNING) return 2;
    if (s == ThreadInfo.BLOCKED) return 0;
    if (s == ThreadInfo.UNBLOCKED) return 2;
    if (s == ThreadInfo.WAITING) return 4;
    if (s == ThreadInfo.TIMEOUT_WAITING) return 5;
    if (s == ThreadInfo.NOTIFIED) return 2;
    if (s == ThreadInfo.STOPPED) return 2; // ??
    if (s == ThreadInfo.INTERRUPTED) return 2;
    if (s == ThreadInfo.UNBLOCKED_INTERRUPTED) return 2;
    if (s == ThreadInfo.TIMEDOUT) return 2;
    if (s == ThreadInfo.TERMINATED) return 3;

    assert true : "illegal thread state: " + s;
    return -1;
  }
  
  // it's synchronized
  /*
  public static void join__ (MJIEnv env, int objref) {
    ThreadInfo ti = getThreadInfo(env,objref);
    
    if (ti.isAlive()) {
      env.wait(objref);
    }
  }
   */

  protected static ThreadInfo createThreadInfo (MJIEnv env, int objref) {
    return ThreadInfo.createThreadInfo(env.getVM(), objref);
  }
  
  protected static ThreadInfo getThreadInfo (MJIEnv env, int objref) {
    return ThreadInfo.getThreadInfo(env.getVM(), objref);
  }
  
}
