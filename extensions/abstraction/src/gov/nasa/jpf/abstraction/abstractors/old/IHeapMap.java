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
package gov.nasa.jpf.abstraction.abstractors.old;

import gov.nasa.jpf.abstraction.state.ObjectNode;

/**
 * For mapping old objRefs to new heap objects.  Could precompute skeleton
 * objects or could create on demand (preferred).
 * 
 * @author peterd
 */
public interface IHeapMap {
  ObjectNode mapOldHeapRef(int objRef);
  
  /**
   * Uses the specified abstractor for resolving a VM objRef to an ObjectNode.
   * This should be used in cases in which an object within another is treated
   * differently from others of its type because it is ``in'' the other object.
   *  
   * @param assertOnlyRef if true, should raise some failure if objRef has
   * already been requested or is requested in the future.
   */
  <T extends ObjectNode> T customMapOldHeapRef(int objRef, IObjectAbstractor<T> abs,
      boolean assertOnlyRef);
}
