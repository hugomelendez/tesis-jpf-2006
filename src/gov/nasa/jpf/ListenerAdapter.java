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
package gov.nasa.jpf;

import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.VMListener;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListener;

/**
 * Adapter class that dummy implements both VMListener and SearchListener interfaces
 * Used to ease implementation of listeners that only process a few notifications
 */
public class ListenerAdapter implements VMListener, SearchListener {

  /* (non-Javadoc)
   * @see gov.nasa.jpf.VMListener#instructionExecuted(gov.nasa.jpf.VM)
   */
  public void instructionExecuted(JVM vm) {
  }

  public void executeInstruction(JVM vm) {
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.VMListener#threadStarted(gov.nasa.jpf.VM)
   */
  public void threadStarted(JVM vm) {
  }

  public void threadWaiting (JVM vm) {
  }

  public void threadNotified (JVM vm) {
  }

  public void threadInterrupted (JVM vm) {
  }

  public void threadScheduled (JVM vm) {
  }

  public void threadBlocked (JVM vm) {
  }
  
  /* (non-Javadoc)
   * @see gov.nasa.jpf.VMListener#threadTerminated(gov.nasa.jpf.VM)
   */
  public void threadTerminated(JVM vm) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.VMListener#classLoaded(gov.nasa.jpf.VM)
   */
  public void classLoaded(JVM vm) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.VMListener#objectCreated(gov.nasa.jpf.VM)
   */
  public void objectCreated(JVM vm) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.VMListener#objectReleased(gov.nasa.jpf.VM)
   */
  public void objectReleased(JVM vm) {
  }

  public void objectLocked (JVM vm) {
  }

  public void objectUnlocked (JVM vm) {
  }

  public void objectWait (JVM vm) {
  }

  public void objectNotify (JVM vm) {
  }

  public void objectNotifyAll (JVM vm) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.VMListener#gcBegin(gov.nasa.jpf.VM)
   */
  public void gcBegin(JVM vm) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.VMListener#gcEnd(gov.nasa.jpf.VM)
   */
  public void gcEnd(JVM vm) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.VMListener#exceptionThrown(gov.nasa.jpf.VM)
   */
  public void exceptionThrown(JVM vm) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.SearchListener#stateAdvanced(gov.nasa.jpf.Search)
   */
  public void stateAdvanced(Search search) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.SearchListener#stateProcessed(gov.nasa.jpf.Search)
   */
  public void stateProcessed(Search search) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.SearchListener#stateBacktracked(gov.nasa.jpf.Search)
   */
  public void stateBacktracked(Search search) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.SearchListener#stateRestored(gov.nasa.jpf.Search)
   */
  public void stateRestored(Search search) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.SearchListener#propertyViolated(gov.nasa.jpf.Search)
   */
  public void propertyViolated(Search search) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.SearchListener#searchStarted(gov.nasa.jpf.Search)
   */
  public void searchStarted(Search search) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.SearchListener#searchConstraintHit(gov.nasa.jpf.Search)
   */
  public void searchConstraintHit(Search search) {
  }

  /* (non-Javadoc)
   * @see gov.nasa.jpf.SearchListener#searchFinished(gov.nasa.jpf.Search)
   */
  public void searchFinished(Search search) {
  }

  public void choiceGeneratorSet (JVM vm) {
  }

  public void choiceGeneratorAdvanced (JVM vm) {
  }

  public void choiceGeneratorProcessed (JVM vm) {
  }

}
