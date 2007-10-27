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
import gov.nasa.jpf.util.Debug;
import gov.nasa.jpf.util.HashData;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Vector;

/**
 * Describes an element of memory containing the field values of a class or an
 * object. In the case of a class, contains the values of the static fields. For
 * an object contains the values of the object fields.
 * 
 * @see gov.nasa.jpf.jvm.FieldInfo
 */
public abstract class ElementInfo implements Cloneable {
  protected Fields          fields;

  protected Monitor         monitor;

  protected Area<?>         area;

  protected int             index;

  // object attribute flag values
  public static final int   ATTR_NONE          = 0x0;

  public static final int   ATTR_PROP_MASK     = 0x0000ffff;

  // the propagated ones - only lower 16 bits can be used

  // reachable from different threads
  public static final int   ATTR_TSHARED       = 0x1;

  // this one is redundant if we just base it on the ClassInfo
  // (->fields->classinfo)
  // but we might use code attrs in the future to set this on a per-instance
  // basis

  // object doesn't change value
  public static final int   ATTR_IMMUTABLE     = 0x2;

  // don't promote to shared along this path
  public static final int   ATTR_NO_PROMOTE    = 0x4;

  // the non-propagated attributes - use only higher 16 bits

  // to-be-done, would be code attr, too, and could easily be checked at runtime
  // (hello, a new property)
  public static final int   ATTR_SINGLE_WRITER = 0x10000;

  // don't propagate attributes through this object
  public static final int   ATTR_NO_PROPAGATE  = 0x20000;

  // object is assumed to be fully protected (i.e. no field access that is
  // not protected by a lock)
  public static final int   ATTR_PROTECTED     = 0x40000;

  // don't reycle this object as long as the flag is set
  public static final int   ATTR_PINDOWN       = 0x80000;

  // these are our state-stored object attributes
  // WATCH OUT! only include info that otherwise reflects a state change, so
  // that
  // we don't introduce new changes. It's value is used to hash the state!
  // <2do> what a pity - 32 stored bits for (currently) only 2 bits of
  // information,
  // but we might use this as a hash for more complex reference info in the
  // future.
  // We distinguish between propagates and private object attributes, the first
  // ones tored in the lower 2 bytes
  protected int             attributes;

  /*
   * The following information is used to cache the value of the indexes so that
   * an explicit indexing is not necessary at each storing.
   */
  protected boolean         fChanged           = true;

  /**
   * the monitor index (into the Monitor HashPool) which is stored/restored
   * for/in backtracking.
   * The critical thing is to not change a Monitor object once it is stored.
   * The first operation after that happened (i.e. storeDataTo() or
   * backtrackTo got called) has to clone the Monitor object, all subsequent
   * operations on it until it gets stored can use the same object
   * '-1' means we have a fresh Monitor object, i.e. we don't have to clone
   */
  protected boolean         mChanged           = true;

  FieldLockInfo[] fLockInfo;
  
  public ElementInfo(Fields f, Monitor m) {
    fields = f;
    monitor = m;

    attributes = f.getClassInfo().getElementInfoAttrs();
  }

  protected ElementInfo() {
  }

  public String toString() {
    return (getClassInfo().getName() + '@' + index);
  }

  public FieldLockInfo getFieldLockInfo (FieldInfo fi) {
    if (fLockInfo == null) {
      fLockInfo = new FieldLockInfo[getNumberOfFields()];
    }
    return fLockInfo[fi.getFieldIndex()];
  }
  
  public void setFieldLockInfo (FieldInfo fi, FieldLockInfo flInfo) {
    fLockInfo[fi.getFieldIndex()] = flInfo;
  }

  /**
   * we had a GC, update the live objects
   */
  void cleanUp () {
    if (fLockInfo != null) {
      for (int i=0; i<fLockInfo.length; i++) {
        FieldLockInfo fli = fLockInfo[i];
        if (fli != null) {
          fLockInfo[i] = fli.cleanUp();
        }
      }
    }
  }
  
/*** remove ***/
  static HashMap<String,FieldLockInfo> lockDiscipline =
    new HashMap<String,FieldLockInfo>();

  public FieldLockInfo getFieldLockInfo(String fid) {
    return lockDiscipline.get(fid);
  }
  public void setFieldLockInfo(String fid, FieldLockInfo flInfo) {
    lockDiscipline.put(fid, flInfo);
  }
/*** remove ***/
  
  /**
   * do we have a reference field with value objRef?
   */
  boolean hasRefField(int objRef) {
    return fields.hasRefField(objRef);
  }

  void setShared() {
    attributes |= ATTR_TSHARED;
  }

  /**
   * set shared, but only if the ATTR_TSHARED bit isn't masked out
   */
  void setShared(int attrMask) {
    attributes |= (attrMask & ATTR_TSHARED);
  }

