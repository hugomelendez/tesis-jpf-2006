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
package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.DynamicArea;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.FieldInfo;
import gov.nasa.jpf.jvm.ThreadInfo;

/**
 * class abstracting instructions that access instance fields
 */
public abstract class InstanceFieldInstruction extends FieldInstruction
{
  int lastThis = -1;
  
  public FieldInfo getFieldInfo () {
    if (fi == null) {
      ClassInfo ci = ClassInfo.getClassInfo(className);
      if (ci != null) {
        fi = ci.getInstanceField(fname);
      }
    }
    return fi;
  }
  
  boolean isSchedulingRelevant (ThreadInfo ti, int objRef) {
    DynamicArea da = DynamicArea.getHeap();  
      
    if (!ti.hasOtherRunnables() || !da.isSchedulingRelevantObject(objRef)) {
      return false;
    }
    
    if (ti.usePorSyncDetection()) {
      if (fname.startsWith("this$")) {
        // that one is an automatically created outer object reference in an inner class
        // it can't be set. Unfortunately, we don't have an 'immutable' attribute for
        // fields, just objects, so we can't push it into class load time attributing
        // must be filtered out before we call 'isLockProtected'
        return false;
      }
  
      if (!checkFieldFilter()) {
        return false;
      }
      
      if (!ti.getMethod().isSyncRelevant() || isLockProtected(da.get(objRef), ti)) {
        return false;
      }
    }
        
    return true;
  }

  boolean isNewPorFieldBoundary (ThreadInfo ti, FieldInfo fi, int objRef) {
    return (!ti.isFirstStepInsn()) && ti.usePorFieldBoundaries() && isSchedulingRelevant(ti, objRef);
  }
  
  public int getLastThis() {
    return lastThis;
  }
  
  public ElementInfo getLastElementInfo () {
    if (lastThis != -1) {
      return DynamicArea.getHeap().get(lastThis);
    }
    
    return null;
  }
}

