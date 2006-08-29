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
package gov.nasa.jpf.symmetry;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.Config.Exception;
import gov.nasa.jpf.abstraction.old.AbstractedState;
import gov.nasa.jpf.abstraction.old.ClassObject;
import gov.nasa.jpf.abstraction.old.FrameNode;
import gov.nasa.jpf.abstraction.old.HeapObject;
import gov.nasa.jpf.abstraction.old.ThreadObject;

import java.util.Arrays;
import java.util.Comparator;

public class ThreadSymmetry implements /*IStateTransformer, */ Comparator<ThreadObject> {
  boolean removeNames = true;
  boolean removeTargets = false;
  boolean removeGroups = true;
  
  public void init (Config config) throws Exception {
    // TODO
  }

  public void performAbstraction (AbstractedState state) {
    int nThreads = state.threads.size();
    ThreadObject[] th = state.threads.toArray(new ThreadObject[nThreads]);
    if (removeNames) {
      for (int i = 0; i < nThreads; i++) {
        th[i].name = null;
      }
    }
    if (removeTargets) {
      for (int i = 0; i < nThreads; i++) {
        th[i].target = null;
      }
    }
    if (removeGroups) {
      for (int i = 0; i < nThreads; i++) {
        th[i].group = null;
      }
    }

    Arrays.sort(th);  // default to vm ordering (below is stable sort)
    Arrays.sort(th,this);
    
    for (int i = 0; i < nThreads; i++) {
      th[i].threadId = i;
    }
    
    // for now
    state.orderedThreads.clear();
    state.orderedThreads.append(th);
  }

  int instanceVal(HeapObject obj) {
    if (obj == null) {
      return -2;
    } else if (obj.clazz == null) {
      return -1;
    } else {
      return obj.clazz.classId;
    }
  }
  
  public int compare (ThreadObject th1, ThreadObject th2) {
    int comp;
    
    comp = th1.frames.size() - th2.frames.size();
    if (comp != 0) return comp;
    
    comp = th1.status - th2.status;
    if (comp != 0) return comp;
    
    comp = instanceVal(th1.target) - instanceVal(th2.target);
    if (comp != 0) return comp;
    
    int nFrames = th1.frames.size();
    
    for (int i = nFrames - 1; i >= 0; i--) {
      FrameNode f1 = th1.frames.get(i);
      FrameNode f2 = th2.frames.get(i);
      
      comp = f1.methodId - f2.methodId;
      if (comp != 0) return comp;
      comp = f1.instrOff - f2.instrOff;
      if (comp != 0) return comp;
    }
    
    for (int i = nFrames - 1; i >= 0; i--) {
      FrameNode f1 = th1.frames.get(i);
      FrameNode f2 = th2.frames.get(i);
      
      // these should not actually be different:
      comp = f1.prims.size() - f2.prims.size();
      if (comp != 0) return comp;
      comp = f1.objs.size() - f2.objs.size();
      if (comp != 0) return comp;
      
      // compare data:
      comp = f1.prims.compareTo(f2.prims);
      if (comp != 0) return comp;
    }

    for (int i = nFrames - 1; i >= 0; i--) {
      FrameNode f1 = th1.frames.get(i);
      FrameNode f2 = th2.frames.get(i);
      
      int len = f1.objs.size();
      for (int j = 0; j < len; j++) {
        // compare nullness and instance class
        comp = instanceVal(f1.objs.get(j)) - instanceVal(f2.objs.get(j));
        if (comp != 0) return comp;
      }
    }
    
    // give up
    return 0;
  }

  
  
  public boolean prelinearization () {
    return false;
  }

  // DO-NOTHING FUNCTIONS
  public void performPreabstraction (ClassObject[] classes,
                                     ThreadObject[] threads,
                                     HeapObject[] newHeap) { }

}
