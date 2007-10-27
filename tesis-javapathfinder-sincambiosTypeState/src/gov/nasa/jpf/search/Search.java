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
package gov.nasa.jpf.search;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.Error;
import gov.nasa.jpf.ErrorList;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.Property;
import gov.nasa.jpf.State;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.Path;
import gov.nasa.jpf.jvm.Transition;
import gov.nasa.jpf.util.Debug;
import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.ObjArray;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * the mother of all search classes. Mostly takes care of listeners, keeping
 * track of state attributes and errors. This class mainly keeps the
 * general search info like depth, configured properties etc.
 */
public abstract class Search {
  
  public static final String DEPTH_CONSTRAINT = "DEPTH";
  public static final String QUEUE_CONSTRAINT = "QUEUE";
  public static final String SIZE_CONSTRAINT = "SIZE";
  
  protected ErrorList errors = new ErrorList();

  protected int       depth = 0;
  protected JVM       vm;
  protected Property  property;

  // the forward() attributes, e.g. used by the listeners
  protected boolean isEndState = false;
  protected boolean isNewState = true;

  protected boolean matchDepth;
  protected long    minFreeMemory;
  protected int     depthLimit;
  
  protected String lastSearchConstraint;

  // these states control the search loop
  protected boolean done = false;
  protected boolean doBacktrack = false;
  SearchListener     listener;
  
  Config config; // to later-on access settings that are only used once (not ideal)
  
  // statistics
  //int maxSearchDepth = 0;
  
  /** storage to keep track of state depths */
  final IntVector stateDepth = new IntVector();

  protected Search (Config config, JVM vm) {
    this.vm = vm;
    this.config = config;
    
    depthLimit = config.getInt("search.depth_limit", -1);
    matchDepth = config.getBoolean("search.match_depth");
    minFreeMemory = config.getMemorySize("search.min_free", 1024<<10);
    
    try {
      property = getProperties(config);
      if (property == null) {
        Debug.println(Debug.ERROR, "no property");
      }
    } catch (Throwable t) {
      Debug.println(Debug.ERROR, "Search initialization failed: " + t);
    }
  }

  public abstract void search ();
  
  public void addProperty (Property newProperty) {
    property = PropertyMulticaster.add(property, newProperty);
  }
  
  public void removeProperty (Property oldProperty) {
     property = PropertyMulticaster.remove(property, oldProperty);
  }
  
  /**
   * return set of configured properties
   * note there is a nameclash here - JPF 'properties' have nothing to do with
   * Java properties (java.util.Properties)
   */
  protected Property getProperties (Config config) throws Config.Exception {
    Property props = null;
    
    ObjArray<Property> a =
      config.getInstances("search.properties", Property.class);
    if (a != null) {
      for (Property p : a) {
        props = PropertyMulticaster.add(props, p);
      }
    }
        
    return props;
  }

  protected boolean hasPropertyTermination () {
    if (isPropertyViolated()) {
      if (done) {
        return true;
      }
    }
    
    return false;
  }
  
  boolean isPropertyViolated () {
    if ((property != null) && !property.check(this, vm)) {
      error(property, vm.getPath());
      return true;
    }
    
    return false;
  }
    
  public void addListener (SearchListener newListener) {
    listener = SearchListenerMulticaster.add(listener, newListener);
  }

  public void removeListener (SearchListener removeListener) {
    listener = SearchListenerMulticaster.remove(listener,removeListener);
  }

  public ErrorList getErrors () {
    return errors;
  }

  public JVM getVM() {
    return vm;
  }
  
  public boolean isEndState () {
    return isEndState;
  }
  
  public boolean hasNextState () {
    return !isEndState();
  }

  public boolean isNewState () {
    boolean isNew = vm.isNewState();

    if (matchDepth) {
      int id = vm.getStateId();

      if (isNew) {
        setStateDepth(id, depth);
      } else {
        return depth < getStateDepth(id);
      }
    }

    return isNew;
  }

  public boolean isVisitedState () {
    return !isNewState();
  }

  public int getDepth () {
    return depth;
  }

  public String getSearchConstraint () {
    return lastSearchConstraint;
  }
  
  public Transition getTransition () {
    return vm.getLastTransition();
  }

  public int getStateNumber () {
    return vm.getStateId();
  }

