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

import gov.nasa.jpf.jvm.Verify;


/**
 * @author Sarfraz Khurshid (khurshid@lcs.mit.edu)
 *
 * do _add methods really need "synchronized"?  what about other methods that
 * don't synchronize for accessing "header"?  -peterd & FindBugs
 */
public class StringPathCondition {
  StringConstraint header;
  int              count;

  public 
  synchronized boolean _add (StringComparator c, String l, String r) {
    return c.compare(l, r);
  }

  public 
  synchronized boolean _add (StringComparator c, StringExpression l, String r) {
    if (l instanceof StringConstant) {
      return c.compare(((StringConstant) l).value, r);
    }

    return _add(c, l, new StringConstant(r));
  }

  public 
  synchronized boolean _add (StringComparator c, String l, StringExpression r) {
    if (r instanceof StringConstant) {
      return c.compare(l, ((StringConstant) r).value);
    }

    return _add(c, new StringConstant(l), r);
  }

  public 
  synchronized boolean _add (StringComparator c, StringExpression l, 
                             StringExpression r) {
    //        System.out.println("INSIDE _ADD");
    //        System.out.println(l.toString());
    //        System.out.println(c.toString());
    //        System.out.println(r.toString());
    if (l == r) {
      return c.compare(l);
    }

    if ((l instanceof StringConstant) && (r instanceof StringConstant)) {
      return c.compare(((StringConstant) l).value, ((StringConstant) r).value);
    }

    boolean b = !Verify.randomBool();

    if (!b) {
      c = c.not();
    }

    StringConstraint t;
    t = new StringConstraint(l, c, r);

    if (!hasConstraint(t)) {
      t.and = header;
      header = t;
      count++;
    }

    //  	System.out.println(toString());
    //  	if (Verify.simplify(this)) return b;
    //  	else Verify.ignoreIf(true);
    return b;
  }

  public int count () {
    return count;
  }

  public boolean hasConstraint (StringConstraint c) {
    StringConstraint t = header;

    while (t != null) {
      if (c.equals(t)) {
        return true;
      }

      t = t.and;
    }

    return false;
  }

  public String toString () {
    return "# = " + count + 
           ((header == null)         ? "" : "\n" + header.toString());
  }

  /**
   * DOCUMENT ME!
   */
  public static class StringConstraint {
    StringExpression left;
    StringComparator comp;
    StringExpression right;
    StringConstraint and;

    public StringConstraint (StringExpression l, StringComparator c, 
                             StringExpression r) {
      left = l;
      comp = c;
      right = r;
    }

    public boolean equals (Object o) {
      if (!(o instanceof StringConstraint)) {
        return false;
      }

      return left.equals(((StringConstraint) o).left) && 
             comp.equals(((StringConstraint) o).comp) && 
             right.equals(((StringConstraint) o).right);
    }

    public int hashCode() {
      assert false : "hashCode not designed";
      return 42; // any arbitrary constant will do
      // thanks, FindBugs!
    }

    public String toString () {
      return left.toString() + comp.toString() + right.toString() + 
             ((and == null)         ? "" : " and\n" + and.toString());
    }
  }
}
