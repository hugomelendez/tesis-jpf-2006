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
import gov.nasa.jpf.symbolic.integer.*;


import java.util.*;

import issta2006.PathConditionPrinting;
import issta2006.Counter.*;

import omega.Relation;

public class JPF_issta2006_BinTree_SymBinTree {

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

	public static String res;

	public static boolean checkAbstractState(MJIEnv env, int objRef, int which, int size) {
		
		if (which == 7) {
			return matchStructs1(env,objRef,size);
		}
		
		res = "";

		// ADD ABSTRACTION FUNCTION(S) COMPUTATION HERE (chosen by parameter which >=2)
		if (which == 6) {
			buildTree(env, env.getIntField(objRef,  "root"));
		}

		if (!abs_states.contains(res)) {
			abs_states.add(res);
			//	    System.out.println("new state: "+res);
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

	public class constraintsMap extends PredicateMap {
		int key_number = 0;
		public MJIEnv env;
		
		public void setEnv(MJIEnv mjienv) {
			env = mjienv;
			key_number = 0;
		}
		
		public void evaluate() {
			int keyRef;
			
			if (env.isInstanceOf(ref,"BinomialHeap.BinomialHeap$SymBinomialHeapNode")) {
				key_number++;
				keyRef = env.getIntField(ref,  "value");
				Reconstruction.updateBindings(env, ref, key_number, keyRef);
			}
			else {
				keyRef = -1;
			}
		}
		
		public String getRep() {
			return " in_key " + key_number;
		}		
	}
	
	public static boolean matchStructs1(MJIEnv env, int t, int problemSize) {
//		int size = env.getIntField(t, "size");
//		String shape = "\n" + size;
		String shape = "";
		key_number = 1;

		Reconstruction.createRelation(problemSize);

		int root = env.getIntField(t,  "root");

		constraintsMap cm = (new JPF_issta2006_BinTree_SymBinTree()).new constraintsMap();
		cm.setEnv(env);
		cm.setRef(root);

		DynamicArea da = env.getVM().getDynamicArea();

		shape += da.linearizeRoot(root, cm).toString();

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
	
	
}
