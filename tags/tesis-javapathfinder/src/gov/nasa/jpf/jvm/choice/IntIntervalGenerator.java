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
package gov.nasa.jpf.jvm.choice;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.IntChoiceGenerator;

/* pcmehlitz,
 *  you indended to handle the randomize stuff in here?  i've fixed it
 * to use the super implementation, which is the RandomOrderIntCG.  feel
 * free to make more efficient if you want.
 *                               -peterd/pcd/pcdillinger */ 

/**
 * Choice Generator that enumerates an interval of int values Pretty simplistic
 * implementation for now, but at least it can count up and down
 */
public class IntIntervalGenerator extends IntChoiceGenerator {

  //see comment at top!
  //boolean randomize;
  
  int min, max;

  int next;

  int delta;

  public void reset () {
    if (delta == 0) {
      throw new JPFException("IntIntervalGenerator delta value is 0");
    }

    if (min > max) {
      int t = max;
      max = min;
      min = t;
    }

    if (delta > 0) {
      next = min - delta;
    } else {
      next = max + delta;
    }
  }

  public IntIntervalGenerator(int min, int max, int delta) {
    super(null);

    this.min = min;
    this.max = max;
    this.delta = delta;

    reset();
  }

  public IntIntervalGenerator(int min, int max) {
    this(min, max, 1);
  }

  public IntIntervalGenerator(Config conf, String id) {
    super(id);
    min = conf.getInt(id + ".min");
    max = conf.getInt(id + ".max");
    delta = conf.getInt(id + ".delta", 1);

    reset();
  }

  public int getNextChoice () {
    return next;
  }

  public boolean hasMoreChoices () {
    if (delta > 0) {
      return (next < max);
    } else {
      return (next > min);
    }
  }

  public void advance () {
    next += delta;
  }
  
  public int getTotalNumberOfChoices () {
    return ((Math.abs(max - min) / delta) + 1);
  }

  public int getProcessedNumberOfChoices () {
    if (delta > 0) {
      if (next < min)
        return 0;
    } else {
      if (next > max)
        return 0;
    }

    return ((Math.abs(next - min) / delta) + 1);
  }

  public String toString () {
    StringBuffer sb = new StringBuffer();
    sb.append(getClass().getName());
    if (id == null) {
      sb.append('[');
    } else {
      sb.append("[id=\"");
      sb.append(id);
      sb.append("\",");
    }
    sb.append(min);
    sb.append("..");
    sb.append(max);
    sb.append(",delta=");
    if (delta > 0) {
      sb.append('+');
    }
    sb.append(delta);
    sb.append(",cur=");
    sb.append(getNextChoice());
    sb.append(']');
    return sb.toString();
  }
  
  //see comment at top 
  //public IntIntervalGenerator randomize() {
  //  // we handle this at creation time
  //  return this;
  //}
}
