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
import gov.nasa.jpf.symbolic.integer.*;

import issta2006.PathConditionPrinting;

import java.util.*;

import issta2006.Counter.TestCount;

import omega.Relation;


public class JPF_issta2006_TreeMap_SymTreeMap {

	//-----
	
	static int nStates = 0;

	public static HashMap<String,ArrayList<Relation>> states = new HashMap<String,ArrayList<Relation>>();

	// a state has the form <s,<r1,r2 ...>> where s is a shape and 
	// r1, r2 ... are simplified relations

	public static void printStructs(MJIEnv env, int robj) {
		//System.out.println("STATES\n"+states);
		System.out.println("Number of states " + nStates + " Number of shapes "
				+ states.size());
	}

	static int key_number;

	public static boolean matchStructs1(MJIEnv env, int t, int problemSize) {
		
		int size = env.getIntField(t, "size");
		String shape = "\n" + size;
		key_number = 1;

		Reconstruction.createRelation(problemSize);

		int root = env.getIntField(t,"root");

		constraintsMap cm = (new JPF_issta2006_TreeMap_SymTreeMap()).new constraintsMap();
		cm.setEnv(env);
		cm.setRef(root);
		
		DynamicArea da = env.getVM().getDynamicArea();
		
		shape += da.linearizeRoot(root,cm).toString();
		
		//System.out.println("shape =  " + shape );
		
		Reconstruction.reconstructPathCondition(env);

		Relation r = Reconstruction.getRelation(false);
		ArrayList<Relation> rs = states.get(shape);

		if (rs == null) { // new state
			rs = new ArrayList<Relation>();
			rs.add(r);
			nStates++;
			//System.out.println("shape = " + shape);
			//r.prefixPrint();
			states.put(shape, rs);
			//printStructs(env,root);
			return false;
		}

		for (int i = 0; i < rs.size(); i++) {
			Relation old_r = rs.get(i);
			//System.out.println("old_r");
			//old_r.prefixPrint();
			// check validity r => old_r
			if (Subsumption.check(r, old_r, false, problemSize)) {
				//System.out.println("r => old_r is true");
				return true;
			}
			
			// check validity old_r => r
			if (Subsumption.check(old_r, r, false, problemSize)) {
				// replace old_r with r and return false
				//System.out.println("old_r => r is true");
				rs.remove(i);
				i--; // dirty
				nStates--;
			}
			
		}

		rs.add(r); // add this new relation to the visited state
		nStates++;
		//printStructs(env,root);
		return false;
	}

	static String buildTree99(MJIEnv env, int n) {
		String shape = " { ";
		shape = shape + ((n == -1) ? " -1 " : " 0 ");
		if (n != -1) {

			shape = shape + "In_" + key_number + " ";
			int keyRef = env.getIntField(n,"key");
			Reconstruction.updateBindings(env, n, key_number, keyRef);
			key_number++;

			shape += env.getIntField(n, "color");
			shape += buildTree99(env, env.getIntField(n, "left"));
			shape += buildTree99(env, env.getIntField(n, "right"));
		}
		shape += " } ";
		return shape;
	}
	
	//-----
	
	
	private static Set<String> tests = new HashSet<String>();

	private static Set<String> abs_states = new HashSet<String>();

	//private static HashMap<String,Integer> StringDepthMap = new HashMap<String,Integer>();
	
	public static int gen_native(MJIEnv env, int objRef, int br, int e, int root) {
		String res = br + ",";
//For Basic Block Coverage
//START comment here

		if (e == -1) {
			res += "entry=null";
		} else {
			int e_left = env.getIntField(e,"left");
			res += (e_left == -1) ? "L-" : "L+";
			int e_right = env.getIntField(e,"right");
			res += (e_right == -1) ? "R-" : "R+";
			int e_parent = env.getIntField(e,"parent");
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
			int pcRef = env.getStaticObjectField("gov.nasa.jpf.symbolic.integer."+
																					 "Expression","pc");
			//JPF_gov_nasa_jpf_jvm_Verify.solve(env,objRef,pcRef,true);
			JPF_gov_nasa_jpf_symbolic_integer_SymbolicConstraintsSolver.solve(env,objRef,pcRef,true);
			String PC = PathConditionPrinting.decodePC(env,pcRef);
  		    System.out.println(PC);
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
			if (env.isInstanceOf(ref,"TreeMap.SymTreeMap$Entry")) {
				color = env.getIntField(ref, "color");
			}
		}
		
		public String getRep() {
			return "" + color;
		}		
	}
	
	public class constraintsMap extends PredicateMap {
		int color = -1;
		int key_number = 0;
		public MJIEnv env;
		
		public void setEnv(MJIEnv mjienv) {
			env = mjienv;
			key_number = 0;
		}
		
		public void evaluate() {
			int keyRef;
			
			if (env.isInstanceOf(ref,"TreeMap.SymTreeMap$Entry")) {
				key_number++;
				keyRef = env.getIntField(ref,"key");
				Reconstruction.updateBindings(env, ref, key_number, keyRef);
			}
			else {
				keyRef = -1;
			}
			if (env.isInstanceOf(ref,"TreeMap.SymTreeMap$Entry")) {
				color = env.getIntField(ref, "color");
			}
			else
				color = -1;
		}
		
		public String getRep() {
			return " c " + color + " in_key " + key_number;
		}		
	}
	
	public static void matchStructs(MJIEnv env, int t, boolean color) {
		DynamicArea da = env.getVM().getDynamicArea();
		colorMap cm = (new JPF_issta2006_TreeMap_SymTreeMap()).new colorMap();
		cm.setEnv(env);
		cm.setRef(t);
		if (!color)
		  tree = da.linearizeRoot(t).toString();
		else
			tree = da.linearizeRoot(t,cm).toString();
	}
	
	public static boolean checkAbstractState(MJIEnv env, int objRef, 
			 																		 int which, int depth, int size) {
		tree = "";

		if (which == 2) {
			buildTree(env, env.getIntField(objRef,"root"), true);
		}
		if (which == 3) {
			buildTree(env, env.getIntField(objRef,"root"), false);
		}
		if (which == 6) {
			matchStructs(env,env.getIntField(objRef,"root"),false);
		}
		if (which == 5) {
			matchStructs(env,env.getIntField(objRef,"root"),true);
		}
		if (which == 7) {
			return matchStructs1(env,objRef,size);
		}
		
		if (!abs_states.contains(tree)) {
			//StringDepthMap.put(tree,new Integer(depth));
			//System.out.println("NO MATCH");
			abs_states.add(tree);
			return false;
		}		
		/*
		else if (depth != -1){
			int old_depth = ((Integer)StringDepthMap.get(tree)).intValue();
			if (old_depth > depth)
				return false;
		}*/
		//System.out.println("!!!!!MATCH!!!!");
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
