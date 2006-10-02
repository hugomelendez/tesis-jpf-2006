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
 * MJIEnv is the call environment for "native" methods, i.e. code that
 * is executed by the JVM, not by JPF.
 *
 * Since library abstractions are supposed to be "user code", we provide
 * this class as a (little bit of) insulation towards the inner JPF workings.
 *
 * There are two APIs exported by this class. The public methods (like
 * getStringObject) don't expose JPF internals, and can be used from non
 * gov.nasa.jpf.jvm NativePeer classes). The rest is package-default
 * and can be used to fiddle around as much as you like to (if you are in
 * the ..jvm package)
 * 
 * Note that MJIEnv objects are now per-ThreadInfo (i.e. the variable
 * call envionment only includes MethodInfo and ClassInfo), which means
 * MJIEnv can be used in non-native methods (but only carefully, if you
 * don't need mi or ci)
 */
public class MJIEnv {
  public static final int NULL = -1;
  
  JVM                     vm;
  ClassInfo               ci;
  MethodInfo              mi;
  ThreadInfo              ti;
  DynamicArea             da;
  StaticArea              sa;
  boolean                 repeat;
  String                  exception;
  String                  exceptionDetails;

  MJIEnv (ThreadInfo ti) {
    this.ti = ti;
    
    // set those here so that we don't have an inconsistent state between
    // creation of an MJI object and the first native method call in
    // this thread (where any access to the da or sa would bomb)
    vm = ti.getVM();
    da = vm.getDynamicArea();
    sa = vm.getStaticArea();
  }

  public JVM getVM () {
    return vm;
  }
  
  public boolean isArray (int objref) {
    return da.get(objref).isArray();
  }

  public int getArrayLength (int objref) {
    if (isArray(objref)) {
      return da.get(objref).arrayLength();
    } else {
      throwException("java.lang.IllegalArgumentException");

      return 0;
    }
  }

  public String getArrayType (int objref) {
    return da.get(objref).getArrayType();
  }

  public int getArrayTypeSize (int objref) {
    return Types.getTypeSize(getArrayType(objref));
  }

  // the instance field setters
  public void setBooleanField (int objref, String fname, boolean val) {
    setIntField(objref, fname, Types.booleanToInt(val));
  }

  public boolean getBooleanField (int objref, String fname) {
    return Types.intToBoolean(getIntField(objref, fname));
  }

  public void setByteField (int objref, String fname, byte val) {
    setIntField(objref, fname, /*(int)*/ val);
  }

  public byte getByteField (int objref, String fname) {
    return (byte) getIntField(objref, fname);
  }

  public void setCharField (int objref, String fname, char val) {
    setIntField(objref, fname, /*(int)*/ val);
  }

  public char getCharField (int objref, String fname) {
    return (char) getIntField(objref, fname);
  }

  public void setDoubleField (int objref, String fname, double val) {
    setLongField(objref, fname, Types.doubleToLong(val));
  }

  public double getDoubleField (int objref, String fname) {
    return Types.longToDouble(getLongField(objref, fname));
  }

  public void setFloatField (int objref, String fname, float val) {
    setIntField(objref, fname, Types.floatToInt(val));
  }

  public float getFloatField (int objref, String fname) {
    return Types.intToFloat(getIntField(objref, fname));
  }

  public void setByteArrayElement (int objref, int index, byte value) {
    da.get(objref).setElement(index, value);
  }

  public void setCharArrayElement (int objref, int index, char value) {
    da.get(objref).setElement(index, value);
  }
  
  public void setIntArrayElement (int objref, int index, int value) {
    da.get(objref).setElement(index, value);
  }

  public int getIntArrayElement (int objref, int index) {
    return da.get(objref).getElement(index);
  }

  public char getCharArrayElement (int objref, int index) {
    return (char) da.get(objref).getElement(index);
  }
  
  public void setIntField (int objref, String fname, int val) {
    ElementInfo ei = da.get(objref);
    ei.setIntField(fname, val);
  }

  // these two are the workhorses
  public void setDeclaredIntField (int objref, String refType, String fname, int val) {
    ElementInfo ei = da.get(objref);
    ei.setDeclaredIntField(fname, refType, val);
  }

  public int getIntField (int objref, String fname) {
    ElementInfo ei = da.get(objref);
    return ei.getIntField(fname);
  }

  public int getDeclaredIntField (int objref, String refType, String fname) {
    ElementInfo ei = da.get(objref);
    return ei.getDeclaredIntField(fname, refType);
  }

  // these two are the workhorses
  public void setDeclaredReferenceField (int objref, String refType, String fname, int val) {
    ElementInfo ei = da.get(objref);
    ei.setDeclaredReferenceField(fname, refType, val);
  }
  
  public void setReferenceField (int objref, String fname, int ref) {
     ElementInfo ei = da.get(objref);
     ei.setReferenceField(fname, ref);
  }

  public int getReferenceField (int objref, String fname) {
    return getIntField(objref, fname);
  }


  // the box object accessors (should probably test for the appropriate class)
  public boolean getBooleanValue (int objref) {
    return getBooleanField(objref, "value");
  }
  
  public byte getByteValue (int objref) {
    return getByteField(objref, "value");
  }
  
  public char getCharValue (int objref) {
    return getCharField(objref, "value");
  }
  
  public short getShortValue (int objref) {
    return getShortField(objref, "value");
  }
  
  public int getIntValue (int objref) {
    return getIntField(objref, "value");
  }
  
  public long getLongValue (int objref) {
    return getLongField(objref, "value");
  }
  
  public float getFloatValue (int objref) {
    return getFloatField(objref, "value");
  }
  
  public double getDoubleValue (int objref) {
    return getDoubleField(objref, "value");
  }
  
  
  public void setLongArrayElement (int objref, int index, long value) {
    da.get(objref).setLongElement(index, value);
  }

  public long getLongArrayElement (int objref, int index) {
    return da.get(objref).getLongElement(index);
  }

  public void setLongField (int objref, String fname, long val) {
    ElementInfo ei = da.get(objref);
    ei.setLongField(fname, val);
  }

//  public void setLongField (int objref, String refType, String fname, long val) {
//    ElementInfo ei = da.get(objref);
//    ei.setLongField(fname, refType, val);
//  }

  public long getLongField (int objref, String fname) {
    ElementInfo ei = da.get(objref);
    return ei.getLongField(fname);
  }

//  public long getLongField (int objref, String refType, String fname) {
//    ElementInfo ei = da.get(objref);
//    return ei.getLongField(fname, refType);
//  }

  public void setReferenceArrayElement (int objref, int index, int eRef) {
    da.get(objref).setElement(index, eRef);
  }

  public int getReferenceArrayElement (int objref, int index) {
    return da.get(objref).getElement(index);
  }

  public void setShortField (int objref, String fname, short val) {
    setIntField(objref, fname, /*(int)*/ val);
  }

  public short getShortField (int objref, String fname) {
    return (short) getIntField(objref, fname);
  }

  public String getTypeName (int objref) {
    return da.get(objref).getType();
  }
  
  public boolean isInstanceOf (int objref, String clsName) {
    ClassInfo ci = getClassInfo(objref);
    return ci.instanceOf(clsName);
  }
  
  // the static field setters
  public void setStaticBooleanField (String clsName, String fname,
                                     boolean value) {
    setStaticIntField(clsName, fname, Types.booleanToInt(value));
  }

  public boolean getStaticBooleanField (String clsName, String fname) {
    return Types.intToBoolean(getStaticIntField(clsName, fname));
  }

  public void setStaticByteField (String clsName, String fname, byte value) {
    setStaticIntField(clsName, fname, /*(int)*/ value);
  }

  public byte getStaticByteField (String clsName, String fname) {
    return (byte) getStaticIntField(clsName, fname);
  }

  public void setStaticCharField (String clsName, String fname, char value) {
    setStaticIntField(clsName, fname, /*(int)*/ value);
  }

  public char getStaticCharField (String clsName, String fname) {
    return (char) getStaticIntField(clsName, fname);
  }

  public void setStaticDoubleField (String clsName, String fname, double val) {
    setStaticLongField(clsName, fname, Types.doubleToLong(val));
  }

  public double getStaticDoubleField (String clsName, String fname) {
    return Types.longToDouble(getStaticLongField(clsName, fname));
  }

  public void setStaticFloatField (String clsName, String fname, float value) {
    setStaticIntField(clsName, fname, Types.floatToInt(value));
  }

  public float getStaticFloatField (String clsName, String fname) {
    return Types.intToFloat(getStaticIntField(clsName, fname));
  }

  public void setStaticIntField (String clsName, String fname, int value) {
    ClassInfo ci = ClassInfo.getClassInfo(clsName);
    sa.get(ci.getName()).setIntField(fname, value);
  }
  
  public void setStaticIntField (int clsObjRef, String fname, int val) {
    try {
      ElementInfo cei = getClassElementInfo(clsObjRef);
      cei.setIntField(fname, val);
    } catch (Throwable x) {
      throw new JPFException("set static field failed: " + x.getMessage());
    }
  }

  public int getStaticIntField (String clsName, String fname) {
    ClassInfo         ci = ClassInfo.getClassInfo(clsName);
    StaticElementInfo ei = sa.get(ci.getName());

    return ei.getIntField(fname);
  }

  public void setStaticLongField (String clsName, String fname, long value) {
    ClassInfo ci = ClassInfo.getClassInfo(clsName);
    sa.get(ci.getName()).setLongField(fname, value);
  }

  public long getStaticLongField (String clsName, String fname) {
    ClassInfo         ci = ClassInfo.getClassInfo(clsName);
    StaticElementInfo ei = sa.get(ci.getName());

    return ei.getLongField(fname);
  }

  public void setStaticReferenceField (String clsName, String fname, int objref) {
    ClassInfo ci = ClassInfo.getClassInfo(clsName);

    // <2do> - we should REALLY check for type compatibility here
    sa.get(ci.getName()).setReferenceField(fname, objref);
  }

  public int getStaticObjectField (String clsName, String fname) {
    return getStaticIntField(clsName, fname);
  }

  public short getStaticShortField (String clsName, String fname) {
    return (short) getStaticIntField(clsName, fname);
  }

  /**
   * turn JPF String object into a JVM String object
   * (this is a method available for non gov..jvm NativePeer classes)
   */
  public String getStringObject (int objref) {
    if (objref != -1) {
      ElementInfo ei = getElementInfo(objref);
      return ei.asString();
    } else {
      return null;
    }
  }

  public byte[] getByteArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    byte[] a = ei.asByteArray();
    
    return a;
  }
  
  public char[] getCharArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    char[] a = ei.asCharArray();
    
    return a;
  }
  
  public short[] getShortArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    short[] a = ei.asShortArray();
    
    return a;
  }

  public int[] getIntArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    int[] a = ei.asIntArray();
    
    return a;
  }

  public long[] getLongArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    long[] a = ei.asLongArray();
    
    return a;
  }

  public float[] getFloatArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    float[] a = ei.asFloatArray();
    
    return a;
  }

  public double[] getDoubleArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    double[] a = ei.asDoubleArray();
    
    return a;
  }

  public boolean[] getBooleanArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    boolean[] a = ei.asBooleanArray();
    
    return a;
  }

  
  public boolean canLock (int objref) {
    ElementInfo ei = getElementInfo(objref);

    return ei.canLock(ti);
  }

  public int newBooleanArray (int size) {
    return da.newArray("Z", size, ti);
  }

  public int newByteArray (int size) {
    return da.newArray("B", size, ti);
  }

  public int newCharArray (int size) {
    return da.newArray("C", size, ti);
  }

  public int newDoubleArray (int size) {
    return da.newArray("D", size, ti);
  }

  public int newFloatArray (int size) {
    return da.newArray("F", size, ti);
  }

  public int newIntArray (int size) {
    return da.newArray("I", size, ti);
  }

  public int newLongArray (int size) {
    return da.newArray("J", size, ti);
  }

  /**
   * watch out - we don't check if the class is initialized, since the
   * caller would have to take appropriate action anyways
   */
  public int newObject (ClassInfo ci) {
    return da.newObject(ci, ti);
  }

  public int newObjectArray (String elementClsName, int size) {
    ClassInfo ci = ClassInfo.getClassInfo("[" + elementClsName);
    ci.loadAndInitialize(ti);
    
    return da.newArray(elementClsName, size, ti);
  }

  public int newShortArray (int size) {
    return da.newArray("S", size, ti);
  }

  public int newString (String s) {
    return da.newString(s, ti);
  }

  public int newString (int arrayRef) {
    String t = getArrayType(arrayRef);
    String s = null;
    
    if ("C".equals(t)) {          // character array
      char[] ca = getCharArrayObject(arrayRef);
      s = new String(ca);
    } else if ("B".equals(t)) {   // byte array
      byte[] ba = getByteArrayObject(arrayRef);
      s = new String(ba);
    }
    
    if (s == null) {
      return NULL;
    }
    
    return newString(s);
  }
  
  public void notify (int objref) {
    // objref can't be NULL since the corresponding INVOKE would have failed
    ElementInfo ei = getElementInfo(objref);
    
    if (!ei.isLockedBy(ti)){
      throwException("java.lang.IllegalMonitorStateException",
                                 "un-synchronized notify");
      return;
    }
    
    ei.notifies(getSystemState(), ti);
  }

  public void notifyAll (int objref) {
    // objref can't be NULL since the corresponding INVOKE would have failed
    ElementInfo ei = getElementInfo(objref);

    if (!ei.isLockedBy(ti)){
      throwException("java.lang.IllegalMonitorStateException",
                                 "un-synchronized notifyAll");
      return;
    }

    ei.notifiesAll();
  }

  /**
   * repeat execution of the InvokeInstruction that caused a native method call
   * NOTE - this does NOT mean it's the NEXT executed insn, since the native method
   * might have pushed direct call frames on the stack before asking us to repeat it
   */
  public void repeatInvocation () {
    repeat = true;
  }

  public void throwException (String classname) {
    exception = Types.asTypeName(classname);
  }

  public void throwException (String classname, String details) {
    exception = Types.asTypeName(classname);
    exceptionDetails = details;
  }

  public void wait (int objref, long timeout) {    
    // objref can't be NULL since the corresponding INVOKE would have failed
    ElementInfo ei = getElementInfo(objref);
    
    if (!ei.isLockedBy(ti)){
      throwException("java.lang.IllegalMonitorStateException",
                                 "un-synchronized wait");
      return;
    }

    
    ei.wait(ti, timeout);
  }

  void setCallEnvironment (MethodInfo mi) {
    this.mi = mi;
    
    if (mi != null){
      ci = mi.getClassInfo();
    } else {
      //ci = null;
      //mi = null;
    }

    repeat = false;
    exception = null;
    exceptionDetails = null;
  }
  
  void clearCallEnvironment () {
    setCallEnvironment(null);
  }

  ElementInfo getClassElementInfo (int clsObjRef) {
    ElementInfo ei = da.get(clsObjRef);
    int         cref = ei.getIntField("cref");

    ElementInfo cei = sa.get(cref);

    return cei;
  }

  ClassInfo getClassInfo () {
    return ci;
  }

  ClassInfo getClassInfo (int objref) {
    ElementInfo ei = getElementInfo(objref);
    return ei.getClassInfo();
  }

  public String getClassName (int objref) {
    return getClassInfo(objref).getName();
  }
  
  DynamicArea getDynamicArea () {
    return JVM.getVM().getDynamicArea();
  }

  ElementInfo getElementInfo (int objref) {
    return da.get(objref);
  }

  int getStateId () {
    return JVM.getVM().getStateId();
  }
  
  String getException () {
    return exception;
  }

  String getExceptionDetails () {
    return exceptionDetails;
  }

  KernelState getKernelState () {
    return JVM.getVM().getKernelState();
  }

  MethodInfo getMethodInfo () {
    return mi;
  }

  boolean getRepeat () {
    return repeat;
  }

  StaticArea getStaticArea () {
    return JVM.getVM().getStaticArea();
  }

  SystemState getSystemState () {
    return JVM.getVM().getSystemState();
  }

  ThreadInfo getThreadInfo () {
    return ti;
  }

  // <2do> - naming? not very intuitive
  void lockNotified (int objref) {
    ElementInfo ei = getElementInfo(objref);
    ei.lockNotified(ti);
  }
}
