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
package issta2006.TreeMap;

import gov.nasa.jpf.jvm.*;
import java.util.*;
import issta2006.Counter.TestCount;


public class JPF_issta2006_TreeMap_TreeMap {

	private static Set<String> tests = new HashSet<String>();

	private static Set<String> abs_states = new HashSet<String>();

	public static int gen_native(MJIEnv env, int objRef, int br, int e, int root) {
		
		String res = br + ",";
//For Basic Block Coverage
//START comment here
		
		if (e == -1) {
			res += "entry=null";
		} else {
			int e_left = env.getIntField(e, "left");
			res += (e_left == -1) ? "L-" : "L+";
			int e_right = env.getIntField(e, "right");
			res += (e_right == -1) ? "R-" : "R+";
			int e_parent = env.getIntField(e, "parent");
			res += (e_parent == -1) ? "P-" : "P+";
			int e_color = env.getIntField(e, "color");
			res += (e_color == 0) ? "RED" : "BLACK";
			res += (e == root) ? "root" : "";
		}
//For Basic Block Coverage
//END comment here

		if (!tests.contains(res)) {
			tests.add(res);
			System.out.println("Test case number " + tests.size() + " for '"
					+ res + "': ");
			//System.out.println("tree = " + tree);
			TestCount.number = tests.size();
			return tests.size();
		}
		return 0;
	}

	static String tree;

	public class colorMap extends PredicateMap {
		int color = -1;
		
		public MJIEnv env;
		
		public void setEnv(MJIEnv mjienv) {
			env = mjienv;
		}
		
		public void evaluate() {
			color = -1;
			if (env.isInstanceOf(ref,"TreeMap.TreeMap$Entry")) {
				color = env.getIntField(ref, "color");
			}
		}
		
		public String getRep() {
			return "" + color;
		}		
	}
	
	public class colorKeyMap extends PredicateMap {
		int color = -1;
		int key = -1;
		
		public MJIEnv env;
		
		public void setEnv(MJIEnv mjienv) {
			env = mjienv;
		}
		
		public void evaluate() {
			color = -1;
			key = -1;
			if (env.isInstanceOf(ref,"TreeMap.TreeMap$Entry")) {
				color = env.getIntField(ref, "color");
				key = env.getIntField(ref, "key");
			}
		}
		
		public String getRep() {
			return "" + color + " " + key;
		}		
	}
	
	public static void matchStructs(MJIEnv env, int t, boolean color) {
		DynamicArea da = env.getVM().getDynamicArea();
		colorMap cm = (new JPF_issta2006_TreeMap_TreeMap()).new colorMap();
		cm.setEnv(env);
		cm.setRef(t);
		if (!color)
		  tree = da.linearizeRoot(t).toString();
		else
			tree = da.linearizeRoot(t,cm).toString();
	}

	public static void matchStructsAll(MJIEnv env, int t) {
		DynamicArea da = env.getVM().getDynamicArea();
		colorKeyMap cm = (new JPF_issta2006_TreeMap_TreeMap()).new colorKeyMap();
		cm.setEnv(env);
		cm.setRef(t);
		tree = da.linearizeRoot(t,cm).toString();
	}

	
	public static boolean checkAbstractState(MJIEnv env, int objRef, int which) {
		tree = "";

		if (which == 2) {
			buildTree(env, env.getIntField(objRef, "root"), false);
		}
		if (which == 3) {
			buildTree(env, env.getIntField(objRef, "root"), true);
		}
		if (which == 6) {
			matchStructs(env,env.getIntField(objRef, "root"),false);
		}
		if (which == 5) {
			matchStructs(env,env.getIntField(objRef, "root"),true);
		}
		if (which == 7) {
			matchStructsAll(env,env.getIntField(objRef, "root"));
		}

		if (!abs_states.contains(tree)) {
			abs_states.add(tree);
			return false;
		}
		return true;
	}

	static void buildTree(MJIEnv env, int n, boolean color) {
		tree = tree + " { ";
		tree = tree + ((n == -1) ? " -1 " : " 0 ");
		if (n != -1) {
			if (color)
				tree += env.getIntField(n, "color");
			buildTree(env, env.getIntField(n,"left"), color);
			buildTree(env, env.getIntField(n,"right"), color);
		}
		tree = tree + " } ";
	}
}
