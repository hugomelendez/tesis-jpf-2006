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
package gov.nasa.jpf.symmetry;

import gov.nasa.jpf.jvm.Verify;

/**
 * This class contains special methods that will be erased from the call
 * stack during symmetry reduction.  Erasure insures ordering is not
 * introduced into state.  Actual order should be irrelevant for these
 * operations.
 * 
 * TODO: above
 * 
 * @author peterd
 */
final class ThreadSetOps {
  private ThreadSetOps() {}
  
  
  // iterator ok; will be GCed in abstraction
  static void joinAll(SymThreadSet set) throws InterruptedException {
    for (Thread th : (Iterable<Thread>) set) {
      th.join();
    }
  }

  static void startAll(SymThreadSet set) {
    Verify.beginAtomic(); /* atomic ok; only true craziness could detect non-atomic
                           * simultaneous start()s. */
    for (Thread th : (Iterable<Thread>) set) {
      th.start();
    }
    Verify.endAtomic();
  }
}
