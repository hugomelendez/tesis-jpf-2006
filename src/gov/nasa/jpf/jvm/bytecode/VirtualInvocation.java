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
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;


/**
 * a base class for virtual call instructions
 */
public abstract class VirtualInvocation extends InvokeInstruction {
  
  protected VirtualInvocation () {}
  
  protected VirtualInvocation (MethodInfo mi, String cname, String mname, String signature,
                               int offset, int position) {
    super(mi, cname, mname, signature, offset, position);
  }
    
  public boolean isExecutable (SystemState ss, KernelState ks, ThreadInfo ti) {
    int objRef = getCalleeThis(ti);
    MethodInfo mi = getInvokedMethod( ks, objRef);
    
    if ((objRef == -1) || (mi == null)) {
      return true; // make sure we execute so that we get the exception
    }
    
    return mi.isExecutable(ti);
  }

  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo ti) {
    int objRef = getCalleeThis(ti);
    
    if (objRef == -1) { // NPE
      return ti.createAndThrowException("java.lang.NullPointerException",
                                        "calling '" + mname + "' on null object");
    }

    MethodInfo mi = getInvokedMethod(ks, objRef);
    if (mi == null) {
      return ti.createAndThrowException("java.lang.NoSuchMethodException",
                                        getCalleeClassInfo(ks, objRef).getName() + "." + mname);
    }
    
    if (mi.isSynchronized()) {
      ElementInfo ei = ks.da.get(objRef);

      if (ei.getLockingThread() == ti) {
        assert ei.getLockCount() > 0;
        // a little optimization - recursive locks are always left movers
        return  mi.execute(ti);
      }
      
      // first time around - reexecute if the scheduling policy gives us a choice point
      if (!ti.isFirstStepInsn()) {

        if (!ei.canLock(ti)) {
          // block first, so that we don't get this thread in the list of CGs
          ei.block(ti);
        }
        
        ChoiceGenerator cg = ss.getSchedulerFactory().createSyncMethodEnterCG(ei, ti);
        if (cg != null) { // Ok, break here
          if (!ti.isBlocked()) {
            // record that this thread would lock the object upon next execution
            ei.registerLockContender(ti);
          }
          ss.setNextChoiceGenerator(cg);
          return this;   // repeat exec, keep insn on stack    
        }
        
        assert !ti.isBlocked() : "scheduling policy did not return ChoiceGenerator for blocking INVOKE";
      }
    }
    
    // this will lock the object if necessary
    return mi.execute(ti);
  }

  int getCalleeThis (ThreadInfo ti) {
    return ti.getCalleeThis( getArgSize());
  }
  
  ClassInfo getCalleeClassInfo (KernelState ks, int objRef) {
    return ks.da.get(objRef).getClassInfo();
  }
  
  /**
   * cache the actual callee class MethodInfo
   */
  MethodInfo getInvokedMethod (KernelState ks, int objRef) {
    if (objRef != -1) {
      
      // <?> - can this be ambiguous? probably yes if we had a backtrace
      // with different heap state where the heap symmetry didn't work
      if (lastObj != objRef) { // it's good we are not multithreaded
        
        ClassInfo mci = getCalleeClassInfo(ks, objRef);
        invokedMethod = mci.getMethod(mname, true);
        
        // here we could catch the NoSuchMethodError
        if (invokedMethod == null) {
          lastObj = -1;
        } else {
          lastObj = objRef;
        }
      }
    } else {
      lastObj = -1;
    }
    
    return invokedMethod;
  }
  
}
