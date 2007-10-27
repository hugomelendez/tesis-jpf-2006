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

import gov.nasa.jpf.util.Printable;
import gov.nasa.jpf.util.StackNode;

import java.io.PrintWriter;


/**
 * Path represents the data structure in which a execution trace is recorded.
 *
 * It usually gets created by a VirtualMachine, can be stored in a file
 * via the JPF '-save-path' option, re-executed with '-execute-path', or
 * explicitly loaded via 'JPF.loadPath(fileName)'
 *
 * A path instance is specific to a certain 'application', and consists of a
 * list of 'Transition' objects
 */
public class Path implements Printable {
  String             application;
  private StackNode<Transition> stack;
  private int        size;
  
  public Path (String app) {
    this(app,null,0);
  }

  private Path(String app, StackNode<Transition> stk, int sz) {
    application = app;
    stack = stk;
    size = sz;
  }
  
  public Path clone() {
    return new Path(application,stack,size);
  }
  
  public String getApplication () {
    return application;
  }

  public Transition getLast () {
    if (stack != null) {
      return stack.data;
    } else {
      return null;
    }
  }

  public void add (Transition t) {
    stack = new StackNode<Transition>(t,stack);
    size++;
  }

  public Transition get (int pos) {
    if (pos >= size) throw new IndexOutOfBoundsException("" + pos + " >= " + size);
    if (pos < 0) throw new IllegalArgumentException("Negative index.");

    StackNode<Transition> tmp = stack;
    for (int down = size - 1 - pos; down > 0; down--) {
      tmp = tmp.next;
    }
    return tmp.data;
  }

  public int length () {
    return size;
  }

  public boolean hasOutput () {
    return hasOutput(stack);
  }
  
  static boolean hasOutput(StackNode<Transition> stack) {
    if (stack == null) {
      return false;
    } else {
      return stack.data.getOutput() != null || hasOutput(stack.next);
    }
  }
  
  
  public void printOutputOn (PrintWriter pw) {
    int i;
    int length = size;
    Transition t;
    String s;
    
    for (i=0; i<length; i++) {
      t = get(i);
      s = t.getOutput();
      if (s != null) {
        pw.print(s);
      }
    }
  }
  
  public void printOn (PrintWriter pw) {
    int    length = size;
    Transition entry;

    for (int index = 0; index < length; index++) {
      pw.print("Transition #");
      pw.print(index);
      
      if ((entry = get(index)) != null) {
        pw.print(' ');

        entry.printOn(pw);
      }
    }
  }

  public void removeLast () {
    stack = stack.next;
    size--;
  }
}
