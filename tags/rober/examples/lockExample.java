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
import gov.nasa.jpf.jvm.Verify;


/**
 * DOCUMENT ME!
 */
class lockExample {
  public static int LOCK = 0;

  public static void lock () {
    assert (LOCK == 0);
    LOCK = 1;
  }

  public static void main (String[] args) {
    int got_lock = 0;

    do {
      if (Verify.randomBool()) {
        lock();
        got_lock++;
      }

      if (got_lock != 0) {
        unlock();
      }

      got_lock--;
    } while (Verify.randomBool());
  }

  public static void unlock () {
    assert (LOCK == 1);
    LOCK = 0;
  }
}
