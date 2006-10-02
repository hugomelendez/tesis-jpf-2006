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
package gov.nasa.jpf.embedded;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.Transition;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListener;


/**
 * test client to show embedded JPF usage
 * <2do> - TestSearchListener is now obsolete, we have real listener tools
 * like ExecTracker etc.
 */
public class TestSearchListener implements SearchListener {
  
  String padLeft (int n, int len) {
    String s = Integer.toString(n);
    
    if (s.length() >= len) {
      return s;
    } else {
      return "      ".substring(0, 6 - s.length()) + s;
    }
  }
  
  void log (String prefix, Search search) {
    Transition trans = search.getTransition();
    int depth = search.getDepth();
    
    if (trans != null) {
      System.out.print(prefix);
    
      System.out.print('[');
      System.out.print(trans.getThread());
      System.out.print("] ");

      System.out.print( padLeft( search.getStateNumber(), 6));
      System.out.print( padLeft( depth, 4));
    
    
      System.out.print("  : ");
      System.out.println(trans.getLabel().trim());
    }
  }
  
  public void stateRestored(Search search) {
    log( "! ", search);
  }
  
  public void stateBacktracked(Search search) {
    log( "< ", search);
  }
  
  public void searchStarted(Search search) {
    System.out.println("-------------------- search started");
  }

  public void searchFinished(Search search) {
    System.out.println("-------------------- search finished");
  }
  
  public void propertyViolated(Search search) {
    // TODO
  }
    
  public void searchConstraintHit(Search search) {
    // TODO
  }
  
  public void stateAdvanced(Search search) {
    log( search.hasNextState() ? "> " : "* ", search);
  }
  
  public void stateProcessed (Search search) {
    log( ".", search);
  }
  
  void filterArgs (String[] args) {
    // we don't have any
  }
  
  public static void main (String[] args) {
    TestSearchListener listener = new TestSearchListener();
    listener.filterArgs(args);
    
    Config conf = JPF.createConfig(args);
    // here we can set our own params
    JPF jpf = new JPF(conf);
    jpf.getSearch().addListener(listener);

    // here you can set the option fields explicitly
    //opts.execute_path=true;
    System.out.println("---------------- starting JPF on class: " + args[0]);
    jpf.run();
    System.out.println("---------------- JPF terminated");
  }
}
