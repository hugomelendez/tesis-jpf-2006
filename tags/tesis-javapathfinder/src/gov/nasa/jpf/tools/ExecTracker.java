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
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.jvm.bytecode.InvokeInstruction;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.Step;
import java.io.PrintWriter;
import gov.nasa.jpf.util.SourceRef;

/**
 * Listener tool to monitor JPF execution. This class can be used as a drop-in
 * replacement for JPF, which is called by ExecTracker.
 * ExecTracker is mostly a VMListener of 'instructionExecuted' and
 * a SearchListener of 'stateAdvanced' and 'statehBacktracked'
 */
public class ExecTracker extends ListenerAdapter {
  
  boolean printInsns = true;
  boolean printLines = false;
  
  PrintWriter pw;
  SourceRef   lastLine;
  String      linePrefix;
  
  public ExecTracker (Config config) {
    printInsns = config.getBoolean("et.print_insns", true);
    printLines = config.getBoolean("et.print_lines", false);
  }
  
  /******************************************* SearchListener interface *****/
  
  public void stateRestored(Search search) {
    int id = search.getStateNumber();
    System.out.println("----------------------------------- [" +
                       search.getDepth() + "] restored: " + id);
  }
    
  //--- the ones we are interested in
  public void searchStarted(Search search) {
    System.out.println("----------------------------------- search started");
  }

  public void stateAdvanced(Search search) {
    int id = search.getStateNumber();
    
    System.out.print("----------------------------------- [" +
                     search.getDepth() + "] forward: " + id);
    if (search.isNewState()) {
      System.out.print(" new");
    } else {
      System.out.print(" visited");
    }
    
    if (search.isEndState()) {
      System.out.print(" end");
    }
    
    System.out.println();
    
    lastLine = null; // in case we report by source line
    linePrefix = null;
  }

  public void stateProcessed (Search search) {
    int id = search.getStateNumber();
    System.out.println("----------------------------------- [" +
                       search.getDepth() + "] done: " + id);
  }

  public void stateBacktracked(Search search) {
    int id = search.getStateNumber();
    
    System.out.println("----------------------------------- [" +
                       search.getDepth() + "] backtrack: " + id);
  }
  
  public void searchFinished(Search search) {
    System.out.println("----------------------------------- search finished");
  }

  /******************************************* VMListener interface *********/

  public void gcEnd(JVM vm) {
    System.out.println("\t\t # garbage collection");
  }

  //--- the ones we are interested in
  public void instructionExecuted(JVM jvm) {
    ThreadInfo ti = jvm.getLastThreadInfo();
    
    if (linePrefix == null) {
      linePrefix = Integer.toString( ti.getIndex()) + " : ";
    }
    
    if (printLines) {
      if (pw == null) {
        pw = new PrintWriter(System.out);
      }
      if (lastLine == null) {
        lastLine = new SourceRef();
      }
      Step step = jvm.getLastStep();
      step.printStepOn(pw,lastLine,linePrefix);
      pw.flush();
      
    } else {
      if (printInsns) {
        Instruction insn = jvm.getLastInstruction();
        System.out.print( linePrefix);
        System.out.print(insn);
        
        if (insn instanceof InvokeInstruction) {
          MethodInfo mi = ((InvokeInstruction)insn).getInvokedMethod(); 
          if ((mi != null) && mi.isMJI()) { // Huhh? why do we have to check this?
            System.out.print(" [native]");
          }
        }
        
        System.out.println();
      }
    }
  }

  public void threadStarted(JVM jvm) {
    ThreadInfo ti = jvm.getLastThreadInfo();

    System.out.println( "\t\t # thread started: " + ti.getName() + " index: " + ti.getIndex());
  }

  public void threadTerminated(JVM jvm) {
    ThreadInfo ti = jvm.getLastThreadInfo();
    
    System.out.println( "\t\t # thread terminated: " + ti.getName() + " index: " + ti.getIndex());
  }
  
  public void notifyExceptionThrown (JVM jvm) {
    ElementInfo ei = jvm.getLastElementInfo();
    MethodInfo mi = jvm.getLastThreadInfo().getMethod();
    System.out.println("\t\t\t\t # exception: " + ei + " in " + mi);
  }
  
  public void choiceGeneratorAdvanced (JVM jvm) {
    System.out.println("\t\t # choice: " + jvm.getLastChoiceGenerator());
  }
  
  /****************************************** private stuff ******/

  void filterArgs (String[] args) {
    for (int i=0; i<args.length; i++) {
      if (args[i] != null) {
        if (args[i].equals("-print-lines")) {
          printLines = true;
          args[i] = null;
        }
      }
    }
  }
  
  public static void main (String[] args) {
    
    Config conf = JPF.createConfig(args);
    // do special settings here

    ExecTracker listener = new ExecTracker(conf);
    listener.filterArgs(args);  // check and remove our own args
    
    JPF jpf = new JPF(conf);
        
    jpf.addSearchListener(listener);
    jpf.addVMListener(listener);

    jpf.run();
  }
}