  /**
   * the recursive phase2 marker entry, which propagates the attributes set by a
   * previous phase1. This one is called on all 'root'-marked objects after
   * phase1 is completed. ElementInfo is not an ideal place for this method, as
   * it has to access some innards of both ClassInfo (FieldInfo container) and
   * Fields. But on the other hand, we want to keep the whole heap traversal
   * business as much centralized in ElementInfo and DynamicArea as possible
   * 
   * @aspects: gc
   */
  void markRecursive(int tid, int attrMask) {
    DynamicArea heap = DynamicArea.getHeap();
    int i, n;

    if (isArray()) {
      if (fields.isReferenceArray()) {
        n = fields.arrayLength();
        for (i = 0; i < n; i++) {
          heap.markRecursive( fields.getIntValue(i), tid, attributes, attrMask, null);
        }
      }
    } else {
      ClassInfo ci = getClassInfo();
      boolean isWeakRef = ci.isWeakReference();
        
      do {
        n = ci.getNumberOfDeclaredInstanceFields();
        boolean isRef = isWeakRef && ci.isRefClass(); // is this the java.lang.ref.Reference part?
        
        for (i = 0; i < n; i++) {
          FieldInfo fi = ci.getDeclaredInstanceField(i);
          if (fi.isReference()) {
            if ((i == 0) && isRef) {
              // we need to reset the ref field once the referenced object goes away
              // NOTE: only the *first* WeakReference field is a weak ref
              // (this is why we have our own implementation)
              heap.registerWeakReference(fields);
            } else {

              // the refAttrs are not immediately masked because we have to preserve
              // the mask values up to the point where we would promote an otherwise
              // unshared root object due to a different thread id (in case we
              // didn't catch a mask on the way that prevents this)
              heap.markRecursive( fields.getReferenceValue(fi.getStorageOffset()),
                                  tid, attributes, attrMask, fi);
            }
          }
        }
        ci = ci.getSuperClass();
      } while (ci != null);
    }
  }

  void propagateAttributes(int refAttr, int attrMask) {
    attributes |= ((refAttr & attrMask) & ATTR_PROP_MASK);
  }

  int getAttributes () {
    return attributes;
  }
    
  
  public boolean isShared() {
    return ((attributes & ATTR_TSHARED) != 0);
  }

  public boolean isImmutable() {
    return ((attributes & ATTR_IMMUTABLE) != 0);
  }

  public boolean isSchedulingRelevant() {
    // only mutable, shared objects are relevant
    return ((attributes & (ATTR_TSHARED | ATTR_IMMUTABLE)) == ATTR_TSHARED);
  }

  /**
   * check if there are any propagated attributes in ei we don't have yet
   */
  public boolean needsAttributePropagationFrom(ElementInfo ei) {
    int a = attributes;
    int o = ei.attributes;

    if (a != o) {
      if ((o & ATTR_PROP_MASK) > (a & ATTR_PROP_MASK))
        return true;

      for (int i = 0; i < 16; i++, o >>= 1, a >>= 1) {
        if ((o & 0x1) > (a & 0x1)) {
          return true;
        }
      }
    }

    return false;
  }

  public void setArea(Area<?> newArea) {
    area = newArea;
  }

  public Area<?> getArea() {
    return area;
  }

  /** a bit simplistic, but will do for object equalness */
  public boolean equals(Object other) {
    if (getClass() != other.getClass())
      return false;

    ElementInfo ei = (ElementInfo) other;
    return fields.equals(ei.fields);
  }

  public ClassInfo getClassInfo() {
    return fields.getClassInfo();
  }

  abstract protected FieldInfo getDeclaredFieldInfo(String clsBase, String fname);

  abstract protected ElementInfo getElementInfo(ClassInfo ci);

  abstract protected FieldInfo getFieldInfo(String fname); 

  public void setIntField(FieldInfo fi, int value) {
    //checkFieldInfo(fi); // in case somebody caches and uses the wrong
    // FieldInfo
    ElementInfo ei = getElementInfo(fi.getClassInfo()); // might not be 'this'
                                                        // in case of a static

    if (!fi.isReference()) {
      ei.cloneFields().setIntValue(fi.getStorageOffset(), value);
    } else {
      throw new JPFException("reference field: " + fi.getName());
    }
  }

  public void setDeclaredIntField(String fname, String clsBase, int value) {
    setIntField(getDeclaredFieldInfo(clsBase, fname), value);
  }

  public void setBooleanField (String fname, boolean value) {
    setIntField( getFieldInfo(fname), value ? 1 : 0);
  }
  
  public void setIntField(String fname, int value) {
    setIntField(getFieldInfo(fname), value);
  }

  public void setDoubleField (String fname, double value) {
    setLongField(fname, Types.doubleToLong(value));
  }
  
