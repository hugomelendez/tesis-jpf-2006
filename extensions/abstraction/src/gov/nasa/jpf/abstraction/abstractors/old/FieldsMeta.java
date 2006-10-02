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

import gov.nasa.jpf.abstraction.state.NodeMetaData;
import gov.nasa.jpf.abstraction.state.ObjectNode;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.Fields;
import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.ObjVector;

/**
 * Associates the data in an abstract state node (with fields) with the VM
 * fields they came from.  Primitives that require more than 1 int take up the
 * appropriate number of slots by padding with nulls (the only case in which nulls
 * are allowed.
 * 
 * @author peterd
 */
public class FieldsMeta implements NodeMetaData {
  public final FieldInfo[] objFields;
  public final FieldInfo[] primFields;
  public final int nFields;
  
  public FieldsMeta(FieldsMetaBuilder builder) {
    this(builder.getObjs(),builder.getPrims(),builder.getNumberOfFields());
  }
  
  public FieldsMeta(FieldInfo[] objFields, FieldInfo[] primFields, int nFields) {
    this.objFields = objFields;
    this.primFields = primFields;
    this.nFields = nFields;
  }
  
  public int getNumberOfFields() {
    return nFields;
  }

  public int getNumberOfStorageInts() {
    return objFields.length + primFields.length;
  }
  
  
  // *********************** For Filling In Objects ******************** //
  public ObjVector<ObjectNode> getObjects(Fields values, IHeapMap heapMap) {
    int len = objFields.length;
    ObjVector<ObjectNode> refs = new ObjVector<ObjectNode>(len);
    for (int i = 0; i < len; i++) {
      int objref = values.getReferenceValue(objFields[i].getStorageOffset());
      refs.add(heapMap.mapOldHeapRef(objref));
    }
    return refs;
  }
  
  public IntVector getPrims(Fields values) {
    int len = primFields.length;
    IntVector prims = new IntVector(len);
    int offset = -2; // starts invalid
    for (int i = 0; i < len; i++) {
      FieldInfo fi = primFields[i]; 
      if (fi == null) {
        offset++; // add one to previous offset; > 1 int needed for field 
      } else {
        offset = fi.getStorageOffset();
      }
      prims.add(values.getIntValue(offset));
    }
    return prims;
  }
  
  
}
