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
package gov.nasa.jpf.jvm;

import gov.nasa.jpf.JPFException;

/**
 * A specialized version of ElementInfo for use in the StaticArea.  The
 * StaticElementInfo is only used to store "static class fields" in the
 * StaticArea.  It specifically knows about the relationship amongst
 * classes, and will recursively lookup a data member if needed.
 *
 * @see gov.nasa.jpf.jvm.ElementInfo
 */
public final class StaticElementInfo extends ElementInfo {
  int classObjectRef = -1;
  int status = ClassInfo.UNINITIALIZED;

  public StaticElementInfo () {
  }

  public StaticElementInfo (Fields f, Monitor m, int classObjRef) {
    super(f, m);
    classObjectRef = classObjRef;
  }

  public int getStatus() {
    return status;
  }
  
  void setStatus (int newStatus) {
    if (status != newStatus) {
      status = newStatus;
      area.markChanged(index);
    }
  }
  
  public void restoreStatus(int s) {
    status = s;
  }
  
  void createStartupClassObject (ClassInfo ci, ThreadInfo ti) {
    classObjectRef = ci.createClassObject(ti, index);
  }
  
  protected FieldInfo getDeclaredFieldInfo (String clsBase, String fname) {
    ClassInfo ci = ClassInfo.getClassInfo(clsBase);
    FieldInfo fi = ci.getDeclaredStaticField(fname);
    
    if (fi == null) {
      throw new JPFException("class " + ci.getName() +
                                         " has no static field " + fname);
    }
    return fi;
  }

  protected FieldInfo getFieldInfo (String fname) {
    ClassInfo ci = getClassInfo();
    FieldInfo fi = ci.getStaticField(fname);
    
    if (fi == null) {
      throw new JPFException("class " + ci.getName() +
                                         " has no static field " + fname);
    }
    return fi;
  }
  
  protected void checkFieldInfo (FieldInfo fi) {
    if (getClassInfo() != fi.getClassInfo()) {
      throw new JPFException("wrong static FieldInfo : " + fi.getName()
          + " , no such field in class " + getClassInfo().getName());
    }
  }

  public int getNumberOfFields () {
    return getClassInfo().getNumberOfStaticFields();
  }
  
  public FieldInfo getFieldInfo (int fieldIndex) {
    return getClassInfo().getStaticField(fieldIndex);
  }
  
  protected ElementInfo getElementInfo (ClassInfo ci) {
    if (ci == getClassInfo()) {
      return this;
    } else {
      return ((StaticArea)(Area)area).get( ci.getName());
    }
  }
  
  /**
   * mark all our fields as static (shared) reachable. No need to set our own
   * attributes, since we reside in the StaticArea
   * @aspects: gc
   */
  void markStaticRoot () {
    // WATCH IT! this overrides the heap object behavior in our super class.
    // See ElementInfo.markStaticRoot() for details
    
    DynamicArea heap = DynamicArea.getHeap();
    ClassInfo ci = getClassInfo();
    int n = ci.getNumberOfStaticFields();
    
    for (int i=0; i<n; i++) {
      FieldInfo fi = ci.getStaticField(i);
      if (fi.isReference()) {
        heap.markStaticRoot(fields.getIntValue(fi.getStorageOffset()));
      }
    }
    
    // don't forget the class object itself (which is not a field)
    heap.markStaticRoot(classObjectRef);
  }
      
  protected Ref getRef () {
    return new ClassRef(getIndex());
  }

  public int getClassObjectRef () {
    return classObjectRef;
  }
  
  public void restoreClassObjectRef(int r) {
    classObjectRef = r;
  }

  public String toString() {
    return getClassInfo().getName(); // don't append index (useless and misleading for statics)
  }
  
  /*  never used? -pcd
  private void blowup () {
    throw new JPFException("cannot access StaticElementInfo by index");
  }
  */
}

