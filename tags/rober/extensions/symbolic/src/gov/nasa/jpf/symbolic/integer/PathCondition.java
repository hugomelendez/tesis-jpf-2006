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

import gov.nasa.jpf.jvm.Verify;

/**
 * @author Sarfraz Khurshid (khurshid@lcs.mit.edu)
 *  
 */
public class PathCondition {
  static int symbolicVarCount = 0;

  static boolean flagSolved = false;

  public Constraint header;

  Constraint predicate;

  int count = 0;

  public synchronized boolean _add(Comparator c, int l, int r) {
    flagSolved = false; // C

    return c.compare(l, r);
  }

  public synchronized boolean _add(Comparator c, Expression l, int r) {
    flagSolved = false; // C

    if (l instanceof IntegerConstant) {
      return c.compare(((IntegerConstant) l).value, r);
    }

    return _add(c, l, new IntegerConstant(r));
  }

  public synchronized boolean _add(Comparator c, int l, Expression r) {
    flagSolved = false; // C

    if (r instanceof IntegerConstant) {
      return c.compare(l, ((IntegerConstant) r).value);
    }

    return _add(c, new IntegerConstant(l), r);
  }

  public synchronized boolean _add(Comparator c, Expression l, Expression r) {
    //System.out.println("INSIDE _ADD");
    //System.out.println(l.toString());
    //System.out.println(c.toString());
    //System.out.println(r.toString());
    flagSolved = false; // C

    if (l == r) {
      return c.compare(l);
    }

    if ((l instanceof IntegerConstant) && (r instanceof IntegerConstant)) {
      return c
          .compare(((IntegerConstant) l).value, ((IntegerConstant) r).value);
    }

    //boolean b = Verify.randomBool();
    boolean b = (c == Comparator.EQ) ? (!Verify.randomBool()) : Verify
        .randomBool();

    if (!b) {
      c = c.not();
    }

    Constraint t;

    if (c instanceof NotEqual) {
      if (Verify.randomBool()) {
        c = Comparator.LT;
      } else {
        c = Comparator.GT;
      }
    }

    if ((l instanceof LinearExpression) && (r instanceof LinearExpression)) {
      t = new LinearConstraint(l, c, r);
    } else {
      t = new NonLinearConstraint(l, c, r);
    }

    /* this is buggy somehow */
    if (!hasConstraint(t)) {
      /* if (symIntRangeOk(t)) { */
      t.and = header;
      header = t;
      count++;
      //}
    }

    //System.out.println("going to simplify " + this);
    if (SymbolicConstraints.simplify(this, false)) {
      return b;
    } else {
      Verify.ignoreIf(true);
    }

    return b;
  }

  public synchronized void _addDet(Comparator c, Expression l, int r) {
    flagSolved = false; // C

    _addDet(c, l, new IntegerConstant(r));
  }

  public synchronized void _addDet(Comparator c, int l, Expression r) {
    flagSolved = false; // C

    _addDet(c, new IntegerConstant(l), r);
  }

  public synchronized void _addDet(Comparator c, Expression l, Expression r) {
    Constraint t;

    if (c instanceof NotEqual) {
      if (Verify.randomBool()) {
        c = Comparator.LT;
      } else {
        c = Comparator.GT;
      }
    }

    flagSolved = false; // C

    if ((l instanceof LinearExpression) && (r instanceof LinearExpression)) {
      t = new LinearConstraint(l, c, r);
    } else {
      t = new NonLinearConstraint(l, c, r);
    }

    if (!hasConstraint(t)) {
      /* if (symIntRangeOk(t)) { */
      t.and = header;
      header = t;
      count++;
      //}
    } else {
      return;
    }

    //      System.out.println(toString());
    if (SymbolicConstraints.simplify(this, false)) {
      return;
    } else {
      Verify.ignoreIf(true);
    }
  }

  public synchronized boolean _check(Comparator c, Expression l, int r) {
    flagSolved = false; // C

    return _check(c, l, new IntegerConstant(r));
  }

  public synchronized boolean _check(Comparator c, int l, Expression r) {
    flagSolved = false; // C

    return _check(c, new IntegerConstant(l), r);
  }

  public synchronized boolean _check(Comparator c, Expression l, Expression r) {
    Constraint t;

    flagSolved = false; // C

    // check pc -> predicate
    // !(pc & !pred)
    //c = c.not();

    if (c instanceof NotEqual) {
      if (Verify.randomBool()) {
        c = Comparator.LT;
      } else {
        c = Comparator.GT;
      }
    }

    if ((l instanceof LinearExpression) && (r instanceof LinearExpression)) {
      t = new LinearConstraint(l, c, r);
    } else {
      t = new NonLinearConstraint(l, c, r);
    }

    predicate = t;
    count++;

    //System.out.println("Predicate = " + predicate + " PC = " + this);

    boolean result = SymbolicConstraints.checkImp(this, false);

    //System.out.println("Predicate = " + predicate + " PC = " + this +
    //                   " is (pc & pred) satisfiable = " + result);

    // finished checking predicate can remove it again
    predicate = null;

    return result;
  }

  public int count() {
    return count;
  }

  public boolean hasConstraint(Constraint c) {
    Constraint t = header;

    while (t != null) {
      if (c.equals(t)) {
        return true;
      }

      t = t.and;
    }

    return false;
  }

  public synchronized boolean solve() {
    flagSolved = true;

    return SymbolicConstraintsSolver.solve(this, false);
  }

  public boolean symIntRangeOk(Constraint c) {
    if (c.left instanceof SymbolicInteger) {
      if (c.right instanceof IntegerConstant) {
        return c.comp
            .updateSymbolicIntegerRange((SymbolicInteger) c.left,
                                        ((IntegerConstant) c.right).value);
      }
    }

    if (c.left instanceof IntegerConstant) {
      if (c.right instanceof SymbolicInteger) {
        return c.comp.not()
            .updateSymbolicIntegerRange((SymbolicInteger) c.right,
                                        ((IntegerConstant) c.left).value);
      }
    }

    return true;
  }

  public String toString() {
    return "# = " + count + ((header == null) ? "" : "\n" + header.toString());
  }

  // when flag is true, we have a valid solution
  abstract static class Constraint {
    Expression left;

    Comparator comp;

    Expression right;

    Constraint and;

    Constraint(Expression l, Comparator c, Expression r) {
      left = l;
      comp = c;
      right = r;
    }

    public boolean equals(Object o) {
      if (!(o instanceof Constraint)) {
        return false;
      }

      return left.equals(((Constraint) o).left)
          && comp.equals(((Constraint) o).comp)
          && right.equals(((Constraint) o).right);
    }

    public int hashCode() {
      assert false : "hashCode not designed";
      return 42; // any arbitrary constant will do
      // thanks, FindBugs!
    }
    
    public String toString() {
      return left.toString() + comp.toString() + right.toString()
          + ((and == null) ? "" : " &&\n" + and.toString());
    }
  }

  static class LinearConstraint extends Constraint {
    LinearConstraint(Expression l, Comparator c, Expression r) {
      super(l, c, r);
    }

    public String toString() {
      return /* "%Linear% " + */super.toString();
    }
  }

  static class NonLinearConstraint extends Constraint {
    NonLinearConstraint(Expression l, Comparator c, Expression r) {
      super(l, c, r);
    }

    public String toString() {
      return "%NonLin% " + super.toString();
    }
  }
}
