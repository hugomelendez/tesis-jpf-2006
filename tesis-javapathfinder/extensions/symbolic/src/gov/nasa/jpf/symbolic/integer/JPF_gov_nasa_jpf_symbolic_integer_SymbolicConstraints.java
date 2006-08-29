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

import gov.nasa.jpf.jvm.MJIEnv;
import gov.nasa.jpf.jvm.Types;

import java.util.*;

// new cream library for constraint solving
import jp.ac.kobe_u.cs.cream.*;
import jp.ac.kobe_u.cs.cream.Solver;


//import gov.nasa.jpf.jvm.reflection.Reflection;
import omega.*;

/**
 * The MJI class that decodes constraints and calls the omega lib to
 * see if the patchondition is satisfiable
 */
public class JPF_gov_nasa_jpf_symbolic_integer_SymbolicConstraints {
  public static String    qualifier = "gov.nasa.jpf.symbolic.integer.";
  static Relation         pathConstraints;
  static VarDecl[]        s;
  static F_And             pcRoot;
  static ConstraintHandle pcHandle;
  static int              SIZE = 0; // number of symbolic integers
  static HashMap<Integer,Object> symVar; // a map between symbolic integers and indexes
  
  //Cream Support
  static Network net;
  static Solver solver; 
  
  // to be used in s
  static int count; // counts symbolic integers

  static boolean isSatisfiable (boolean debugFlag) {
    if (pathConstraints == null) {
      return true;
    }

    if (debugFlag) {
      pathConstraints.prefixPrint();
    }

    //System.out.println("test constraints");
    //pathConstraints.simplify();
    boolean result = pathConstraints.isSatisfiable();

    if (debugFlag) {
      System.out.println("After simplification:");
      pathConstraints.prefixPrint();

      //Iterator symVarIt = symVar.values().iterator();
      Iterator<Integer> symVarIt = symVar.keySet().iterator();

      System.out.println("Mapping:");

      while (symVarIt.hasNext()) {
        Integer v = symVarIt.next();
        int     hashC = v.intValue() ^ 0xABCD;
        System.out.println("INT_" + hashC + " : " + "In_" + 
                           (((Integer) symVar.get(v)).intValue() + 1));
      }
    }

    pathConstraints.delete();
    pathConstraints = null;

    s = null;
    pcRoot = null;
    pcHandle = null;
    symVar = null;

    return result;
  }

  //Cream Support
  static public void createSolver() {
  	net = new Network();
  	symVar = new HashMap<Integer,Object>();
  }
  
  static boolean isSatisfiableCream (MJIEnv env) {
  	solver = new DefaultSolver(net);
  	//System.out.println("solving = " + net);
  	Solution solution = solver.findBest();
  	if (solution != null) {
  		Iterator<Integer> symVarIt = symVar.keySet().iterator();
  		while (symVarIt.hasNext()) {
  			Integer v = symVarIt.next();
  			int sValue = solution.getIntValue((IntVariable)symVar.get(v));
  			env.setIntField(v.intValue(),"solution",sValue);
  		}
  	}
  	net = null;
    return solution != null;
  }

