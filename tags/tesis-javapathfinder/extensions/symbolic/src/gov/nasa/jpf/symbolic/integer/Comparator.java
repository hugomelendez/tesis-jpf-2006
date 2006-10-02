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
public abstract class Comparator {
  public static final Equal        EQ = new Equal();
  public static final NotEqual     NE = new NotEqual();
  public static final LessThan     LT = new LessThan();
  public static final LessEqual    LE = new LessEqual();
  public static final GreaterThan  GT = new GreaterThan();
  public static final GreaterEqual GE = new GreaterEqual();

  public abstract boolean compare (int i, int j);

  public abstract boolean compare (Expression e);

  public abstract Comparator not ();

  public abstract boolean updateSymbolicIntegerRange (SymbolicInteger e, int i);
}
