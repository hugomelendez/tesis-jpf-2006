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
package issta2006.BinTree;

import gov.nasa.jpf.jvm.*;
import issta2006.Counter.*;

import java.util.*;


public class JPF_issta2006_BinTree_BinTree {

	private static Set<String> tests = new HashSet<String>();

	private static Set<String> abs_states = new HashSet<String>();

	public static int gen_native(MJIEnv env, int objRef, int br, int n0, int x,
			int n1, int n2) {
		String res = br + ",";
//For Basic Block Coverage
//START comment here
		
		int temp;
		if (n0 == -1) {
			res += "-";
		} else {
			// commented out because of symbolic version
			//	    temp = env.getIntField(n0,null, "value");
			//	    if (temp < x) res+= "<";
			//	    if (temp == x) res+= "=";
			//	    if (temp > x) res+= ">";
			temp = env.getIntField(n0,  "left");
			res += (temp == -1) ? "L-" : "L+";
			temp = env.getIntField(n0,  "right");
			res += (temp == -1) ? "R-" : "R+";
		}
		res += (n1 == -1) ? "P-" : "P+";
		if (n2 == -1) {
			res += "B-";
		} else {
			temp = env.getIntField(n2,  "left");
			res += (temp == -1) ? "BL-" : "BL+";
			temp = env.getIntField(n2,  "right");
			res += (temp == -1) ? "BR-" : "BR+";
		}
//End comment here
		
		if (!tests.contains(res)) {
			tests.add(res);
			System.out.println("Test case number " + tests.size() + " for '"
					+ res + "': ");
			TestCount.number = tests.size();
			return tests.size();
		}
		return 0;
	}

	public static String res;

	public static void structOnly(MJIEnv env, int t) {
		DynamicArea da = env.getVM().getDynamicArea();
		Vector<String> result = da.linearizeRoot(t);
		res = result.toString();
	}
	
	public class totalMap extends PredicateMap {
		int value = -99;
		
		public MJIEnv env;
		
		public void setEnv(MJIEnv mjienv) {
			env = mjienv;
		}
		
		public void evaluate() {
			value = -99;
			if (env.isInstanceOf(ref,"BinTree.Node")) {
			  value = env.getIntField(ref,  "value");
			}
		}
		
		public String getRep() {
			return " v " + value;
		}
		
	}
	
	public static void structComplete(MJIEnv env, int t) {
		DynamicArea da = env.getVM().getDynamicArea();
		totalMap cm = (new JPF_issta2006_BinTree_BinTree()).new totalMap();
		cm.setEnv(env);
		cm.setRef(t);
		res = (da.linearizeRoot(t,cm)).toString();
	}

	
	public static boolean checkAbstractState(MJIEnv env, int objRef, int which) {
		res = "";

		// ADD ABSTRACTION FUNCTION(S) COMPUTATION HERE (chosen by parameter which >=2)
		if (which == 2) {
			buildTree(env, env.getIntField(objRef,  "root"));
		}
		if (which == 6) {
			structOnly(env,env.getIntField(objRef,  "root"));
		}
		if (which == 7) {
			structComplete(env,env.getIntField(objRef,  "root"));
		}
		
		//System.out.println(res);
		
		if (!abs_states.contains(res)) {
			abs_states.add(res);
			return false;
		}
		
		return true;
	}

	static void buildTree(MJIEnv env, int n) {
		res += " { ";
		res += ((n == -1) ? " -1 " : " 0 ");
		if (n != -1) {
			buildTree(env, env.getIntField(n,  "left"));
			buildTree(env, env.getIntField(n,  "right"));
		}
		res = res + " } ";
	}

	// adding outside predicate
	
	public static boolean checkAbstractState2(MJIEnv env, int objRef, int which, int x) {
		res = "";

		// ADD ABSTRACTION FUNCTION(S) COMPUTATION HERE (chosen by parameter which >=2)
		if (which == 2) {
			buildTree2(env, env.getIntField(objRef,  "root"),x);
		}
		if (which == 6) {
			structOnly(env,env.getIntField(objRef,  "root"));
		}
		if (which == 7) {
			structComplete(env,env.getIntField(objRef,  "root"));
		}
		
		//System.out.println(res);
		
		if (!abs_states.contains(res)) {
			abs_states.add(res);
			return false;
		}
		
		//System.out.println("old");
		
		return true;
	}
	
	static void buildTree2(MJIEnv env, int n, int x) {
		res += " { ";
		res += ((n == -1) ? " -1 " : " 0 ");
		if (n != -1) {
			int v = env.getIntField(n,  "value");
			//System.out.println("v = " + v + " x = " + x);
			// x > v
			//res += ((x >= v) ? " x >= v " : " x < v ");
      // x < v
			//res += ((x < v) ? " x < v " : " x >= v ");
			//res += " v " + v;
			buildTree2(env, env.getIntField(n,  "left"),x);
			buildTree2(env, env.getIntField(n,  "right"),x);
		}
		res = res + " } ";
	}


	
}
