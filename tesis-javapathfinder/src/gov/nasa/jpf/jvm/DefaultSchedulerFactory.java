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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.choice.ThreadChoiceFromSet;

public class DefaultSchedulerFactory implements SchedulerFactory {

  /**
   * this is an example of a side effect CG - we not just enumerate, but
   * also change thread states in the process. We could do that generically
   * in SystemState.nextSuccessor(), but the problem is that we have to change it
   * back to TIMEOUT_WAITING for all threads that follow it in this CG set (if another
   * thread is scheduled, the waiter should not be runnable), and the best place to remember
   * this is the CG itself. This plus the fact this is policy, and if we at some point model
   * exec time, we do it with a different SchedulerFactory 
   */
  static class TimeOutWaitCG extends ThreadChoiceFromSet {
    ThreadInfo waiter;
    
    public TimeOutWaitCG(ThreadInfo[] runnables, ThreadInfo waiter) {
      super(runnables, true);
      this.waiter = waiter;
    }

    public void advance () {
      super.advance();
      
      if (getNextChoice() == waiter) {
        // if the next choice is the waiter, change it's state to TIMEOUT, to make sure it
        // reacquires the lock and is runnable again
        waiter.setStatus(ThreadInfo.TIMEDOUT);
      } else {
        // for all other choices, the waiter has to be exactly that - TIMEOUT_WAITING,
        // i.e not runnable
        waiter.setStatus(ThreadInfo.TIMEOUT_WAITING);
      }
    }
  }
  
  protected JVM vm;
  
  public DefaultSchedulerFactory (Config config, JVM vm) {
    this.vm = vm;
  }

  /*************************************** internal helpers *****************/
  
  /**
   * post process a list of choices. This is our primary interface towards
   * subclasses (together with overriding the relevant insn APIs
   */
  protected ThreadInfo[] filter ( ThreadInfo[] list) {
    // we have nothing to do, but subclasses can use it to
    // shuffle the order (e.g. to avoid the IdleFilter probblem),
    // or to filter out the top priorities
    return list;
  }

  
  
  protected ChoiceGenerator getSyncCG (ElementInfo ei, ThreadInfo ti) {
    ThreadInfo[] choices = getRunnablesIfChoices();
    if (choices != null) {
      return new ThreadChoiceFromSet( choices, true);
    } else {
      return null;
    }
  }

  /**************************************** our choice acquisition methods ***/
  
  /**
   * get list of all runnable threads
   */
  protected ThreadInfo[] getRunnables() {
    ThreadList tl = vm.getThreadList();
    return filter(tl.getRunnableThreads());
  }
    
  /**
   * return a list of runnable choices, or null if there is only one
   */
  protected ThreadInfo[] getRunnablesIfChoices() {
    ThreadList tl = vm.getThreadList();
    
    if (tl.getRunnableThreadCount() > 1) {
      return filter(tl.getRunnableThreads());
    } else {
      return null;
    }
  }

  protected ThreadInfo[] getRunnablesWith (ThreadInfo ti) {
    ThreadList tl = vm.getThreadList();
    return filter( tl.getRunnableThreadsWith(ti));
  }
  
  protected ThreadInfo[] getRunnablesWithout (ThreadInfo ti) {
    ThreadList tl = vm.getThreadList();
    return filter( tl.getRunnableThreadsWithout(ti));
  }
  

  /************************************ the public interface towards the insns ***/ 

  public ChoiceGenerator createSyncMethodEnterCG (ElementInfo ei, ThreadInfo ti) {
    return createMonitorEnterCG(ei, ti);
  }

  public ChoiceGenerator createSyncMethodExitCG (ElementInfo ei, ThreadInfo ti) {
    return null; // nothing, left mover
  }

  public ChoiceGenerator createMonitorEnterCG (ElementInfo ei, ThreadInfo ti) {
    if (ti.isBlocked()) { // we have to return something
      return new ThreadChoiceFromSet(getRunnables(), true);
    } else {
      return getSyncCG(ei, ti);
    }
  }

  public ChoiceGenerator createMonitorExitCG (ElementInfo ei, ThreadInfo ti) {
    return null; // nothing, left mover
  }
  

  public ChoiceGenerator createWaitCG (ElementInfo ei, ThreadInfo ti, long timeOut) {
    if (timeOut == 0) {
      return new ThreadChoiceFromSet(getRunnables(), true);
    } else {
      return new TimeOutWaitCG( getRunnablesWith(ti), ti);
    }
  }

  public ChoiceGenerator createNotifyCG (ElementInfo ei, ThreadInfo ti) {
    ThreadInfo[] waiters = ei.getWaitingThreads();
    if (waiters.length < 2) {
      // if there are less than 2 threads waiting, there is no nondeterminism
      return null;
    } else {
      return new ThreadChoiceFromSet(waiters, false);
    }
  }

  public ChoiceGenerator createNotifyAllCG (ElementInfo ei, ThreadInfo ti) {
    return null; // no nondeterminism here, left mover
  }

  public ChoiceGenerator createSharedFieldAccessCG (ElementInfo ei, ThreadInfo ti) {
    return getSyncCG(ei, ti);
  }

  public ChoiceGenerator createThreadStartCG (ThreadInfo newThread) {
    // left mover, we go on until we hit something that might
    // be affected by the new thread (which is either
    // RUNNING or SYNC_RUNNING)
    return null;
  }

  public ChoiceGenerator createThreadYieldCG (ThreadInfo yieldThread) {
    //return null;

    ThreadInfo[] runnables = getRunnablesIfChoices();
    if (runnables != null) {
      // we treat this just as an ordinary rescheduling point
      return new ThreadChoiceFromSet(runnables, true);
    } else {
      return null; // no alternatives, go on
    }
  }

  public ChoiceGenerator createThreadSleepCG (ThreadInfo sleepThread, long millis, int nanos) {
    // we treat this as a simple reschedule
    return createThreadYieldCG(sleepThread);
  }

  public ChoiceGenerator createThreadTerminateCG (ThreadInfo terminateThread) {
    // terminateThread is already TERMINATED at this point
    ThreadList tl = vm.getThreadList();
    if (tl.getRunnableThreadCount() > 0) {
      return new ThreadChoiceFromSet(getRunnablesWithout(terminateThread), true);
    } else {
      return null;
    }
  }
}
