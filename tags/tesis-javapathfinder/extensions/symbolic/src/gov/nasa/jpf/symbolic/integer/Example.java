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
package gov.nasa.jpf.symbolic.integer;

/**
 * @author Sarfraz Khurshid (khurshid@lcs.mit.edu)
 *
 */
class Example {
  public static void main (String[] a) {
    PathCondition pc = new PathCondition();

    Expression    e1 = new SymbolicInteger();
    Expression    e2 = new IntegerConstant(2);
    Expression    e3 = new IntegerConstant(3);

    if (pc._add(Comparator.LT, e1, e2)) {
      System.out.println("x == 1");

      if (pc._add(Comparator.GT, e1, e3)) {
        System.out.println("should not happen");
      }

      System.out.println("x == 1  and x != 2");
    }

    //Verify.simplify(pc);
    //System.out.println(pc);
  }
}
