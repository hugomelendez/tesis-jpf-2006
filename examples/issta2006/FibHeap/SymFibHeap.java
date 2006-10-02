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

// FibHeap.java
// from : http://sciris.shu.edu/~borowski/Puzzle/Puzzle.html

import gov.nasa.jpf.symbolic.integer.*;
import gov.nasa.jpf.symbolic.integer.Comparator;

public class SymFibHeap {
	private SymNode min;

	private int n;

	public SymFibHeap() {
	}

	//    --------------------------------------------------------------------
	public static void outputTestSequence(int number) {
	}

	public native boolean checkAbstractState(int which, int size);

	public native static int gen_native(int br, SymNode n, SymNode m); //SPECIFY

	public static void gen(int br, SymNode n, SymNode m) {//SPECIFY
		int c = gen_native(br, n, m);//SPECIFY
		if (c != 0)
			outputTestSequence(c);
	}

	//-------------------------------------------------------------------

	private void cascadingCut(SymNode y) {
		SymNode z = y.parent;
		if (z != null)
			if (!y.mark) {
				gen(0, y, null);
				y.mark = true;
			} else {
				gen(1, y, null);
				cut(y, z);
				cascadingCut(z);
			}
		else
			gen(2, y, null);
	}

	private void consolidate() {
		int D = n + 1;
		SymNode A[] = new SymNode[D];
		for (int i = 0; i < D; i++) {
			gen(3, A[i], null);
			A[i] = null;
		}

		int k = 0;
		SymNode x = min;
		if (x != null) {
			k++;
			for (x = x.right; x != min; x = x.right) {
				gen(4, x, null);
				k++;
			}
		}
		while (k > 0) {
			int d = x.degree;
			SymNode rightSymNode = x.right;
			gen(5, x, null);
			while (A[d] != null) {
				SymNode y = A[d];
				//		if (x.cost > y.cost)    {
				if (Expression.pc._add(Comparator.GT, x.cost, y.cost)) {
					gen(6, x, y);
					SymNode temp = y;
					y = x;
					x = temp;
				} else
					gen(7, x, y);
				link(y, x);
				A[d] = null;
				d++;
			}

			gen(8, x, null);
			A[d] = x;
			x = rightSymNode;
			k--;
		}

		min = null;
		for (int i = 0; i < D; i++)
			if (A[i] != null)
				if (min != null) {
					gen(9, A[i], null);
					A[i].left.right = A[i].right;
					A[i].right.left = A[i].left;
					A[i].left = min;
					A[i].right = min.right;
					min.right = A[i];
					A[i].right.left = A[i];
					//		    if (A[i].cost < min.cost) {
					if (Expression.pc._add(Comparator.LT, A[i].cost, min.cost)) {
						gen(10, A[i], min);
						min = A[i];
					} else
						gen(11, A[i], min);
				} else {
					gen(12, A[i], null);
					min = A[i];
				}
	}

	private void cut(SymNode x, SymNode y) {
		x.left.right = x.right;
		x.right.left = x.left;
		y.degree--;
		if (y.child == x) {
			gen(13, x, y);
			y.child = x.right;
		} else
			gen(20, x, y);
		if (y.degree == 0) {
			gen(14, y, x);
			y.child = null;
		} else
			gen(24, x, y);
		x.left = min;
		x.right = min.right;
		min.right = x;
		x.right.left = x;
		x.parent = null;
		x.mark = false;
	}

	public void decreaseKey(SymNode x, Expression c) {
		//	if (c > x.cost)  {
		Expression.pc._addDet(Comparator.LE, c, x.cost);
		if (Expression.pc._add(Comparator.GT, c, x.cost)) {
			System.out.println("c = " + c + " x.cost " + x.cost);
			System.err.println("Error: new key is greater than current key.");
			return;
		}
		x.cost = c;
		SymNode y = x.parent;
		//	if ((y != null) && (x.cost < y.cost))  {
		if ((y != null) && (Expression.pc._add(Comparator.LT, x.cost, y.cost))) {
			cut(x, y);
			cascadingCut(y);
		}
		//	if (x.cost < min.cost)
		if (Expression.pc._add(Comparator.LT, x.cost, min.cost))
			min = x;
	}

	// I don't know how to do this (simply) with symbolic execution... 
	//    public void delete(SymNode node)   {
	//	decreaseKey(node, Integer.MIN_VALUE);
	//	removeMin();
	//    }

	public void delete(SymNode node) {
		//SymbolicInteger symv = new SymbolicInteger("minint_val");
		//decreaseKey(node, new IntegerConstant(Integer.MIN_VALUE));
		decreaseKey(node, new IntegerConstant(-10000));
		//Expression.pc._addDet(Comparator.EQ, symv, Integer.MIN_VALUE);
		//decreaseKey(node, symv);
		removeMin();
	}

	public boolean empty() {
		return min == null;
	}

	public void insert(SymbolicInteger c) {
		SymNode n = new SymNode(c);
		insert(n);
	}

	public SymNode insert(SymNode toInsert) {
		if (min != null) {
			toInsert.left = min;
			toInsert.right = min.right;
			min.right = toInsert;
			toInsert.right.left = toInsert;
			//	    if (toInsert.cost < min.cost) {
			if (Expression.pc._add(Comparator.LT, toInsert.cost, min.cost)) {
				gen(21, min, null);
				min = toInsert;
			} else {
				gen(22, min, null);
			}
		} else {
			min = toInsert;
			gen(23, min, null);
		}
		n++;
		return toInsert;
	}

	private void link(SymNode node1, SymNode node2) {
		node1.left.right = node1.right;
		node1.right.left = node1.left;
		node1.parent = node2;
		if (node2.child == null) {
			gen(15, node1, node2);
			node2.child = node1;
			node1.right = node1;
			node1.left = node1;
		} else {
			gen(16, node1, node2);
			node1.left = node2.child;
			node1.right = node2.child.right;
			node2.child.right = node1;
			node1.right.left = node1;
		}
		node2.degree++;
		node1.mark = false;
	}

	public SymNode min() {
		return min;
	}

	public SymNode removeMin() {
		SymNode z = min;
		if (z != null) {
			int i = z.degree;
			SymNode x = z.child;
			while (i > 0) {
				gen(17, x, z);
				SymNode nextChild = x.right;
				x.left.right = x.right;
				x.right.left = x.left;
				x.left = min;
				x.right = min.right;
				min.right = x;
				x.right.left = x;
				x.parent = null;
				x = nextChild;
				i--;
			}
			z.left.right = z.right;
			z.right.left = z.left;
			if (z == z.right) {
				gen(18, x, z);
				min = null;
			} else {
				gen(19, x, z);
				min = z.right;
				consolidate();
			}

			n--;
		}
		return z;
	}

	public int size() {
		return n;
	}

	public static SymFibHeap union(SymFibHeap heap1, SymFibHeap heap2) {
		SymFibHeap heap = new SymFibHeap();
		if ((heap1 != null) && (heap2 != null)) {
			heap.min = heap1.min;
			if (heap.min != null) {
				if (heap2.min != null) {
					heap.min.right.left = heap2.min.left;
					heap2.min.left.right = heap.min.right;
					heap.min.right = heap2.min;
					heap2.min.left = heap.min;
					//		    if (heap2.min.cost < heap1.min.cost)
					if (Expression.pc._add(Comparator.LT, heap2.min.cost,
							heap1.min.cost))
						heap.min = heap2.min;
				}
			} else
				heap.min = heap2.min;
			heap.n = heap1.n + heap2.n;
		}
		return heap;
	}

}
