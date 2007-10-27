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
import gov.nasa.jpf.jvm.ElementInfo;
import gov.nasa.jpf.jvm.StaticArea;


/**
 * heuristic state prioritizer that uses fields of the Main class uner test
 * to determine priorities (i.e. priorities can be set by the program under test)
 *  
 * <2do> pcm - does this still make sense in light of MJI ? If we keep it, this
 * has tobe moved to the Verify interface!
 */
public class UserHeuristic implements Heuristic {
  static final int defaultValue = 1000;
  JVM               vm;

  public UserHeuristic (HeuristicSearch hSearch) {
    vm = hSearch.getVM();
  }

  public int heuristicValue () {
    // <2do> pcm - BAD, remove the VM nuts-and-bolts dependencies
    StaticArea ss = vm.getStaticArea();
    ElementInfo   p = ss.get("Main");
    // <2dp> - this is not initialized !

    // this code is ugly because of the Reference interface
    if (p != null) {
      ElementInfo b = p.getObjectField("buffer");

      if (b != null) {
        int current = b.getIntField("current");
        int capacity = b.getIntField("capacity");

        return (capacity - current);
      }
    }

    return defaultValue;
  }

  public void processParent () {
  }
}
