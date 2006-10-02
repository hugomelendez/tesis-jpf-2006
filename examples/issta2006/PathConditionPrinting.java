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
package issta2006;


import gov.nasa.jpf.jvm.JPF_gov_nasa_jpf_jvm_Verify;
import gov.nasa.jpf.jvm.MJIEnv;

/**
 * @author wvisser
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class PathConditionPrinting {
	public static String    qualifier = "gov.nasa.jpf.symbolic.integer.";
	
	static String getValues (int eRef, int mult, MJIEnv env) {
    if (eRef == -1) {
      return "";
    }

    String eType = JPF_gov_nasa_jpf_jvm_Verify.getType(eRef, env);

    if (eType.equals(qualifier + "IntegerConstant")) {
      int value = env.getIntField(eRef, "value");
      return ""+value;
    } else if (eType.equals(qualifier + "SymbolicInteger")) {
    	int name_ref = env.getReferenceField(eRef,"name");
      return env.getStringObject(name_ref) + "(" + env.getIntField(eRef,"solution") + ")";
    } else if (eType.equals(qualifier + "BinaryLinearExpression")) {
      int    opRef = env.getReferenceField(eRef, "op");

      int    e_leftRef = env.getReferenceField(eRef, "left");
      int    e_rightRef = env.getReferenceField(eRef, "right");

      String opType = JPF_gov_nasa_jpf_jvm_Verify.getType(opRef, env);
      String e_leftType = JPF_gov_nasa_jpf_jvm_Verify.getType(e_leftRef, env);
      String e_rightType = JPF_gov_nasa_jpf_jvm_Verify.getType(e_rightRef, env);

      if (opType.equals(qualifier + "Plus")) {
        return getValues(e_leftRef, mult, env) + " PLUS " +
               getValues(e_rightRef, mult, env);
      } else if (opType.equals(qualifier + "Minus")) {
      	return getValues(e_leftRef, mult, env) + " MINUS " +
        			 getValues(e_rightRef, -mult, env);
      } else { // multiply
        if (e_leftType.equals(qualifier + "IntegerConstant")) {
          int value = env.getIntField(e_leftRef, "value");
          return getValues(e_rightRef, mult * value, env);
        } else if (e_rightType.equals(qualifier + "IntegerConstant")) {
          int value = env.getIntField(e_rightRef, "value");
          return getValues(e_leftRef, mult * value, env);
        } else {
          throw new RuntimeException("Error: Binary Non Linear Operation");
        }
      }
    } else {
      throw new RuntimeException("Error: Binary Non Linear Expression " + 
                                 eType);
    }
  }

	
	public static String decodePC(MJIEnv env, int objRef) {
		String result = "";
		
		if (objRef == -1) {
      return "empty";
    }

    int expressionLength = env.getStaticIntField(qualifier +
                                                 "PathCondition",
                                                 "symbolicVarCount");


    // number of symbolic values in the path condition
    int     cRef = env.getReferenceField(objRef, "header");

    boolean constraintsPresent = false;

    while (cRef != -1) { // to write getType

      if (JPF_gov_nasa_jpf_jvm_Verify.getType(cRef, env)
            .equals(qualifier + "PathCondition$LinearConstraint")) {
        int    c_compRef = env.getReferenceField(cRef, "comp");

        int    c_leftRef = env.getReferenceField(cRef, "left");
        int    c_rightRef = env.getReferenceField(cRef, "right");

        String c_compType = JPF_gov_nasa_jpf_jvm_Verify.getType(c_compRef, env);

        if (c_compType.equals(qualifier + "Equal") ||
                c_compType.equals(qualifier + "EQ")) {
        	result += getValues(c_leftRef, 1, env) + " == " +
                   getValues(c_rightRef, -1, env);
        } else if (c_compType.equals(qualifier + "NotEqual") ||
                       c_compType.equals(qualifier + "NE")) {
        	result += getValues(c_leftRef, 1, env) + " != " +
          			   getValues(c_rightRef, -1, env);
        } else if (c_compType.equals(qualifier + "LessThan") ||
                       c_compType.equals(qualifier + "LT")) {
        	result += getValues(c_leftRef, 1, env) + " < " +
 			   					 getValues(c_rightRef, -1, env);
        } else if (c_compType.equals(qualifier + "GreaterEqual") ||
                       c_compType.equals(qualifier + "GE")) {
        	result += getValues(c_leftRef, 1, env) + " >= " +
 					 getValues(c_rightRef, -1, env);
        } else if (c_compType.equals(qualifier + "LessEqual") ||
                       c_compType.equals(qualifier + "LE")) {
        	result += getValues(c_leftRef, 1, env) + " <= " +
					 				 getValues(c_rightRef, -1, env);
        } else {
        	result += getValues(c_leftRef, 1, env) + " > " +
	 				 getValues(c_rightRef, -1, env);
        }

        constraintsPresent = true;
      } else {
        System.out.println("non-linear constraint");
      }
      result += " && ";
      cRef = env.getReferenceField(cRef, "and");
    }

		return result;
	}
}
