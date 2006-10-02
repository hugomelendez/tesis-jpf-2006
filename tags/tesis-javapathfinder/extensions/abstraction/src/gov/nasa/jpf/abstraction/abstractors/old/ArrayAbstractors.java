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

import gov.nasa.jpf.abstraction.state.PrimArrayObject;
import gov.nasa.jpf.jvm.DynamicElementInfo;
import gov.nasa.jpf.jvm.Fields;
import gov.nasa.jpf.util.IntVector;

public class ArrayAbstractors {
  private ArrayAbstractors() {}
  
  public static class BasicPrimArrayAbstractor
  implements IObjectAbstractor<PrimArrayObject> {
    public PrimArrayObject createInstanceSkeleton(DynamicElementInfo dei) {
      return new PrimArrayObject();
    }

    public void fillInstanceData(DynamicElementInfo dei, PrimArrayObject skel, IHeapMap heapMap) {
      skel.classId = dei.getClassInfo().getUniqueId();
      //skel.meta = null;  // any better ideas?  ;)
      Fields fields = dei.getFields();
      skel.prims = new IntVector(fields.size());
      skel.prims.append(fields.dumpRawValues());
    }
  }
  
  
}
