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
 * model class of MJI test
 */
public class TestNativePeer {
  static int sdata;

  static {
    // only here to be intercepted
    sdata = 0; // dummy insn required for the Eclipse compiler (skips empty methods)
  }

  int idata;

  TestNativePeer () {
  }

  TestNativePeer (int data) {
    // only here to be intercepted
  }

  public static void main (String[] args) {
    TestNativePeer t = new TestNativePeer();

    if (args.length > 0) {
      // just run the specified tests
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        // note that we don't use reflection here because this would
        // blow up execution/test scope under JPF
        if ("testClInit".equals(func)) {
          t.testClInit();
        } else if ("testInit".equals(func)) {
          t.testInit();
        } else if ("testNativeInstanceMethod".equals(func)) {
          t.testNativeInstanceMethod();
        } else if ("testNativeStaticMethod".equals(func)) {
          t.testNativeStaticMethod();
        } else if ("testNativeCreateStringArray".equals(func)) {
          t.testNativeCreateStringArray();
        } else if ("testNativeCreateIntArray".equals(func)) {
          t.testNativeCreateIntArray();
        } else if ("testNativeCreate2DimIntArray".equals(func)) {
          t.testNativeCreate2DimIntArray();
        } else if ("testNativeException".equals(func)) {
          t.testNativeException();
        } else if ("testNativeCrash".equals(func)) {
          t.testNativeCrash();
        } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      // that's mainly for our standalone test verification
      t.testClInit();
      t.testInit();
      t.testNativeInstanceMethod();
      t.testNativeStaticMethod();
      t.testNativeCreateStringArray();
      t.testNativeCreateIntArray();
      t.testNativeCreate2DimIntArray();
      t.testNativeException();
      t.testNativeCrash();
    }
  }

  public void testClInit () {
    if (sdata != 42) {
      throw new RuntimeException("native '<clinit>' failed");
    }
  }

  public void testInit () {
    TestNativePeer t = new TestNativePeer(42);

    if (t.idata != 42) {
      throw new RuntimeException("native '<init>' failed");
    }
  }

  public void testNativeCreate2DimIntArray () {
    int[][] a = nativeCreate2DimIntArray(2, 3);

    if (a == null) {
      throw new RuntimeException("native int[][]  creation failed: null");
    }

    if (!a.getClass().isArray()) {
      throw new RuntimeException("native int[][] creation failed: not an array");
    }

    if (!a.getClass().getComponentType().getName().equals("[I")) {
      throw new RuntimeException(
            "native int[][] creation failed: wrong component type");
    }

    if (!(a[1][1] == 42)) {
      throw new RuntimeException("native int[][] element init failed");
    }
  }

  public void testNativeCreateIntArray () {
    int[] a = nativeCreateIntArray(3);

    if (a == null) {
      throw new RuntimeException("native int array creation failed: null");
    }

    if (!a.getClass().isArray()) {
      throw new RuntimeException(
            "native int array creation failed: not an array");
    }

    if (a.getClass().getComponentType() != int.class) {
      throw new RuntimeException(
            "native int array creation failed: wrong component type");
    }

    if (!(a[1] == 1)) {
      throw new RuntimeException("native int array element init failed");
    }
  }

  public void testNativeCreateStringArray () {
    String[] a = nativeCreateStringArray(3);

    if (a == null) {
      throw new RuntimeException("native String array creation failed: null");
    }

    if (!a.getClass().isArray()) {
      throw new RuntimeException(
            "native String array creation failed: not an array");
    }

    if (a.getClass().getComponentType() != String.class) {
      throw new RuntimeException(
            "native String array creation failed: wrong component type");
    }

    if (!"one".equals(a[1])) {
      throw new RuntimeException("native String array element init failed");
    }
  }

  public void testNativeException () {
    try {
      nativeException();
    } catch (UnsupportedOperationException ux) {
      String details = ux.getMessage();

      if ("caught me".equals(details)) {
        ux.printStackTrace();
        return;
      } else {
        throw new RuntimeException("wrong native exception details: " + 
                                   details);
      }
    } catch (Throwable t) {
      throw new RuntimeException("wrong native exception type: " + 
                                 t.getClass());
    }

    throw new RuntimeException("no native exception thrown");
  }
  
  public void testNativeCrash () {
    nativeCrash();
  }

  public void testNativeInstanceMethod () {
    int res = nativeInstanceMethod(2.0, '?', true, 40);

    if (res != 42) {
      throw new RuntimeException("native instance method failed");
    }
  }

  public void testNativeStaticMethod () {
    long res = nativeStaticMethod(40, "Blah");

    if (res != 42) {
      throw new RuntimeException("native instance method failed");
    }
  }

  native int[][] nativeCreate2DimIntArray (int s1, int s2);

  native int[] nativeCreateIntArray (int size);

  native String[] nativeCreateStringArray (int size);

  native void nativeException ();
  
  native int nativeCrash ();

  native int nativeInstanceMethod (double d, char c, boolean b, int i);

  native long nativeStaticMethod (long l, String s);
}
