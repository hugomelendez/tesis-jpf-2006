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
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.SystemState;
import gov.nasa.jpf.jvm.ThreadInfo;


/**
 * Invoke instance method; special handling for superclass, private,
 * and instance initialization method invocations
 * ..., objectref, [arg1, [arg2 ...]] => ...
 */
public class INVOKESPECIAL extends InvokeInstruction {
  public INVOKESPECIAL () {}

  public INVOKESPECIAL (MethodInfo mi, String cname, String mname, String signature,
                       int offset, int position) {
    super(mi, cname, mname, signature, offset, position);
  }

  public int getByteCode () {
    return 0xB7;
  }
  
  public boolean isExecutable (SystemState ss, KernelState ks, ThreadInfo th) {
    MethodInfo mi = getInvokedMethod(th, ks);
    if (mi == null) {
      return true;
    }

    return mi.isExecutable(th);
  }

  public Instruction execute (SystemState ss, KernelState ks, ThreadInfo th) {
    MethodInfo mi = getInvokedMethod(th, ks);
    if (mi == null) {
      return th.createAndThrowException("java.lang.NoSuchMethodException",
                                   "calling " + cname + "." + mname);
    }

    return mi.execute(th);
  }

  int getCalleeThis (ThreadInfo ti) {
    return ti.getCalleeThis( getArgSize());
  }
  
  /**
   * we can do some more caching here - the MethodInfo should be const
   */
  MethodInfo getInvokedMethod (ThreadInfo th, KernelState ks) {
    
    // since INVOKESPECIAL is only used for private methods and ctors,
    // we don't have to deal with null object calls
    
    if (invokedMethod == null) {
      ClassInfo ci = ClassInfo.getClassInfo(cname);
      invokedMethod = ci.getMethod(mname, true);
    }

    return invokedMethod; // we can store internally
  }
  
  public String toString() {
    MethodInfo callee = getInvokedMethod();
  
    return "invokespecial " + ((callee != null) ? callee.getFullName() : "?");
  }

}
