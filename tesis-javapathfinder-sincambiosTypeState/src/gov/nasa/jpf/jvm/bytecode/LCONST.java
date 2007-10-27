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

import org.apache.bcel.classfile.ConstantPool;


/**
 * Push long constant
 * ... => ..., <l>
 */
public class LCONST extends Instruction {
  private long value;

  public void setPeer (org.apache.bcel.generic.Instruction i, ConstantPool cp) {
    value = ((org.apache.bcel.generic.LCONST) i).getValue().longValue();
  }

  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo th) {
    th.longPush(value);

    return getNext(th);
  }

  public int getByteCode () {
    if (value == 0) {
      return 0x09;
    } else {
      return 0x0a;
    }
  }
  
  public String getMnemonic () {
    if (value == 0) {
      return "lconst_0";
    } else {
      return "lconst_1";
    }    
  }
  
}
