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
package gov.nasa.jpf.abstraction.old;

import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.ObjVector;

public class ThreadObject extends InstanceObject implements Comparable<ThreadObject> {
  public ObjVector<FrameNode> frames;
  
  public HeapObject name;
  public HeapObject group;
  public HeapObject target;
  public int status;
  
  
  /**
   * Starts off as id from VM.  If 
   */
  public int threadId;

  
  public ObjVector<HeapObject> getObjs () {
    ObjVector<HeapObject> ret = new ObjVector<HeapObject>(objFields.size()+3);
    ret.add(name);
    ret.add(group);
    ret.add(target);
    ret.append(objFields);
    return ret;
  }

  public void serializeTo1(IntVector v) {
    super.serializeTo1(v);
    v.add(status);
    if (frames == null) {
      v.add(-1);
    } else {
      v.add(frames.size());
      for (FrameNode fn : frames) {
        if (fn == null) {
          v.add(-1);
        } else {
          v.add(fn.instrOff);
          v.add(fn.methodId);
          AbstractedState.serializePrims(fn.prims, v);
          AbstractedState.serializeRefs(fn.objs, v);
        }
      }
    }
  }

  /** thread comparison for linearization. */
  public int compareTo (ThreadObject that) {
    // two threads with same id will cause blowup
    assert this.threadId != that.threadId || this == that; 
    
    return this.threadId - that.threadId;
  }
}
