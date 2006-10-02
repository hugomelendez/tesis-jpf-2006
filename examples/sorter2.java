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
import gov.nasa.jpf.jvm.Verify;


/**
 * DOCUMENT ME!
 */
class sorter2 {
  public static void main (String[] args) {
    int a = Verify.random(4);
    int b = Verify.random(4);
    int c = Verify.random(4);
    int d = Verify.random(4);
    int temp = 0;
    Verify.instrumentPoint("pre-sort");

    if (a > b) {
      temp = b;
      b = a;
      a = temp;
    }

    if (b > c) {
      temp = c;
      c = b;
      b = temp;
    }

    if (c > d) {
      temp = d;
      d = c;
      c = temp;
    }

    if (b > c) {
      temp = c;
      c = b;
      b = temp;
    }

    if (a > b) {
      temp = b;
      b = a;
      a = temp;
    }

    Verify.instrumentPoint("post-sort");

    assert ((a <= b) && (b <= c) && (c <= d));
  }
}
