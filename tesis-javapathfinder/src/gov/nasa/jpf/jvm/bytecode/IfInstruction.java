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

import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;

/**
 * abstraction for all comparison instructions
 */
public abstract class IfInstruction extends Instruction {
  int target; /** jump offset */
  
  boolean conditionValue;  /** value of last evaluation of branch condition */
  
  /**
   * return which branch was taken. Only useful after instruction got executed
   */
  public boolean getConditionValue() {
    return conditionValue;
  }

  public void setPeer (org.apache.bcel.generic.Instruction insn,
                       org.apache.bcel.classfile.ConstantPool cp) {
    target = ((org.apache.bcel.generic.BranchInstruction) insn).getTarget().getPosition();
  }
  
  /** 
   * retrieve value of jump condition from operand stack
   */
  abstract boolean popConditionValue(ThreadInfo ti);
  
  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo ti) {
    conditionValue = popConditionValue(ti);
    if (conditionValue) {
      return ti.getMethod().getInstructionAt(target);
    } else {
      return getNext(ti);
    }
  }

}
