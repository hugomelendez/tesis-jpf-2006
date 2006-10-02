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
package gov.nasa.jpf.mc;

import gov.nasa.jpf.jvm.Verify;

public class TestAtomic {
  
  static int data = 42;
  
  public static void main (String[] args){
    TestAtomic t = new TestAtomic();
/**
    if (args.length > 0) {
      // just run the specified tests
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        // note that we don't use reflection here because this would
        // blow up execution/test scope under JPF
        if ("testNoRace".equals(func)) {
          t.testNoRace();
        } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      // that's mainly for our standalone test verification
      t.testNoRace();
    }
**/
    //t.testNoRace();
    t.testDataCG();
  }
  
  public void testNoRace () {
    Runnable r = new Runnable() {
      public void run() {
        assert data == 42;
        data += 1;
        assert data == 43;
        data -= 1;
        assert data == 42;
      }
    };
    
    Thread t = new Thread(r);
    
    Verify.beginAtomic();
    t.start();
    assert data == 42;
    data += 2;
    assert data == 44;
    data -= 2;
    assert data == 42;
    Verify.endAtomic();
  }
  
  public void testDataCG () {
    Runnable r = new Runnable() {
      public void run () {
        data += 10;
      }
    };
    
    Thread t = new Thread(r);
    
    Verify.beginAtomic();
    t.start();
    int i= Verify.getInt(1,2);
    data += i;
    assert data < 45;
    Verify.incrementCounter(0);
    assert i == Verify.getCounter(0);
    Verify.endAtomic();
  }
}
