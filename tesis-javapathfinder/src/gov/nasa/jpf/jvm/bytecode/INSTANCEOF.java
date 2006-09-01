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
 * Determine if object is of given type
 * ..., objectref => ..., result
 */
public class INSTANCEOF extends Instruction {
  private String type;

  public void setPeer (org.apache.bcel.generic.Instruction i, ConstantPool cp) {
    type = cp.constantToString(cp.getConstant(
                                     ((org.apache.bcel.generic.INSTANCEOF) i).getIndex()))
             .replace('.', '/');

    if (!type.startsWith("[")) {
      type = "L" + type + ";";
    }
  }

  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo th) {
    int objref = th.pop();

    if (objref == -1) {
      th.push(0, false);
    } else if (ks.da.get(objref).instanceOf(type)) {
      th.push(1, false);
    } else {
      th.push(0, false);
    }

    return getNext(th);
  }

  public int getByteCode () {
    return 0xC1;
  }
}
