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
package gov.nasa.jpf.jvm;

import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.classfile.Field;

/**
 * default Attributor implementation to set method and fiel attributes
 * at class load time. Note this is critical functionality, esp.
 * with respect to threading
 */
public class DefaultAttributor implements Attributor {
  
  // <2do> we should turn atomicity and scheduling relevance into general
  // MethodInfo attributes, to keep it consistent with object and field attrs
  
  public boolean isMethodAtomic (JavaClass jc, Method mth, String uniqueName) {
    
    // per default, we set all standard library methods atomic
    // (aren't we nicely optimistic, are we?)
    if (jc.getPackageName().startsWith( "java.")) {
      String clsName = jc.getClassName();
      
      // except of the signal methods, of course
      if (clsName.equals("java.lang.Object")) {
        if (uniqueName.startsWith("wait(") ||
            uniqueName.equals("notify()V")) {
          return false;
        }
      } else if (clsName.equals("java.lang.Thread")) {
        if (uniqueName.equals("join()V")) {
          return false;
        }
      }
      
      return true;
    }
    
    return false;
  }
  
  /**
   * is calling a certain method in the context of multiple runnable threads
   * scheduling relevant (i.e. has to be considered as a step boundary
   */
  public int getSchedulingRelevance (JavaClass jc, Method mth, String uniqueName) {
    String cls = jc.getClassName();
    
    if (cls.equals("java.lang.Thread")) {
      if (uniqueName.equals("start()")) {
        // <2do> - this is not required if we re-compute reachability after
        // a thread becomes runnable, so that subsequent field/lock accesses
        // depending on the number of runnables will break correctly. Check
        // what other insns (beyond ref-field assignment) might be in the same
        // category (on-the-fly remark w/o saving the state)
        
        //return MethodInfo.SR_ALWAYS;
      } else if (uniqueName.equals("yield()V") ||
          uniqueName.equals("sleep(J)V") ||
          uniqueName.equals("join()V")) {
        return MethodInfo.SR_RUNNABLES;
      }
    } else if (cls.equals("java.lang.Object")) {
      if ( /*uniqueName.equals("wait()") || */
          uniqueName.equals("wait(J)V") ||
          uniqueName.equals("notify()V")) {
        return MethodInfo.SR_RUNNABLES;
      }
    } /*else if (cls.equals("gov.nasa.jpf.jvm.Verify")) {
      // note that the Verify.randoms do not fall into this category,
      // since they are always step boundaries
      if (uniqueName.equals("beginAtomic()V") ||
          uniqueName.equals("endAtomic()V")) {
        //return MethodInfo.SR_ALWAYS;
      }
    }*/

    if (mth.isSynchronized()) {
      return MethodInfo.SR_SYNC;
    }

    return MethodInfo.SR_NEVER;
  }
  
  /**
   * answer the type based object attributes for this class. See
   * ElementInfo for valid choices
   */
  public int getObjectAttributes (JavaClass jc) {
    String clsName = jc.getClassName();
    
    // very very simplistic for now
    if (clsName.equals("java.lang.String") ||
       clsName.equals("java.lang.Integer") ||
       clsName.equals("java.lang.Long") ||
       clsName.equals("java.lang.Class")
        /* ..and a lot more.. */
       ) {
      return ElementInfo.ATTR_IMMUTABLE;
    } else {
      return 0;
    }
  }
  
  // <2do> what about immutable fields? don't say there are none - try to set one
  // of the compiler generated magics (like 'this$xx' in inner classes)
  
  public int getFieldAttributes (JavaClass jc, Field f) {
    int attr = ElementInfo.ATTR_PROP_MASK;
    String clsName = jc.getClassName();
    String fName = f.getName();
    
    if (clsName.equals("java.lang.ThreadGroup")) {
      if (fName.equals("threads")) {
        attr &= ~ElementInfo.ATTR_TSHARED;
      }
    }
    
    if (f.isFinal()) {
      attr |= ElementInfo.ATTR_IMMUTABLE;
    }
    
    return attr;
  }
}

