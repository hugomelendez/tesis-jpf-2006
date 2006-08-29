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
 * This is a raw test class, which produces AssertionErrors for all
 * cases we want to catch. Make double-sure we don't refer to any
 * JPF class in here, or we start to check JPF recursively.
 * To turn this into a Junt test, you have to write a wrapper
 * TestCase, which just calls the testXX() methods.
 * The Junit test cases run JPF.main explicitly by means of specifying
 * which test case to run, but be aware of this requiring proper
 * state clean up in JPF !
 *
 * KEEP IT SIMPLE - it's already bad enough we have to mimic unit tests
 * by means of system tests (use whole JPF to check if it works), we don't
 * want to make the observer problem worse by means of enlarging the scope
 * JPF has to look at
 *
 * Note that we don't use assert expressions, because those would already
 * depend on working java.lang.Class APIs
 */
package gov.nasa.jpf.jvm;

/**
 * test of java.lang.Class API
 */
public class TestJavaLangClass {
  static String clsName = "gov.nasa.jpf.jvm.TestJavaLangClass";

  int data = 42; // that creates a default ctor for our newInstance test
  
  public static void main (String[] args) {
    TestJavaLangClass t = new TestJavaLangClass();

    if (args.length > 0) {
      // just run the specified tests
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        // note that we don't use reflection here because this would
        // blow up execution/test scope under JPF
        if ("testClassForName".equals(func)) {
          t.testClassForName();
        } else if ("testClassField".equals(func)) {
          t.testClassField();
        } else if ("testGetClass".equals(func)) {
          t.testGetClass();
        } else if ("testIdentity".equals(func)) {
          t.testIdentity();
        } else if ("testNewInstance".equals(func)) {
          t.testNewInstance();
         } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      // that's mainly for our standalone test verification
      t.testClassForName();
      t.testClassField();
      t.testGetClass();
      t.testIdentity();
      t.testNewInstance();
    }
  }

  @SuppressWarnings("null")
  public void testClassField () {
    Class<?> clazz = TestJavaLangClass.class;

    if (clazz == null) {
      throw new RuntimeException("class field not set");
    }

    if (!clsName.equals(clazz.getName())) {
      throw new RuntimeException("getName() wrong for class field");
    }
  }

  /**************************** tests **********************************/
  public void testClassForName () {
    Class<?> clazz = null;

    try {
      clazz = Class.forName(clsName);
      System.out.println("loaded " + clazz.getName());
    } catch (Exception x) {
    }

    if (clazz == null) {
      throw new RuntimeException("Class.forName() returned null object");
    }

    if (!clsName.equals(clazz.getName())) {
      throw new RuntimeException(
            "getName() wrong for Class.forName() acquired class");
    }
  }

  public void testGetClass () {
    Class<?> clazz = this.getClass();

    if (clazz == null) {
      throw new RuntimeException("Object.getClass() failed");
    }

    if (!clsName.equals(clazz.getName())) {
      throw new RuntimeException(
            "getName() wrong for getClass() acquired class");
    }
  }

  public void testIdentity () {
    Class<?> clazz1 = null;
    Class<?> clazz2 = TestJavaLangClass.class;
    Class<?> clazz3 = this.getClass();

    try {
      clazz1 = Class.forName(clsName);
    } catch (Exception x) {
    }

    if (clazz1 != clazz2) {
      throw new RuntimeException(
            "Class.forName() and class field not identical");
    }

    if (clazz2 != clazz3) {
      throw new RuntimeException(
            "Object.getClass() and class field not identical");
    }
  }
  
  public void testNewInstance () {
    try {
      Class<?> clazz = TestJavaLangClass.class;
      TestJavaLangClass o = (TestJavaLangClass) clazz.newInstance();
      
      System.out.println("new instance: " + o);
      
      if (o.data != 42) {
        throw new RuntimeException(
          "Class.newInstance() failed to call default ctor");        
      }
    } catch (Exception e) {
      throw new RuntimeException(
          "Class.newInstance() caused exception: " + e);
    }
  }
}
