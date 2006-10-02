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
package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.jvm.ChoiceGenerator;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;


/**
 * abstraction for the various return instructions
 */
public abstract class ReturnInstruction extends Instruction {

  // note these are only callable from within the same execute - thread interleavings
  // would cause races
  abstract void storeReturnValue (ThreadInfo th);
  abstract void pushReturnValue (ThreadInfo th);
  
  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo ti) {
    
    if (!ti.isFirstStepInsn()) {
      mi.leave(ti);  // takes care of unlocking before potentially creating a CG
      
      if (mi.isSynchronized()) {
        int objref = ti.getThis();
        ElementInfo ei = ks.da.get(objref);      
      
        ChoiceGenerator cg = ss.getSchedulerFactory().createSyncMethodExitCG(ei, ti);
        if (cg != null) {
          ss.setNextChoiceGenerator(cg);
          ti.skipInstructionLogging();
          return this; // re-execute
        }
      }
    }
    
    storeReturnValue(ti);
    //mi.leave(ti);  // takes care of potential unlocking

    if (!ti.popFrame()) {
      // no more frames, done with this thread (hence no need for a return value push)
      // the thread is already marked as terminated here - happens in popFrame()
      
      ChoiceGenerator cg = ss.getSchedulerFactory().createThreadTerminateCG(ti);
      if (cg != null) {
        ss.setNextChoiceGenerator(cg);
      }
              
      return null;
      
    } else { // there are still frames on the stack
      Instruction nextPC = ti.getReturnFollowOnPC();
      
      if (nextPC != ti.getPC()) {
        // don't remove or push args if we repeat this insn!
        ti.removeArguments(mi);
        pushReturnValue(ti);
      }

      return nextPC;
    }
  }
}
