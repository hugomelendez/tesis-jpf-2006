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
package gov.nasa.jpf.abstraction;

import java.util.LinkedList;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.AbstractSerializer;
import gov.nasa.jpf.jvm.JVM;

public class AbstractingSerializer extends AbstractSerializer {
  protected StateGraphBuilder builder;
  protected LinkedList<StateGraphTransform> transforms = new LinkedList<StateGraphTransform>();
  protected StateGraphSerializer serializer;
  protected boolean started = false;
  
  @Override
  public void attach(JVM jvm) throws Config.Exception {
    super.attach(jvm);
    Config config = jvm.getConfig();
    
    builder = config.getInstance("abstraction.builder.class", StateGraphBuilder.class);
    if (builder == null) {
      // TODO
    }
    builder.attach(jvm);
    
    Iterable<StateGraphTransform> configTransforms = 
      config.getInstances("abstraction.transforms", StateGraphTransform.class);
    for (StateGraphTransform t : configTransforms) {
      transforms.addLast(t);
      t.init(config);
    }
    
    serializer = config.getInstance("abstraction.serializer.class", StateGraphSerializer.class);
    if (serializer == null) {
      serializer = new DefaultStateGraphSerializer();
    }
    serializer.init(config);
    // the serializer loads the linearizer if needed (probably)
  }

  public void appendTransform(StateGraphTransform t) {
    assert !started : "Attempt to change abstraction after state matching has started.";
    transforms.addLast(t);
  }
  
  protected int[] computeStoringData() {
    started = true;
    StateGraph g = builder.buildStateGraph();
    for (StateGraphTransform transform : transforms) {
      transform.transformStateGraph(g);
    }
    return serializer.serializeStateGraph(g);
  }
}

