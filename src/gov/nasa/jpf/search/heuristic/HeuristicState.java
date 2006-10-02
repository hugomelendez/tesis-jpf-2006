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

import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.VMState;


/**
 * wrapper for queuing states that are annotated by a priority that is
 * computed by a configured heuristic. The queue order corresponds to
 * the priorities  
 */
public class HeuristicState  {
  public          Object  otherData;
  protected final VMState virtualState;
  protected       int     priority;
  protected final int     uniqueID; // Used so restored states will be "closer."
  
  // see below
  //private int[][]   IndexIFMap = new int[1][1];
  //private java.util.Hashtable InstructionMap = new java.util.Hashtable();

  public HeuristicState (JVM vm, int initPriority) {
    uniqueID = vm.getStateId();
    virtualState = vm.getState();
    priority = initPriority;
  }
  
  public void updatePriority (int pr) {
    priority = pr;
  }

  public int getPriority () {
    return priority;
  }

  public VMState getVirtualState () {
    return virtualState;
  }

  public void restoreCoverage () {
    // <2do> adapt to listener coverage mnager (if not obsolete)
    //CoverageManager.setIndexIFMap(IndexIFMap);
    //CoverageManager.setInstructionMap(InstructionMap);
  }

  public void saveCoverage () {
    // <2do> adapt to listener coverage manager (if not obsolete)
    //IndexIFMap = CoverageManager.getIndexIFMap();
    //InstructionMap = CoverageManager.getInstructionMap();
  }

  public String toString () {
    return "hs" + priority;
  }
}
