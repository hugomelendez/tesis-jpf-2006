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
package gov.nasa.jpf.jvm;

import org.apache.bcel.classfile.*;


/**
 * type, name and attribute information for 'double' fields
 */
public class DoubleFieldInfo extends FieldInfo {
  double init;
  
  public DoubleFieldInfo (String name, String type, int staticAttr,
                          ConstantValue cv, ClassInfo ci, int idx, int off) {
    super(name, type, staticAttr, cv, ci, idx, off);
    init = (cv != null) ? Double.parseDouble(cv.toString()) : 0.0;
  }

  public void initialize (Fields f) {
    f.setDoubleValue(storageOffset, init);
  }

  public String valueToString (Fields f) {
    double d = f.getDoubleValue(storageOffset);
    return Double.toString(d);
  }
}
