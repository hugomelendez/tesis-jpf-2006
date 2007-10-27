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

import gov.nasa.jpf.Config;

/**
 * a pretty simple ChoiceGenerator that returns a boolean
 */
public class BooleanChoiceGenerator extends ChoiceGenerator {

  // do we evaluate [false, true] or [true, false]
  static boolean falseFirst = true;

  int count = -1;
  boolean next;
  
  public BooleanChoiceGenerator(Config conf, String id) {
    super(id);

    falseFirst = conf.getBoolean("cg.boolean.false_first", true);
    next = !falseFirst;
  }

  public boolean hasMoreChoices () {
    return (count < 1);
  }

  public boolean getNextChoice () {
    return next;
  }

  public void advance () {
    if (count < 1) {
      count++;
      next = !next;
    }
  }

  public void reset () {
    count = -1;
    next = !falseFirst;
  }
  
  public int getTotalNumberOfChoices () {
    return 2;
  }

  public int getProcessedNumberOfChoices () {
    return (count+1);
  }
  
  public String toString () {
    StringBuffer sb = new StringBuffer();
    sb.append(getClass().getName());
    sb.append('[');
    if (count < 1) {
      sb.append(MARKER);
      sb.append(next);
      sb.append(',');
      sb.append(!next);
    } else {
      sb.append(MARKER);
      sb.append(!next);
      sb.append(',');
      sb.append(next);
    }
    sb.append(']');
    return sb.toString();
  }
  
  public BooleanChoiceGenerator randomize () {
    next = random.nextBoolean();
    return this;
  }
}
