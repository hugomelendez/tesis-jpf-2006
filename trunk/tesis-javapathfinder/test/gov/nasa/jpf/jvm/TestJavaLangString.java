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
 * test of java.lang.String APIs
 */
public class TestJavaLangString {
  public static void main (String[] args) {
    TestJavaLangString t = new TestJavaLangString();

    if (args.length > 0) {
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        if ("testIntern".equals(func)) {
          t.testIntern();
        } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      t.testIntern();
    }
  }

  public void testIntern () {
    String a = "Blah".intern();
    String b = new String("Blah");

    assert (a != b) : "'new String(intern) != intern' failed";

    String c = b.intern();

    assert (a == c) : "'(new String(intern)).intern() == intern' failed";
  }
}
