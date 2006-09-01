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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.DynamicArea;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;


/**
 * Set field in object
 * ..., objectref, value => ...
 * 
 * Hmm, this is at the upper level of complexity because of the unified CG handling
 */
public class PUTFIELD extends InstanceFieldInstruction implements StoreInstruction
{
  
  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo ti) {
    
    FieldInfo fi = getFieldInfo();
    int objRef = ti.peek( (fi.getStorageSize() == 1) ? 1 : 2);

    // if this produces an NPE, force the error w/o further ado
    if (objRef == -1) {
      return ti.createAndThrowException("java.lang.NullPointerException",
                                 "referencing field '" + fname + "' on null object");
    }
    ElementInfo ei = DynamicArea.getHeap().get(objRef);
    
    // check if this breaks the current transition
    if (isNewPorFieldBoundary(ti, fi, objRef)) {
      if (createAndSetFieldCG(ss, ei, ti)) {
        return this;
      }
    }

    // start the real execution by getting the value from the operand stack
    long lvalue = 0;
    int ivalue = 0;
    switch (fi.getStorageSize()) {
      case 1:
        ivalue = ti.pop();
        break;
      case 2:
        lvalue = ti.longPop();
        break;
      default:
        throw new JPFException("invalid field type");
    }
    
    ti.pop(); // we already have the objRef
    lastThis = objRef;

    if (fi.isReference()) {
      ei.setReferenceField(fi, ivalue);
    } else {
      if (size == 1) {
        ei.setIntField(fi, ivalue);
      } else {
        ei.setLongField(fi, lvalue);
      }
    }
    
    return getNext(ti);
  }

  public int getByteCode () {
    return 0xB5;
  }
  
}



