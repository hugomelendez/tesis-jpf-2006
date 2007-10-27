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
import gov.nasa.jpf.jvm.Types;

import org.apache.bcel.classfile.ConstantPool;


/**
 * Compare double
 * ..., value1, value2 => ..., result
 */
public class DCMPL extends Instruction {
  public void setPeer (org.apache.bcel.generic.Instruction i, ConstantPool cp) {
  }

  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo th) {
    double v1 = Types.longToDouble(th.longPop());
    double v2 = Types.longToDouble(th.longPop());

    if (!th.getVM().checkNaNcompare(v1,v2)){
      return th.createAndThrowException("java.lang.ArithmeticException",
                                   "comparing in-exact values: " + v2 + ">" + v1);
    }
    
    if (Double.isNaN(v1) || Double.isNaN(v2)) {
      th.push(-1, false);
    } else if (v1 == v2) {
      th.push(0, false);
    } else if (v2 > v1) {
      th.push(1, false);
    } else {
      th.push(-1, false);
    }

    return getNext(th);
  }

  public int getByteCode () {
    return 0x97;
  }
}
