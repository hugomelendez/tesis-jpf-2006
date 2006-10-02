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

import java.util.Arrays;
import java.util.Set;

import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.ObjVector;

public class AbstractedState {
  /** all classes.  these can retain their ordering since they have clear identity. */
  public ObjVector<ClassObject> classes;
  
  /** all threads, in no particular order. */
  public Set<ThreadObject> threads;
  
  /** all heap objects, including theads & classes.  in no particular order. */
  public Set<HeapObject> heap;
  
  
  // POST-LINEARIZATION:
  public ObjVector<ThreadObject> orderedThreads;
  
  public ObjVector<HeapObject> orderedHeap;
  
  
  
  
  // LINEARIZATION:
  /**
   * Orders threads into orderedThreads according to their threadIds.  To get
   * thread symmetry, a separate abstraction must reorder the threadIds.
   */
  protected void orderThreads() {
    ThreadObject[] th = threads.toArray(new ThreadObject[threads.size()]);
    Arrays.sort(th);
    if (orderedThreads == null) {
      orderedThreads = new ObjVector<ThreadObject>(th);
    } else {
      orderedThreads.clear();
      orderedThreads.append(th);
    }
  }
  
  protected void clearHeapRefs() {
    for (HeapObject obj : heap) {
      obj.linearObjRef = -1;
    }
  }
  
  protected void addOrdered(HeapObject obj) {
    if (obj != null && obj.linearObjRef < 0) {
      obj.linearObjRef = orderedHeap.size();
      orderedHeap.add(obj);
    }
  }
  
  protected void walkThreads() {
    for (ThreadObject to : orderedThreads) {
      if (to == null) continue;
      addOrdered(to);
      if (to.frames == null) continue;
      for (FrameNode fn : to.frames) {
        if (fn == null || fn.objs == null) continue;
        for (HeapObject obj : fn.objs) {
          addOrdered(obj);
        }
      }
    }
  }
  
  protected void walkClasses() {
    for (ClassObject co : classes) {
      if (co == null) continue;
      addOrdered(co);
      //addOrdered(co.nameObj); // taken care of with walkHeap()
      if (co.objStatics == null) continue;
      for (HeapObject obj : co.objStatics) {
        addOrdered(obj);
      }
    }
  }
  
  protected void walkHeap() {    
    for (int i = 0; i < orderedHeap.size(); i++) { // end can grow
      HeapObject ho = orderedHeap.get(i);
      if (ho == null) continue;
      if (ho.linearObjRef < 0) continue; // garbage collected
      ObjVector<HeapObject> objs = ho.getObjs();
      if (objs == null) continue;
      for (HeapObject obj : objs) {
        addOrdered(obj);
      }
    }
  }
  
  public void linearize(boolean synchronize) {
    // order the threads according to their ids
    orderThreads();
    
    clearHeapRefs();
    
    if (orderedHeap == null) {
      orderedHeap = new ObjVector<HeapObject>(heap.size());
    } else {
      orderedHeap.clear();
    }
    walkThreads();
    walkClasses();
    walkHeap();
   
    if (!synchronize) return;
    
    // make sure "heap" contains the same items as "orderedHeap"
    heap.clear();
    
    for (HeapObject obj : orderedHeap) {
      heap.add(obj);
    }
  }
  
  
  // DEFAULT SERIALIZATION
  
  public void serializeTo(IntVector v) {
    v.add(orderedHeap.size());
    for (HeapObject obj : orderedHeap) {
      if (obj == null) {
        v.add(-1);
      } else {
        obj.serializeTo(v);
      }
    }
  }
  
  public static void serializePrims(IntVector prims, IntVector v) {
    if (prims == null) {
      v.add(-1);
    } else {
      v.add(prims.size());
      v.append(prims);
    }
  }
  
  public static void serializeRef(HeapObject obj, IntVector v) {
    if (obj == null) {
      v.add(-1);
    } else {
      v.add(obj.linearObjRef);
    }
  }

  public static void serializeRefs(ObjVector<HeapObject> objs, IntVector v) {
    if (objs == null) {
      v.add(-1);
    } else {
      v.add(objs.size());
      for (HeapObject obj : objs) {
        serializeRef(obj,v);
      }
    }
  }
}
