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
package gov.nasa.jpf.search.heuristic;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.JVM;


/**
 * Heuristic to maximize thread interleavings. It is particularly good at
 * flushing out concurrency errors, since it schedules different threads 
 * as much as possible.
 * 
 */
public class Interleaving implements Heuristic {
  private int[]   threads;
  HeuristicSearch search;
  JVM              vm;
  int             threadHistoryLimit;

  public Interleaving (Config config, HeuristicSearch hSearch) {
    vm = hSearch.getVM();
    search = hSearch;
    
    threadHistoryLimit = config.getInt("search.heuristic.thread_history_limit", -1);
  }

  public int heuristicValue () {
    int aliveThreads = vm.getAliveThreadCount();

    int lastRun = vm.getLastTransition().getThread();
    int h_value = 0;

    if (aliveThreads > 1) {
      for (int i = 0; i < threads.length; i++) {
        if (lastRun == threads[i]) {
          h_value += ((threads.length - i) * aliveThreads);
        }
      }
    }

    int newSize = threads.length + 1;

    if ((threadHistoryLimit > 0) &&
            (newSize > threadHistoryLimit)) {
      newSize = threadHistoryLimit;
    }

    int[] newThreads = new int[newSize];
    newThreads[0] = lastRun;

    for (int i = 1; i < newSize; i++) {
      newThreads[i] = threads[i - 1];
    }

    search.getNew().otherData = newThreads;

    return h_value;
  }

  public void processParent () {
    Object oldValue = search.getOld().otherData;

    if (oldValue == null) {
      threads = new int[0];
    } else {
      threads = (int[]) oldValue;
    }
  }
}
