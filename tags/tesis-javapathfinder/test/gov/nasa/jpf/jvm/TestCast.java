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

/**
 * test cast operations
 */
public class TestCast {
  public static void main (String[] args) {
    TestCast t = new TestCast();

    if (args.length > 0) {
      // just run the specified tests
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        // note that we don't use reflection here because this would
        // blow up execution/test scope under JPF
        if ("testCast".equals(func)) {
          t.testCast();
        } else if ("testCastFail".equals(func)) {
          t.testCastFail();
        } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      // that's mainly for our standalone test verification
      t.testCast();
      t.testCastFail();
    }
  }

  @SuppressWarnings("cast")
  public void testCast () {
    B b = new B();
    A a = b;

    ignoreI((I) a);
    ignoreK((K) b);
  }

  public void testCastFail () {
    A a = new A();

    try {
      ignoreI((I) a);
    } catch (ClassCastException ccx) {
      return;
    }

    throw new RuntimeException("illegal cast passed");
  }

  static interface I {
  }

  static interface J extends I {
  }

  static interface K {
  }

  public void ignoreI(I i) { }
  public void ignoreJ(J j) { }
  public void ignoreK(K k) { }
  
  static class A implements K {
  }

  static class B extends A implements J {
  }
}
