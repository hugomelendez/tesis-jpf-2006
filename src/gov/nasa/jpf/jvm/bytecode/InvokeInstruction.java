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
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.Types;

import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.generic.ConstantPoolGen;


/**
 * abstraction for all invoke instructions
 */
public abstract class InvokeInstruction extends Instruction {
  /* Those are all from the BCEL class, i.e. straight from the class file.
   * Note that we can't directly resolve to MethodInfo objects because
   * the corresponding class might not be loaded yet (has to be done
   * on execution)
   */
  public String cname;
  public String mname;
  public String signature;

  int argSize = -1;

  /** to cache the last callee object */
  int lastObj = Integer.MIN_VALUE;
  
  /**
   * watch out - this is only const for static and special invocation
   * all virtuals will use it only as a cache
   */
  MethodInfo invokedMethod;
  
  protected InvokeInstruction () {}
  
  public void setPeer (org.apache.bcel.generic.Instruction i, ConstantPool cp) {
    org.apache.bcel.generic.InvokeInstruction ii;
    ConstantPoolGen                           cpg;

    cpg = ClassInfo.getConstantPoolGen(cp);
    ii = (org.apache.bcel.generic.InvokeInstruction) i;

    cname = ii.getReferenceType(cpg).toString();
    signature = ii.getSignature(cpg);
    mname = MethodInfo.getUniqueName(ii.getMethodName(cpg), signature);
  }

  /**
   * return the last invoked MethodInfo (cached). Note this is only
   * valid AFTER the insn got checked/executed, since it has to be set by
   * the concrete InvokeInstruction subclasses (and only statics and specials
   * give us const MehodInfos)
   */
  public MethodInfo getInvokedMethod () {
    return invokedMethod;
  }
  
  protected InvokeInstruction (MethodInfo mi, String cname, String mname, String signature,
                       int offset, int position) {
    this.mi = mi;
    this.cname = cname;
    this.mname = mname + signature;
    this.signature = signature;
    this.offset = offset;
    this.position = position;
  }
    
  protected int getArgSize () {
    if (argSize < 0) {
      argSize = Types.getArgumentsSize(signature) + 1; // 'this'
    }

    return argSize;
  }
  
}
