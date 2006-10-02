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

import gov.nasa.jpf.abstraction.state.InstanceObject;
import gov.nasa.jpf.abstraction.state.ObjectNode;
import gov.nasa.jpf.abstraction.state.StaticsNode;
import gov.nasa.jpf.jvm.DynamicElementInfo;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.StaticElementInfo;
import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.ObjVector;

public class NoFieldsAbstractor extends FieldsMeta implements
    IStaticAbstractor, IObjectAbstractor<InstanceObject>, IClassAbstractorPair<InstanceObject> {
  public static final NoFieldsAbstractor instance = new NoFieldsAbstractor();
  
  NoFieldsAbstractor() {
    super(new FieldInfo[] {},new FieldInfo[] {}, 0);
  }

  // not recommended; NullAbstractor prefered for static
  public StaticsNode createStaticSkeleton(StaticElementInfo sei) {
    return new StaticsNode();
  }

  public void fillStaticData(StaticElementInfo sei, StaticsNode skel,
      IHeapMap heapMap) {
    skel.meta = this;
    skel.prims = new IntVector(0);
    skel.refs = new ObjVector<ObjectNode>(0);
  }

  
  
  public InstanceObject createInstanceSkeleton(DynamicElementInfo dei) {
    return new InstanceObject();
  }

  public void fillInstanceData(DynamicElementInfo dei, InstanceObject skel,
      IHeapMap heapMap) {
    skel.meta = this;
    skel.classId = dei.getClassInfo().getUniqueId();
    skel.prims = new IntVector(0);
    skel.refs = new ObjVector<ObjectNode>(0);
  }
  
  
  
  
  public IObjectAbstractor<InstanceObject> getInstanceAbstractor() { return this; }

  public IStaticAbstractor getStaticAbstractor() { return this; }
}
