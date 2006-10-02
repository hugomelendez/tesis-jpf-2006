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

import java.lang.reflect.Field;

public class TestFieldReflection {

  public static void main (String[] args) {
    TestFieldReflection t = new TestFieldReflection();

    if (args.length > 0) {
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        if ("testInstanceInt".equals(func)) {
          t.testInstanceInt();
        } else if ( "testInstanceDouble".equals(func)) {
          t.testInstanceDouble();
        } else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      t.testInstanceInt();
      t.testInstanceDouble();
    }
  }

  int instInt = 42;
  double instDouble = 42.0;
  
  void testInstanceInt () {
    try {
      Class<?> cls = TestFieldReflection.class;
      Field f = cls.getField("instInt");
      
      int i = f.getInt(this);
      assert i == 42;
      
      f.setInt(this, 43);
      assert instInt == 43;
      
    } catch (Throwable t) {
      assert false : "unexpected exception: " + t;
    }
  }
  
  void testInstanceDouble () {
    try {
      Class<?> cls = TestFieldReflection.class;
      Field f = cls.getField("instDouble");
      
      double d = f.getDouble(this);
      assert d == 42.0;
      
      f.setDouble(this, 43.0);
      assert instDouble == 43.0;
      
    } catch (Throwable t) {
      assert false : "unexpected exception: " + t;
    }
  }
  
}
