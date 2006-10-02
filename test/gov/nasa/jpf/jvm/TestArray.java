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
 * JPF part of unit test for standard VM array operations.
 */
public class TestArray {
  TestArray () {
  }

  public static void main (String[] args) {
    TestArray t = new TestArray();

    if (args.length > 0) {
      // just run the specified tests
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        // note that we don't use reflection here because this would
        // blow up execution/test scope under JPF
        if ("testIntArray".equals(func)) {
          t.testIntArray();
        } else if ("testCharArray".equals(func)) {
          t.testCharArray();
        } else if ("testStringArray".equals(func)) {
          t.testStringArray();
        } else if ("test2DArray".equals(func)) {
          t.test2DArray();
        } else if ("test2DStringArray".equals(func)) {
          t.test2DStringArray();
        } else if ("testAoBX".equals(func)) {
          t.testAoBX();
        } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      // that's mainly for our standalone test verification
      t.testIntArray();
      t.testCharArray();
      t.testStringArray();
      t.test2DArray();
      t.test2DStringArray();
      t.testAoBX();
    }
  }

  void test2DArray () {
    long[][] a = new long[2][3];

    a[0][1] = 42;

    assert (a.getClass().isArray());
    assert (a.getClass().getName().equals("[[J"));
    assert (a.getClass().getComponentType().getName().equals("[J"));
    assert (a[0][1] == 42);
  }

  void test2DStringArray () {
    String[][] a = new String[3][5];

    a[2][2] = "fortytwo";

    assert (a.getClass().isArray());
    assert (a.getClass().getName().equals("[[Ljava.lang.String;"));
    assert (a.getClass().getComponentType().getName().equals("[Ljava.lang.String;"));
    assert (a[2][2].equals("fortytwo"));
  }

  void testAoBX () {
    int[] a = new int[2];

    assert (a.length == 2);

    try {
      a[2] = 42;
    } catch (ArrayIndexOutOfBoundsException aobx) {
      return;
    }

    throw new RuntimeException("array bounds check failed");
  }

  void testCharArray () {
    char[] a = new char[5];

    a[2] = 'Z';

    assert (a.getClass().isArray());
    assert (a.getClass().getName().equals("[C"));
    assert (a.getClass().getComponentType() == char.class);
    assert (a[2] == 'Z');
  }

  void testIntArray () {
    int[] a = new int[10];

    a[1] = 42;

    assert (a.getClass().isArray());
    assert (a.getClass().getName().equals("[I"));    
    assert (a.getClass().getComponentType() == int.class);
    assert (a[1] == 42);
  }

  void testStringArray () {
    String[] a = { "one", "two", "three" };

    assert (a.getClass().isArray());
    assert (a.getClass().getName().equals("[Ljava.lang.String;"));
    assert (a.getClass().getComponentType() == String.class);
    assert (a[1].equals("two"));
  }
}
