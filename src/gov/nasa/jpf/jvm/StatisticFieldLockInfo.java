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

import java.util.List;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.util.Debug;
import java.util.logging.Logger;

/**
 * a FieldLockSet implementation with the following strategy
 *   - at each check, store the intersection of the current threads lock set
 *     with the previous field lock set
 *   - if the access was checked less than CHECK_THRESHOLD times, report the
 *     field as unprotected
 *   - if the field lock set doesn't become empty after CHECK_THRESHOLD, report
 *     the field as protected
 *
 *   - as an optimization, raise the check level above the threshold if we
 *     have a good probability that a current lock is a protection lock for this
 *     field
 */

public class StatisticFieldLockInfo extends FieldLockInfo {

  static Logger log = JPF.getLogger("gov.nasa.jpf.jvm.FieldLockInfo");
  
  final static int CHECK_THRESHOLD = 5;
  
  int[] lset;
  int checkLevel;
  
  // just a debugging tool
  static void appendLockSet (StringBuffer sb, int[] lockSet) {
    DynamicArea heap = DynamicArea.getHeap();
    
    if ((lockSet == null) || (lockSet.length == 0)) {
      sb.append( "{}");
    } else {
      sb.append("{");
      for (int i=0; i<lockSet.length;) {
        int ref = lockSet[i];
        if (ref != -1) {
          ElementInfo ei = heap.get(ref);
          if (ei != null) {
            sb.append(ei);
          } else {
            sb.append("?@");
            sb.append(lockSet[i]);
          }
        }
        i++;
        if (i<lockSet.length) sb.append(',');
      }
      sb.append("}");
    }
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
    sb.append( "lock candidates: ");
    appendLockSet(sb, lset);
    sb.append( ", current locks: ");
    appendLockSet(sb, ti.getLockedObjectReferences());
    sb.append('\n');
    sb.append(" >>> re-run with 'vm.por.sync_detection=false' or exclude field from checks <<<");
    
    log.severe(sb.toString());
  }
  
  /**
   * check if the current thread lockset contains a lock with a high probability
   * that it is a protection lock for this field. We need this to avoid
   * state explosion due to the number of fields to check. Note that we don't
   * necessarily have to answer/decide which one is the best match in case of
   * several candidates (if we don't use this to reduce to StatisticFieldLockInfo1)
   *
   * For instance fields, this would be a lock with a distance <= 1.
   * For static fields, the corresponding class object is a good candidate.
   */
  static ElementInfo strongProtectionCandidate (ElementInfo ei, FieldInfo fi, ThreadInfo ti) {
    List<ElementInfo> currentLocks = ti.getLockedObjects();
    int n = currentLocks.size();
    
    if (fi.isStatic()) { // static field, check for class object locking
      ClassInfo ci = fi.getClassInfo();
      int cref = ci.getClassObjectRef();
      
      for (int i=0; i<n; i++) {
        ElementInfo e = currentLocks.get(i); // the locked object
        if (e.getIndex() == cref) {
          Debug.print(Debug.MESSAGE, "sync-detection: ");
          Debug.print(Debug.MESSAGE, ei);
          Debug.print(Debug.MESSAGE, " assumed to be synced on class object: ");
          Debug.println(Debug.MESSAGE, e);
          return e;
        }
      }
      
    } else { // instance field, use lock distance as a heuristic
      for (int i=0; i<n; i++) {
        ElementInfo e = currentLocks.get(i); // the locked object
        int eidx = e.getIndex();
        
        // case 1: synchronization on field owner itself
        if (ei == e) {
          Debug.print(Debug.MESSAGE, "sync-detection: ");
          Debug.print(Debug.MESSAGE, ei);
          Debug.println(Debug.MESSAGE, " assumed to be synced on itself");
          return e;
        }
        
        // case 2: synchronization on owner of object holding field (sync wrapper)
        if (e.hasRefField(ei.getIndex())) {
          Debug.print(Debug.MESSAGE, "sync-detection: ");
          Debug.print(Debug.MESSAGE, ei);
          Debug.print(Debug.MESSAGE, " assumed to be synced on object wrapper: ");
          Debug.println(Debug.MESSAGE, e);
          return e;
        }
        
        // case 3: synchronization on sibling field that is a private lock object
        if (ei.hasRefField(eidx)) {
          Debug.print(Debug.MESSAGE, "sync-detection: ");
          Debug.print(Debug.MESSAGE, ei);
          Debug.print(Debug.MESSAGE, " assumed to be synced on sibling: ");
          Debug.println(Debug.MESSAGE, e);
          return e;
        }
      }
    }
    
    return null;
  }
  
