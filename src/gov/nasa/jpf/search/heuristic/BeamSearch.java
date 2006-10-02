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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.JVM;

import java.util.Comparator;
import java.util.TreeSet;


/**
 * A BeamSearch is a HeuristicSearch with a state queue that is reset at each
 * search level (i.e. it doesn't hop between search levels whe fetching the
 * next state from the queue)
 */
public class BeamSearch extends HeuristicSearch {
  private TreeSet<HeuristicState> parents;

  public BeamSearch (Config config, JVM vm) throws Config.Exception {
    super(config, vm);
  }

  public void search () {
    int maxDepth = getMaxSearchDepth();

    h_state = new HeuristicState(vm, initHeuristicValue);
    heuristic.processParent();

    if (pathCoverage) {
      h_state.saveCoverage();
    }

    done = false;

    if (hasPropertyTermination()) {
      return;
    }

    generateChildren(maxDepth);

    while ((queue.size() != 0) && !done) {
      expandChildren(maxDepth);
    }
  }

  private void expandChildren (int maxDepth) {
  	// <2do> provide search listener hooks to monitor queue size and expanded states
  	
    Comparator<? super HeuristicState> comp = queue.comparator();
    parents = new TreeSet<HeuristicState>(comp);
    parents.addAll(queue); // <2do> is duplication needed/redundant?  -pcd
    queue = new TreeSet<HeuristicState>(comp); 

    while ((parents.size() != 0) && !done) {
      expandState();
      generateChildren(maxDepth);
    }
  }

  private void expandState () {
    //int s = parents.size();  /* unused */
    h_state = parents.first();
    parents.remove(h_state);
    vm.restoreState(h_state.getVirtualState());
    heuristic.processParent();
  }
}
