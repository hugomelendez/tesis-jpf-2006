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


import java.util.Collection;
import java.util.Iterator;

class CanonicalThreadSet extends CanonicalEqSet<Thread> implements SymThreadSet {
  public synchronized CanonicalThreadSet clone () {
    CanonicalThreadSet that = new CanonicalThreadSet();
    that.data = this.data;
    return that;
  }

  public synchronized void joinAll () throws InterruptedException {
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

  public synchronized void startAll () {
    ThreadSetOps.startAll(this);
  }

  public synchronized boolean add (Thread o) {
    return super.add(o);
  }

  public synchronized boolean addAll (Collection<? extends Thread> c) {
    return super.addAll(c);
  }

  public synchronized boolean addAll (SymEqCollection<? extends Thread> c) {
    return super.addAll(c);
  }

  public synchronized void clear () {
    super.clear();
  }

  public synchronized boolean contains (Object o) {
    return super.contains(o);
  }

  public synchronized boolean containsAll (Collection<?> c) {
    return super.containsAll(c);
  }

  public synchronized boolean containsAll (SymEqCollection<?> c) {
    return super.containsAll(c);
  }

  public synchronized boolean isEmpty () {
    return super.isEmpty();
  }

  public synchronized Iterator<Thread> iterator () {
    return clone().superIterator();
  }

  private Iterator<Thread> superIterator() {
    return super.iterator();
  }
  
  public synchronized boolean remove (Object o) {
    return super.remove(o);
  }

  public synchronized boolean removeAll (Collection<?> c) {
    return super.removeAll(c);
  }

  public synchronized boolean removeAll (SymEqCollection<?> c) {
    return super.removeAll(c);
  }

  public synchronized boolean retainAll (Collection<?> c) {
    return super.retainAll(c);
  }

  public synchronized boolean retainAll (SymEqCollection<?> c) {
    return super.retainAll(c);
  }

  public synchronized int size () {
    return super.size();
  }

  public synchronized Object[] toArray () {
    return super.toArray();
  }

  public synchronized <V> V[] toArray (V[] a) {
    return super.toArray(a);
  }
}
