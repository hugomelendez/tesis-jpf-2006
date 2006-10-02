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
public class IntegerConstant extends LinearExpression {
  int value;

  public IntegerConstant (int i) {
    value = i;
  }

  public Expression _minus (int i) {
    return new IntegerConstant(value - i);
  }

  public Expression _minus (Expression e) {
    if (e instanceof IntegerConstant) {
      return new IntegerConstant(value - ((IntegerConstant) e).value);
    } else {
      return super._minus(e);
    }
  }

  public Expression _mul (int i) {
    return new IntegerConstant(value * i);
  }

  public Expression _mul (Expression e) {
    if (e instanceof IntegerConstant) {
      return new IntegerConstant(value * ((IntegerConstant) e).value);
    } else if (e instanceof LinearExpression) {
      return new BinaryLinearExpression(this, MUL, e);
    } else {
      return super._mul(e);
    }
  }

  public Expression _plus (int i) {
    return new IntegerConstant(value + i);
  }

  public Expression _plus (Expression e) {
    if (e instanceof IntegerConstant) {
      return new IntegerConstant(value + ((IntegerConstant) e).value);
    } else {
      return super._plus(e);
    }
  }

  public boolean equals (Object o) {
    if (!(o instanceof IntegerConstant)) {
      return false;
    }

    return value == ((IntegerConstant) o).value;
  }

  public int hashCode() {
    return value;
  }
  
  public String toString () {
    return value + "";
  }

  public int value () {
    return value;
  }
  
  public int solution() {
  		return value;
  }
}
