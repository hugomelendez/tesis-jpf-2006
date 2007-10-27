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

import java.util.BitSet;

import gov.nasa.jpf.Config;

/**
 * Contains the list of all currently active threads.
 * 
 * Note that this list may both shrink or (re-) grow on backtrack. This imposes
 * a challenge for keeping ThreadInfo identities, which are otherwise nice for
 * directly storing ThreadInfo references in Monitors and/or listeners. 
 */
public class ThreadList {
  /**
   * The threads.
   */
  private ThreadInfo[] threads;

  /**
   * Reference of the kernel state this thread list belongs to.
   */
  public KernelState ks;

  /**
   * Creates a new empty thread list.
   */
  public ThreadList (Config config, KernelState ks) {
    this.ks = ks;
    threads = new ThreadInfo[0];
  }

  
  public int add (ThreadInfo ti) {
    int n = threads.length;
    ThreadInfo[] newList = new ThreadInfo[n+1];
    System.arraycopy(threads, 0, newList, 0, n);
    newList[n] = ti;

    threads = newList;
    
    return n; // the index where we added
  }
  

  public boolean anyAliveThread () {
    for (int i = 0, l = threads.length; i < l; i++) {
      if (threads[i].isAlive()) {
        return true;
      }
    }

    return false;
  }

  /**
   * Returns the array of threads.
   */
  public ThreadInfo[] getThreads() {
    return threads.clone();
  }
  
  /**
   * Returns a specific thread.
   */
  public ThreadInfo get (int index) {
    return threads[index];
  }

  /**
   * Returns the length of the list.
   */
  public int length () {
    return threads.length;
  }

  /**
   * Replaces the array of ThreadInfos.
   */
  public void setAll(ThreadInfo[] threads) {
    this.threads = threads;
  }
  
  public ThreadInfo locate (int objref) {
    for (int i = 0, l = threads.length; i < l; i++) {
      if (threads[i].getObjectReference() == objref) {
        return threads[i];
      }
    }

    return null;
  }

  public void markRoots () {
    for (int i = 0, l = threads.length; i < l; i++) {
      if (threads[i].isAlive()) {
        threads[i].markRoots();
      }
    }
  }

  public int getNonDaemonThreadCount () {
    int nd = 0;

    for (int i = 0; i < threads.length; i++) {
      if (!threads[i].isDaemon()) {
        nd++;
      }
    }

    return nd;
  }

  public int getRunnableThreadCount () {
    int n = 0;

    for (int i = 0; i < threads.length; i++) {
      if (threads[i].isRunnable()) {
        n++;
      }
    }

    return n;
  }
  
  public ThreadInfo[] getRunnableThreads() {
    int nRunnable = getRunnableThreadCount();
    ThreadInfo[] list = new ThreadInfo[nRunnable];
    
    for (int i = 0, j=0; i < threads.length; i++) {
      if (threads[i].isRunnable()) {
        list[j++] = threads[i];
        if (j == nRunnable) {
          break;
        }        
      }
    }

    return list;
  }
  
  public ThreadInfo[] getRunnableThreadsWith (ThreadInfo ti) {
    int nRunnable = getRunnableThreadCount();
    ThreadInfo[] list =  new ThreadInfo[ti.isRunnable() ? nRunnable : nRunnable+1];

    for (int i = 0, j=0; i < threads.length; i++) {
      if (threads[i].isRunnable() || (threads[i] == ti)) {
        list[j++] = threads[i];
        if (j == list.length) {
          break;
        }
      }
    }

    return list;
  }
  
  public ThreadInfo[] getRunnableThreadsWithout( ThreadInfo ti) {
    int nRunnable = getRunnableThreadCount();
    
    if (ti.isRunnable()) {
      nRunnable--;
    }
    ThreadInfo[] list = new ThreadInfo[nRunnable];
    
    for (int i = 0, j=0; i < threads.length; i++) {
      if (threads[i].isRunnable() && (ti != threads[i])) {
        list[j++] = threads[i];
        if (j == nRunnable) {
          break;
        }
      }
    }

    return list;
  }
  
  
  public int getLiveThreadCount () {
    int n = 0;

    for (int i = 0; i < threads.length; i++) {
      if (threads[i].isAlive()) {
        n++;
      }
    }

    return n;
  }  
  
  boolean hasOtherRunnablesThan (ThreadInfo ti) {
    int n = threads.length;
    
    for (int i=0; i<n; i++) {
      if (threads[i] != ti) {
        if (threads[i].isRunnable()) {
          return true;
        }
      }
    }
    
    return false;
  }


  public void sweepTerminated(BitSet isUsed) {
    // illegal?
    /*
    ThreadInfo[] newThreads = threads;
    for (int i = 0; i < threads.length; i++) {
      ThreadInfo ti = threads[i];
      if (ti.threadData.status == ThreadInfo.TERMINATED &&
          !isUsed.get(ti.threadData.objref)) {
        newThreads = Monitor.remove(newThreads, ti);
        ks.changed();
      }
    }
    threads = newThreads;
    */
  }
}
