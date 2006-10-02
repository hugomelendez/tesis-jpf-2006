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

import org.junit.runner.JUnitCore;
import org.junit.Test;

/**
 * JPF driver for exception handling test
 */
public class TestExceptionJPF extends TestJPF {
  static final String TEST_CLASS = "gov.nasa.jpf.jvm.TestException";


  public static void main (String[] args) {
    JUnitCore.main("gov.nasa.jpf.jvm.TestExceptionJPF");
  }


  /**************************** tests **********************************/
  @Test
  public void testNPE () {
    String[] args = { TEST_CLASS, "testNPE" };
    runJPFException(args, "java.lang.NullPointerException");
  }

  @Test
  public void testNPECall () {
    String[] args = { TEST_CLASS, "testNPECall" };
    runJPFException(args, "java.lang.NullPointerException");
  }

  @Test
  public void testArrayIndexOutOfBoundsLow () {
    String[] args = { TEST_CLASS, "testArrayIndexOutOfBoundsLow" };
    runJPFException(args, "java.lang.ArrayIndexOutOfBoundsException");
  }

  @Test
  public void testArrayIndexOutOfBoundsHigh () {
    String[] args = { TEST_CLASS, "testArrayIndexOutOfBoundsHigh" };
    runJPFException(args, "java.lang.ArrayIndexOutOfBoundsException");
  }

  @Test
  public void testLocalHandler () {
    String[] args = { TEST_CLASS, "testLocalHandler" };
    runJPFnoException(args);
  }

  @Test
  public void testCallerHandler () {
    String[] args = { TEST_CLASS, "testCallerHandler" };
    runJPFnoException(args);
  }

  @Test
  public void testEmptyHandler () {
    String[] args = { TEST_CLASS, "testEmptyHandler" };
    runJPFnoException(args);
  }

  @Test
  public void testEmptyTryBlock () {
    String[] args = { TEST_CLASS, "testEmptyTryBlock" };
    runJPFnoException(args);
  }
  
}