  public FieldLockInfo checkProtection (ElementInfo ei, FieldInfo fi, ThreadInfo ti) {
    List<ElementInfo> currentLocks = ti.getLockedObjects();
    int nLocks = currentLocks.size();
    
    checkLevel++;
    if (checkLevel == 1) { // first time, not checked before
      if (nLocks == 0) {  // no locks held, this thing is unprotected
//System.out.println("@ no lock active: " + ei);
        return empty;
        
      } else {
        
        // check if we have a lock that is a strong candidate for accessing this
        // field (like the object that holds the field)
        ElementInfo lcei = strongProtectionCandidate(ei,fi,ti);
        if (lcei != null) {
//System.out.println("@ strong prot-lock candidate for: " + ei + " is: " + lcei);
          // shortcut (we could also turn it into a StaticFieldLockInfo1 here)
          checkLevel = CHECK_THRESHOLD;
        }

        if (nLocks == 1) { // only one lock
          // Ok, let's simplify this, no need to waste all the memory for a set
          return new SingleFieldLockInfo(currentLocks.get(0).getIndex(), checkLevel);
          
        } else { // we have a set of protection lock candidates
          lset = new int[nLocks];
          for (int i=0; i<nLocks; i++) {
            ElementInfo lei = currentLocks.get(i);
            lset[i] = lei.getIndex();
          }
        }
      }

    } else {  // already checked this before (but the simple single lock case is now
              // handled by SingleFieldLockInfo)
      
      if (nLocks == 0) { // no current locks, so intersection is empty
        lockAssumptionFailed(ei, fi, ti);
        return empty;
        
      } else { // we had a lock set, and there currently is more than one lock held
        int i, j, l =0;
        int[] newLset = new int[lset.length];
        
        for (i=0; i<nLocks; i++) { // get the set intersection
          ElementInfo lei = currentLocks.get(i);
          int leidx = lei.getIndex();
          
          for (j=0; j<lset.length; j++) {
            if (lset[j] == leidx) {
              newLset[l++] = leidx;
              break; // sets don't contain duplicates
            }
          }
        }
        
        if (l == 0) { // intersection empty
          lockAssumptionFailed(ei, fi, ti);
          return empty;          
        } else if (l < newLset.length) { // intersection did shrink
          lset = new int[l];
          System.arraycopy(newLset, 0, lset, 0, l);
        } else {
          // no change
        }
      }
    }
    
    return this;
  }
  
  public boolean isProtected () {
    return (checkLevel >= CHECK_THRESHOLD);
  }


  public FieldLockInfo cleanUp () {
    DynamicArea area = DynamicArea.getHeap();
    int[] newSet = null;
    int l = 0;
    
    if (lset != null) {
      for (int i=0; i<lset.length; i++) {
        if (area.get(lset[i]) == null) { // we got a stale one, so we have to change us
          
          if (newSet == null) { // first one, copy everything up to it
            newSet = new int[lset.length-1];
            if (i > 0) {
              System.arraycopy(lset, 0, newSet, 0, i);
              l = i;
            }
          }
        } else {
          if (newSet != null) { // we already had a dangling ref, now copy the live ones
            newSet[l++] = lset[i];
          }
        }
      }
    }
    
    if (l == 1) {
        assert (newSet != null);
        return new SingleFieldLockInfo(newSet[0], checkLevel);
    } else {
      if (newSet != null) {
        if (l == newSet.length) { // we just had one stale ref 
          lset = newSet;
        } else { // several stales - make a new copy
          if (l == 0) {
            return empty;
          } else {
            lset = new int[l];
            System.arraycopy(newSet, 0, lset, 0, l);
          }
        }
      }
      return this;
    }
  }
}
