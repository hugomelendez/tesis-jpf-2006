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

import gov.nasa.jpf.jvm.MethodInfo;


/**
 * Invoke instance method; dispatch based on class
 * ..., objectref, [arg1, [arg2 ...]] => ...
 */
public class INVOKEVIRTUAL extends VirtualInvocation {
  public INVOKEVIRTUAL () {}
  
  public INVOKEVIRTUAL (MethodInfo mi, String cname, String mname, String signature,
                       int offset, int position) {
    super(mi, cname, mname, signature, offset, position);
  }

  public int getByteCode () {
    return 0xB6;
  }
  
  public String toString() {
    return "invokevirtual " + getInvokedMethod().getFullName();
  }
}
