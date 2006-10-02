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

import gov.nasa.jpf.JPF;
import java.util.logging.Logger;
import java.util.List;

/**
 * private optimization for the trivial case of single locks. We definitely don't
 * want to allocate int[] and long[] (BitSet) objects to store a single ref!
 */
public class SingleFieldLockInfo extends FieldLockInfo {

  static Logger log = JPF.getLogger("gov.nasa.jpf.jvm.FieldLockInfo");
  
  int lock;
  int checkLevel;

  SingleFieldLockInfo (int lockRef, int nChecks) {
    lock = lockRef;
    checkLevel = nChecks;
  }
    
  void lockAssumptionFailed (ElementInfo ei, FieldInfo fi, ThreadInfo ti) {
    String src = ti.getMethod().getClassInfo().getSourceFileName();
    int line = ti.getLine();
    
    StringBuffer sb = new StringBuffer();
    sb.append( "Warning: unprotected field access of: ");
    sb.append(ei);
    sb.append('.');
    sb.append(fi.getName());
    sb.append( " in thread: ");
    sb.append( ti.getName());
    sb.append( " (");
    sb.append( src);
    sb.append(':');
    sb.append(line);
    sb.append(")\n");
    sb.append( "assumed lock: ");
    sb.append(DynamicArea.getHeap().get(lock));
    sb.append( ", current locks: ");
    StatisticFieldLockInfo.appendLockSet(sb, ti.getLockedObjectReferences());
    sb.append('\n');
    sb.append(" >>> re-run with 'vm.por.sync_detection=false' or exclude field from checks <<<");
    
    log.severe(sb.toString());
  }

  
  public FieldLockInfo checkProtection (ElementInfo ei, FieldInfo fi,
                                        ThreadInfo ti) {
    List<ElementInfo> currentLocks = ti.getLockedObjects();
    int n = currentLocks.size();
    boolean active = false;
    
    checkLevel++;
    for (int i=0; i<n; i++) {
      ElementInfo lei = currentLocks.get(i);
      if (lei.getIndex() == lock) {
        active = true;
        break;
      }
    }
    
    if (!active) {
      if (checkLevel > StatisticFieldLockInfo.CHECK_THRESHOLD) {
        lockAssumptionFailed(ei, fi, ti);
      }
      return empty;
    }
    
    return this;
  }

  public boolean isProtected () {
    return (checkLevel >= StatisticFieldLockInfo.CHECK_THRESHOLD);
  }

  public FieldLockInfo cleanUp () {
    DynamicArea area = DynamicArea.getHeap();
    if (area.get(lock) == null) {
      return FieldLockInfo.empty;
    } else {
      return this;
    }
  }
  
}
