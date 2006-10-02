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
class LinearExpression extends Expression {
  public Expression _minus (int i) {
    return new BinaryLinearExpression(this, MINUS, new IntegerConstant(i));
  }

  public Expression _minus (Expression e) {
    if (e instanceof LinearExpression) {
      return new BinaryLinearExpression(this, MINUS, e);
    } else {
      return super._minus(e);
    }
  }

  public Expression _mul (int i) {
    return new BinaryLinearExpression(this, MUL, new IntegerConstant(i));
  }

  public Expression _mul (Expression e) {
    if (e instanceof IntegerConstant) {
      return new BinaryLinearExpression(this, MUL, e);
    } else {
      return super._mul(e);
    }
  }

  public Expression _plus (int i) {
    return new BinaryLinearExpression(this, PLUS, new IntegerConstant(i));
  }

  public Expression _plus (Expression e) {
    if (e instanceof LinearExpression) {
      return new BinaryLinearExpression(this, PLUS, e);
    } else {
      return super._plus(e);
    }
  }
}
