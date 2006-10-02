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

import java.math.BigInteger;

public class TestBigInteger {

  public static void main (String[] args) {
    TestBigInteger t = new TestBigInteger();

    if (args.length > 0) {
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        if ("testArithmeticOps".equals(func)) {
          t.testArithmeticOps();
        } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      t.testArithmeticOps();
    }
  }

  /************************** test methods ************************/
  void testArithmeticOps () {
    BigInteger big = new BigInteger("4200000000000000000");
    BigInteger o =       new BigInteger("100000000000000");
    BigInteger noSoBig = new BigInteger("1");
    
    BigInteger x = big.add(noSoBig);
    String s = x.toString();
    assert s.equals("4200000000000000001");
    
    x = big.divide(o);
    int i = x.intValue();
    assert i == 42000;
  }
}
