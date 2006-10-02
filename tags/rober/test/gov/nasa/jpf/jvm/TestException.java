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
 * JPF unit test for exception handling
 */
@SuppressWarnings("null")
public class TestException {
  int data;

  void foo () {
  }
  
  static void bar () {
    TestException o = null;
    o.foo();
  }
  
  public static void main (String[] args) {
    if (args.length > 0) {
      // just run the specified tests
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        // note that we don't use reflection here because this would
        // blow up execution/test scope under JPF
        if ("testNPE".equals(func)) { testNPE(); }
        else if ("testNPECall".equals(func)) { testNPECall(); }
        else if ("testArrayIndexOutOfBoundsLow".equals(func)) { testArrayIndexOutOfBoundsLow(); }
        else if ("testArrayIndexOutOfBoundsHigh".equals(func)) { testArrayIndexOutOfBoundsHigh(); }
        else if ("testLocalHandler".equals(func)) { testLocalHandler(); }
        else if ("testCallerHandler".equals(func)) { testCallerHandler(); }
        else if ("testEmptyHandler".equals(func)) { testEmptyHandler(); }
        else if ("testEmptyTryBlock".equals(func)) { testEmptyTryBlock(); }
        
        else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      testNPE();
      testNPECall();
      testArrayIndexOutOfBoundsLow();
      testArrayIndexOutOfBoundsHigh();
      testLocalHandler();
      testCallerHandler();
      testEmptyHandler();
      testEmptyTryBlock();
    }
  }

  static void testNPE () {
    TestException o = null;
    o.data = -1;

    assert false : "should never get here";
  }
  
  static void testNPECall () {
    TestException o = null;
    o.foo();

    assert false : "should never get here";
  }

  static void testArrayIndexOutOfBoundsLow () {
    int[] a = new int[10];
    a[-1] = 0;

    assert false : "should never get here";
  }

  static void testArrayIndexOutOfBoundsHigh () {
    int[] a = new int[10];
    a[10] = 0;

    assert false : "should never get here";
  }

  static void testLocalHandler () {
    try {
      TestException o = null;
      o.data = 0;
    } catch (IllegalArgumentException iax) {
      assert false : "should never get here";
    } catch (NullPointerException npe) {
      return;
    } catch (Exception x) {
      assert false : "should never get here";
    }
    
    assert false : "should never get here";
  }

  static void testCallerHandler () {
    try {
      bar();
    } catch (Throwable t) {
      return;
    }
    
    assert false : "should never get here";
  }
  
  static void testEmptyHandler () {
    try {
      throw new RuntimeException("should be empty-handled");
    } catch (Throwable t) {
      // nothing
    }
  }
  
  static void testEmptyTryBlock () {
    try {
      // nothing
    } catch (Throwable t) {
      assert false : "should never get here";
    }
  }
}