  public void setLongField(String fname, long value) {
    FieldInfo fi = getFieldInfo(fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    ei.cloneFields().setLongValue(fi.getStorageOffset(), value);
  }

  void updateReachability(int oldRef, int newRef) {
    ThreadInfo ti = ThreadInfo.getCurrent(); // might be null if still in VM
                                             // init
    if ((ti == null) || ti.isInCtor() || !ti.usePor()) {
      return;
    }

    if (oldRef != newRef) {
      DynamicArea heap = DynamicArea.getHeap();
      ElementInfo oei, nei;

      if (isShared()) {
        if (oldRef != -1) {
          oei = heap.get(oldRef);
          if (!oei.isImmutable()) { // it's already shared, anyway
            // Ok, give up and do a full mark, the old object might not be
            // reachable anymore
            heap.analyzeHeap(false); // takes care of the newRef, too
            return;
          }
        }

        if (newRef != -1) {
          nei = heap.get(newRef);
          if (!nei.isShared() && !nei.isImmutable()) {
            // no need to walk the whole heap, just recursively promote nei
            // and all its reachables to 'shared'
            nei.setShared();
            // <2do> - this would be the place to add listener notification
            
            heap.initGc(); // doesn't belong here, should be encapsulated in DA
            nei.markRecursive(ti.getIndex(), ATTR_PROP_MASK);
          }
        }
      } else { // we are not shared (oldRef can't change status)
        if (newRef != -1) {
          nei = heap.get(newRef);
          if (nei.isSchedulingRelevant()) { // shared and mutable
            // give up, nei might become non-shared
            heap.analyzeHeap(false);
          }
        }
      }
    }

    if (oldRef != -1) {
      JVM.getVM().getSystemState().activateGC(); // needs GC at the end of this
                                                 // transition
    }
  }

  public void setReferenceField(FieldInfo fi, int value) {
    ElementInfo ei = getElementInfo(fi.getClassInfo()); // might not be 'this'
                                                        // in case of a static
    Fields f = ei.cloneFields();
    int off = fi.getStorageOffset();

    if (fi.isReference()) {
      int oldValue = f.getReferenceValue(off);
      f.setReferenceValue(off, value);
      updateReachability(oldValue, value);
    } else {
      throw new JPFException("not a reference field: " + fi.getName());
    }
  }

  public void setDeclaredReferenceField(String fname, String clsBase, int value) {
    setReferenceField(getDeclaredFieldInfo(clsBase, fname), value);
  }

  public void setReferenceField(String fname, int value) {
    setReferenceField(getFieldInfo(fname), value);
  }

  public int getDeclaredReferenceField(String fname, String clsBase) {
    FieldInfo fi = getDeclaredFieldInfo(clsBase, fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());

    if (!fi.isReference()) {
      throw new JPFException("not a reference field: " + fi.getName());
    }
    return ei.fields.getIntValue(fi.getStorageOffset());
  }

  public int getReferenceField(String fname) {
    FieldInfo fi = getFieldInfo(fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());

    if (!fi.isReference()) {
      throw new JPFException("not a reference field: " + fi.getName());
    }
    return ei.fields.getIntValue(fi.getStorageOffset());
  }

  
  public int getDeclaredIntField(String fname, String clsBase) {
    // be aware of that static fields are not flattened (they are unique), i.e.
    // the FieldInfo might actually refer to another ClassInfo/StaticElementInfo
    FieldInfo fi = getDeclaredFieldInfo(clsBase, fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getIntValue(fi.getStorageOffset());
  }

  public int getIntField(String fname) {
    // be aware of that static fields are not flattened (they are unique), i.e.
    // the FieldInfo might actually refer to another ClassInfo/StaticElementInfo
    FieldInfo fi = getFieldInfo(fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getIntValue(fi.getStorageOffset());
  }

  public void setDeclaredLongField(String fname, String clsBase, long value) {
    FieldInfo fi = getDeclaredFieldInfo(clsBase, fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    ei.cloneFields().setLongValue(fi.getStorageOffset(), value);
  }

  public long getDeclaredLongField(String fname, String clsBase) {
    FieldInfo fi = getDeclaredFieldInfo(clsBase, fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getLongValue(fi.getStorageOffset());
  }

  public long getLongField(String fname) {
    FieldInfo fi = getFieldInfo(fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getLongValue(fi.getStorageOffset());
  }

  public boolean getDeclaredBooleanField(String fname, String refType) {
    FieldInfo fi = getDeclaredFieldInfo(refType, fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getBooleanValue(fi.getStorageOffset());
  }

  public boolean getBooleanField(String fname) {
    FieldInfo fi = getFieldInfo(fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getBooleanValue(fi.getStorageOffset());
  }

  public byte getDeclaredByteField(String fname, String refType) {
    FieldInfo fi = getDeclaredFieldInfo(refType, fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getByteValue(fi.getStorageOffset());
  }

  public byte getByteField(String fname) {
    FieldInfo fi = getFieldInfo(fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getByteValue(fi.getStorageOffset());
  }

  public char getDeclaredCharField(String fname, String refType) {
    FieldInfo fi = getDeclaredFieldInfo(refType, fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getCharValue(fi.getStorageOffset());
  }
  
  public char getCharField(String fname) {
    FieldInfo fi = getFieldInfo(fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getCharValue(fi.getStorageOffset());
  }

  public double getDeclaredDoubleField(String fname, String refType) {
    FieldInfo fi = getDeclaredFieldInfo(refType, fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getDoubleValue(fi.getStorageOffset());
  }

  public double getDoubleField(String fname) {
    FieldInfo fi = getFieldInfo(fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getDoubleValue(fi.getStorageOffset());
  }

  public float getDeclaredFloatField(String fname, String refType) {
    FieldInfo fi = getDeclaredFieldInfo(refType, fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getFloatValue(fi.getStorageOffset());
  }

  public float getFloatField(String fname) {
    FieldInfo fi = getFieldInfo(fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getFloatValue(fi.getStorageOffset());
  }

  public short getDeclaredShortField(String fname, String refType) {
    FieldInfo fi = getDeclaredFieldInfo(refType, fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getShortValue(fi.getStorageOffset());
  }

  public short getShortField(String fname) {
    FieldInfo fi = getFieldInfo(fname);
    ElementInfo ei = getElementInfo(fi.getClassInfo());
    return ei.fields.getShortValue(fi.getStorageOffset());
  }

  /**
   * note this only holds for instance fields, and hence the method has to
   * be overridden in StaticElementInfo
   */
  private void checkFieldInfo(FieldInfo fi) {
    if (!getClassInfo().isInstanceOf(fi.getClassInfo())) {
      throw new JPFException("wrong FieldInfo : " + fi.getName()
          + " , no such field in " + getClassInfo().getName());
    }
  }

  // those are the cached field value accessors. The caller is responsible
  // for assuring type compatibility
  public int getIntField(FieldInfo fi) {
    checkFieldInfo(fi);
    return fields.getIntValue(fi.getStorageOffset());
  }

  public int getReferenceField (FieldInfo fi) {
    // the reference is just an 'int' for us
    return getIntField(fi);
  }
  
  public long getLongField(FieldInfo fi) {
    checkFieldInfo(fi);
    return fields.getLongValue(fi.getStorageOffset());
  }

  public void setLongField(FieldInfo fi, long val) {
    checkFieldInfo(fi);
    cloneFields().setLongValue(fi.getStorageOffset(), val);
  }

  protected void checkArray(int index) {
    if (!isArray()) { // <2do> should check for !long array
      throw new JPFException(
          "cannot access non array objects by index");
    }
    if ((index < 0) || (index >= fields.size())) {
      throw new JPFException("illegal array offset: " + index);
    }
  }

  protected void checkLongArray(int index) {
    if (!isArray()) { // <2do> should check for !int array
      throw new JPFException(
          "cannot access non array objects by index");
    }
    if ((index < 0) || (index >= (fields.size() - 1))) {
      throw new JPFException("illegal long array offset: " + index);
    }
  }

  private boolean isReferenceArray() {
    return getClassInfo().isReferenceArray();
  }

  // those are not really fields, so treat them differently!
  public void setElement(int index, int value) {
    checkArray(index);
    if (isReferenceArray()) {
      cloneFields().setReferenceValue(index, value);
    } else {
      cloneFields().setIntValue(index, value);
    }
  }

  public void setLongElement(int index, long value) {
    checkArray(index);
    cloneFields().setLongValue(index*2, value);
  }

  public int getElement(int index) {
    checkArray(index);
    return fields.getIntValue(index);
  }

  public long getLongElement(int index) {
    checkArray(index);
    return fields.getLongValue(index*2);
  }

  public void setIndex(int newIndex) {
    index = newIndex;
  }

  public int getIndex() {
    return index;
  }

  public int getThisReference() {
    return index;
  }

  public int getLockCount() {
    return monitor.getLockCount();
  }

  public ThreadInfo getLockingThread() {
    return monitor.getLockingThread();
  }

  public boolean isLocked() {
    return (monitor.getLockCount() > 0);
  }

  public boolean isArray() {
    return fields.isArray();
  }

  public String getArrayType() {
    if (!fields.isArray()) {
      throw new JPFException("object is not an array");
    }

    return Types.getArrayElementType(fields.getType());
  }

  public Object getBacktrackData() {
    return null;
  }

  public char getCharArrayElement(int index) {
    return (char) getElement(index);
  }

  public int getIntArrayElement(int findex) {
    return getElement(findex);
  }

  public long getLongArrayElement(int findex) {
    return getLongElement(findex);
  }

  public boolean[] asBooleanArray() {
    return fields.asBooleanArray();
  }

  public byte[] asByteArray() {
    return fields.asByteArray();
  }

  public short[] asShortArray() {
    return fields.asShortArray();
  }

  public char[] asCharArray() {
    return fields.asCharArray();
  }

  public int[] asIntArray() {
    return fields.asIntArray();
  }

  public long[] asLongArray() {
    return fields.asLongArray();
  }

  public float[] asFloatArray() {
    return fields.asFloatArray();
  }

  public double[] asDoubleArray() {
    return fields.asDoubleArray();
  }

  public boolean isNull() {
    return (index == -1);
  }

  public ElementInfo getDeclaredObjectField(String fname, String referenceType) {
    return area.ks.da.get(getDeclaredIntField(fname, referenceType));
  }

  public ElementInfo getObjectField(String fname) {
    return area.ks.da.get(getIntField(fname));
  }


  /**
   * answer an estimate of the heap size in bytes (this is of course VM
   * dependent, but we can give an upper bound for the fields/elements, and that
   * should be good in terms of application specific properties)
   */
  public int getHeapSize() {
    return fields.getHeapSize();
  }

  public String getStringField(String fname) {
    int ref = getIntField(fname);

    if (ref != -1) {
      ElementInfo ei = area.ks.da.get(ref);
      if (ei == null) {
        System.out.println("OUTCH: " + ref + ", this: " + index);
        throw new NullPointerException(); // gets rid of null warning -pcd
      }
      return ei.asString();
    } else {
      return "null";
    }
  }

  public String getType() {
    return fields.getType();
  }

  /**
   * get a cloned list of the waiters for this object
   */
  public ThreadInfo[] getWaitingThreads() {
    ThreadInfo[] locked = monitor.getLockedThreads();
    int i, j, n = 0;
    
    for (i=0; i<locked.length; i++) {
      if (locked[i].isWaiting()) {
        n++;
      }
    }
    
    if (n == 0) {
      return Monitor.emptySet;
    } else {
      ThreadInfo[] waiters = new ThreadInfo[n];
      for (i=0, j=0; j<n; i++) {
        if (locked[i].isWaiting()) {
          waiters[j++] = locked[i];
        }
      }
      
      return waiters;
    }
  }

  public int arrayLength() {
    return fields.arrayLength();
  }

  public String asString() {
    if (!fields.getClassInfo().instanceOf("java.lang.String")) {
      throw new JPFException("object is not of type java.lang.String");
    }

    int value = getDeclaredIntField("value", "java.lang.String");
    int length = getDeclaredIntField("count", "java.lang.String");
    int offset = getDeclaredIntField("offset", "java.lang.String");

    ElementInfo e = area.get(value);

    StringBuffer sb = new StringBuffer();

    for (int i = offset; i < (offset + length); i++) {
      sb.append((char) e.fields.getIntValue(i));
    }

    return sb.toString();
  }

  void updateLockingInfo() {
    int i;
    
    ThreadInfo ti = monitor.getLockingThread();
    if (ti != null) {
      // here we can update ThreadInfo lock object info (so that we don't
      // have to store it separately)

      // NOTE - the threads need to be restored *before* the Areas, or this is
      // going to choke

      // note that we add only once, i.e. rely on the monitor lockCount to
      // determine when to remove an object from our lock set
      //assert area.ks.tl.get(ti.index) == ti;  // covered by verifyLockInfo
      ti.addLockedObject(this);
    }
    
    if (monitor.hasLockedThreads()) {
      ThreadInfo[] lockedThreads = monitor.getLockedThreads();
      for (i=0; i<lockedThreads.length; i++) {
        ti = lockedThreads[i];
        //assert area.ks.tl.get(ti.index) == ti;  // covered by verifyLockInfo
        ti.setLockRef(index);
      }
    }
  }

  public boolean canLock(ThreadInfo th) {
    return monitor.canLock(th);
  }

  public void checkArrayBounds(int index)
      throws ArrayIndexOutOfBoundsExecutiveException {
    if (outOfBounds(index)) {
      throw new ArrayIndexOutOfBoundsExecutiveException(
          ThreadInfo.getCurrent().createAndThrowException(
              "java.lang.ArrayIndexOutOfBoundsException", Integer.toString(index)));
    }
  }

  public void checkLongArrayBounds(int index)
      throws ArrayIndexOutOfBoundsExecutiveException {
    checkArrayBounds(index);
    checkArrayBounds(index + 1);
  }

  public Object clone() {
    try {
      ElementInfo ei = (ElementInfo) super.clone();

      if (ei.fChanged) {
        ei.fields = fields.clone();
      }

      if (ei.mChanged) {
        ei.monitor = monitor.clone();
      }

      area = null;
      index = -1;

      return ei;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
      throw new InternalError("should not happen");
    }
  }

  public void hash(HashData hd) {
    fields.hash(hd);
    monitor.hash(hd);
  }

  public int hashCode() {
    HashData hd = new HashData();

    hash(hd);

    return hd.getValue();
  }

  public boolean instanceOf(String type) {
    return Types.instanceOf(fields.getType(), type);
  }

  abstract public int getNumberOfFields();

  abstract public FieldInfo getFieldInfo(int i);

  public void log() { // <2do> replace this
    if (fChanged) {
      Debug.println(Debug.MESSAGE, "(fields have changed)");
    }

    int n = getNumberOfFields();
    for (int i = 0; i < n; i++) {
      FieldInfo fi = getFieldInfo(i);
      Debug.println(Debug.MESSAGE, fi.getName() + ": "
          + fi.valueToString(fields));
    }

    if (mChanged) {
      Debug.println(Debug.MESSAGE, "(monitor has changed)");
    }

    //monitor.log();
  }
    
  /**
   * threads that will grab our lock on their next execution have to be
   * registered, so that they can be blocked in case somebody else gets
   * scheduled
   */
  public void registerLockContender (ThreadInfo ti) {
    
    assert ti.lockRef == -1 || ti.lockRef == index :
      "thread " + ti + " trying to register for : " + this + 
      " ,but already blocked on: " + area.get(ti.lockRef);
    
    // note that using the lockedThreads list is a bit counter-intuitive
    // since the thread is still in RUNNING or UNBLOCKED state, but it will
    // remove itself from there once it resumes: lock() calls setMonitorWithoutLocked(ti)
    setMonitorWithLocked(ti);

    // added to satisfy invariant implied by updateLockingInfo() -peterd 
    ti.setLockRef(index);
  }
  
  /**
   * somebody made up his mind and decided not to enter a synchronized section
   * of code it had registered before (e.g. INVOKECLINIT)
   */
  public void unregisterLockContender (ThreadInfo ti) {
    setMonitorWithoutLocked(ti);

    // moved from INVOKECLINIT -peterd
    ti.resetLockRef();
  }
  
  void blockLockContenders () {
    // check if there are any other threads that have to change status because they
    // require to lock us in their next exec
    ThreadInfo[] lockedThreads = monitor.getLockedThreads();
    for (int i=0; i<lockedThreads.length; i++) {
      if (lockedThreads[i].isRunnable()) {
        lockedThreads[i].setStatus(ThreadInfo.BLOCKED);
        //lockedThreads[i].setLockRef(index);
      }
    }    
  }
  
  /**
   * from a MONITOR_ENTER or sync INVOKExx if we cannot acquire the lock
   */
  public void block (ThreadInfo ti) {
//System.out.println("@@ block on " + this);
    assert (monitor.getLockingThread() != null) && (monitor.getLockingThread() != ti) :
          "attempt to block " + ti.getName() + " on unlocked or own locked object: " + this;
        
    setMonitorWithLocked(ti);

    ti.setStatus(ThreadInfo.BLOCKED);    
    ti.setLockRef(index);

//dumpMonitor();
//JVM.getVM().dumpThreadStates();
  }

  /**
   * from a MONITOR_ENTER or sync INVOKExx if we can acquire the lock
   */
  public void lock (ThreadInfo ti) {
//System.out.println("@@ lock " + this);
    assert (monitor.getLockingThread() == null) ||  (monitor.getLockingThread() == ti): 
      "locking: " + this + " by " + ti.getName() + " failed, lock owned by: "
        + monitor.getLockingThread().getName();
    
    // the thread might be still in the lockedThreads list if this is the
    // first step of a transition
    setMonitorWithoutLocked(ti);
    monitor.setLockingThread(ti);
    monitor.incLockCount();

    // before we execute anything else, mark this thread as not being blocked anymore
    ti.resetLockRef();

    if (ti.getStatus() == ThreadInfo.UNBLOCKED) {
      ti.setStatus(ThreadInfo.RUNNING);
    }

    blockLockContenders();
        
    // don't re-add if we are recursive - the lock count is avaliable in the monitor
    if (monitor.getLockCount() == 1) {
      ti.addLockedObject(this);
    }

//dumpMonitor();
//JVM.getVM().dumpThreadStates();
  }
  
  /**
   * from a MONITOR_EXIT or sync method RETURN
   * release a possibly recursive lock if lockCount goes to zero
   */
  public void unlock (ThreadInfo ti) {
//System.out.println("@@ unlock " + this);
    assert (monitor.getLockCount() > 0) && (monitor.getLockingThread() == ti) :
      "attempt of " + ti.getName() + " " + "to release non-owned lock for object: " + this;

    if (monitor.getLockCount() == 1) {
      ti.removeLockedObject(this);
      
      ThreadInfo[] lockedThreads = monitor.getLockedThreads();
      for (int i = 0; i < lockedThreads.length; i++) {
        switch (lockedThreads[i].getStatus()) {
        case ThreadInfo.NOTIFIED:
        case ThreadInfo.BLOCKED:
//System.out.println("@@  UNBLOCK: " + lockedThreads[i].getName());
          lockedThreads[i].setStatus(ThreadInfo.UNBLOCKED);

          // this used to break invariant implied by updateLockingInfo() -peterd 
          //lockedThreads[i].resetLockRef();
          
          break;
        case ThreadInfo.INTERRUPTED:
//System.out.println("@@  UNBLOCKED_INTERRUPTED: " + lockedThreads[i].getName());
          lockedThreads[i].setStatus(ThreadInfo.UNBLOCKED_INTERRUPTED);
          
          // this used to break invariant implied by updateLockingInfo() -peterd 
          //lockedThreads[i].resetLockRef();

          break;
        case ThreadInfo.WAITING:
          // TODO: pcm, should anything go here?
          
          break;
        default:
          assert false : "Monitor.lockedThreads<->ThreadData.status inconsistency! " + lockedThreads[i].getStatusName();
          // why is it in the list - when someone unlocks, all others should have been blocked
        }
      }
      
      // leave the contenders - we need to know whom to block on subsequent lock
      setMonitor();
      
      monitor.decLockCount();
      monitor.setLockingThread(null);
//dumpMonitor();
//JVM.getVM().dumpThreadStates();
    
    } else { // recursive unlock
      setMonitor();
      monitor.decLockCount();
    }
  }

  /**
   * notifies one of the waiters. Note this is a potentially non-deterministic action
   * if we have several waiters, since we have to try all possible choices.
   * Note that even if we notify a thread here, it still remains in the lockedThreads
   * list until the lock is released (notified threads cannot run right away)
   */
  public void notifies (SystemState ss, ThreadInfo ti) {
//System.out.println("@@ notify " + this);
    assert monitor.getLockingThread() != null : "notify on unlocked object: " + this;
    
    ThreadInfo[] locked = monitor.getLockedThreads();
    int i, nWaiters=0, iWaiter=0;
    
    for (i=0; i<locked.length; i++) {
      if (locked[i].isWaiting()) {
        nWaiters++;
        iWaiter = i;
      }
    }
        
    if (nWaiters == 0) {
      // no waiters, nothing to do
    } else if (nWaiters == 1) {
      // very deterministic, just a little optimization
      locked[iWaiter].setStatus(ThreadInfo.NOTIFIED);
    } else {
      // Ok, this is the non-deterministic case
      ChoiceGenerator cg = ss.getChoiceGenerator();

      assert (cg != null) && (cg instanceof ThreadChoiceGenerator) :
        "notify " + this + " without ThreadChoiceGenerator: " + cg;
      
      ThreadInfo tiNotify = ((ThreadChoiceGenerator)cg).getNextChoice();
      tiNotify.setStatus(ThreadInfo.NOTIFIED);
    }
    
//dumpMonitor();
//JVM.getVM().dumpThreadStates();
    ti.getVM().notifyObjectNotifies(ti, this);
  }

  /**
   * notify all waiters. This is a deterministic action
   * all waiters remain in the locked list, since they still have to be unblocked
   */
  public void notifiesAll() {
    assert monitor.getLockingThread() != null : "notifyAll on unlocked object: " + this;
    
    ThreadInfo[] locked = monitor.getLockedThreads();
    for (int i=0; i<locked.length; i++) {
      locked[i].setStatus(ThreadInfo.NOTIFIED);
    }

//dumpMonitor();
//JVM.getVM().dumpThreadStates();

    JVM.getVM().notifyObjectNotifiesAll(ThreadInfo.currentThread, this);
  }
  
  /**
   * wait to be notified. thread has to hold the lock, but gives it up in the wait.
   * Make sure lockCount can be restored properly upon notification 
   */
  public void wait (ThreadInfo ti, long timeout) {
//System.out.println("@@ wait on: " + this + " timeout: " + timeout);
    assert (monitor.getLockingThread() == ti) : "wait on unlocked object: " + this;
    
    // store the lock count in the waiting thread (we have to restore to
    // the same value once we get notified)
    ti.setLockCount(monitor.getLockCount());

    // we have to add this thread to the locked list since it will be still blocked once it got notified
    setMonitorWithLocked(ti);
    monitor.setLockingThread( null);
    monitor.setLockCount(0);

    ti.removeLockedObject(this); //wv: remove locked object here
    ti.setLockRef(index);
    
    if (timeout == 0) {
      ti.setStatus(ThreadInfo.WAITING);
    } else {
      ti.setStatus(ThreadInfo.TIMEOUT_WAITING);
    }

    ThreadInfo[] lockedThreads = monitor.getLockedThreads();
    for (int i=0; i<lockedThreads.length; i++) {
      switch (lockedThreads[i].getStatus()) {
      case ThreadInfo.BLOCKED:
      case ThreadInfo.NOTIFIED:
        lockedThreads[i].setStatus(ThreadInfo.UNBLOCKED);
        break;
      case ThreadInfo.INTERRUPTED:
        lockedThreads[i].setStatus(ThreadInfo.UNBLOCKED_INTERRUPTED);
        break;        
      }
    }
    
//dumpMonitor();
//JVM.getVM().dumpThreadStates();
    
    ti.getVM().notifyObjectWait(ti, this);
  }

  /**
   * re-acquire lock after being notified. This is the notified thread, i.e. the one
   * that will come out of an wait()
   */
  public void lockNotified (ThreadInfo ti) {
    assert ti.isUnblocked() : "resume waiting thread " + ti.getName() + " which is not unblocked";
    
//System.out.println("@@ got notified " + this);

    // in case we get here from an UNBLOCKED_INTERRUPTED state, throwing the exception
    // has to be done from the WAIT insn, not here

    setMonitorWithoutLocked(ti);
    monitor.setLockingThread( ti);
    monitor.setLockCount( ti.getLockCount());
    
    ti.setStatus( ThreadInfo.RUNNING);
    ti.setLockCount(0);
    ti.resetLockRef();

    blockLockContenders();
    
    // pcm - this is important, if we later-on backtrack (reset the
    // ThreadInfo.lockedObjects set, and then restore from the saved heap), the
    // lock set would not include the lock when we continue to execute this thread
    ti.addLockedObject(this); //wv: add locked object back here

    
//dumpMonitor();
//JVM.getVM().dumpThreadStates();
  }

  void dumpMonitor () {
    PrintWriter pw = new PrintWriter(System.out, true);
    pw.print( "monitor ");
    //pw.print( mIndex);
    monitor.printFields(pw);
    pw.flush();
  }
  
  public boolean outOfBounds(int index) {
    if (!fields.isArray()) {
      throw new JPFException("object is not an array");
    }

    return (index < 0 || index >= fields.size());
  }

  /**
   * imperatively set GC status
   * 
   * @param keepAlive -
   *          true: keep alive no matter what, false: gc normally
   */
  public void pinDown(boolean keepAlive) {
    if (keepAlive) {
      attributes |= ATTR_PINDOWN;
    } else {
      attributes &= ~ATTR_PINDOWN;
    }
  }

  public void restoreFields(Fields f) {
    fields = f;
  }

  public Fields getFields() {
    return fields;
  }

  public void restoreMonitor(Monitor m) {
    monitor = m;
  }

  protected Monitor getMonitor() {
    return monitor;
  }

  public void restoreAttributes(int a) {
    attributes = a;
  }
  
  public void markUnchanged() {
    fChanged = false;
    mChanged = false;
  }
  
  /**
   * The various lock methods need access to a Ref object to do their work. The
   * subclass should return an appropriate type. This is a simple factory
   * method.
   * 
   * @return the right kind of Ref object for the given ElementInfo
   */
  protected abstract Ref getRef();

  protected Fields cloneFields() {
    if (fChanged) {
      return fields;
    }

    fChanged = true;
    area.markChanged(index);

    return fields = fields.clone();
  }

  /**
   * this has to be called every time we create a new monitor, so that we know it's
   * not yet stored (mIndex = -1), and memory has changed (area)
   */
  void resetMonitorIndex () {
    mChanged = true;
    area.markChanged(index);
  }
  
  /**
   * the setMonitorXX() calls have to preclude any monitor modification (changing
   * lockingThread, lockCount, or blocked/waiter threads, since we have to make
   * sure we don't modify an already pool-stored Monitor object
   * 
   * this is not very nice, but for the sake of consistency (ThreadData index,
   * Fields index etc.) we keep it. The plethora of setMonitorXX methods is due to
   * some optimization, since we don't want to first initialize, then clone,
   * and finally replace waiter/blocked arrays. Stupid thing is that is a Monitor
   * optimization which is just here because of the associated mIndex == -1 check
   */
  void setMonitor () {
    if (mChanged) {
      // nothing to do, use the existing monitor
    } else {
      resetMonitorIndex();
      monitor = monitor.clone();
    }
  }
  
  @Deprecated
  void setMonitorWithNoLocked () {
    if (mChanged) { // no need to clone, it hasn't been pooled yet
      monitor.resetLockedThreads();
    } else {
      resetMonitorIndex();
      monitor = monitor.cloneWithoutLocked();
    }
  }

  void setMonitorWithLocked( ThreadInfo ti) {
    if (mChanged) { // no need to clone, it hasn't been pooled yet
      monitor.addLocked(ti);
    } else {
      resetMonitorIndex();
      monitor = monitor.cloneWithLocked(ti);
    }
  }
  
  @Deprecated
  void setMonitorWithLocked (ThreadInfo[] ti) {
    if (mChanged) { // no need to clone, it hasn't been pooled yet
      monitor.setLocked(ti);
    } else {
      resetMonitorIndex();
      monitor = monitor.cloneWithLocked(ti);
    }
    
  }
  
  void setMonitorWithoutLocked (ThreadInfo ti) {
    if (mChanged) { // no need to clone, it hasn't been pooled yet
      monitor.removeLocked(ti);
    } else {
      resetMonitorIndex();
      monitor = monitor.cloneWithoutLocked(ti);
    }    
  }

  boolean isLockedBy(ThreadInfo ti) {
    return ((monitor != null) && (monitor.getLockingThread() == ti));
  }

  void _printAttributes(String cls, String msg, int oldAttrs) {
    if (getClassInfo().getName().equals(cls)) {
      System.out.println(msg + " " + this + " attributes: "
          + Integer.toHexString(attributes) + " was: "
          + Integer.toHexString(oldAttrs));
    }
  }
  
  /*
   * The following code is used to linearize a rooted structure in the heap
   */
  
  public Vector<String> linearize (Vector<String> result) {
    DynamicArea heap = DynamicArea.getHeap();
    int i, n;
    
    if (isArray()) {
      if (fields.isReferenceArray()) {
        n = fields.arrayLength();
        for (i=0; i<n; i++) {
          result = heap.linearize(fields.getIntValue(i),result);
        }
      }
    } else {
      ClassInfo ci = getClassInfo();
      do {
        n = ci.getNumberOfDeclaredInstanceFields();
        for (i=0; i<n; i++) {
          FieldInfo fi = ci.getDeclaredInstanceField(i);
          if (fi.isReference()) {
            if ((i == 0) && ci.isWeakReference()) {
              // we need to reset the ref field once the referenced object goes away
              // NOTE: only the *first* WeakReference field is a weak ref
              // (this is why we have our own implementation)

            	//dont' know what to do here?
            	return result;
            	
            } else {
              // the refAttrs are not immediately masked because we have to preserve
              // the mask values up to the point where we would promote a otherwise
              // unshared root object due to a different thread id (in case we didn't
              // catch a mask on the way that prevents this)
              result = heap.linearize(fields.getReferenceValue(fi.getStorageOffset()),result);
            }
          }
        }
        ci = ci.getSuperClass();
      } while (ci != null);
    }
    return result;
  }
  
  // for debugging
  void verifyLockInfo(ThreadList tl) {
    int i;
    
    ThreadInfo ti = monitor.getLockingThread();
    if (ti != null) {
      assert area.ks.tl.get(ti.index) == ti;
      assert ti.lockedObjects.contains(this);
    }
    
    if (monitor.hasLockedThreads()) {
      ThreadInfo[] lockedThreads = monitor.getLockedThreads();
      for (i=0; i<lockedThreads.length; i++) {
        ti = lockedThreads[i];
        assert area.ks.tl.get(ti.index) == ti;
        assert ti.lockRef == index;
      }
    }
  }
}