  public boolean requestBacktrack () {
    return false;
  }

  public boolean supportsBacktrack () {
    return false;
  }

  public boolean supportsRestoreState () {
    // not supported by default
    return false;
  }

  protected int getMaxSearchDepth () {
    int searchDepth = Integer.MAX_VALUE;

    if (depthLimit > 0) {
      int initialDepth = vm.getPathLength();

      if ((Integer.MAX_VALUE - initialDepth) > depthLimit) {
        searchDepth = depthLimit + initialDepth;
      }
    }

    return searchDepth;
  }

  public int getDepthLimit () {
    return depthLimit;
  }
  
  protected SearchState getSearchState () {
    return new SearchState(this);
  }

  protected void error (Property property, Path path) {
    Error error = new Error(property, path,
                            config.getBoolean("search.print_path"));

    if (config.getBoolean("search.print_errors")) {
      // <2do> should use the logger
      PrintWriter pw = new PrintWriter(System.err, true);
      error.printOn(pw);
      pw.println();
    }

    String fname = config.getString("search.error_path");
    boolean getAllErrors = config.getBoolean("search.multiple_errors");
    if (fname != null) {
      if (getAllErrors) {
        int i = fname.lastIndexOf('.');

        if (i >= 0) {
          fname = fname.substring(0, i) + '-' + errors.size() +
                  fname.substring(i);
        }
      }
      
      savePath(path, fname);
    }

    errors.add(error);
    done = !getAllErrors;
    notifyPropertyViolated();
  }

  public void savePath(Path path, String fname) {
    try {
      FileWriter w = new FileWriter(fname);
      vm.savePath(path, w);
      w.close();
    } catch (IOException e) {
      Debug.println(Debug.ERROR, "Failed to saved trace: " + fname);
    }
  }
  
  protected void notifyStateAdvanced () {
    if (listener != null) {
      listener.stateAdvanced(this);
    }
  }

  protected void notifyStateProcessed () {
    if (listener != null) {
      listener.stateProcessed(this);
    }
  }
  
  protected void notifyStateRestored () {
    if (listener != null) {
      listener.stateRestored(this);
    }
  }
  
  protected void notifyStateBacktracked () {
    if (listener != null) {
      listener.stateBacktracked(this);
    }
  }

  protected void notifyPropertyViolated () {
    if (listener != null) {
      listener.propertyViolated(this);
    }
  }

  protected void notifySearchStarted () {
    if (listener != null) {
      listener.searchStarted(this);
    }
  }

  protected void notifySearchConstraintHit (String constraintId) {
    if (listener != null) {
      lastSearchConstraint = constraintId;
      listener.searchConstraintHit(this);
    }
  }

  protected void notifySearchFinished () {
    if (listener != null) {
      listener.searchFinished(this);
    }
  }


  protected boolean forward () {
    boolean ret = vm.forward();
    
    if (ret) {
      isNewState = vm.isNewState();
    } else {
      isNewState = false;
    }
    
    isEndState = vm.isEndState();
    
    return ret;
  }

  protected boolean backtrack () {
    isNewState = false;
    isEndState = false;
    
    return vm.backtrack();
  }

  protected void restoreState (State state) {
    // not supported by default
  }


  void setStateDepth (int stateId, int depth) {
    stateDepth.set(stateId, depth + 1);
  }

  int getStateDepth (int stateId) {
    int depthPlusOne = stateDepth.get(stateId);
    if (depthPlusOne <= 0) { 
      throw new JPFException("Asked for depth of unvisited state");
    } else {
      return depthPlusOne - 1;
    }
  }

  /**
   * check if we have a minimum amount of free memory left. If not, we rather want to stop in time
   * (with a threshold amount left) so that we can report something useful, and not just die silently
   * with a OutOfMemoryError (which isn't handled too gracefully by most VMs)
   */
  protected boolean checkStateSpaceLimit () {
    Runtime rt = Runtime.getRuntime();
    
    long avail = rt.freeMemory();
    
    // we could also just check for a max number of states, but what really
    // limits us is the memory required to store states
    
    if (avail < minFreeMemory) {
      // try to collect first
      rt.gc();
      avail = rt.freeMemory();
      
      if (avail < minFreeMemory) {
        // Ok, we give up, threshold reached
        return false;
      }
    }
      
    return true;
  }
}

