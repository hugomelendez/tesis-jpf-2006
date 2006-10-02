//
// Copyright (C) 2005 United States Government as represented by the
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

import java.util.ArrayList;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.ClassInfo;
import gov.nasa.jpf.jvm.DynamicArea;
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.ReferenceChoiceGenerator;

/**
 * a choice generator that enumerates the set of all objects of a certain type. This
 * is a replacement for the old 'Verify.randomObject'
 */
public class TypedObjectChoice extends ReferenceChoiceGenerator {
  
  // the requested object type
  String type;
  
  // the object references
  int[] values; 
  
  // our enumeration state
  int count;
  
  public TypedObjectChoice (Config conf, String id) throws Config.Exception {
    DynamicArea heap = DynamicArea.getHeap();
    
    type = conf.getString(id + ".type");
    if (type == null) {
      throw conf.exception("missing 'type' property for TypedObjectGenerator " + id);
    }
    
    ArrayList<ElementInfo> list = new ArrayList<ElementInfo>();
    
    for ( ElementInfo ei : heap) {
      ClassInfo ci = ei.getClassInfo();
      if (ci.instanceOf(type)) {
        list.add(ei);
      }
    }
    
    values = new int[list.size()];
    int i = 0;
    for ( ElementInfo ei : list) {
      values[i++] = ei.getIndex();
    }
    
    count = -1;
  }
  
  @Override
  public void advance () {
    count++;
  }

  @Override
  public int getProcessedNumberOfChoices () {
    return count+1;
  }

  @Override
  public int getTotalNumberOfChoices () {
    return values.length;
  }

  @Override
  public boolean hasMoreChoices () {
    return (count < values.length-1);
  }

  @Override
  public void reset () {
    count = -1;
  }

  public int getNextChoice () {
    if ((count >= 0) && (count < values.length)) {
      return values[count];
    } else {
      return -1;
    }
  }
  
  public String toString() {
    StringBuilder sb = new StringBuilder("TypedObjectGenerator [id=");
    sb.append(id);
    sb.append(",type=");
    sb.append(type);
    sb.append(",values=");
    for (int i=0; i< values.length; i++) {
      if (i>0) {
        sb.append(',');
      }
      if (i == count) {
        sb.append("=>");
      }
      sb.append(values[i]);
    }
    sb.append(']');
    
    return sb.toString();
  }
  
  public TypedObjectChoice randomize() {
    for (int i = values.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      int tmp = values[i];
      values[i] = values[j];
      values[j] = tmp;
    }
    return this;
  }
}
