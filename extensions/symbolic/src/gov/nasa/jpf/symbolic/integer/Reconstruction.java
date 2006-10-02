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
import gov.nasa.jpf.jvm.*;

import java.util.*;
import omega.*;

//Author: Corina Pasareanu
/* reconstructs the constraints from the path condition */
public class Reconstruction {

	static Relation r;

	static F_Exists exists_;

	static F_And exists_stuff;

	static String qualifier = "gov.nasa.jpf.symbolic.integer.";

	static HashMap<Integer,VarDecl> symVar; // a map between symbolic integers and VarDecl

	private static String getType (int objRef, MJIEnv env) {
    return Types.getTypeName(env.getTypeName(objRef));
  }
	public static void createRelation(int problemSize) {
		symVar = new HashMap<Integer,VarDecl>();
		RelBody.skipSetChecks = 1;
		r = new Relation(problemSize); // Corina: TODO!!! change here the param
		exists_ = r.addAnd().addExists();
		exists_stuff = exists_.addAnd();
	}

	public static void updateBindings(MJIEnv env, int root, int key_number, int keyRef) {
		// add constraint key==key_value
		VarDecl key = r.setVar(key_number);
		ConstraintHandle handle = exists_stuff.addEQ(false);
		handle.updateCoefficient(key, 1);
		updateWithExpression(handle, env, keyRef, -1);		
	}

	public static void reconstructPathCondition(MJIEnv env) {

		int pcRef = env.getStaticObjectField(qualifier + "Expression", "pc");
		if (pcRef == -1)
			return;

		int cRef = env.getReferenceField(pcRef, "header");
		while (cRef != -1) {

			String constraintType = getType(cRef,env);

			if (constraintType.equals(qualifier
					+ "PathCondition$LinearConstraint")) {

				int c_compRef = env.getReferenceField(cRef, "comp");
				String c_compType =  getType(
						c_compRef, env);

				int c_leftRef = env.getReferenceField(cRef, "left");
				int c_rightRef = env.getReferenceField(cRef, "right");

				if (c_compType.equals(qualifier + "Equal")
						|| c_compType.equals(qualifier + "EQ")) {
					ConstraintHandle handle = exists_stuff.addEQ(false);
					updateWithExpression(handle, env, c_leftRef, 1);
					updateWithExpression(handle, env, c_rightRef, -1);
				}

				else if (c_compType.equals(qualifier + "NotEqual")
						|| c_compType.equals(qualifier + "NE")) {
					System.out.println("error here");
					assert (false);
					return;
				}

				else if (c_compType.equals(qualifier + "LessThan")
						|| c_compType.equals(qualifier + "LT")) {
					ConstraintHandle handle = exists_stuff.addGEQ(false);
					updateWithExpression(handle, env, c_leftRef, -1);
					updateWithExpression(handle, env, c_rightRef, 1);
					handle.updateConstant(-1);
				} else if (c_compType.equals(qualifier + "GreaterEqual")
						|| c_compType.equals(qualifier + "GE")) {
					ConstraintHandle handle = exists_stuff.addGEQ(false);
					updateWithExpression(handle, env, c_leftRef, 1);
					updateWithExpression(handle, env, c_rightRef, -1);
				}

				else if (c_compType.equals(qualifier + "LessEqual")
						|| c_compType.equals(qualifier + "LE")) {
					ConstraintHandle handle = exists_stuff.addGEQ(false);
					updateWithExpression(handle, env, c_leftRef, -1);
					updateWithExpression(handle, env, c_rightRef, 1);
				}

				else { // GT
					ConstraintHandle handle = exists_stuff.addGEQ(false);
					updateWithExpression(handle, env, c_leftRef, 1);
					updateWithExpression(handle, env, c_rightRef, -1);
					handle.updateConstant(-1);
				}
			}

			else {
				System.out
						.println("non-linear constraint!!! " + constraintType);
			}

			cRef = env.getReferenceField(cRef, "and");
		}

		return;
	}

	static void updateWithExpression(ConstraintHandle handle, MJIEnv env,
			int eRef, int mult) {

		if (eRef == -1) {
			assert (false);
			return;
		}

		String eType =  getType(eRef, env);
		if (eType.equals(qualifier + "IntegerConstant")) {
			int value = env.getIntField(eRef, "value");
			handle.updateConstant(value);

		} else if (eType.equals(qualifier + "SymbolicInteger")) {
			// mapping to be changed 
			Integer v = new Integer(eRef);
			VarDecl d = symVar.get(v);

			if (d == null) {
				d = exists_.declare();
				symVar.put(v, d);
			}
			handle.updateCoefficient(d, mult);

		} else if (eType.equals(qualifier + "BinaryLinearExpression")) {
			int opRef = env.getReferenceField(eRef, "op");
			String opType =  getType(opRef, env);

			int e_leftRef = env.getReferenceField(eRef, "left");
			int e_rightRef = env.getReferenceField(eRef, "right");

			if (opType.equals(qualifier + "Plus")) {
				updateWithExpression(handle, env, e_leftRef, mult);
				updateWithExpression(handle, env, e_rightRef, mult);

			} else if (opType.equals(qualifier + "Minus")) {
				updateWithExpression(handle, env, e_leftRef, mult);
				updateWithExpression(handle, env, e_rightRef, -mult);
			} else { // multiply

				String e_leftType =  getType(e_leftRef, env);
				String e_rightType =  getType(e_rightRef, env);

				if (e_leftType.equals(qualifier + "IntegerConstant")) {
					int value = env.getIntField(e_leftRef, "value");
					updateWithExpression(handle, env, e_rightRef, mult * value);

				} else if (e_rightType.equals(qualifier + "IntegerConstant")) {
					int value = env.getIntField(e_rightRef, "value");
					updateWithExpression(handle, env, e_leftRef, mult * value);
				} else {
					throw new RuntimeException(
							"Error: Binary Non Linear Operation");
				}

			}

		}

	}

	public static Relation getRelation(boolean flag) {
		if (flag)
			r.prefixPrint();
		r.simplify();
		if (flag)
			r.prefixPrint();
		return r;
	}
}

