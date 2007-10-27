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
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.KernelState;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;

import org.apache.bcel.classfile.ConstantPool;


/**
 * Create new multidimensional array
 * ..., count1, [count2, ...] => ..., arrayref
 */
public class MULTIANEWARRAY extends Instruction {
  private String type;
  private int    dimensions;

  public void setPeer (org.apache.bcel.generic.Instruction i, ConstantPool cp) {
    type = cp.constantToString(cp.getConstant(
                                     ((org.apache.bcel.generic.MULTIANEWARRAY) i).getIndex()));
    dimensions = ((org.apache.bcel.generic.MULTIANEWARRAY) i).getDimensions();
  }

  public int allocateArray (String type, int[] dim, ThreadInfo th, int d) {
    int         l = dim[d++];
    int         arrayRef = th.list.ks.da.newArray(type.substring(d), l, th);
    ElementInfo e = th.list.ks.da.get(arrayRef);

    if (dim.length > d) {
      for (int i = 0; i < l; i++) {
        e.setElement(i, allocateArray(type, dim, th, d));
      }
    } else {
      for (int i = 0; i < l; i++) {
        e.setElement(i, -1);
      }
    }

    return arrayRef;
  }

  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo ti) {
    int[] dim = new int[dimensions];

    for (int i = dimensions - 1; i >= 0; i--) {
      dim[i] = ti.pop();
    }

    ClassInfo ci = ClassInfo.getClassInfo(type);
    if (!ci.isInitialized()) {
      ci.loadAndInitialize(ti);
    }

    
    int arrayRef = allocateArray(type, dim, ti, 0);


    // put the result (the array reference) on the stack
    ti.push(arrayRef, true);

    return getNext(ti);
  }

  public int getByteCode () {
    return 0xC5;
  }
}
