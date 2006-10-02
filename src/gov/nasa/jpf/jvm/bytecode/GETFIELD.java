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
 * Fetch field from object
 * ..., objectref => ..., value 
 */
public class GETFIELD extends InstanceFieldInstruction {
    
  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo ti) {
    int objRef = ti.peek(); // don't pop yet, we might re-execute
    if (objRef == -1) {
      return ti.createAndThrowException("java.lang.NullPointerException",
                                        "referencing field '" + fname + "' on null object");
    }
    
    FieldInfo fi = getFieldInfo();
    ElementInfo ei = DynamicArea.getHeap().get(objRef);
    
    // check if this breaks the current transition
    if (isNewPorFieldBoundary(ti, fi, objRef)) {
      if (createAndSetFieldCG(ss, ei, ti)) {
        return this;
      }
    }
    
    ti.pop(); // Ok, now we can remove the object ref from the stack
    
    // We could encapsulate the push in ElementInfo, but not the GET, so
    // we keep it at a similiar level
    switch (fi.getStorageSize()) {
      case 1:
        ti.push( ei.getIntField(fi), fi.isReference());
        break;
      case 2:
        ti.longPush( ei.getLongField(fi));
        break;
      default:
        throw new JPFException("invalid field type");
    }
    lastThis = objRef;
    
    return getNext(ti);
  }
  
  public int getByteCode () {
    return 0xB4;
  }
   
}
