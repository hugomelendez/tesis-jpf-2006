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
package gov.nasa.jpf.abstraction.old;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.abstraction.old.AbstractedState;
import gov.nasa.jpf.abstraction.old.ClassObject;
import gov.nasa.jpf.abstraction.old.HeapObject;
import gov.nasa.jpf.abstraction.old.ThreadObject;

public interface StateAbstractor {
  void init(Config config) throws Config.Exception;
  
  /**
   * Walk the skeleton data structures, removing any containers that need
   * not be filled (because their contents would be abstracted away). 
   */
  void performPreabstraction (ClassObject[] classes,
                              ThreadObject[] threads,
                              HeapObject[] newHeap);

  /**
   * Returns whether this abstraction requests input be linearized before
   * calling performAbstraction. 
   */
  boolean prelinearization ();

  /** 
   * Converts 'state' to an abstracted version.
   */
  void performAbstraction(AbstractedState state);

}
