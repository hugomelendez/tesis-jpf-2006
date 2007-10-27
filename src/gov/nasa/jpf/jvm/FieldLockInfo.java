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

/**
 * class encapsulating the lock protection detection for field access
 * instructions. Used by on-the-fly partial order reduction in FieldInstruction
 * to determine if a GET/PUT_FIELD/STATIC insn has to be treated as a
 * boundary step (terminates a transition). If the field access is always
 * protected by a lock, only the corresponding sync (INVOKExx or MONITORENTER)
 * are boundary steps, thus the number of states can be significantly reduced
 */
public abstract class FieldLockInfo implements Cloneable  {
  static protected final FieldLockInfo empty = new EmptyFieldLockInfo();
  
  public abstract FieldLockInfo checkProtection (ElementInfo ei, FieldInfo fi, ThreadInfo ti);
  public abstract boolean isProtected ();
  
  public abstract FieldLockInfo cleanUp ();
  
  /*
   * we need this for faster instantiation. Make sure it gets overridden in
   * case there is a need for per-instance parameterization
   */
  public Object clone () throws CloneNotSupportedException {
    return super.clone();
  }
}

/**
 * FieldLockSet implementation for fields that are terminally considered to be unprotected
 */
class EmptyFieldLockInfo extends FieldLockInfo {
  public FieldLockInfo checkProtection (ElementInfo ei, FieldInfo fi, ThreadInfo ti) {
    return this;
  }
    
  public FieldLockInfo cleanUp () {
    return this;
  }
  
  public boolean isProtected () {
    return false;
  }
}

