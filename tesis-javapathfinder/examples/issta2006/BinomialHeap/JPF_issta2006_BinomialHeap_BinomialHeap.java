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
package issta2006.BinomialHeap;

import gov.nasa.jpf.jvm.*;


import java.util.*;

import issta2006.Counter.TestCount;


public class JPF_issta2006_BinomialHeap_BinomialHeap {

	private static Set<String> tests = new HashSet<String>();

	private static Set<String> abs_states = new HashSet<String>();

	public static int gen_native(MJIEnv env, int objRef, int br, int n1, int n2) {
		String res = br + ",";
// For Basic Block Coverage
// START comment here
		int temp, temp2;
		if (n1 == -1) {
			res += "null";
		} else {
			temp = env.getIntField(n1,"child");
			res += (temp == -1) ? "C-" : "C+";
			temp = env.getIntField(n1,"sibling");
			res += (temp == -1) ? "S-" : "S+";
			temp = env.getIntField(n1,"parent");
			res += (temp == -1) ? "P-" : "P+";
		}

		if (n2 == -1) {
			res += "null";
		} else {
			temp = env.getIntField(n2, "child");
			res += (temp == -1) ? "C-" : "C+";
			temp = env.getIntField(n2, "sibling");
			res += (temp == -1) ? "S-" : "S+";
			temp = env.getIntField(n2, "parent");
			res += (temp == -1) ? "P-" : "P+";
		}
		if (n1 != -1 && n2 != -1) {
			// commented out because of symbolic version
			// 	    temp = env.getIntField(n1,null,"key");
			// 	    temp2 = env.getIntField(n2,null,"key");
			// 	    if (temp<temp2) res+="<";
			// 	    if (temp==temp2) res+="=";
			// 	    if (temp>temp2) res+=">";
			temp = env.getIntField(n1, "degree");
			temp2 = env.getIntField(n2,"degree");
			if (temp < temp2)
				res += "<";
			if (temp == temp2)
				res += "=";
			if (temp > temp2)
				res += ">";
		}
//END comment here
		if (!tests.contains(res)) {
			tests.add(res);
			System.out.println("Test case number " + tests.size() + " for '"
					+ res + "': ");
			TestCount.number = tests.size();
			return tests.size();
		}
		return 0;
	}

	static String heap;

	static int node_id;

	static Map<Integer,Integer> node_map;

	public class totalMap extends PredicateMap {
		int key = -99;
		int degree = -99;
		
		public MJIEnv env;
		
		public void setEnv(MJIEnv mjienv) {
			env = mjienv;
		}
		
		public void evaluate() {
			key = -99;
			degree = -99;
			if (env.isInstanceOf(ref,"BinomialHeap.BinomialHeap$BinomialHeapNode")) {
			  key = env.getIntField(ref, "key");
			  degree = env.getIntField(ref, "degree");
			}
		}
		
		public String getRep() {
			return " k " + key + " d " + degree;
		}
		
	}
	
	public static void structOnly(MJIEnv env, int t) {
		DynamicArea da = env.getVM().getDynamicArea();
		Vector<String> result = da.linearizeRoot(t);
		heap = result.toString();
	}

	public static void structComplete(MJIEnv env, int t) {
		DynamicArea da = env.getVM().getDynamicArea();
		totalMap cm = (new JPF_issta2006_BinomialHeap_BinomialHeap()).new totalMap();
		cm.setEnv(env);
		cm.setRef(t);
		heap = (da.linearizeRoot(t,cm)).toString();
	}

	
	public static boolean checkAbstractState(MJIEnv env, int objRef, int which) {

		// ADD ABSTRACTION FUNCTION(S) COMPUTATION HERE (chosen by parameter
		// which >=2)
		
		
		if ((which >= 2) && (which <= 5)) {
			heap = "{";
			node_map = new HashMap<Integer,Integer>();
			node_map.put(new Integer(-1), new Integer(-1));
			node_id = 0;

			//	heap += "size"+ env.getIntField(objRef,null,"size")+" ";
			int min = env.getIntField(objRef, "Nodes");
			if (min != -1) {
				heap = heap + node_id;
				if (which == 3)
					heap = heap + "deg" + env.getIntField(min, "degree");
				if (which == 4 || which == 5)
					heap = heap + "key" + env.getIntField(min,  "key");
				node_map.put(new Integer(min), new Integer(node_id));
				node_id++;
				buildHeap(env, env.getIntField(min, "parent"), which);
				buildHeap(env, env.getIntField(min, "sibling"), which);
				buildHeap(env, env.getIntField(min, "child"), which);
			} else
				heap = heap + "-1";
			heap = heap + " } ";
		}

		if (which == 6) {
			structOnly(env,env.getIntField(objRef, "Nodes"));
		}
		
		if (which == 7) {
			structComplete(env,env.getIntField(objRef, "Nodes"));
		}
		
		
		//System.out.println(heap);

		if (!abs_states.contains(heap)) {
			abs_states.add(heap);
			return false;
		}
		return true;
	}

	static void buildHeap(MJIEnv env, int n, int which) {
		heap = heap + " { ";
		Integer value = node_map.get(new Integer(n));
		if (value == null) {
			heap = heap + node_id;
			if (which == 3)
				heap += "deg" + env.getIntField(n, "degree");
			if (which == 5)
				heap = heap + "key" + env.getIntField(n, "key");
			node_map.put(new Integer(n), new Integer(node_id));
			node_id++;
			buildHeap(env, env.getIntField(n,"parent"), which);
			buildHeap(env, env.getIntField(n,"sibling"), which);
			buildHeap(env, env.getIntField(n,"child"), which);
		} else
			heap = heap + value;

		heap = heap + " } ";
	}

}
