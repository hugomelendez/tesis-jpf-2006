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

import gov.nasa.jpf.*;
import gov.nasa.jpf.util.*;

import java.io.PrintWriter;

/**
 * concrete type to store execution paths. TrailInfo corresponds to Transition,
 * i.e. all instructions executed in the context of a vm.forward() leading
 * into a new state
 */
public class Transition implements Printable { // <2do> cloneable
  
  private int    thread;
  private Step   first, last;
  int nSteps;

  private Object annotation;
  String         output;
  
  static boolean showSteps; // do we report steps?
  
  static boolean init (Config config) {
    showSteps = config.getBoolean("vm.report.show_steps");
    return true;
  }
  
  public Transition (int t) {
    thread = t;
  }

  public String getLabel () {
    if (last != null) {
      return last.getLineString();
    } else {
      return "?";
    }
  }

  public void setOutput (String s) {
    output = s;
  }

  public void setAnnotation (Object o) {
    annotation = o;
  }
  
  public Object getAnnotation () {
    return annotation;
  }
  
  public String getOutput () {
    return output;
  }
  
  public Step getStep (int index) {
    Step s = first;
    for (int i=0; s != null && i < index; i++) s = s.next;
    return s;
  }

  public Step getLastStep () {
    return last;
  }
  
  public int getStepCount () {
    return nSteps;
  }

  public int getThread () {
    return this.thread;
  }

  
  void addStep (Step step) {
    if (first == null) {
      first = step;
      last = step;
    } else {
      last.next = step;
      last = step;
    }
    nSteps++;
  }
    
  public void printOn (PrintWriter pw) {
    SourceRef sr = new SourceRef();

    pw.print("Thread #");
    pw.print(thread);

    pw.println();

    if (showSteps) {
      for (Step s=first; s != null; s = s.next) {
        s.printStepOn(pw, sr, null);
      }
    }
  }

  public void toXML (PrintWriter pw) {
    pw.println("\t<Step Thread=\"" + getThread() + "\">");

    for (Step s=first; s!= null; s=s.next) {
      s.toXML(pw);
    }
    
    pw.println("\t</Step>");
  }

  public static void toXML (PrintWriter s, Path path) {
    s.println("<?xml version=\"1.0\"?>");
    s.println("<!DOCTYPE Trace [");

    s.println("\t<!ELEMENT Trace (Step+)>");
    s.println("\t<!ATTLIST Trace");
    s.println("\t\tHandler CDATA #REQUIRED");
    s.println("\t\tApplication CDATA #REQUIRED");
    s.println("\t\tMessage CDATA #IMPLIED>");

    s.println("\t<!ELEMENT Step (Instruction+,Comment*)>");
    s.println("\t<!ATTLIST Step");
    s.println("\t\tThread CDATA #REQUIRED ");

    s.println("\t<!ELEMENT Instruction (EMPTY)>");
    s.println("\t<!ATTLIST Instruction");
    s.println("\t\tFile CDATA #REQUIRED");
    s.println("\t\tLine CDATA #REQUIRED>");

    s.println("\t<!ELEMENT Comment (#PCDATA)>");
    s.println("]>");
    s.println();

    s.print("<Trace ");
    s.print(" Handler=\"gov.nasa.jpf.jvm.JVMXMLTraceHandler\"");
    s.print(" Application=\"");
    s.print(JVM.getVM().getMainClassName());
    s.println("\">");

    for (int i = 0; i < path.length(); i++) {
      Transition ti = path.get(i);
      ti.toXML(s);
    }

    s.println("</Trace>");
  }
}
