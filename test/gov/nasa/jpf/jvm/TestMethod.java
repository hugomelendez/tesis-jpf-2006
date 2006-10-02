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

interface TMI {
  void gna();
}

class TestMethodBase implements TMI {
  
  int baseData;

  static int sData;
  
  static {
    sData = -1;
  }
  
  static void taz () {
    sData = 24;
  }
  
  TestMethodBase (int a) {
    assert a == 42;
    baseData = 42;
  }

  boolean baz (boolean p, byte b, char c, short s, int i, long l, float f, double d, Object o) {
    assert p;
    assert b == 4;
    assert c == '?';
    assert s == 42;
    assert i == 4242;
    assert l == 424242;
    assert f == 4.2f;
    assert d == 4.242;
    assert o.equals(new Integer(42));

    baseData = 44;

    return p;
  }
  
  void faz () {
    gna();
  }
  
  public void gna () {
    baseData = 0;
  }
  
  int har () {
    return priv();
  }
  
  private int priv () {
    return 7;
  }
}

/**
 * method invocation test
 */
public class TestMethod extends TestMethodBase {
  
  int data;
  
  static void taz () {
    sData = 9;
  }
  
  TestMethod () {
    super(42);
    
    data = 42;
  }

  TestMethod (int a) {
    super(a);
    
    data = a;
  }
  
  double foo (boolean p, byte b, char c, short s, int i, long l, float f, double d, Object o) {
    assert p;
    assert b == 4;
    assert c == '?';
    assert s == 42;
    assert i == 4242;
    assert l == 424242;
    assert f == 4.2f;
    assert d == 4.242;
    assert o.equals(new Integer(42));

    data = 43;

    return d;
  }

  public void gna () {
    baseData = 45;
  }
  
  int priv () {
    return 8;
  }
  
  public static void main (String[] args) {
    new TestMethod();

    if (args.length > 0) {
      // just run the specified tests
      for (int i = 0; i < args.length; i++) {
        String func = args[i];

        // note that we don't use reflection here because this would
        // blow up execution/test scope under JPF
        if ("testCtor".equals(func)) { testCtor(); }
        else if ( "testCall".equals(func)) { testCall(); }
        else if ( "testInheritedCall".equals(func)) { testInheritedCall(); }
        else if ( "testVirtualCall".equals(func)) { testVirtualCall(); }
        else if ( "testSpecialCall".equals(func)) { testSpecialCall(); }
        else if ( "testStaticCall".equals( func)) { testStaticCall(); }
        else {
          throw new IllegalArgumentException("unknown test function");
        }
      }
    } else {
      testCtor();
      testCall();
      testInheritedCall();
      testVirtualCall();
      testSpecialCall();
      testStaticCall();
    }
  }

  public static void testCtor () {
    TestMethod o1 = new TestMethod();
    assert o1.data == 42;
    assert o1.baseData == 42;
    
    TestMethod o2 = new TestMethod(42);
    assert o2.data == 42;
    assert o2.baseData == 42;
  }
  
  public static void testCall () {
    TestMethod o = new TestMethod();
    assert o.foo( true, (byte)4, '?', (short)42, 4242, 424242, 4.2f, 4.242, new Integer(42)) == 4.242;
    assert o.data == 43;
  }
  
  public static void testInheritedCall () {
    TestMethod o = new TestMethod();
    assert o.baz( true, (byte)4, '?', (short)42, 4242, 424242, 4.2f, 4.242, new Integer(42));
    assert o.baseData == 44;
  }
  
  public static void testVirtualCall () {
    TestMethod o = new TestMethod();
    TestMethodBase b = o;
    
    b.faz();
    assert o.baseData == 45;
  }
  
  public static void testSpecialCall () {
    TestMethod o = new TestMethod();
    assert o.har() == 7;
  }
  
  public static void testStaticCall () {
    assert TestMethodBase.sData == -1;
    
    TestMethod.taz();
    assert TestMethodBase.sData == 9;
    
    TestMethodBase.taz();
    assert TestMethodBase.sData == 24;
    
    // used to be:
    //TestMethod o = new TestMethod();
    //o.taz();
    // statically equiv. to this: (no warnings) - pcd
    new TestMethod();
    TestMethod.taz();
    
    assert TestMethodBase.sData == 9;
  }
  
  public static void testInterfaceCall () {
    TestMethod o = new TestMethod();
    TMI ifc = o;
    
    ifc.gna();
    assert o.baseData == 45;
  }
}
