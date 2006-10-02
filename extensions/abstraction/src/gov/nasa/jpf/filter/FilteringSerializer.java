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
package gov.nasa.jpf.filter;

import gov.nasa.jpf.Config.Exception;
import gov.nasa.jpf.jvm.AbstractSerializer;
import gov.nasa.jpf.jvm.ArrayFields;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.DynamicElementInfo;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.Fields;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.StackFrame;
import gov.nasa.jpf.jvm.StaticElementInfo;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.util.BitArray;
import gov.nasa.jpf.util.FinalBitSet;
import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.ObjVector;

public class FilteringSerializer extends AbstractSerializer {
  protected FilterConfiguration filter;
  
  // indexed by method globalId
  final ObjVector<FinalBitSet> localCache    = new ObjVector<FinalBitSet>();
  // indexed by class uniqueId
  final ObjVector<FinalBitSet> instanceCache = new ObjVector<FinalBitSet>();
  // indexed by class uniqueId
  final ObjVector<FinalBitSet> staticCache   = new ObjVector<FinalBitSet>();

  @Override
  public void attach(JVM jvm) throws Exception {
    super.attach(jvm);
    filter = jvm.getConfig().getInstance("filter.class", FilterConfiguration.class);
    if (filter == null) filter = new DefaultFilterConfiguration(); 
    filter.init(jvm.getConfig());
  }

  
  FinalBitSet getLocalFilter(MethodInfo mi) {
    int mid = mi.getGlobalId();
    FinalBitSet v = localCache.get(mid);
    if (v == null) {
      BitArray a = filter.getFrameLocalInclusion(mi);
      a.invert(); // included => filtered
      v = FinalBitSet.create(a);
      if (v == null) throw new IllegalStateException("Null BitSet returned.");
      localCache.set(mid, v);
    }
    return v;
  }
  
  FinalBitSet getIFields(ClassInfo ci) {
    int cid = ci.getUniqueId();
    FinalBitSet v = instanceCache.get(cid);
    if (v == null) {
      BitArray b = new BitArray(ci.getInstanceDataSize());
      b.setAll();
      for (FieldInfo fi : filter.getMatchedInstanceFields(ci)) {
        int start = fi.getStorageOffset();
        int end = start + fi.getStorageSize();
        for (int i = start; i < end; i++) {
          b.clear(i);
        }
      }
      v = FinalBitSet.create(b);
      if (v == null) throw new IllegalStateException("Null BitArray returned.");
      instanceCache.set(cid, v);
    }
    return v;
  }
  
  FinalBitSet getSFields(ClassInfo ci) {
    int cid = ci.getUniqueId();
    FinalBitSet v = staticCache.get(cid);
    if (v == null) {
      BitArray b = new BitArray(ci.getStaticDataSize());
      b.setAll();
      for (FieldInfo fi : filter.getMatchedStaticFields(ci)) {
        int start = fi.getStorageOffset();
        int end = start + fi.getStorageSize();
        for (int i = start; i < end; i++) {
          b.clear(i);
        }
      }
      v = FinalBitSet.create(b);
      if (v == null) throw new IllegalStateException("Null BitArray returned.");
      staticCache.set(cid, v);
    }
    return v;
  }
  
  protected transient IntVector buf = new IntVector(300);
  protected int[] computeStoringData() {
    buf.clear();
    
    buf.add(ks.tl.length());
    for (ThreadInfo t : ks.tl.getThreads()) {
      buf.add2(t.getObjectReference(),t.getStatus());
      StackFrame[] frames = t.cloneStack();
      buf.add(frames.length);
      for (StackFrame f : frames) {
        int ocount = f.getTopPos() + 1;
        int lcount = f.getLocalVariableCount();
        int len = lcount + ocount;
        MethodInfo mi = f.getMethodInfo();
        buf.add3(mi.getGlobalId(),
                 f.getPC().getOffset(),
                 //[i don't think it's *required*, but to be safe:
                 len); //]
        FinalBitSet filtered = getLocalFilter(mi);
        if (filtered == FinalBitSet.empty) {
          for (int i = 0; i < lcount; i++) {
            buf.add(f.getLocalVariable(i));
          }
        } else {
          for (int i = 0; i < lcount; i++) {
            if (! filtered.get(i)) { 
              buf.add(f.getLocalVariable(i));
            }
          }
        }
        for (int i = 0; i < ocount; i++) {
          buf.add(f.getAbsOperand(i));
        }
      }
    }
    
    buf.add(ks.da.getLength());
    for (DynamicElementInfo d : Misc.iterableFromIterator(ks.da.iterator())) {
      if (d == null) {
        buf.add(-1);
      } else {
        Fields fields = d.getFields();
        ClassInfo ci = fields.getClassInfo();
        buf.add(ci.getUniqueId());
        if (fields instanceof ArrayFields) {
          int[] values = fields.dumpRawValues();
          buf.add(values.length);
          buf.append(values);
        } else {
          FinalBitSet filtered = getIFields(ci);
          int max = ci.getInstanceDataSize();
          if (filtered == FinalBitSet.empty) {
            buf.append(fields.dumpRawValues());
          } else {
            for (int i = 0; i < max; i++) {
              if (! filtered.get(i)) { 
                buf.add(fields.getIntValue(i));
              }
            }
          }
        }
      }
    }

    //[not really needed, but to be safe:
    buf.add(ks.sa.getLength());
    //]
    for (StaticElementInfo s : Misc.iterableFromIterator(ks.sa.iterator())) {
      if (s == null) {
        buf.add(-1);
      } else {
        buf.add(s.getStatus());

        Fields fields = s.getFields();
        ClassInfo ci = fields.getClassInfo();
        FinalBitSet filtered = getSFields(ci);
        int max = ci.getStaticDataSize();
        if (filtered == FinalBitSet.empty) {
          buf.append(fields.dumpRawValues());
        } else {
          for (int i = 0; i < max; i++) {
            if (! filtered.get(i)) { 
              buf.add(fields.getIntValue(i));
            }
          }
        }
      }
    }
    return buf.toArray();
  }

}
