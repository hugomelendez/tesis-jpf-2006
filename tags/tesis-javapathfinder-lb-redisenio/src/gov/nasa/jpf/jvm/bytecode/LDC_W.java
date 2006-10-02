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
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.Types;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.Type;


/**
 * Push item from runtime constant pool (wide index)
 * ... => ..., value
 */
public class LDC_W extends Instruction {
  private int     value;  
  private String  string;
  Type            type;
  
  public void setPeer (org.apache.bcel.generic.Instruction i, ConstantPool cp) {
    ConstantPoolGen cpg = ClassInfo.getConstantPoolGen(cp);
    type = ((org.apache.bcel.generic.LDC_W) i).getType(cpg);
    
    int index = ((org.apache.bcel.generic.LDC_W) i).getIndex();
    
    if (type == Type.STRING) {
      string = cp.constantToString(cp.getConstant(
                                         ((ConstantString) cp.getConstant(index)).getStringIndex()));
      
    } else if (type == Type.INT) {
      value = ((ConstantInteger) cp.getConstant(index)).getBytes();
      
    } else if (type == Type.FLOAT) {
      value = Types.floatToInt(((ConstantFloat) cp.getConstant(index)).getBytes());
      
    /* 
     * Java 1.5 silently introduced a class file change - LDCs can now directly reference class
     * constpool entries. To make it more interesting, BCEL 5.1 chokes on this with a hard exception.
     * As of Aug 2004, this was fixed in the BCEL Subversion repository, but there is no new
     * release out yet. In order to compile this code with BCEL 5.1, we can't even use Type.CLASS.
     * The current hack should compile with both BCEL 5.1 and svn, but only runs - when encountering
     * a Java 1.5 class file - if the BCEL svn jar is used
     */
//    } else if (type == Type.CLASS) {
    } else if (type.getType() == Constants.T_REFERENCE) {  // <2do> replace when BCEL > 5.1
      // that's kind of a hack - if this is a const class ref to the class that is
      // currently loaded, we don't have a corresponding object created yet, and
      // the StaticArea access methods might do a recursive class init. Our solution
      // is to store the name, and resolve the reference when we get executed
      string = cp.constantToString(index, Constants.CONSTANT_Class);
      
    } else {
      throw new JPFException("invalid type of constant");
    }
  }

  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo ti) {
    if (type == Type.STRING) {
      ti.push(ks.da.newConstantString(string), true);
//    } else if (type == Type.CLASS) {
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
    return 0x13;
  }
}
