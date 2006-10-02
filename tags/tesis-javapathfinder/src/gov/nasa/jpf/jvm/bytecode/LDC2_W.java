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

import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.Types;

import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Type;


/**
 * Push long or double from runtime constant pool (wide index)
 * ... => ..., value
 */
public class LDC2_W extends Instruction {
  private long value;

  public void setPeer (org.apache.bcel.generic.Instruction i, ConstantPool cp) {
    ConstantPoolGen cpg = ClassInfo.getConstantPoolGen(cp);
    Type type = ((org.apache.bcel.generic.LDC2_W) i).getType(cpg);

    if (type == Type.LONG) {
      value = ((ConstantLong) cp.getConstant(
                     ((org.apache.bcel.generic.LDC2_W) i).getIndex())).getBytes();
    } else {
      value = Types.doubleToLong(
                    ((ConstantDouble) cp.getConstant(
                           ((org.apache.bcel.generic.LDC2_W) i).getIndex())).getBytes());
    }
  }

  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo th) {
    th.longPush(value);

    return getNext(th);
  }

  public int getByteCode () {
    return 0x14;
  }
}
