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

import java.util.Set;


/**
 * @author Sarfraz Khurshid (khurshid@lcs.mit.edu)
 *
 */
public abstract class Expression {
  public static PathCondition pc = new PathCondition();
  static final Plus           PLUS = new Plus();
  static final Minus          MINUS = new Minus();
  static final MultiplyBy     MUL = new MultiplyBy();

  public static Expression _Expression () {
    return new SymbolicInteger();

    //return new IntegerConstant(2 - Verify.random(2));
  }

  public boolean _EQ (Expression r) {
    return pc._add(Comparator.EQ, this, r);

    //        return pc._addEQ(this, r);
  }

  public boolean _GE (Expression r) {
    return pc._add(Comparator.GE, this, r);

    //        return pc._addGE(this, r);
  }

  public boolean _GT (Expression r) {
	  return pc._add(Comparator.GT, this, r);

	  //        return pc._addGE(this, r);
	}

	public boolean _LE (Expression r) {
		return pc._add(Comparator.LE, this, r);

		//        return pc._addGE(this, r);
	  }


  public boolean _LT (Expression r) {
    return pc._add(Comparator.LT, this, r);

    //        return pc._addLT(this, r);
  }

  public boolean _NE (Expression r) {
    return pc._add(Comparator.NE, this, r);

    //        return pc._addNE(this, r);
  }

  public Expression _minus (int i) {
    return new BinaryNonLinearExpression(this, MINUS, new IntegerConstant(i));
  }

  public Expression _minus (Expression e) {
    return new BinaryNonLinearExpression(this, MINUS, e);
  }

  public Expression _mul (int i) {
    return new BinaryNonLinearExpression(this, MUL, new IntegerConstant(i));
  }

  public Expression _mul (Expression e) {
    return new BinaryNonLinearExpression(this, MUL, e);
  }

  public Expression _plus (int i) {
    return new BinaryNonLinearExpression(this, PLUS, new IntegerConstant(i));
  }

  public Expression _plus (Expression e) {
    return new BinaryNonLinearExpression(this, PLUS, e);
  }

  public void _restore_original_fields (Set<?> visited) {
  }

	//TODO test this
  public int solution() {
		System.out.println("Expression Solution request Error: " + this);
  	return -666;
  }
  
}
