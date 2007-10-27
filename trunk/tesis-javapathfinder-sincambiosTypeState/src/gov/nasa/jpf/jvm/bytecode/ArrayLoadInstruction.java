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

import gov.nasa.jpf.jvm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;


/**
 *abstraction for all array load instructions
 */
public abstract class ArrayLoadInstruction extends ArrayInstruction {
  
  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo th) {
    int         index;
    int         arrayRef;
    ElementInfo e;

    index = th.pop();
    arrayRef = th.pop();

    if (arrayRef == -1) {
      return th.createAndThrowException("java.lang.NullPointerException");
    }

    e = ks.da.get(arrayRef);

    try {
      push(th, e, index);

      return getNext(th);
    } catch (ArrayIndexOutOfBoundsExecutiveException ex) {
      return ex.getInstruction();
    }
  }

  protected boolean isReference () {
    return false;
  }

  protected void push (ThreadInfo th, ElementInfo e, int index)
                throws ArrayIndexOutOfBoundsExecutiveException {
    e.checkArrayBounds(index);
    th.push(e.getElement(index), isReference());
  }

  public boolean isSchedulingRelevant (SystemState ss, KernelState ks, ThreadInfo ti) {
    // should be further discriminated based on if the array object
    // is reachable for multiple threads
    if (ti.usePorFieldBoundaries()) {
      if (ks.da.isSchedulingRelevantObject(ti.peek(1)) && ti.hasOtherRunnables()) {
        return true;
      }
    }

    return false;
  }
}
