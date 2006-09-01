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

import java.util.Comparator;

/**
 * the default comparator for heuristic state ordering
 * 
 * this class has to determine which state to choose if the
 * heuristic values are equal 
 */
public class DefaultComparator implements Comparator<HeuristicState> {
  public int compare (HeuristicState hs1, HeuristicState hs2) {    
    int diff;
    // order by priority first
    diff = hs1.getPriority() - hs2.getPriority();
    if (diff != 0) return diff;

    // order by id second
    diff = hs1.uniqueID - hs2.uniqueID;
    if (diff != 0) return diff;

    // finally fall back on hashcodes
    diff = hs1.hashCode() - hs2.hashCode();
    if (diff != 0) return diff;

    diff = hs1.getVirtualState().hashCode() - hs2.getVirtualState().hashCode();
    if (diff != 0) return diff;

    // would only get here if VM allows same hashCode for different objects
    assert hs1.equals(hs2) : "Notion of .equals() inconsistent with Comparator.";
    return 0;
  }
}
