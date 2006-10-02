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

class Node {
	public int value;

	public Node left, right;

	public Node(int x) {
		value = x;
		left = null;
		right = null;
	}

}

public class BinTree {

	private Node root;

	public BinTree() {
		root = null;
	}

	//----
	public static void outputTestSequence(int number) {
	}

	public native boolean checkAbstractState(int which);
	public native boolean checkAbstractState2(int which, int x);

	
	public native static int gen_native(int br, Node n0, int x, Node n1, Node n2);

	public static void gen(int br, Node n0, int x, Node n1, Node n2) {
		int c = gen_native(br, n0, x, n1, n2);
		if (c != 0)
			outputTestSequence(c);
	}

	//----

	public void add(int x) {
		Node current = root;

		if (root == null) {
			gen(0, current, x, null, null);
			root = new Node(x);
			return;
		}

		while (current.value != x) {
			if (x < current.value) {
				if (current.left == null) {
					gen(1, current, x, null, null);
					current.left = new Node(x);
				} else {
					gen(2, current, x, null, null);
					current = current.left;
				}
			} else {
				if (current.right == null) {
					gen(3, current, x, null, null);
					current.right = new Node(x);
				} else {
					gen(4, current, x, null, null);
					current = current.right;
				}
			}
		}
	}

	public boolean find(int x) {
		Node current = root;

		while (current != null) {

			if (current.value == x) {
				gen(5, current, x, null, null);
				return true;
			}

			if (x < current.value) {
				gen(6, current, x, null, null);
				current = current.left;
			} else {
				gen(7, current, x, null, null);
				current = current.right;
			}
		}
		gen(16, current, x, null, null);

		return false;
	}

	public boolean remove(int x) {
		Node current = root;
		Node parent = null;
		boolean branch = true; //true =left, false =right

		while (current != null) {

			if (current.value == x) {
				Node bigson = current;
				while (bigson.left != null || bigson.right != null) {
					parent = bigson;
					if (bigson.right != null) {
						gen(8, current, x, bigson, parent);
						bigson = bigson.right;
						branch = false;
					} else {
						gen(9, current, x, bigson, parent);
						bigson = bigson.left;
						branch = true;
					}
				}

				//		System.out.println("Remove: current "+current.value+" parent "+parent.value+" bigson "+bigson.value);
				if (parent != null) {
					if (branch) {
						gen(10, current, x, bigson, parent);
						parent.left = null;
					} else {
						gen(11, current, x, bigson, parent);
						parent.right = null;
					}
				}

				if (bigson != current) {
					gen(12, current, x, bigson, parent);
					current.value = bigson.value;
				} else {
					gen(13, current, x, bigson, parent);
				}

				return true;
			}

			parent = current;
			//	    if (current.value <x ) { // THERE WAS ERROR
			if (current.value > x) {
				gen(14, current, x, null, parent);
				current = current.left;
				branch = true;
			} else {
				gen(15, current, x, null, parent);
				current = current.right;
				branch = false;
			}
		}

		gen(17, current, x, null, parent);
		return false;
	}

}
