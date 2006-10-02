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
package issta2006.FibHeap;

import gov.nasa.jpf.jvm.*;


import java.util.*;

import issta2006.Counter.TestCount;


public class JPF_issta2006_FibHeap_FibHeap {

	private static Set<String> tests = new HashSet<String>();

	private static Set<String> abs_states = new HashSet<String>();

	public static int gen_native(MJIEnv env, int objRef, int br, int n, int m) {

		String res = br + ",";
//		For Basic Block Coverage
//		START comment here
		
		int temp;

		if (n == -1) {
			res += "null";
		} else {
			temp = env.getIntField(n,  "child");
			res += (temp == -1) ? "1" : "0";
			temp = env.getIntField(n,  "parent");
			res += (temp == -1) ? "1" : "0";
			temp = env.getIntField(n,  "right");
			res += (temp == n) ? "1" : "0";
			temp = env.getIntField(n,  "left");
			res += (temp == n) ? "1" : "0";
			temp = env.getIntField(n,  "degree");
			res += (temp == 0) ? "1" : "0";
		}
		if (m == -1) {
			res += "null";
		} else {
			temp = env.getIntField(m,  "child");
			res += (temp == -1) ? "1" : "0";
			temp = env.getIntField(m,  "parent");
			res += (temp == -1) ? "1" : "0";
			temp = env.getIntField(m,  "right");
			res += (temp == n) ? "1" : "0";
			temp = env.getIntField(m,  "left");
			res += (temp == n) ? "1" : "0";
			temp = env.getIntField(m,  "degree");
			res += (temp == 0) ? "1" : "0";
		}
		if (n != -1 && m != -1) {
			// commented out because of symbolic execution...
			//	    int temp2;
			//	    temp = env.getIntField(n,  "cost");
			//	    temp2 = env.getIntField(n,  "cost");
			//	    res += (temp>temp2)?"1":"0";
			temp = env.getIntField(n,  "child");
			res += (temp == m) ? "1" : "0";
			temp = env.getIntField(m,  "child");
			res += (temp == n) ? "1" : "0";
		}
//For Basic Block Coverage
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

	static int min_cost, node_id;

	static Map<Integer,Integer> node_map;

	public class costMap extends PredicateMap {
		int cost_pred = -1;
		int min_ref = -1;
		int min_cost = -1;
		
		public MJIEnv env;
		
		public void setEnv(MJIEnv mjienv) {
			env = mjienv;
		}

		public void setMin(int m) {
			min_ref = m;
			if (min_ref != -1)
				min_cost = env.getIntField(min_ref,  "cost"); 
		}
		
		public void evaluate() {
			int cost = -1;
			if (env.isInstanceOf(ref,"FibHeap.Node")) {
			  cost = env.getIntField(ref,  "cost");
				if (cost > min_cost)
					cost_pred = 0;
				else if (cost == min_cost)
					cost_pred = 1;
				else
					cost_pred = 2;			  
			}
			//System.out.println("cost_pred = " + cost_pred);
		}
		
		public String getRep() {
			return "" + cost_pred;
		}
		
	}
	
	public class totalMap extends PredicateMap {
		int cost = -1;
		
		public MJIEnv env;
		
		public void setEnv(MJIEnv mjienv) {
			env = mjienv;
		}
		
		public void evaluate() {
			cost = -1;
			if (env.isInstanceOf(ref,"FibHeap.Node")) {
			  cost = env.getIntField(ref,  "cost");
			}
		}
		
		public String getRep() {
			return "" + cost;
		}
		
	}
	
	public static void matchStructs(MJIEnv env, int h, int which) {
		DynamicArea da = env.getVM().getDynamicArea();
		int min = env.getIntField(h,  "min");
		costMap cm = (new JPF_issta2006_FibHeap_FibHeap()).new costMap();
		cm.setEnv(env);
		cm.setMin(min);
		cm.setRef(h);
		if (which == 6)
			heap = (da.linearizeRoot(h)).toString();
		else if (which == 8)
			heap = (da.linearizeRoot(h,cm)).toString();
		//System.out.println(heap);
	}

	public static void matchStructsAll(MJIEnv env, int h) {
		DynamicArea da = env.getVM().getDynamicArea();
		totalMap cm = (new JPF_issta2006_FibHeap_FibHeap()).new totalMap();
		cm.setEnv(env);
		cm.setRef(h);
		heap = (da.linearizeRoot(h,cm)).toString();
	}

	
	public static boolean checkAbstractState(MJIEnv env, int objRef, int which) {

		if ((which >= 2) && (which <= 5)) {
			heap = "";
			node_map = new HashMap<Integer,Integer>();
			node_map.put(new Integer(-1), new Integer(-1));
			node_id = 0;

			int min = env.getIntField(objRef,  "min");
			heap = heap + " { ";
			if (min != -1) {
				heap = heap + node_id;
				if (which == 3)
					heap = heap + "cost" + env.getIntField(min,  "cost");
				min_cost = env.getIntField(min,  "cost");
				node_map.put(new Integer(min), new Integer(node_id));
				node_id++;
				buildHeap(env, env.getIntField(min,  "left"), which);
				buildHeap(env, env.getIntField(min,  "right"), which);
				buildHeap(env, env.getIntField(min,  "child"), which);
			} else
				heap = heap + "-1";
			heap = heap + " } ";
		}
		
		if ((which == 6) || (which == 8)) {
			matchStructs(env,objRef, which);
		}
		if (which == 7) {
			matchStructsAll(env,objRef);
		}
		
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
			int cost = env.getIntField(n,  "cost");
			if (which == 4)
				heap += (cost == min_cost) ? "1" : "0";
			if (which == 5) {
				if (cost > min_cost)
					heap = heap + "cost1";
				else if (cost == min_cost)
					heap = heap + "cost0";
				else
					heap = heap + "cost-1";
			}
			node_map.put(new Integer(n), new Integer(node_id));
			node_id++;
			buildHeap(env, env.getIntField(n,  "left"), which);
			buildHeap(env, env.getIntField(n,  "right"), which);
			buildHeap(env, env.getIntField(n,  "child"), which);
		} else
			heap = heap + value;

		heap = heap + " } ";
	}

}