  public static boolean solveCream (MJIEnv env) {
  	solver = new DefaultSolver(net);
  	//System.out.println("solving = " + net);
  	Solution solution = solver.findFirst(1000);
  	if (solution != null) {
  		Iterator<Integer> symVarIt = symVar.keySet().iterator();
  		while (symVarIt.hasNext()) {
  			Integer v = symVarIt.next();
  			int sValue = solution.getIntValue((IntVariable)symVar.get(v));
  			env.setIntField(v.intValue(),"solution",sValue);
  		}
  	}
  	net = null;
    return solution != null;
  }

  
  static IntVariable getExpression (int eRef, MJIEnv env) {
    if (eRef == -1) {
      return null;
    }

    String eType = getType(eRef,env);

    if (eType.equals(qualifier + "IntegerConstant")) {
      int value = env.getIntField(eRef, "value");
      return new IntVariable(net,value);
    } else if (eType.equals(qualifier + "SymbolicInteger")) {
      Integer v = new Integer(eRef);
      IntVariable s_v = (IntVariable) symVar.get(v);

      if (s_v == null) {
        s_v = new IntVariable(net);
        symVar.put(v, s_v);
        count++;
      }

      return s_v;
    } else if (eType.equals(qualifier + "BinaryLinearExpression") || 
    		eType.equals(qualifier + "BinaryNonLinearExpression")) {
      int    opRef = env.getReferenceField(eRef, "op");

      int    e_leftRef = env.getReferenceField(eRef, "left");
      int    e_rightRef = env.getReferenceField(eRef, "right");

      String opType = getType(opRef,env);
      String e_leftType = getType(e_leftRef,env);
      String e_rightType = getType(e_rightRef,env);

      if (opType.equals(qualifier + "Plus")) {
        return getExpression(e_leftRef, env)
          .add(getExpression(e_rightRef, env));
      } else if (opType.equals(qualifier + "Minus")) {
        return getExpression(e_leftRef, env)
          .subtract(getExpression(e_rightRef, env));
      } else { // multiply
        if (e_leftType.equals(qualifier + "IntegerConstant") || 
                e_rightType.equals(qualifier + "IntegerConstant")) {
          return getExpression(e_leftRef, env)
            .multiply(getExpression(e_rightRef, env));
        } else // I have to extend this to non-linear expressions
        {
        	return getExpression(e_leftRef, env)
            .multiply(getExpression(e_rightRef, env));
          //throw new RuntimeException("Error: Binary Non Linear Operation");
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

    IntVariable lExp = getExpression(lRef, env);
    IntVariable rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }
    lExp.equals(rExp);
  }

  static void GEQ (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    IntVariable lExp = getExpression(lRef, env);
    IntVariable rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

    lExp.ge(rExp);
  }

  static void GT (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    IntVariable lExp = getExpression(lRef, env);
    IntVariable rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

   lExp.gt(rExp);
  }

  static void LEQ (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    IntVariable lExp = getExpression(lRef, env);
    IntVariable rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

    lExp.le(rExp);
  }

  static void LT (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    IntVariable lExp = getExpression(lRef, env);
    IntVariable rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

    lExp.lt(rExp);
  }

  static void NEQ (int lRef, int rRef, MJIEnv env) {
    if ((lRef == -1) || (rRef == -1)) {
      return;
    }

    IntVariable lExp = getExpression(lRef, env);
    IntVariable rExp = getExpression(rRef, env);

    if ((lExp == null) || (rExp == null)) {
      return;
    }

    lExp.notEquals(rExp);
  }
  
  // End Cream Support
  

  static void setValues (int c) {
    pcHandle.updateConstant(c);
  }

  static void setValues (int eRef, int mult, MJIEnv env) {
    if (eRef == -1) {
      return;
    }

    String eType = getType(eRef,env);

    if (eType.equals(qualifier + "IntegerConstant")) {
      int value = env.getIntField(eRef, "value");
      pcHandle.updateConstant(value * mult);
    } else if (eType.equals(qualifier + "SymbolicInteger")) {
      Integer v = new Integer(eRef);
      Integer index = (Integer) symVar.get(v);

      if (index == null) {
        index = new Integer(count);
        count++;
        symVar.put(v, index);
      }

      pcHandle.updateCoefficient(s[index.intValue()], mult);
    } else if (eType.equals(qualifier + "BinaryLinearExpression")) {
      int    opRef = env.getReferenceField(eRef, "op");

      int    e_leftRef = env.getReferenceField(eRef, "left");
      int    e_rightRef = env.getReferenceField(eRef, "right");

      String opType = getType(opRef,env);
      String e_leftType = getType(e_leftRef,env);
      String e_rightType = getType(e_rightRef,env);

      if (opType.equals(qualifier + "Plus")) {
        setValues(e_leftRef, mult, env);
        setValues(e_rightRef, mult, env);
      } else if (opType.equals(qualifier + "Minus")) {
        setValues(e_leftRef, mult, env);
        setValues(e_rightRef, -mult, env);
      } else { // multiply

        if (e_leftType.equals(qualifier + "IntegerConstant")) {
          int value = env.getIntField(e_leftRef, "value");
          setValues(e_rightRef, mult * value, env);
        } else if (e_rightType.equals(qualifier + "IntegerConstant")) {
          int value = env.getIntField(e_rightRef, "value");
          setValues(e_leftRef, mult * value, env);
        } else {
          throw new RuntimeException("Error: Binary Non Linear Operation");
        }
      }
    } else {
      throw new RuntimeException("Error: Binary Non Linear Expression " + 
                                 eType);
    }
  }

 
  static void addEQConstraint () {
    pcHandle = pcRoot.addEQ(false);
  }

  static void addGEQConstraint () {
    pcHandle = pcRoot.addGEQ(false);
  }

  static void addNEQConstraint () {
    throw new RuntimeException("Error: NEQ Constraint not handled!!!");

    // pcHandle = pcRoot.addNot().addAnd().addEQ(false);
  }

  static void createRelation (int expressionLength) {
    RelBody.skipSetChecks = 1; // don't know what this is !!!
    SIZE = expressionLength + 1;
    pathConstraints = new Relation(SIZE);
    s = new VarDecl[SIZE];

    for (int i = 0; i < SIZE; i++) {
      s[i] = pathConstraints.setVar(i + 1);

      //System.out.println(s[i]);
    }

    pcRoot = pathConstraints.addAnd();

    symVar = new HashMap<Integer,Object>();
    count = 1;
  }
  
  // simplify from Verify
  
  public static String getType (int objRef, MJIEnv env) {
    return Types.getTypeName(env.getTypeName(objRef));
  }
  
  public static final boolean simplify(MJIEnv env, int clsObjRef, int objRef,
			boolean b) {
		// System.out.println("debugFlag " + b);
		if (objRef == -1) {
			System.out.println("empty path condition");

			return true;
		}

		int expressionLength = env.getStaticIntField(
				qualifier + "PathCondition",
				"symbolicVarCount");

		// number of symbolic values in the path condition
		createRelation(expressionLength);

		int cRef = env.getReferenceField(objRef, "header");

		boolean constraintsPresent = false;

		while (cRef != -1) { // to write getType

			if (getType(cRef, env).equals(
					qualifier + "PathCondition$LinearConstraint")) {
				int c_compRef = env.getReferenceField(cRef, "comp");

				int c_leftRef = env.getReferenceField(cRef, "left");
				int c_rightRef = env.getReferenceField(cRef, "right");

				String c_compType = getType(c_compRef, env);

				if (c_compType.equals(qualifier + "Equal")
						|| c_compType.equals(qualifier + "EQ")) {
					addEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
				} else if (c_compType.equals(qualifier + "NotEqual")
						|| c_compType.equals(qualifier + "NE")) {
					addNEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
				} else if (c_compType.equals(qualifier + "LessThan")
						|| c_compType.equals(qualifier + "LT")) {
					addGEQConstraint();
					setValues(c_rightRef, 1, env);
					setValues(c_leftRef, -1, env);
					setValues(-1);
				} else if (c_compType.equals(qualifier + "GreaterEqual")
						|| c_compType.equals(qualifier + "GE")) {
					addGEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
				} else if (c_compType.equals(qualifier + "LessEqual")
						|| c_compType.equals(qualifier + "LE")) {
					addGEQConstraint();
					setValues(c_rightRef, 1, env);
					setValues(c_leftRef, -1, env);
				} else {
					addGEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
					setValues(-1);
				}

				constraintsPresent = true;
			} else {
				System.out.println("non-linear constraint");
			}

			cRef = env.getReferenceField(cRef, "and");
		}

		boolean result = true;

		if (constraintsPresent) {
			result = isSatisfiable(b);
		}
		
		return result;
	}
  
  public static final boolean checkImp(MJIEnv env, int clsObjRef, int objRef,
			boolean b) {
		// System.out.println("debugFlag " + b);
		if (objRef == -1) {
			System.out.println("empty path condition");
			return true;
		}

		int expressionLength = env.getStaticIntField(
				qualifier + "PathCondition",
				"symbolicVarCount");

		int cRef = env.getReferenceField(objRef, "header");

		int cPredRef = env.getReferenceField(objRef, "predicate");

		//number of symbolic values in the path condition including predicate
	  createRelation(expressionLength);

		//adding predicate first

		boolean constraintsPresent = false;

		while (cPredRef != -1) { // to write getType

			if (getType(cPredRef, env).equals(
					qualifier
							+ "PathCondition$LinearConstraint")) {
				int c_compRef = env.getReferenceField(cPredRef, "comp");

				int c_leftRef = env.getReferenceField(cPredRef, "left");
				int c_rightRef = env.getReferenceField(cPredRef, "right");

				String c_compType = getType(c_compRef, env);

				if (c_compType.equals(qualifier + "Equal")
						|| c_compType.equals(qualifier
								+ "EQ")) {
					addEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
				} else if (c_compType.equals(qualifier
						+ "NotEqual")
						|| c_compType.equals(qualifier
								+ "NE")) {
					addNEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
				} else if (c_compType.equals(qualifier
						+ "LessThan")
						|| c_compType.equals(qualifier
								+ "LT")) {
					addGEQConstraint();
					setValues(c_rightRef, 1, env);
					setValues(c_leftRef, -1, env);
					setValues(-1);
				} else if (c_compType.equals(qualifier
						+ "GreaterEqual")
						|| c_compType.equals(qualifier
								+ "GE")) {
					addGEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
				} else if (c_compType.equals(qualifier
						+ "LessEqual")
						|| c_compType.equals(qualifier
								+ "LE")) {
					addGEQConstraint();
					setValues(c_rightRef, 1, env);
					setValues(c_leftRef, -1, env);
				} else {
					addGEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
					setValues(-1);
				}

				constraintsPresent = true;
			} else {
				System.out.println("non-linear constraint");
			}

			cPredRef = env.getReferenceField(cPredRef, "and");
		}

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
					addEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
				} else if (c_compType.equals(qualifier
						+ "NotEqual")
						|| c_compType.equals(qualifier
								+ "NE")) {
					addNEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
				} else if (c_compType.equals(qualifier
						+ "LessThan")
						|| c_compType.equals(qualifier
								+ "LT")) {
					addGEQConstraint();
					setValues(c_rightRef, 1, env);
					setValues(c_leftRef, -1, env);
					setValues(-1);
				} else if (c_compType.equals(qualifier
						+ "GreaterEqual")
						|| c_compType.equals(qualifier
								+ "GE")) {
					addGEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
				} else if (c_compType.equals(qualifier
						+ "LessEqual")
						|| c_compType.equals(qualifier
								+ "LE")) {
					addGEQConstraint();
					setValues(c_rightRef, 1, env);
					setValues(c_leftRef, -1, env);
				} else {
					addGEQConstraint();
					setValues(c_leftRef, 1, env);
					setValues(c_rightRef, -1, env);
					setValues(-1);
				}

				constraintsPresent = true;
			} else {
				System.out.println("non-linear constraint");
			}

			cRef = env.getReferenceField(cRef, "and");
		}

		boolean result = true;

		if (constraintsPresent) {
			result = isSatisfiable(b);
		}

		return result;
	}
}
