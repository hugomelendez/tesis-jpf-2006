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

import java.io.PrintWriter;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.util.HashData;


/**
 * the class that encapsulates not only the current execution state of the VM
 * (the KernelState), but also the part of it's history that is required
 * by JVM to backtrack, plus some potential annotations that can be used to
 * control the search (i.e. forward/backtrack calls)
 */
public class SystemState {
    
  /**
   * instances of this class are used to store the SystemState parts which are
   * subject to backtracking/state resetting. At some point, we might have
   * stripped SystemState down enough to just store the SystemState itself
   * (so far, we don't change it's identity, there is only one)
   * the KernelState is still stored separately (which seems to be another
   * anachronism)
   *
   * <2do> - should be probably a inner class object - given that we have only
   * one SystemState instance (but bears the potential of memory leaks then)
   */
  static class Memento {
    ChoiceGenerator curCg;  // the ChoiceGenerator for the current transition 
    ChoiceGenerator nextCg;
    ChoicePoint trace;
    ThreadInfo execThread;
    int id;              // the state id
    
    Memento (SystemState ss) {
      nextCg = ss.nextCg;
      curCg = ss.curCg;
      id = ss.id;
      trace = ss.trace;
      execThread = ss.execThread;
    }
    
    /**
     * this one is used to restore to a state which will re-execute with the next choice
     * of the same CG, i.e. nextCG is reset
     */
    void backtrack (SystemState ss) {
      ss.nextCg = null; // this is important - the nextCG will be set by the next Transition
      ss.curCg = curCg;
      ss.id = id;
      ss.trace = trace;
      ss.execThread = execThread;
    }
    
    /**
     * this one is used if we restore and then advance, i.e. it might change the CG on
     * the next advance (if nextCg was set)
     */
    void restore (SystemState ss) {
      ss.nextCg = nextCg;
      ss.curCg = curCg;
      ss.id = id;
      ss.trace = trace;
      ss.execThread = execThread;
    }
  }
  
  int id;                   /** the state id */
  
  ChoiceGenerator nextCg;   // the ChoiceGenerator for the next transition
  ChoiceGenerator  curCg;   // the ChoiceGenerator used in the current transition
  ThreadInfo execThread;    // currently executing thread, reset by ThreadChoiceGenerators
  
  /** current execution state of the VM (stored separately by VM) */
  public KernelState ks;

  public Transition trail;      /** trace information */

  ///// those are going to be deprecated once we have the new state abstraction
  public boolean ignored;
  public boolean interesting;
  public boolean boring;

  /** uncaught exception in current transition */
  public UncaughtException uncaughtException;

  /** set to true if garbage collection is necessary */
  boolean GCNeeded = false;
  
  /** NOTE: this has changed its meaning. Atomic execution is no longer an optimization
   * feature that is exported to the app via an public API, it now is just an internal
   * mechanism to avoid context switches if we know exactly what we are doing (mostly
   * to avoid context switches on shared object access from inside of model classes).
   * The behavior is as follows:
   * (1) we don't backtrack into atomic sections, hence:
   * (2) SchedulingChoice generation is tolerated, but ignored. nextSuccessor() loops
   * until the atomicLevel drops to 0. If there is a blocking wait, this causes a
   * JPFException.
   * (3) any other ChoiceGenerator is not tolerated, and causes an JPFException
   */
  int atomicLevel;

  /** a previously stored trace we just replay */
  ChoicePoint traceStart;
  ChoicePoint trace;
  
  /** the policy object used to create scheduling related ChoiceGenerators */
  SchedulerFactory schedulerFactory;

  /** do we want CGs to randomize the order in which they return choices? */
  boolean randomizeChoices;
    
  /**
   * Creates a new system state.
   */
  public SystemState (Config config, JVM vm) throws Config.Exception {
    ks = new KernelState(config);
    id = StateSet.UNKNOWN_ID;

    Class[] argTypes = { Config.class, JVM.class };
    Object[] args = { config, vm };
    schedulerFactory = config.getEssentialInstance("vm.scheduler_factory.class", 
                                                    SchedulerFactory.class,
                                                    argTypes, args);
    // so that we have something we can store the path into
    trail = new Transition(0);
    
    traceStart = ChoicePoint.readTrace(config.getString("vm.use_trace"), vm.getMainClassName());
    
    randomizeChoices = config.getBoolean("cg.randomize_choices", false);
  }

  void setStartThread (ThreadInfo ti) {
    execThread = ti;
  }
  
  /**
   * return the stack of CGs of the current path
   */
  public ChoiceGenerator[] getChoiceGenerators () {
    ChoiceGenerator cg;
    int i, n;
    
    cg = curCg;
    for (n=0; cg != null; n++) {
      cg = cg.getPreviousChoiceGenerator();
    }

    ChoiceGenerator[] list = new ChoiceGenerator[n];
    
    cg = curCg;
    for (i=list.length-1; cg != null; i--) {
      list[i] = cg;
      cg = cg.getPreviousChoiceGenerator();
    }
    
    return list;
  }
  
  public int getId () {
    return id;
  }
  
  void setId (int newId) {
    id = newId;
  }
  
  /**
   * use those with extreme care, it overrides scheduling choices
   */
  public void setAtomic () {
    atomicLevel++;
  }
  public void clearAtomic () {
    if (atomicLevel > 0) {
      atomicLevel--;
    }
  }
  public boolean isAtomic () {
    return (atomicLevel > 0);
  }
  
  
  public Transition getTrail() {
    return trail;
  }
  
  public SchedulerFactory getSchedulerFactory () {
    return schedulerFactory;
  }
  
