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

import org.junit.Test;
import org.junit.runner.JUnitCore;


/**
 * JPF test driver for MJI test
 */
public class TestNativePeerJPF extends TestJPF {
  static final String TEST_CLASS = "gov.nasa.jpf.jvm.TestNativePeer";


  public static void main (String[] args) {
    JUnitCore.main("gov.nasa.jpf.jvm.TestNativePeerJPF");
  }


  /**************************** tests **********************************/
  @Test
  public void testNativeClInit () {
    String[] args = { TEST_CLASS, "testClInit" };
    runJPFnoException(args);
  }

  @Test
  public void testNativeCreate2DimIntArray () {
    String[] args = { TEST_CLASS, "testNativeCreate2DimIntArray" };
    runJPFnoException(args);
  }

  @Test
  public void testNativeCreateIntArray () {
    String[] args = { TEST_CLASS, "testNativeCreateIntArray" };
    runJPFnoException(args);
  }

  @Test
  public void testNativeCreateStringArray () {
    String[] args = { TEST_CLASS, "testNativeCreateStringArray" };
    runJPFnoException(args);
  }

  @Test
  public void testNativeException () {
    String[] args = { TEST_CLASS, "testNativeException" };
    runJPFnoException(args);
  }

  @Test
  public void testNativeCrash () {
    String[] args = { TEST_CLASS, "testNativeCrash" };
    runJPFException(args, "java.lang.reflect.InvocationTargetException");
  }
  
  @Test
  public void testNativeInit () {
    String[] args = { TEST_CLASS, "testInit" };
    runJPFnoException(args);
  }

  @Test
  public void testNativeInstanceMethod () {
    String[] args = { TEST_CLASS, "testNativeInstanceMethod" };
    runJPFnoException(args);
  }

  @Test
  public void testNativeStaticMethod () {
    String[] args = { TEST_CLASS, "testNativeStaticMethod" };
    runJPFnoException(args);
  }
}
