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

//import gov.nasa.jpf.jvm.reflection.Reflection;
import de.fhg.first.fd.*;
import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.Types;

import java.util.*;


/**
 * The MJI class that decodes constraints and finds solutions 
 * to the symbolic variables if the PC is satisfiable. Note this
 * should only be used if the PC is satifiable, otherwise the solver 
 * will forever try to find a solution
 */
public class JPF_gov_nasa_jpf_symbolic_integer_SymbolicConstraintsSolver {
  public static String qualifier = "gov.nasa.jpf.symbolic.integer.";
  static Solver        solver;

  // static Var[] s;
  static int     SIZE = 0; // number of symbolic integers
  static HashMap<Integer,Var> symVar; // a map between symbols and Vars
  public static int[]   refs; // a map between counters and Refs

  // to be used in s
  public static int count; // counts symbolic integers
  
  static Var getExpression (int eRef, MJIEnv env) {
    if (eRef == -1) {
      return null;
    }

    String eType = getType(eRef,env);

    if (eType.equals(qualifier + "IntegerConstant")) {
      int value = env.getIntField(eRef, "value");

      return solver.newVar(value);
    } else if (eType.equals(qualifier + "SymbolicInteger")) {
      Integer v = new Integer(eRef);
      Var     s_v = symVar.get(v);

      if (s_v == null) {
        s_v = solver.newVar();
        symVar.put(v, s_v);


        //symVar.put(s_v, v); // trick
        //System.out.println("eRef -----"+eRef+" count "+count);
        refs[count] = eRef;
        count++;
      }

      return s_v;
    } else if (eType.equals(qualifier + "BinaryLinearExpression")) {
      int    opRef = env.getReferenceField(eRef, "op");

      int    e_leftRef = env.getReferenceField(eRef, "left");
      int    e_rightRef = env.getReferenceField(eRef, "right");

      String opType = getType(opRef,env);
      String e_leftType = getType(e_leftRef,env);
      String e_rightType = getType(e_rightRef,env);

      if (opType.equals(qualifier + "Plus")) {
        return (Var) getExpression(e_leftRef, env)
                       .plus(getExpression(e_rightRef, env));
      } else if (opType.equals(qualifier + "Minus")) {
        return (Var) getExpression(e_leftRef, env)
                       .minus(getExpression(e_rightRef, env));
      } else { // multiply

        if (e_leftType.equals(qualifier + "IntegerConstant") || 
                e_rightType.equals(qualifier + "IntegerConstant")) {
          return (Var) getExpression(e_leftRef, env)
                         .times(getExpression(e_rightRef, env));
        } else // I have to eextend this to non-linear expressions
        {
          throw new RuntimeException("Error: Binary Non Linear Operation");
        }
      }
    } else {
      throw new RuntimeException("Error: Binary Non Linear Expression " + 
                                 eType);
    }
  }

  static void EQ (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    Var lExp = getExpression(lRef, env);
    Var rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

    solver.eq(lExp, rExp);
  }

  static void GEQ (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    Var lExp = getExpression(lRef, env);
    Var rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

    solver.geq(lExp, rExp);
  }

  static void GT (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    Var lExp = getExpression(lRef, env);
    Var rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

    solver.gt(lExp, rExp);
  }

  static void LEQ (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    Var lExp = getExpression(lRef, env);
    Var rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

    solver.leq(lExp, rExp);
  }

  static void LT (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    Var lExp = getExpression(lRef, env);
    Var rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

    solver.lt(lExp, rExp);
  }

  static void NEQ (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    Var lExp = getExpression(lRef, env);
    Var rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

    solver.neq(lExp, rExp);
  }

  static void createSolver (int expressionLength) {
    solver = new de.fhg.first.fd.firstcs.Solver();
    SIZE = expressionLength + 1;
    refs = new int[SIZE];

    symVar = new HashMap<Integer,Var>();
    count = 0;
  }
  
  static boolean solve (boolean debugFlag, MJIEnv env) {
    if ((solver == null) || (refs == null)) {
      return true;
    }

    Var[] s = new Var[count];

    for (int i = 0; i < count; i++) {
      Integer eRefObj = new Integer(refs[i]);
      s[i] = symVar.get(eRefObj);

      if (s[i] == null) {
        System.out.println("something wrong " + i + refs[i]);
      }
    }

    Labeling l = solver.newLabeling(s);

    if (l.nextSolution()) {
      // the solutions are now in s
      for (int i = 0; i < count; i++) {
        Integer eRefObj = new Integer(refs[i]);
        Var     solutionVar = symVar.get(eRefObj);

        try {
          env.setIntField(refs[i], "solution", solutionVar.getVal());
        } catch (UninstantiatedException e) {
          System.out.println("unintantiated variable " + solutionVar);
        }
      }

      solver = null;
      s = null;

      return true;
    }

    solver = null;
    s = null;

    return false;
  }
  
  public static String getType (int objRef, MJIEnv env) {
    return Types.getTypeName(env.getTypeName(objRef));
  }
  public static boolean solve(MJIEnv env, int clsObjRef, int objRef, boolean b) {
  	
		// System.out.println("debugFlag " + b);
		if (objRef == -1) {
			System.out.println("empty path condition");

			return true;
		}

		int expressionLength = env.getStaticIntField(
				qualifier + "PathCondition",
				"symbolicVarCount");

		// number of symbolic values in the path condition
		createSolver(expressionLength);

		int cRef = env.getReferenceField(objRef, "header");

		boolean constraintsPresent = false;

		while (cRef != -1) { // to write getType

			if (getType(cRef, env).equals(
					qualifier
							+ "PathCondition$LinearConstraint")) {
				int c_compRef = env.getReferenceField(cRef, "comp");

				int c_leftRef = env.getReferenceField(cRef, "left");
				int c_rightRef = env.getReferenceField(cRef, "right");

				String c_compType = getType(c_compRef, env);

				if (c_compType.equals(qualifier + "Equal")
						|| c_compType.equals(qualifier
								+ "EQ")) {
					EQ(c_leftRef, c_rightRef, env);
				} else if (c_compType.equals(qualifier
						+ "NotEqual")
						|| c_compType.equals(qualifier
								+ "NE")) {
					NEQ(c_leftRef, c_rightRef, env);
				} else if (c_compType.equals(qualifier
						+ "LessThan")
						|| c_compType.equals(qualifier
								+ "LT")) {
					LT(c_leftRef, c_rightRef, env);
				} else if (c_compType.equals(qualifier
						+ "GreaterEqual")
						|| c_compType.equals(qualifier
								+ "GE")) {
					GEQ(c_leftRef, c_rightRef, env);
				} else if (c_compType.equals(qualifier
						+ "LessEqual")
						|| c_compType.equals(qualifier
								+ "LE")) {
					LEQ(c_leftRef, c_rightRef, env);
				} else if (c_compType.equals(qualifier
						+ "GreaterThan")
						|| c_compType.equals(qualifier
								+ "GT")) {
					GT(c_leftRef, c_rightRef, env);
				} else {
					System.out.println("unknown comparison operator"
							+ c_compType);
				}

				constraintsPresent = true;
			} else {
				// I have to extend it with non-linear constraints
				System.out.println("non-linear constraint");
			}

			cRef = env.getReferenceField(cRef, "and");
		}

		boolean result = false;

		if (constraintsPresent) {
			result = solve(b, env);
		}

		if (!result) {
			System.out.println("NO SOLUTION!");
		}
		
		return result;
	}
 
}
