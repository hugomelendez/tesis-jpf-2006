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
import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.symbolic.integer.*;
import gov.nasa.jpf.symbolic.array.*;
import gov.nasa.jpf.symbolic.integer.Comparator;

public class PartitionTest {

	native public static boolean predsVisited(int loc,
											   boolean p, boolean q, boolean r, boolean s);

	native public static void addPredsVisited(int loc,
	                       boolean p, boolean q, boolean r, boolean s);
	public static int[] swap(int a[], int x, int y) { 
		int tmp = a[x];
		a[x] = y;
		a[y] = tmp;
		return a;
	}

	static void printPreds(String loc, boolean a, boolean b, boolean c, boolean d) {
		System.out.println(loc + a+" "+ b+ " " + c+ " " +d);
	}

	static void printPreds(String loc, int[] aa, int lo, int hi, int pivot) {
		boolean a =  (lo < hi);
		boolean b =  (lo <= hi);
		System.out.print(loc + a + " " + b + " ");
		if ((lo >= aa.length))
			System.out.print(" LO_OOBE ");
		else	
			System.out.print((aa[lo]<=pivot) + " ");
		if ((hi >= aa.length))
			System.out.println(" HI_OOBE ");
		else	
			System.out.println((aa[hi]>pivot) + " ");			
	}


	public static void partition(int a[]) {
		int pivot = a[0];
		int lo = 1;
		int hi = a.length-1;
		Verify.ignoreIf(a.length <= 2);
		printPreds("0 -> ",a,lo,hi,pivot);
		while (lo <= hi) {
			while (a[lo] <= pivot) {
				lo++;
				printPreds("4 -> ",a,lo,hi,pivot);
			}
			while (a[hi] > pivot) {
				hi--;
				printPreds("7 -> ",a,lo,hi,pivot);
			}
			if (lo < hi) {
				printPreds("9 -> ",a,lo,hi,pivot);
				int tmp = a[lo];
				a[lo] = a[hi];
				a[hi] = tmp;
			}
		}		
	}

	static ArrayIntStructure a = new ArrayIntStructure("a");
	static Expression pivot = new SymbolicInteger("pivot");
	static Expression lo = new SymbolicInteger("lo");
	static Expression hi = new SymbolicInteger("hi");

	static void predCheck(int loc, boolean lt, boolean le, boolean al, boolean ah) {
		if (predsVisited(loc,lt,le,al,ah)) {
			Verify.ignoreIf(true);
			return; 
		}
		if ((Expression.pc._add(Comparator.LT,lo,hi) == lt) &&
				(Expression.pc._add(Comparator.LE,lo,hi)  == le) &&
				(Expression.pc._add(Comparator.LE,a._get(lo),pivot) == al) &&
				(Expression.pc._add(Comparator.GT,a._get(hi),pivot) == ah))
			findSolution(loc,lt,le,al,ah);			
	}

	public static void partitionSym(ArrayIntStructure a) {
		
			boolean lt;
			boolean le;
			boolean al;
			boolean ah;
		
			pivot = a._get(new IntegerConstant(0));
			//Expression.pc._addDet(Comparator.EQ,lo,1);
			lo = new IntegerConstant(1);
			hi = a.length._minus(1);
			//Expression.pc._addDet(Comparator.EQ,hi,a.length._minus(1));
			Expression.pc._addDet(Comparator.GT,a.length,2);

			lt = Verify.randomBool();
			le = Verify.randomBool();
			al = Verify.randomBool();
			ah = Verify.randomBool();
			predCheck(0,lt,le,al,ah);
			while (Expression.pc._add(Comparator.LE,lo,hi)) {
				while (Expression.pc._add(Comparator.LE,a._get(lo),pivot)) {
					lo = lo._plus(1);
					predCheck(4,lt,le,al,ah);						
				}
				while (Expression.pc._add(Comparator.GT,a._get(hi),pivot)) {
					hi = hi._minus(1);
					predCheck(7,lt,le,al,ah);					
				}
				if (Expression.pc._add(Comparator.LT,lo,hi)) {					
					predCheck(9,lt,le,al,ah);
					Expression tmp = a._get(lo);
					a._set(lo,a._get(hi));
					a._set(hi,tmp);					
				}
			}		
		}

	public static void findSolution(int loc,
																	boolean lt, 
																	boolean le, 
																	boolean al, 
																	boolean ah) {
		Expression.pc.solve();
		int k = 0;
		int length =  a.length.solution();
		Verify.ignoreIf(length > 4);
		int test_a[] = new int[length];
		System.out.println("+" + loc + " -> " + 
											 lt + " " + le + " " + al + " " + ah);	
		System.out.print("a[" + length + "] = [");
		while (k < length) {
			int value = a._get(new IntegerConstant(k)).solution();
			test_a[k] = value;
			if ((k+1) == length)
				System.out.println(value+"]");
			else	
				System.out.print(value+",");
			k++;
		}
		try {
			partition(test_a);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("ArrayOutOfBOunds with " + loc + " -> " + 
			lt + " " + le + " " + al + " " + ah);
		}
		addPredsVisited(loc,lt,le,al,ah);
	}
	

	public static void main(String[] args) {
		partitionSym(a);
	}
}

/*
 Java Pathfinder Model Checker v3.1.1 - (C) 1999,2003 RIACS/NASA Ames Research Center
MC Search

14.45 mins
Stack depth 100

+0 -> true true true true     a[3] = [-1000000,-1000000,-999999]
+0 -> true true false false   a[3] = [-1000000,-999999,-1000000]
+0 -> true true false true    a[3] = [-1000000,-999999,-999999]
+0 -> true true true false    a[3] = [-1000000,-1000000,-1000000] ArrayOutOfBOunds with 0 -> true true true false

+4 -> true true true true     a[4] = [-1000000,-1000000,-1000000,-999999]
+4 -> true true false false   a[4] = [-1000000,-1000000,-999999,-1000000]
+4 -> true true false true    a[4] = [-1000000,-1000000,-999999,-999999]
+4 -> false true false true   a[3] = [-1000000,-1000000,-999999]
+4 -> false true true false   a[3] = [-1000000,-1000000,-1000000] ArrayOutOfBOunds with 4 -> false true true false
+4 -> true true true false    a[4] = [-1000000,-1000000,-1000000,-1000000] ArrayOutOfBOunds with 4 -> true true true false

+7 -> false false false false a[3] = [-1000000,-999999,-999999]
+7 -> false true false true   a[3] = [-1000000,-999999,-999999]
+7 -> true true false false   a[4] = [-1000000,-999999,-1000000,-999999]
+7 -> true true false true    a[4] = [-1000000,-999999,-999999,-999999]

+9 -> true true false false   a[3] = [-1000000,-999999,-1000000]

 */


