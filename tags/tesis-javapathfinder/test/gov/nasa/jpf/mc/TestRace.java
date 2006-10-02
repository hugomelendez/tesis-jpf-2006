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
/**
 * this is a raw test class for detection of thread-shared fields, i.e.
 * it executes the garbage collection based reachability analysis
 */

package gov.nasa.jpf.mc;

class Data {
  int f;
}

class One extends Thread {
  Data d;
  
  One (Data d) {
       this.d = d;
  }
  
  public void run () {
       d.f = 1;
       if (d.f != 1) throw new RuntimeException("race!");
  }
}

class Two extends Thread {
  Data d;
  
  Two (Data d) {
       this.d = d;
  }
  
  public void run () {
       d.f = 0;
       if (d.f != 0) throw new RuntimeException("race!");
  }
  
}

public class TestRace {
  
  public static void main (String[] args) {
    TestRace t = new TestRace();

    if (args.length > 0) {
      // just run the specified tests
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        // note that we don't use reflection here because this would
        // blow up execution/test scope under JPF
        if ("testPeerRace".equals(func)) {
          t.testPeerRace();
        } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      // that's mainly for our standalone test verification
      t.testPeerRace();
    }
  }
  
  public void testPeerRace () {
    Data d = new Data();
    One one = new One(d);
    Two two = new Two(d);
    
    one.start();
    two.start();
  }
  

}


