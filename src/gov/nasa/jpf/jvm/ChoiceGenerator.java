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

import java.util.Random;

import gov.nasa.jpf.jvm.bytecode.Instruction;

/**
* abstract root class for configurable choice generators
*/
public abstract class ChoiceGenerator {
  
  // the marker for the current choice used in String conversion
  public static final char MARKER = '>';
  
  protected static final Random random = new Random();
  
  // want the id to be visible to subclasses outside package
  public String id;
  
  // for subsequent access, there is no need to translate a JPF String object reference
  // into a host VM String anymore (we just need it for creation to look up
  // the class if this is a named CG)
  int idRef;
  
  // we keep a linked list of CG's
  ChoiceGenerator prev;
  
  // the instruction that created this CG
  Instruction insn; 
  
  protected ChoiceGenerator () {
    this.id = "-";
  }
  
  protected ChoiceGenerator (String id) {
    this.id = id;
  }
  
  public String getId () {
    return id;
  }
  
  public int getIdRef () {
    return idRef;
  }
  
  public void setIdRef (int idRef) {
    this.idRef = idRef;
  }
  
  public void setPreviousChoiceGenerator (ChoiceGenerator cg) {
    prev = cg;
  }
  
  public abstract boolean hasMoreChoices ();
  
  /**
   * advance to the next choice
   */
  public abstract void advance ();
  
  /**
   * advance n choices
   * pretty braindead generic solution, but if more speed is needed, we can easily override
   * in the concrete CGs (it's used for path replay)
   */
  public void advance (int nChoices) {
    while (nChoices-- > 0) {
      advance();
    }
  }
  
  public abstract void reset ();
  
  public abstract int getTotalNumberOfChoices ();
  
  public abstract int getProcessedNumberOfChoices ();
  
  public String toString () {
    StringBuffer b = new StringBuffer( getClass().getName());
    b.append(" [id=\"");
    b.append(id);
    b.append("\" ,");
    b.append(getProcessedNumberOfChoices());
    b.append("/");
    b.append(getTotalNumberOfChoices());
    b.append("]");
    return b.toString();
  }
  
  public ChoiceGenerator getPreviousChoiceGenerator() {
    return prev;
  }
  
  /**
   * turn the order of choices random (if it isn't already). Only
   * drawback of this generic method (which might be a decorator
   * factory) is that our abstract type layer (e.g. IntChoiceGenerator)
   * has to guarantee type safety. But hey - this is the first case where
   * we can use covariant return types!
   * 
   * NOTES:
   * - this method may alter this ChoiceGenerator and return that or return
   * a new "decorated" version.
   * - random data can be read from the "Random random" field in this class.
   */
  public abstract ChoiceGenerator randomize ();
}