/*
import gov.nasa.jpf.Config;
import gov.nasa.jpf.abstraction.state.FrameNode;
import gov.nasa.jpf.abstraction.state.InstanceObject;
import gov.nasa.jpf.abstraction.state.ObjectNode;
import gov.nasa.jpf.abstraction.state.PrimArrayObject;
import gov.nasa.jpf.abstraction.state.RefArrayObject;
import gov.nasa.jpf.abstraction.state.RootNode;
import gov.nasa.jpf.abstraction.state.SetObject;
import gov.nasa.jpf.abstraction.state.StaticsNode;
import gov.nasa.jpf.abstraction.state.ThreadNode;
import gov.nasa.jpf.jvm.AbstractSerializer;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.DynamicArea;
import gov.nasa.jpf.jvm.DynamicElementInfo;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.Fields;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.StaticArea;
import gov.nasa.jpf.jvm.StaticElementInfo;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.ThreadList;
import gov.nasa.jpf.symmetry.FastEqSet;
import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.ObjVector;

import java.util.LinkedList;
import java.util.List;

public class AbstractingSerializer extends AbstractSerializer {
  /// inherited:  \\\
  //KernelState ks;\\

  List<IStateTransformer> abstractors = new LinkedList<IStateTransformer>();
  IAbstractedStateSerializer finalSerializer;
  boolean orderThreads;
  
  //===================== Configuration Stuff ====================//
  public void attach (JVM jvm) throws Config.Exception {
    super.attach(jvm);
    Config config = jvm.getConfig();
  
    finalSerializer =
      config.getInstance("vm.serializer.abstracting.serializer",
                                  IAbstractedStateSerializer.class);
    if (finalSerializer == null) {
      finalSerializer = new IAbstractedStateSerializer() {
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
      IStateTransformer abs = config.getInstance(key, IStateTransformer.class);
      if (abs == null) break;
      abstractors.add(abs);
      abs.init(config);
    }
    
    orderThreads = config.getBoolean("vm.serializer.abstracting.orderthreads", false);
  }

  
  //===================== JVM State Retrieval ====================//
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
  
  
  
  //====================== Conversion ====================//
  ThreadInfo[] oldThreads;
  StaticElementInfo[] oldClasses;
  DynamicArea oldHeap;
  
  ObjectNode[] newHeap;
  ClassNode[] classes;
  ThreadNode[] threads;
  AbstractedState state;
  
  ObjectNode newHeapGet(int objRef) {
    if (objRef < 0) {
      return null;
    } else {
      return newHeap[objRef];
    }
  }
  
  void pass1Objects() {
    int heapLen = oldHeap.getLength();
    for (int objRef = 0; objRef < heapLen; objRef++) {
      DynamicElementInfo dei = oldHeap.get(objRef);
      if (dei == null) continue;
      
      if (dei.isArray()) {
        if (dei.getFields().isReferenceArray()) {
          RefArrayObject obj = new RefArrayObject(); 
          obj.refs = new ObjVector<ObjectNode>(dei.getFields().arrayLength());
          newHeap[objRef] = obj;
        } else {
          PrimArrayObject obj = new PrimArrayObject(); 
          obj.prims = new IntVector(dei.getFields().arrayLength()); // approx
          newHeap[objRef] = obj;
        }
      } else {
        boolean isSpecialSet = false;
        ClassInfo ci = dei.getClassInfo();
        
        if (ci.getNumberOfInstanceFields() == 1) {
          FieldInfo fi = ci.getInstanceField(0);
          if (fi.isReference() &&
              fi.getName().equals("data") && 
              ci.instanceOf("gov.nasa.jpf.symmetry.CanonicalEqBag")) {
            isSpecialSet = true;
          }
        }
        if (isSpecialSet) {
          SetObject obj = new SetObject();
          obj.refs = new FastEqSet<ObjectNode>(0);
          newHeap[objRef] = obj;
        } else {
          InstanceObject obj = new InstanceObject();
          obj.prims = new IntVector(0);
          obj.refs = new ObjVector<ObjectNode>(0);
          newHeap[objRef] = obj;
        }
      }
    }
  }

  void pass1Threads() {
    threads = new ThreadNode[oldThreads.length];
    for (int tIndex = 0; tIndex < oldThreads.length; tIndex++) {
      ThreadInfo ti = oldThreads[tIndex];
      
      int thref = ti.getObjectReference();
      
      if (oldHeap.get(thref) == null) { // been garbage collected
        continue;
      }
      
      ThreadNode node = new ThreadNode();
      threads[tIndex] = node;
      
      InstanceObject obj = (InstanceObject) newHeap[thref];
      obj.extra = node;
      node.threadObj = obj;
      
      int ssize = ti.countStackFrames();
      node.frames = new ObjVector<FrameNode>(ssize);
      
      for (int i = 0; i < ssize; i++) {
        FrameNode frame = new FrameNode();
        
        node.frames.add(frame);

        frame.refs = new ObjVector<ObjectNode>(0);
        frame.prims = new IntVector(0);
      }
    }
  }
  
  void pass1Classes() {
    classes = new ClassNode[oldClasses.length];
    for (int cIndex = 0; cIndex < oldClasses.length; cIndex++) {
      int cref = oldClasses[cIndex].getClassObjectRef();

      ClassNode node = new ClassNode();
      classes[cIndex] = node;
      
      StaticsNode statics = new StaticsNode();
      node.statics = statics;
      
      InstanceObject obj = (InstanceObject) newHeap[cref];
      obj.extra = node;
      node.classObject = obj;
      
      statics.prims = new IntVector(0);
      statics.refs = new ObjVector<ObjectNode>(0);
    }
  }


  
  
  // ================= PASS 2 ================//
  void pass2Classes() {
    for (int i = 0; i < classes.length; i++) {
      ClassNode node = classes[i];
      if (node == null) continue; // been thrown out

      StaticElementInfo sei = oldClasses[i];
      ClassInfo sci = sei.getClassInfo();
      Fields sfields = sei.getFields();
      
      // this is unique and unchanging (right now)
      node.classId = sci.getUniqueId();
      
      /*
      int scref = oldClasses[i].getClassObjectRef();
      DynamicElementInfo dei = oldHeap.get(scref);
      ClassInfo dci = dei.getClassInfo();
      Fields dfields = dei.getFields();
      InstanceObject obj = node.classObject;
      
      if (obj != null) {
        // heap object for java.lang.Class
        InstanceObject classObj = (InstanceObject) newHeapGet(dci.getClassObjectRef()); 
        obj.clazz = (ClassNode) classObj.extra;
        
        // the java.lang.String for the name
        FieldInfo nameInfo = dci.getInstanceField("name");
        obj.refs.add(newHeapGet(dfields.getIntValue(nameInfo.getStorageOffset())));
      }
      */

/*
      StaticsNode statics = node.statics;
      
      if (statics != null) {
        int n = sci.getNumberOfStaticFields();
        for (int j=0; j<n; j++) {
          FieldInfo fi = sci.getStaticField(j);
          if (fi.isReference()) {
            statics.refs.add(newHeapGet(sfields.getIntValue(fi.getStorageOffset())));
          } else {
            int start = fi.getStorageOffset();
            int end = start + fi.getStorageSize();
            for (int k = start; k < end; k++) {
              statics.prims.add(sfields.getIntValue(k));
            }
          }
        }
      }
    }
  }
  
  void pass2Threads() {
    for (int i = 0; i < threads.length; i++) {
      ThreadNode node = threads[i];
      if (node == null) continue;  // been thrown out

      ThreadInfo ti = oldThreads[i];
      
      node.status = ti.getStatus();
      
      node.vmThreadId = ti.getIndex();
      
      /*
      DynamicElementInfo ei = oldHeap.get(ti.getObjectReference()); 
      ClassInfo ci = ei.getClassInfo();
      Fields fields = ei.getFields();
      InstanceObject obj = node.threadObj;
      
      if (obj != null) {
        int targetIdx = ci.getInstanceField("target").getStorageOffset();
        obj.refs.add(newHeapGet(fields.getReferenceValue(targetIdx)));

        int permitIdx = ci.getInstanceField("permit").getStorageOffset();
        obj.refs.add(newHeapGet(fields.getReferenceValue(permitIdx)));

        // TODO: symmetry
        int nameIdx = ci.getInstanceField("name").getStorageOffset();
        obj.refs.add(newHeapGet(fields.getReferenceValue(nameIdx)));

        int groupIdx = ci.getInstanceField("group").getStorageOffset();
        obj.refs.add(newHeapGet(fields.getReferenceValue(groupIdx)));

        obj.prims.add(ti.getIndex());
      }
      */


