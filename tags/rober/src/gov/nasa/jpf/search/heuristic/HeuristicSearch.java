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
import gov.nasa.jpf.search.Search;

import java.util.Comparator;
import java.util.TreeSet;


/**
 * a search strategy class that computes all immediate successors of a given
 * state, puts them into a priority queue (the priority is provided by a
 * Heuristic strategy object), and processes states in the sequence of
 * highest priorities. Note that the queue is search-global, i.e. we might hop
 * between search levels.
 */
public class HeuristicSearch extends Search {
  
  static final String DEFAULT_HEURISTIC_PACKAGE = "gov.nasa.jpf.search.heuristic.";
  
  protected TreeSet<HeuristicState> queue;
  protected int         numberNewChildren = 0;
  protected HeuristicState h_state;
  protected HeuristicState new_h_state;
  protected Heuristic     heuristic;
  
  protected boolean heuristicIsPathSensitive;
  protected boolean useAstar;
  protected boolean pathCoverage = false; // set by some Heuristics instances
  protected int initHeuristicValue;
  protected int queueLimit;
  
  // statistics
  int maxHeuristic = Integer.MIN_VALUE;
  int minHeuristic = Integer.MAX_VALUE;
  int heuristicTotal = 0;
  int heuristicCount = 0;
  
  public HeuristicSearch (Config config, JVM vm) throws Config.Exception {
    super(config, vm);
    
    // note this covers three potential Heuristic implementation ctors:
    // (a) (Config,HeuristicSearch), (b) (Config), (c) default
    Class[] argTypes = { Config.class, HeuristicSearch.class };
    Object[] args = { config, this };
    heuristic = config.getEssentialInstance("search.heuristic.class", 
                                                        Heuristic.class, argTypes, args);
    heuristicIsPathSensitive = (heuristic instanceof PathSensitiveHeuristic);
    
    useAstar = config.getBoolean("search.heuristic.astar");
    pathCoverage = config.getBoolean("search.coverage.path");
    
    queue = new TreeSet<HeuristicState>(getComparator(config));
    
    queueLimit = config.getInt("search.heuristic.queue_limit", -1);
    
    initHeuristicValue = config.getInt("search.heuristic.initial_value", 0);
  }
  
  @SuppressWarnings("unchecked")
  protected static Comparator<HeuristicState> getComparator(Config config)
  throws Config.Exception {
    return config.getEssentialInstance("search.heuristic.comparator.class",
                    (Class<Comparator<HeuristicState>>) (Class<?>) Comparator.class);
  }
  
  public HeuristicState getNew () {
    return new_h_state;
  }
  
  public HeuristicState getOld () {
    return h_state;
  }

  void backtrackToParent () {
    backtrack();
    depth--;
    notifyStateBacktracked();    
  }
  
  protected void generateChildren (int maxDepth) {

  	// <2do> add listener notifications to keep track of queue size
  	
    numberNewChildren = 0;
    
    while (!done) {
      if (pathCoverage) {
        h_state.restoreCoverage();
      }
      
      if (!forward()) {
        notifyStateProcessed();
        return;
      }

      depth++;
      notifyStateAdvanced();

      if (hasPropertyTermination()) {
        return;
      }
      
      if (!isEndState) {
        if (isNewState && depth >= maxDepth) { // don't want to see this
          notifySearchConstraintHit(DEPTH_CONSTRAINT);
          // no add
        } else if (isNewState || heuristicIsPathSensitive) {
          // add to or update queue
          
          int h_value = heuristic.heuristicValue();
          
          if (vm.isInterestingState()) {
            h_value = 0;
          } else if (vm.isBoringState()) {
            h_value = (maxHeuristic + 1);
          }

          // update HeuristicSearch specific statistics
          if (maxHeuristic < h_value) {
            maxHeuristic = h_value;
          }
          if (minHeuristic > h_value) {
            minHeuristic = h_value;
          }
          heuristicTotal += h_value;
          heuristicCount++;
          
          if (useAstar) {
            h_value += vm.getPathLength();
          }
          
          new_h_state = new HeuristicState(vm, h_value);
          
          if (h_value >= 0) {
            if (pathCoverage) {
              new_h_state.saveCoverage();
            }
            
            if (isNewState) {
              queue.add(new_h_state);

              numberNewChildren++;
              
              if ((queueLimit > 0) && (queue.size() > queueLimit)) {
                boolean removed = queue.remove(queue.last());
                assert removed : "Inconsistency in heuristic queue.";
                notifySearchConstraintHit( QUEUE_CONSTRAINT);
              }
            } else {
              // TODO: update queue
            }
          }
        }

      } else {  // this is an end state, nothing to queue

      }
      backtrackToParent();
    }
  }
  
  public int getQueueSize () {
    return queue.size();
  }
  
  private void expandState () {
    h_state = queue.first();
    boolean removed = queue.remove(h_state);
    assert removed : "Inconsistency in heuristic queue.";
    
    vm.restoreState(h_state.getVirtualState());
    // note we have to query the depth from the VM because the state is taken from the queue
    // and we have no idea when it was entered there
    depth = vm.getPathLength();
    notifyStateRestored();
    
    heuristic.processParent();
  }
   
  public void search () {
    int maxDepth = getMaxSearchDepth();
        
    h_state = new HeuristicState(vm, initHeuristicValue);
    heuristic.processParent();
    
    if (pathCoverage) {
      h_state.saveCoverage();
    }
        
    done = false;

    notifySearchStarted();
    
    if (hasPropertyTermination()) {
      return;
    }
    
    generateChildren(maxDepth);

    while ((queue.size() != 0) && !done) {
      expandState();
      
      // we could re-init the scheduler here
      generateChildren(maxDepth);
    }
    
    notifySearchFinished();
  }
    
}


