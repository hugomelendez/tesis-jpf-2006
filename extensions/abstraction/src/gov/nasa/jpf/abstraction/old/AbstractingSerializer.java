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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.abstraction.old.AbstractedState;
import gov.nasa.jpf.abstraction.old.ClassObject;
import gov.nasa.jpf.abstraction.old.FrameNode;
import gov.nasa.jpf.abstraction.old.HeapObject;
import gov.nasa.jpf.abstraction.old.InstanceObject;
import gov.nasa.jpf.abstraction.old.PrimArrayObject;
import gov.nasa.jpf.abstraction.old.RefArrayObject;
import gov.nasa.jpf.abstraction.old.ThreadObject;
import gov.nasa.jpf.jvm.AbstractSerializer;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.DynamicArea;
import gov.nasa.jpf.jvm.DynamicElementInfo;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.Fields;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.StaticArea;
import gov.nasa.jpf.jvm.StaticElementInfo;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.ThreadList;
import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.ObjVector;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class AbstractingSerializer extends AbstractSerializer {
  /// inherited:  \\\
  //KernelState ks;\\

  List<StateAbstractor> abstractors = new LinkedList<StateAbstractor>();
  AbstractedStateSerializer finalSerializer;
  boolean orderThreads;
  
  /*===================== Configuration Stuff ====================*/
  public void attach (JVM jvm) throws Config.Exception {
    super.attach(jvm);
    Config config = jvm.getConfig();
  
    finalSerializer =
      config.getInstance("vm.serializer.abstracting.serializer",
                                  AbstractedStateSerializer.class);
    if (finalSerializer == null) {
      finalSerializer = new AbstractedStateSerializer() {
        IntVector v = new IntVector(1024);
        
        public void init (Config config) { }

        public int[] serialize (AbstractedState state) {
          v.clear();
          state.serializeTo(v);
          return v.toArray();
        }
      };
    } else {
      finalSerializer.init(config);
    }
    
    for(;;) {
      String key = "vm.serializer.abstracting." + abstractors.size();
      StateAbstractor abs = config.getInstance(key, StateAbstractor.class);
      if (abs == null) break;
      abstractors.add(abs);
      abs.init(config);
    }
    
    orderThreads = config.getBoolean("vm.serializer.abstracting.orderthreads", false);
  }

  
  /*===================== JVM State Retrieval ====================*/
  ThreadInfo[] getOldThreads() {
    ThreadList tl = ks.getThreadList();
    ThreadInfo[] threads = new ThreadInfo[tl.length()];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = tl.get(i);
    }
    return threads;
  }
  
  StaticElementInfo[] getOldClasses() {
    StaticArea sa = ks.sa;
    StaticElementInfo[] ret = new StaticElementInfo[sa.count()];
    
    int cIndex = 0;
    for (StaticElementInfo sei : sa) {
      ret[cIndex] = sei;
      cIndex++;
    }
    assert cIndex == ret.length : "StaticArea reported wrong number of classes";
    return ret;
  }
  
  
  
  /*====================== Conversion ====================*/
  ThreadInfo[] oldThreads;
  StaticElementInfo[] oldClasses;
  DynamicArea oldHeap;
  
  HeapObject[] newHeap;
  ClassObject[] classes;
  ThreadObject[] threads;
  AbstractedState state;
  
  HeapObject newHeapGet(int objRef) {
    if (objRef < 0) {
      return null;
    } else {
      return newHeap[objRef];
    }
  }
  
  void pass1Threads() {
    threads = new ThreadObject[oldThreads.length];
    for (int tIndex = 0; tIndex < oldThreads.length; tIndex++) {
      ThreadInfo ti = oldThreads[tIndex];
      
      int thref = ti.getObjectReference();
      
      if (oldHeap.get(thref) == null) { // been garbage collected
        continue;
      }
      
      ThreadObject obj = new ThreadObject();
      
      obj.objFields = new ObjVector<HeapObject>(0);
      obj.primFields = new IntVector(0);
      
      newHeap[thref] = obj;
      threads[tIndex] = obj;

      int ssize = ti.countStackFrames();
      obj.frames = new ObjVector<FrameNode>(ssize);
      
      for (int i = 0; i < ssize; i++) {
        FrameNode node = new FrameNode();
        
        obj.frames.add(node);

        node.objs = new ObjVector<HeapObject>(0);
        node.prims = new IntVector(0);
      }
    }
  }
  
  void pass1Classes() {
    classes = new ClassObject[oldClasses.length];
    for (int cIndex = 0; cIndex < oldClasses.length; cIndex++) {
      int cref = oldClasses[cIndex].getClassObjectRef();

      ClassObject obj = new ClassObject();

      obj.primStatics = new IntVector(0);
      obj.objStatics = new ObjVector<HeapObject>(0);
      
      newHeap[cref] = obj;
      classes[cIndex] = obj;
    }
  }

  void pass1Objects() {
    int heapLen = oldHeap.getLength();
    for (int objRef = 0; objRef < heapLen; objRef++) {
      if (newHeap[objRef] != null) continue; // thread or class object already done
      
      DynamicElementInfo dei = oldHeap.get(objRef);
      if (dei == null) continue;
      
      if (dei.isArray()) {
        if (dei.getFields().isReferenceArray()) {
          RefArrayObject obj = new RefArrayObject(); 
          obj.objs = new ObjVector<HeapObject>(dei.getFields().arrayLength());
          newHeap[objRef] = obj;
        } else {
          PrimArrayObject obj = new PrimArrayObject(); 
          obj.primValues = new IntVector(dei.getFields().arrayLength()); // approx
          newHeap[objRef] = obj;
        }
      } else {
        InstanceObject obj = new InstanceObject();
        obj.primFields = new IntVector(0);
        obj.objFields = new ObjVector<HeapObject>(0);
        newHeap[objRef] = obj;
      }
    }
  }


  
  
  /* ================= PASS 2 ================*/
  void pass2Classes() {
    for (int i = 0; i < classes.length; i++) {
      ClassObject obj = classes[i];
      if (obj == null) continue; // been thrown out
      
      StaticElementInfo sei = oldClasses[i];
      int scref = oldClasses[i].getClassObjectRef();
      DynamicElementInfo dei = oldHeap.get(scref);
      ClassInfo sci = sei.getClassInfo();
      ClassInfo dci = dei.getClassInfo();
      Fields sfields = sei.getFields();
      Fields dfields = dei.getFields();

      // heap object for java.lang.Class
      obj.clazz = (ClassObject) newHeapGet(dci.getClassObjectRef());
      
      // the java.lang.String for the name
      FieldInfo nameInfo = dci.getInstanceField("name");
      obj.nameObj = newHeapGet(dfields.getIntValue(nameInfo.getStorageOffset()));
      
      // this is unique and unchanging (right now)
      obj.classId = scref;
      
      // been thrown out?
      if (obj.objStatics == null && obj.primStatics == null) continue;
      
      int n = sci.getNumberOfStaticFields();
      for (int j=0; j<n; j++) {
        FieldInfo fi = sci.getStaticField(j);
        if (fi.isReference()) {
          if (obj.objStatics != null) {
            obj.objStatics.add(newHeapGet(sfields.getIntValue(fi.getStorageOffset())));
          }
        } else {
          if (obj.primStatics != null) {
            int start = fi.getStorageOffset();
            int end = start + fi.getStorageSize();
            for (int k = start; k < end; k++) {
              obj.primStatics.add(sfields.getIntValue(k));
            }
          }
        }
      }
    }
  }
  
  void pass2Threads() {
    for (int i = 0; i < threads.length; i++) {
      ThreadObject obj = threads[i];
      if (obj == null) continue;  // been thrown out

      ThreadInfo ti = oldThreads[i];
      DynamicElementInfo ei = oldHeap.get(ti.getObjectReference()); 
      ClassInfo ci = ei.getClassInfo();
      Fields fields = ei.getFields();
      
      int nameIdx = ci.getInstanceField("name").getStorageOffset();
      obj.name = newHeapGet(fields.getReferenceValue(nameIdx));
      
      int groupIdx = ci.getInstanceField("group").getStorageOffset();
      obj.group = newHeapGet(fields.getReferenceValue(groupIdx));
      
      int targetIdx = ci.getInstanceField("target").getStorageOffset();
      obj.target = newHeapGet(fields.getReferenceValue(targetIdx));
      
      obj.threadId = ti.getIndex();
      obj.status = ti.getStatus();
      
      if (obj.frames == null) continue; // been thrown out
      
      int j = 0;
      for (StackFrame sf : ti) {
        FrameNode node = obj.frames.get(j);
        if (node == null) continue; // been thrown out
        
        node.methodId = sf.getMethodInfo().getGlobalId();
        node.instrOff = sf.getPC().getOffset();
        
        if (node.objs == null && node.prims == null) continue;
        
        int localCount = sf.getLocalVariableCount();
        for (int k = 0, l = localCount; k < l; k++) {
          if (sf.isLocalVariableRef(k)) {
            if (node.objs != null) {
              node.objs.add(newHeapGet(sf.getLocalVariable(k)));
            }
          } else {
            if (node.prims != null) {
              node.prims.add(sf.getLocalVariable(k));
            }
          }
        }

        int top = sf.getTopPos();
        for (int k = 0; k <= top; k++) {
          if (sf.isAbsOperandRef(k)) {
            if (node.objs != null) {
              node.objs.add(newHeapGet(sf.getAbsOperand(k)));
            }
          } else {
            if (node.prims != null) {
              node.prims.add(sf.getAbsOperand(k));
            }
          }
        }
        j++;
      }
    }
  }
  
  void pass2Objects() {
    for (int i = 0; i < newHeap.length; i++) {
      HeapObject preobj = newHeap[i];
      if (preobj == null) continue; // been thrown out
      
      DynamicElementInfo ei = oldHeap.get(i);
      ClassInfo ci = ei.getClassInfo();
      Fields fields = ei.getFields();
      
      // FIXME: could be null?
      preobj.clazz = (ClassObject) newHeapGet(ci.getClassObjectRef());
      
      if (preobj instanceof InstanceObject) { // includes ThreadObject
        InstanceObject obj = (InstanceObject) preobj;
        
        boolean isThread = obj instanceof ThreadObject;
        
        if (obj.primFields == null && obj.objFields == null) continue;
        
        int n = ci.getNumberOfInstanceFields();
        for (int j=0; j<n; j++) {
          FieldInfo fi = ci.getInstanceField(j);
          if (isThread &&
              fi.getClassInfo().getName().equals("java.lang.Thread") &&
              !fi.getName().equals("permit")) {
            // (field from java.lang.Thread other than "permit")
            // do nothing; these are special / already considered
          } else if (fi.isReference()) {
            obj.objFields.add(newHeapGet(fields.getIntValue(fi.getStorageOffset())));
          } else {
            int start = fi.getStorageOffset();
            int end = start + fi.getStorageSize();
            for (int k = start; k < end; k++) {
              obj.primFields.add(fields.getIntValue(k));
            }
          }
        }
      } else if (preobj instanceof PrimArrayObject) {
        PrimArrayObject obj = (PrimArrayObject) preobj;
        if (obj.primValues != null) {
          fields.copyTo(obj.primValues);
        }
      } else if (preobj instanceof RefArrayObject) {
        RefArrayObject obj = (RefArrayObject) preobj;
        if (obj.objs != null) {
          int n = fields.arrayLength();
          for (int j=0; j<n; j++) {
            obj.objs.add(newHeapGet(fields.getIntValue(j)));
          }
        }
      } // else it's class or been abstracted out
    }
  }
  
  
  protected int[] computeStoringData () {
    // fill in old stuff 
    oldThreads = getOldThreads();
    oldClasses = getOldClasses();
    oldHeap = ks.da;
    
    newHeap = new HeapObject[oldHeap.getLength()];
    
    // pass 1: create objects
    pass1Threads();
    pass1Classes();
    pass1Objects();
    
        
    // apply pre-abstractions
    for (StateAbstractor abs : abstractors) {
      abs.performPreabstraction(classes,threads,newHeap);
    }
    
    
    // pass 2: fill in objects
    pass2Classes();
    pass2Threads();
    pass2Objects();
    
    
    // create AbstractedState
    state = new AbstractedState();
    
    state.classes = new ObjVector<ClassObject>(classes);
    
    state.orderedThreads = new ObjVector<ThreadObject>(threads);
    state.threads = new HashSet<ThreadObject>(threads.length);
    for (int i = 0; i < threads.length; i++) {
      if (threads[i] != null) {
        state.threads.add(threads[i]);
        threads[i].threadId = i;
      } // no gaps
    }
    
    state.orderedHeap = new ObjVector<HeapObject>(newHeap); 
    state.heap = new HashSet<HeapObject>(oldHeap.count());
    for (int i = 0; i < newHeap.length; i++) {
      if (newHeap[i] != null) {
        state.heap.add(newHeap[i]);
        newHeap[i].linearObjRef = i;
      } // no gaps
    }

    
    // apply abstractions
    for (StateAbstractor abs : abstractors) {
      if (abs.prelinearization()) {
        state.linearize(true);  //DEBUG
      }
      abs.performAbstraction(state);
    }

    state.linearize(false);  //DEBUG

    // serialize!
    return finalSerializer.serialize(state);
  }  
}
