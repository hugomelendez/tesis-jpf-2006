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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.Debug;
import gov.nasa.jpf.util.HashData;

import java.util.Stack;


/**
 * This class represents the stack of the JVM.
 */
public class KernelState {
  /**
   * The area containing the classes.
   */
  public StaticArea sa;

  /**
   * The area containing the objects.
   */
  public DynamicArea da;

  /**
   * The list of the threads.
   */
  public ThreadList tl;

  /**
   * current listeners waiting for notification of next change.
   */
  private Stack<ChangeListener> listeners = new Stack<ChangeListener>();
  
  /**
   * Creates a new kernel state object.
   */
  public KernelState (Config config) {
    sa = new StaticArea(config,this);
    da = new DynamicArea(config,this);
    tl = new ThreadList(config,this);
  }

  /**
   * interface for getting notified of changes to KernelState and everything
   * "below" it.
   */
  public interface ChangeListener {
    void kernelStateChanged(KernelState ks);
  }

  /**
   * called by internals to indicate a change in KernelState.  list of listeners
   * is emptied.
   */  
  public void changed() {
    while (!listeners.empty()) {
      listeners.pop().kernelStateChanged(this);
    }
  }

  /**
   * push a listener for notification of the next change.  further notification
   * requires re-pushing.
   */
  public void pushChangeListener(ChangeListener cl) {
    if (cl instanceof IncrementalChangeTracker && listeners.size() > 0) {
      for (ChangeListener l : listeners) {
        if (l instanceof IncrementalChangeTracker) {
          throw new IllegalStateException("Only one IncrementalChangeTracker allowed!");
        }
      }
    }
    listeners.push(cl);
  }
  
  /**
   * The program is terminated if there are no alive threads.
   */
  public boolean isTerminated () {
    return !tl.anyAliveThread();
  }

  public int getThreadCount () {
    return tl.length();
  }

  @Deprecated
  public ThreadInfo getThreadInfo (int index) {
    return tl.get(index);
  }

  
  public void gc () {
    da.gc();
    
    // we might have stored stale references in live objects
    da.cleanUpDanglingReferences();
    sa.cleanUpDanglingReferences();
  }

  public void hash (HashData hd) {
    da.hash(hd);
    sa.hash(hd);

    for (int i = 0, l = tl.length(); i < l; i++) {
      tl.get(i).hash(hd);
    }
  }

  public ThreadList getThreadList () {
    return tl;
  }
  
  public void log () {
    da.log();
    sa.log();

    for (int i = 0; i < tl.length(); i++) {
      tl.get(i).log();
    }

    Debug.println(Debug.MESSAGE);
  }
  
}
