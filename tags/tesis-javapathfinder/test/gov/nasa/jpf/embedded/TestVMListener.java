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
package gov.nasa.jpf.embedded;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.VMListener;


/**
 * TestJVMListener - unit test of the JVMListener interface
 */
public class TestVMListener implements VMListener {
  
  public void executeInstruction (JVM vm) {
  }
  
  /**
   * JVM has executed next instruction
   * (can be used to analyze branches, monitor PUTFIELD / GETFIELD and
   * INVOKExx / RETURN instructions)
   */
  public void instructionExecuted(JVM vm) {
    //JJVM jvm = (JJVM) vm;
    //ThreadInfo ti = jvm.getLastThreadInfo();
    //Instruction insn = jvm.getLastInstruction();
    //System.out.println("Instruction executed: " + ti.getIndex() + " : " + insn);
  }
  
  /**
   * new class was loaded
   */
  public void classLoaded(JVM vm) {
    // TODO
  }
  
  /**
   * exception was thrown
   */
  public void exceptionThrown(JVM vm) {
    // TODO
  }

  public void threadWaiting (JVM vm) {
    // TODO
  }

  public void threadNotified (JVM vm) {
    // TODO
  }

  public void threadInterrupted (JVM vm) {
    // TODO
  }

  public void threadScheduled (JVM vm) {
    // TODO
  }

  public void objectLocked (JVM vm) {
    // TODO
  }

  public void objectUnlocked (JVM vm) {
    // TODO
  }

  public void objectWait (JVM vm) {
    // TODO
  }

  public void objectNotify (JVM vm) {
    // TODO
  }

  public void objectNotifyAll (JVM vm) {
    // TODO
  }
  
  /**
   * new object was created
   */
  public void objectCreated(JVM jvm) {
    ThreadInfo ti = jvm.getLastThreadInfo();
    ElementInfo ei = jvm.getLastElementInfo();
    System.out.println("objectCreated: " + ti.getIndex() + ", " +
                      ei.getClassInfo().getName() + " = " + ei.getIndex());
  }
  
  /**
   * object was garbage collected (after potential finalization)
   */
  public void objectReleased(JVM jvm) {
    ElementInfo ei = jvm.getLastElementInfo();
    System.out.println("objectReleased: " + ei.getClassInfo().getName() + " = " + ei.getIndex());
  }

  public void gcBegin(JVM jvm) {
    System.out.println("gcBegin");
  }
  
  public void gcEnd(JVM jvm) {
    System.out.println("gcEnd");
  }
  
  /**
   * new Thread entered run() method
   */
  public void threadStarted(JVM jvm) {
    ThreadInfo ti = jvm.getLastThreadInfo();
    System.out.println("threadStarted: " + ti.getIndex());
  }

  /**
   * Thread exited run() method
   */
  public void threadTerminated(JVM jvm) {
    ThreadInfo ti = jvm.getLastThreadInfo();
    System.out.println("threadTerminated: " + ti.getIndex());
  }

  void filterArgs (String[] args) {
    // we don't have any
  }
  
  public static void main (String[] args) {
    TestVMListener listener = new TestVMListener();
    listener.filterArgs(args);
    
    Config conf = JPF.createConfig(args);
    // here we can set our own params
    JPF jpf = new JPF(conf);
    jpf.addVMListener(listener);

    System.out.println("---------------- starting JPF on class: " + args[0]);
    jpf.run();
    System.out.println("---------------- JPF terminated");
  }

  public void threadBlocked (JVM vm) {
    // TODO Auto-generated method stub
    
  }

  public void choiceGeneratorSet (JVM vm) {
    // TODO Auto-generated method stub
    
  }

  public void choiceGeneratorAdvanced (JVM vm) {
    // TODO Auto-generated method stub
    
  }

  public void choiceGeneratorProcessed (JVM vm) {
    // TODO Auto-generated method stub
    
  }

}