/*
      if (node.frames != null) {
        int j = 0;
        for (StackFrame sf : ti) {
          FrameNode frame = node.frames.get(j);
          if (frame == null) continue; // been thrown out

          MethodInfo mi = sf.getMethodInfo();
          frame.methodId = mi.getGlobalId();
          
          //TODO: hide ThreadOps
          //if (mi.getClassInfo().getName().equals("")) {}
          
          frame.instrOff = sf.getPC().getOffset();

          int localCount = sf.getLocalVariableCount();
          for (int k = 0, l = localCount; k < l; k++) {
            if (sf.isLocalVariableRef(k)) {
              frame.refs.add(newHeapGet(sf.getLocalVariable(k)));
            } else {
              frame.prims.add(sf.getLocalVariable(k));
            }
          }

          int top = sf.getTopPos();
          for (int k = 0; k <= top; k++) {
            if (sf.isAbsOperandRef(k)) {
              frame.refs.add(newHeapGet(sf.getAbsOperand(k)));
            } else {
              frame.prims.add(sf.getAbsOperand(k));
            }
          }
          j++;
        }
      }
    }
  }
  
  void pass2Objects() {
    for (int i = 0; i < newHeap.length; i++) {
      ObjectNode preobj = newHeap[i];
      if (preobj == null) continue; // been thrown out
      
      DynamicElementInfo ei = oldHeap.get(i);
      ClassInfo ci = ei.getClassInfo();
      Fields fields = ei.getFields();
      
      // FIXME: could be null?
      InstanceObject classObj = (InstanceObject) newHeapGet(ci.getClassObjectRef());
      preobj.clazz = (ClassNode) classObj.extra;
      
      if (preobj instanceof InstanceObject) {
        InstanceObject obj = (InstanceObject) preobj;

        int n = ci.getNumberOfInstanceFields();
        for (int j=0; j<n; j++) {
          FieldInfo fi = ci.getInstanceField(j);
          if (fi.isReference()) {
            obj.refs.add(newHeapGet(fields.getIntValue(fi.getStorageOffset())));
          } else {
            int start = fi.getStorageOffset();
            int end = start + fi.getStorageSize();
            for (int k = start; k < end; k++) {
              obj.prims.add(fields.getIntValue(k));
            }
          }
        }
      } else if (preobj instanceof PrimArrayObject) {
        PrimArrayObject obj = (PrimArrayObject) preobj;
        fields.copyTo(obj.prims);
      } else if (preobj instanceof RefArrayObject) {
        RefArrayObject obj = (RefArrayObject) preobj;
        int n = fields.arrayLength();
        for (int j=0; j<n; j++) {
          obj.refs.add(newHeapGet(fields.getIntValue(j)));
        }
      } else {
        SetObject obj = (SetObject) preobj;
        int oldref = fields.getIntValue(0); // get array ref
        Fields subfields = oldHeap.get(oldref).getFields();
        int n = subfields.arrayLength();
        for (int j=0; j<n; j++) {
          obj.refs.add(newHeapGet(subfields.getIntValue(j)));
        }
      }
    }
  }
  
  
  protected int[] computeStoringData () {
    // fill in old stuff 
    oldThreads = getOldThreads();
    oldClasses = getOldClasses();
    oldHeap = ks.da;
    
    newHeap = new ObjectNode[oldHeap.getLength()];
    
    // pass 1: create objects
    pass1Objects();
    pass1Threads();
    pass1Classes();
    
            
    
    // pass 2: fill in objects
    pass2Classes();
    pass2Threads();
    pass2Objects();
    
    
    // create AbstractedState
    state = new AbstractedState();
    
    RootNode root = new RootNode();
    state.root = root;
    
    root.classes = new ObjVector<ClassNode>(classes);
    root.threads = new FastEqSet<ThreadNode>();
    for (int i = 0; i < threads.length; i++) {
      if (threads[i] != null) {
        root.threads.add(threads[i]);
      }
    }

    
    // apply abstractions
    for (StateAbstractor abs : abstractors) {
      if (abs.prelinearization()) {
        state.linearize(true);
      }
      abs.performAbstraction(state);
    }
    
    state.linearize();

    // serialize!
    return finalSerializer.serialize(state);
  }  
}
*/
