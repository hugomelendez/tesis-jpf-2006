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
 * abstraction for all array store instructions
 */
public abstract class ArrayStoreInstruction extends ArrayInstruction
  implements StoreInstruction
{
  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo th) {
    long value;
    int  index;
    int  arrayRef;

    value = getValue(th);
    index = th.pop();
    arrayRef = th.pop();

    ElementInfo e = ks.da.get(arrayRef);

    if (arrayRef == -1) {
      return th.createAndThrowException("java.lang.NullPointerException");
    }

    try {
      setField(e, index, value);

      return getNext(th);
    } catch (ArrayIndexOutOfBoundsExecutiveException ex) {
      return ex.getInstruction();
    }
  }

  protected void setField (ElementInfo e, int index, long value)
                    throws ArrayIndexOutOfBoundsExecutiveException {
    e.checkArrayBounds(index);
    e.setElement(index, (int) value);
  }

  protected long getValue (ThreadInfo th) {
    return /*(long)*/ th.pop();
  }
    
  public boolean isSchedulingRelevant (SystemState ss, KernelState ks, ThreadInfo ti) {
    // should be further discriminated based on if the array object
    // is reachable for multiple threads
    if (ti.usePorFieldBoundaries()) {
      int off = getElementSize() + 1;
      if (ks.da.isSchedulingRelevantObject(ti.peek(off)) && ti.hasOtherRunnables()) {
        return true;
      }
    }
    
    return false;
  }
}