  /**
   * answer the ChoiceGenerator that was used in the current transition
   */
  public ChoiceGenerator getChoiceGenerator () {
    return curCg;
  }
  
  /**
   * set the ChoiceGenerator to be used in the next transition
   */
  public void setNextChoiceGenerator (ChoiceGenerator cg) {
    if (randomizeChoices) {
      nextCg = cg.randomize();
    } else {
      nextCg = cg;
    }
    
    nextCg.setPreviousChoiceGenerator( curCg);
  }
  
  public Object getBacktrackData () {
    return new Memento(this);
  }

  public void backtrackTo (Object backtrackData) {
    ((Memento) backtrackData).backtrack( this);
  }

  public void restoreTo (Object backtrackData) {
    ((Memento) backtrackData).restore( this);
  }
    
  ///// those are probably going away with the revamped state abstraction */
  public void setBoring (boolean b) {
    boring = b;
  }
  public boolean isBoring () {
    return boring;
  }
  public void setIgnored (boolean b) {
    ignored = b;
  }
  public boolean isIgnored () {
    return ignored;
  }
  public void setInteresting (boolean b) {
    interesting = b;
  }
  public boolean isInteresting () {
    return interesting;
  }
  ///// end 
  
  public boolean isInitState () {
    return (id == StateSet.UNKNOWN_ID);
  }
  

  public int getNonDaemonThreadCount () {
    return ks.tl.getNonDaemonThreadCount();
  }

  public ElementInfo getObject (int reference) {
    return ks.da.get(reference);
  }

  @Deprecated
  public ThreadInfo getThread (int index) {
    return ks.tl.get(index);
  }

  @Deprecated
  public ThreadInfo getThread (ElementInfo reference) {
    return getThread(reference.getIndex());
  }

  public int getThreadCount () {
    return ks.tl.length();
  }

  public int getRunnableThreadCount () {
    return ks.tl.getRunnableThreadCount();
  }
  
  public int getLiveThreadCount () {
    return ks.tl.getLiveThreadCount();
  }

  public ThreadInfo getThreadInfo (int idx) {
    return ks.tl.get(idx);
  }

  public Transition getTrailInfo () {
    return trail;
  }

  public UncaughtException getUncaughtException () {
    return uncaughtException;
  }

  public void activateGC () {
    GCNeeded = true;
  }

  public void gcIfNeeded () {
    if (GCNeeded) {
      ks.gc();
      GCNeeded = false;
    }
  }

  public void hash (HashData hd) {
    ks.hash(hd);
  }

  
  void dumpThreadCG (ThreadChoiceGenerator cg) {
    PrintWriter pw = new PrintWriter(System.out, true);
    cg.printOn(pw);
    pw.flush();
  }
  
  void advanceTrace () {
    if (trace != null) {
      trace = trace.next;
    } else {
      if (traceStart != null) {
        trace = traceStart;
      }
    }
    
    if (trace != null) {
      assert curCg.getClass().getName().equals(trace.cgClassName) :
        "wrong choice generator class, expecting: " + trace.cgClassName 
        + ", have: " + curCg.getClass().getName();
    }
  }
  
  void advanceCG () {
    if (trace == null) {
      curCg.advance();
    } else {
      curCg.advance(trace.choice+1); // replay the stored ChoicePoint
    }
  }
  
  /**
   * Compute next state.
   * return 'true' if we actually executed instructions, 'false' if this
   * state was already completely processed
   */
  public boolean nextSuccessor (JVM vm) throws JPFException {
    
    ignored = false;
    interesting = false;
    boring = false;
       
    do {
      // nextCg got set at the end of the previous transition
      if (nextCg != null) {
        curCg = nextCg;
        nextCg = null;
        
        advanceTrace();

        // Hmm, that's a bit late (should be in setNextCG), but we keep it here
        // for the sake of locality, and it's more consistent if it just refers
        // to curCg, i.e. the CG that is actually going to be used
        vm.notifyChoiceGeneratorSet(curCg);
      }

      assert (curCg != null) : "transition without choice generator";

      if (atomicLevel == 0) {
        if (!curCg.hasMoreChoices()) {
          vm.notifyChoiceGeneratorProcessed(curCg);
          return false;
        }
        
        advanceCG();
        vm.notifyChoiceGeneratorAdvanced(curCg);
        
        if (curCg instanceof ThreadChoiceGenerator) {
          ThreadChoiceGenerator tcg = (ThreadChoiceGenerator)curCg;
          if (tcg.isSchedulingPoint()) {
            execThread = tcg.getNextChoice();
            vm.notifyThreadScheduled(execThread);
          }
        }
        
      } else {
        if (curCg instanceof ThreadChoiceGenerator && 
            ((ThreadChoiceGenerator)curCg).isSchedulingPoint()) {
          if (!execThread.isRunnable()) {
            throw new JPFException("thread blocked in atomic section: " + execThread);
          }
        } else {
          // <2do> why is this bad?
          throw new JPFException("data ChoiceGenerator inside of atomic section: " + curCg);
        }
      }

      trail = new Transition(execThread.getIndex());
      //for debugging locks:  -peterd
      //ks.da.verifyLockInfo();
      execThread.executeStep(this);
      //ks.da.verifyLockInfo();

    } while (atomicLevel > 0);
    
    return true;
  }
  
  void recordExecutionStep (Step step) {
    trail.addStep(step);
  }
  
  
  public boolean isEndState () {
    return ks.isTerminated();
  }
    
  // the three primitive ops used from within JVM.forward()
  
  
}

