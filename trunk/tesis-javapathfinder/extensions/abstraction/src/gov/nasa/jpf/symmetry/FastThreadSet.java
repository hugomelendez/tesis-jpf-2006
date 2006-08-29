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


class FastThreadSet extends FastEqSet<Thread> implements SymThreadSet {
  public FastThreadSet() {
    super();
  }
  
  public FastThreadSet(int pow) {
    super(pow);
  }
  
  public void joinAll () throws InterruptedException {
    ThreadSetOps.joinAll(this);
  }

  public void joinAllUninterrupted() {
    boolean interrupted;
    do {    
      try {
        ThreadSetOps.joinAll(this);
        interrupted = false;
      } catch (InterruptedException e) {
        interrupted = true;
      }
    } while( interrupted) ;
  }
  
  public void startAll () {
    ThreadSetOps.startAll(this);
  }
  
  public FastThreadSet clone() {
    FastThreadSet that = new FastThreadSet(tblPow);
    for (Thread o : this) {
      that.strictAdd(o);
    }
    return that;
  }
}
