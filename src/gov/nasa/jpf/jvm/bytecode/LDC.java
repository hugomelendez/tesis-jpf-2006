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
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.Types;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Type;


/**
 * Push item from runtime constant pool
 * ... => ..., value
 */
public class LDC extends Instruction {
  private String  string;
  private int     value;
  private boolean isString;
  Type            type;


  public void setPeer (org.apache.bcel.generic.Instruction i, ConstantPool cp) {
    ConstantPoolGen cpg = ClassInfo.getConstantPoolGen(cp);
    type = ((org.apache.bcel.generic.LDC) i).getType(cpg);

    if (type == Type.STRING) {
      isString = true;
      string = cp.constantToString(cp.getConstant(
                                         ((ConstantString) cp.getConstant(
                                                ((org.apache.bcel.generic.LDC) i).getIndex())).getStringIndex()));

    } else if (type == Type.INT) {
      isString = false;
      string = null;
      value = ((ConstantInteger) cp.getConstant(
                     ((org.apache.bcel.generic.LDC) i).getIndex())).getBytes();

    } else if (type == Type.FLOAT) {
      isString = false;
      string = null;
      value = Types.floatToInt(
                    ((ConstantFloat) cp.getConstant(
                           ((org.apache.bcel.generic.LDC) i).getIndex())).getBytes());

    } else if (type.getType() == Constants.T_REFERENCE) { 
      // direct CLASS constpool entry. see LDC_W
      int index = ((org.apache.bcel.generic.LDC) i).getIndex();
      string = cp.constantToString(index, Constants.CONSTANT_Class);

    } else {
      throw new JPFException("invalid type of constant");
    }
  }

  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo ti) {
    if (isString) {
      ti.push(ks.da.newConstantString(string), true);
    } else if (type.getType() == Constants.T_REFERENCE) {  // <2do> replace when BCEL > 5.1
        ClassInfo ci = ClassInfo.getClassInfo(string);
        
        if (!ci.isInitialized()) {
          if (ci.loadAndInitialize(ti, this) > 0) {
            return ti.getPC();
          }
        }
        
        ti.push(ci.getClassObjectRef(), true);
    } else {
      ti.push(value, false);
    }

    return getNext(ti);
  }

  public int getByteCode () {
    return 0x12;
  }
}
