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
package gov.nasa.jpf.tools;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.DynamicArea;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.DynamicObjectArray;
import gov.nasa.jpf.util.SourceRef;

import java.util.Stack;
import java.util.regex.Pattern;

/**
 * HeapTracker - property-listener class to check heap utilization along all
 * execution paths (e.g. to verify heap bounds)
 */
public class HeapTracker extends PropertyListenerAdapter {
  
  static class PathStat implements Cloneable {
    int nNew = 0;
    int nReleased = 0;
    int heapSize = 0;  // in bytes
    
    public Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        return null;
      }
    }
  }
      
  PathStat stat = new PathStat();
  Stack<PathStat> pathStats = new Stack<PathStat>();
  
  DynamicObjectArray<SourceRef> loc = new DynamicObjectArray<SourceRef>();
  
  int maxState;
  int nForward;
  int nBacktrack;
  
  int nElemTotal;
  int nGcTotal;
  int nSharedTotal;
  int nImmutableTotal;
  
  int nElemMax = Integer.MIN_VALUE;
  int nElemMin = Integer.MAX_VALUE;
  int nElemAv;
  
  int pElemSharedMax = Integer.MIN_VALUE;
  int pElemSharedMin = Integer.MAX_VALUE;
  int pElemSharedAv;
  
  int pElemImmutableMax = Integer.MIN_VALUE;
  int pElemImmutableMin = Integer.MAX_VALUE;
  int pElemImmutableAv;
  
  int nReleased;
  int nReleasedTotal;
  int nReleasedAv;
  int nReleasedMax = Integer.MIN_VALUE;
  int nReleasedMin = Integer.MAX_VALUE;
  
  int maxPathHeap = Integer.MIN_VALUE;
  int maxPathNew = Integer.MIN_VALUE;
  int maxPathReleased = Integer.MIN_VALUE;
  int maxPathAlive = Integer.MIN_VALUE;

  int initHeap = 0;
  int initNew = 0;
  int initReleased = 0;
  int initAlive = 0;

  // used as a property check
  int maxHeapSizeLimit;
  int maxLiveLimit;
  boolean throwOutOfMemory = false;
  Pattern classPattern = null;
  
  void updateMaxPathValues() {
      if (stat.heapSize > maxPathHeap) {
        maxPathHeap = stat.heapSize;
      }
      
      if (stat.nNew > maxPathNew) {
        maxPathNew = stat.nNew;
      }
      
      if (stat.nReleased > maxPathReleased) {
        maxPathReleased = stat.nReleased;
      }
      
      int nAlive = stat.nNew - stat.nReleased;
      if (nAlive > maxPathAlive) {
        maxPathAlive = nAlive;
      }
  }

  public HeapTracker (Config config) {
    maxHeapSizeLimit = config.getInt("heap.size_limit", -1);
    maxLiveLimit = config.getInt("heap.live_limit", -1);
    throwOutOfMemory = config.getBoolean("heap.throw_exception");
    
    String regEx = config.getString("heap.classes");
    if (regEx != null) {
      classPattern = Pattern.compile(regEx);
    }
  }
  
  /******************************************* abstract Property *****/

  /**
   * return 'false' if property is violated
   */
  public boolean check (Search search, JVM vm) {
    if (throwOutOfMemory) {
      // in this case we don't want to stop the program, but see if it
      // behaves gracefully - don't report a property violation
      return true;
    } else {
      if ((maxHeapSizeLimit >= 0) && (stat.heapSize > maxHeapSizeLimit)) {
        return false;
      }
      if ((maxLiveLimit >=0) && ((stat.nNew - stat.nReleased) > maxLiveLimit)) {
        return false;
      }
      
      return true;
    }
  }

  public String getErrorMessage () {
    return "heap limit exceeded: " + stat.heapSize + " > " + maxHeapSizeLimit;
  }
  
  /******************************************* SearchListener interface *****/
  public void searchStarted(Search search) {
    super.searchStarted(search);
    
    updateMaxPathValues();
    pathStats.push(stat);
    
    initHeap = stat.heapSize;
    initNew = stat.nNew;
    initReleased = stat.nReleased;
    initAlive = initNew - initReleased;
    
    stat = (PathStat)stat.clone();
  }
  
  public void stateAdvanced(Search search) {
    
    if (search.isNewState()) {
      int id = search.getStateNumber();

      if (id > maxState) maxState = id;
      
      updateMaxPathValues();
      pathStats.push(stat);
      stat = (PathStat)stat.clone();
      
      nForward++;
    }
  }
  
  public void stateBacktracked(Search search) {
    nBacktrack++;
    
    if (!pathStats.isEmpty()){
      stat = pathStats.pop();
    }
  }
  
  public void searchFinished(Search search) {
    System.out.println("heap statistics:");
    System.out.println("  states:         " + maxState);
    System.out.println("  forwards:       " + nForward);
    System.out.println("  backtrack:      " + nBacktrack);
    System.out.println();
    System.out.println("  gc cycles:      " + nGcTotal);
    System.out.println();
    System.out.println("  max Objects:    " + nElemMax);
    System.out.println("  min Objects:    " + nElemMin);
    System.out.println("  avg Objects:    " + nElemAv);
    System.out.println();
    System.out.println("  max% shared:    " + pElemSharedMax);
    System.out.println("  min% shared:    " + pElemSharedMin);
    System.out.println("  avg% shared:    " + pElemSharedAv);
    System.out.println();
    System.out.println("  max% immutable: " + pElemImmutableMax);
    System.out.println("  min% immutable: " + pElemImmutableMin);
    System.out.println("  avg% immutable: " + pElemImmutableAv);
    System.out.println();
    System.out.println("  max released:   " + nReleasedMax);
    System.out.println("  min released:   " + nReleasedMin);
    System.out.println("  avg released:   " + nReleasedAv);
    
    System.out.println();
    System.out.print(  "  max path heap (B):   " + maxPathHeap);
    System.out.println(" / " + (maxPathHeap - initHeap));
    System.out.print(  "  max path alive:      " + maxPathAlive);
    System.out.println(" / " + (maxPathAlive - initAlive));
    System.out.print(  "  max path new:        " + maxPathNew);
    System.out.println(" / " + (maxPathNew - initNew));
    System.out.print(  "  max path released:   " + maxPathReleased);
    System.out.println(" / " + (maxPathReleased - initReleased));
  }
  
  
  /******************************************* VMListener interface *********/
  public void gcBegin(JVM vm) {
    /**
     System.out.println();
     System.out.println( "----- gc cycle: " + jvm.getDynamicArea().getGcNumber()
     + ", state: " + jvm.getStateId());
     **/
  }
  
  public void gcEnd(JVM jvm) {
    DynamicArea da = jvm.getDynamicArea();
    DynamicArea.Iterator it = da.iterator();
    
    int n = 0;
    int nShared = 0;
    int nImmutable = 0;
    
    while (it.hasNext()) {
      ElementInfo ei = it.next();
      n++;
      
      if (ei.isShared()) nShared++;
      if (ei.isImmutable()) nImmutable++;
      
      //printElementInfo(ei);
    }
    
    nElemTotal += n;
    nGcTotal++;
    
    if (n > nElemMax) nElemMax = n;
    if (n < nElemMin) nElemMin = n;
    
    int pShared = (nShared * 100) / n;
    int pImmutable = (nImmutable * 100) / n;
    
    if (pShared > pElemSharedMax) pElemSharedMax = pShared;
    if (pShared < pElemSharedMin) pElemSharedMin = pShared;
    
    nSharedTotal += nShared;
    nImmutableTotal += nImmutable;
    
    pElemSharedAv = (nSharedTotal * 100) / nElemTotal;
    pElemImmutableAv = (nImmutableTotal * 100) / nElemTotal;
    
    if (pImmutable > pElemImmutableMax) pElemImmutableMax = pImmutable;
    if (pImmutable < pElemImmutableMin) pElemImmutableMin = pImmutable;
    
    nElemAv = nElemTotal / nGcTotal;
    nReleasedAv = nReleasedTotal / nGcTotal;
    
    if (nReleased > nReleasedMax) nReleasedMax = nReleased;
    if (nReleased < nReleasedMin) nReleasedMin = nReleased;
    
    nReleased = 0;
  }

  boolean isRelevantType (ElementInfo ei) {
    if (classPattern == null) return true;
    
    return classPattern.matcher(ei.getClassInfo().getName()).matches();
  }
  
  public void objectCreated(JVM jvm) {
    ElementInfo ei = jvm.getLastElementInfo();
    int idx = ei.getIndex();
    ThreadInfo ti = jvm.getLastThreadInfo();
    int line = ti.getLine();
    MethodInfo mi = ti.getMethod();
    SourceRef sr = null;
    
    if (!isRelevantType(ei)) {
      return;
    }
    
    if (mi != null) {
      String file = mi.getClassInfo().getSourceFileName();
      
      if (file != null) {
        sr = new SourceRef(file, line);
      } else {
        ClassInfo ci = ti.getMethod().getClassInfo();
        sr = new SourceRef(ci.getName(), line);
      }
    }
    
    loc.set(idx, sr);
    
    stat.nNew++;
    stat.heapSize += ei.getHeapSize();
    
    // check if we should simulate an OutOfMemoryError
    if (throwOutOfMemory) {
      if (((maxHeapSizeLimit >=0) && (stat.heapSize > maxHeapSizeLimit)) ||
          ((maxLiveLimit >=0) && ((stat.nNew - stat.nReleased) > maxLiveLimit))){
        DynamicArea.getHeap().setOutOfMemory(true);
      }
    }
  }
  
  public void objectReleased(JVM jvm) {
    ElementInfo ei = jvm.getLastElementInfo();
    
    if (!isRelevantType(ei)) {
      return;
    }
    
    nReleasedTotal++;
    nReleased++;
    
    stat.nReleased++;
    stat.heapSize -= ei.getHeapSize();
  }
  
  /****************************************** private stuff ******/
  protected void printElementInfo(ElementInfo ei) {
    boolean first = false;
    
    System.out.print( ei.getIndex());
    System.out.print( ": ");
    System.out.print( ei.getClassInfo().getName());
    System.out.print( "  [");
    
    if (ei.isShared()) {
      System.out.print( "shared");
      first = false;
    }
    if (ei.isImmutable()) {
      if (!first) System.out.print(' ');
      System.out.print( "immutable");
    }
    System.out.print( "] ");
    
    SourceRef sr = loc.get(ei.getIndex());
    if (sr != null) {
      System.out.println(sr);
    } else {
      System.out.println("?");
    }
  }
  
  
  static void printUsage () {
    System.out.println("HeapTracker - a JPF listener tool to report and check heap utilization");
    System.out.println("usage: java gov.nasa.jpf.tools.HeapTracker <jpf-options> <heapTracker-options> <class>");
    System.out.println("       +heap.size_limit=<num> : report property violation if heap exceeds <num> bytes");
    System.out.println("       +heap.live_limit=<num> : report property violation if more than <num> live objects");
    System.out.println("       +heap.classes=<regEx> : only report instances of classes matching <regEx>");
    System.out.println("       +heap.throw_exception=<bool>: throw a OutOfMemoryError instead of reporting property violation");
  }

  
  public static void main (String[] args) {
    if (args.length == 0) {
      printUsage();
      return;
    }
    Config conf = JPF.createConfig(args);
    // set own options here..
    
    HeapTracker listener = new HeapTracker(conf);    
    
    JPF jpf = new JPF(conf);
    jpf.addSearchListener(listener);
    jpf.addVMListener(listener);

    if (listener.maxHeapSizeLimit >= 0) {
      jpf.addSearchProperty(listener);
    }
    
    jpf.run();
  }
}

