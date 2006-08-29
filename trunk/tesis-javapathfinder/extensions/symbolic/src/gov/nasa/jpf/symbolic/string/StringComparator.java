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
package gov.nasa.jpf.symbolic.string;

/**
 * @author Sarfraz Khurshid (khurshid@lcs.mit.edu)
 *
 */
public abstract class StringComparator {
  public static final StringEquals              EQ = new StringEquals();
  public static final StringNotEquals           NEQ = new StringNotEquals();
  public static final StringEqualsIgnoreCase    EQIC = 
        new StringEqualsIgnoreCase();
  public static final StringNotEqualsIgnoreCase NEQIC = 
        new StringNotEqualsIgnoreCase();

  public abstract boolean compare (String i, String j);

  public abstract boolean compare (StringExpression e);

  public abstract StringComparator not ();
}
