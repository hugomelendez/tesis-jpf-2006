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
class BinaryLinearExpression extends LinearExpression {
  Expression left;
  Operator   op;
  Expression right;

  BinaryLinearExpression (Expression l, Operator o, Expression r) {
    left = l;
    op = o;
    right = r;
  }

	public int solution() {
		int l = left.solution();
		int r = right.solution();
		if (op instanceof Plus)
			return l + r;
		else if (op instanceof Minus)
			return l - r;
		else if (op instanceof MultiplyBy)
			return l * r;
		else {
			System.out.println("ERROR in BinaryLinearSolution solution: l " + l + " op " + op + " r " + r);
			return -999;
		}
	}

  public String toString () {
    return "(" + left.toString() + op.toString() + right.toString() + ")";
  }
}
