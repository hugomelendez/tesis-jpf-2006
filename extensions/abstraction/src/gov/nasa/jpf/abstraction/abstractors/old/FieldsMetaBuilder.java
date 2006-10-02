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
package gov.nasa.jpf.abstraction.abstractors.old;

import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.FieldInfo;

import java.util.ArrayList;

public class FieldsMetaBuilder {
  protected ArrayList<FieldInfo> relevantObj;
  protected ArrayList<FieldInfo> relevantPrim;
  protected int nFields = 0;

  public FieldsMetaBuilder(int max) {
    relevantObj = new ArrayList<FieldInfo>(max);
    relevantPrim = new ArrayList<FieldInfo>(max);
  }
  
  public void reset() {
    relevantObj.clear();
    relevantPrim.clear();
  }
  
  public void add(FieldInfo fi) {
    nFields++;
    if (fi.isReference()) {
      relevantObj.add(fi);
    } else {
      relevantPrim.add(fi);
      for (int j = 1; j < fi.getStorageSize(); j++) {
        relevantPrim.add(null);
      }
    }
  }
  
  public FieldInfo[] getObjs() {
    return relevantObj.toArray(new FieldInfo[relevantObj.size()]);
  }
  
  public FieldInfo[] getPrims() {
    return relevantPrim.toArray(new FieldInfo[relevantPrim.size()]);
  }
  
  public int getNumberOfFields() {
    return nFields;
  }
  
  // STATIC STUFF
  public static FieldsMetaBuilder getStaticMetaBuilder(ClassInfo ci,
      IStaticFieldFilter sff) {
    int max = ci.getNumberOfStaticFields();
    FieldsMetaBuilder builder = new FieldsMetaBuilder(max);
    for (int i = 0; i < max; i++) {
      FieldInfo f = ci.getStaticField(i);
      if (sff.staticFieldRelevant(f)) {
        builder.add(f);
      }
    }
    return builder;
  }
  
  public static FieldsMetaBuilder getObjectMetaBuilder(ClassInfo ci,
      IInstanceFieldFilter iff) {
    int max = ci.getNumberOfInstanceFields();
    FieldsMetaBuilder builder = new FieldsMetaBuilder(max);
    for (int i = 0; i < max; i++) {
      FieldInfo f = ci.getInstanceField(i);
      if (iff.instanceFieldRelevant(f)) {
        builder.add(f);
      }
    }
    return builder;
  }
}
