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


public class BinomialHeap /*implements java.io.Serializable*/{

	// internal class BinomialHeapNode
	public static class BinomialHeapNode /*implements java.io.Serializable*/{
		//private static final long serialVersionUID=6495900899527469811L;

		private int key; // element in current node

		private int degree; // depth of the binomial tree having the current node as its root

		private BinomialHeapNode parent; // pointer to the parent of the current node

		private BinomialHeapNode sibling; // pointer to the next binomial tree in the list

		private BinomialHeapNode child; // pointer to the first child of the current node

		public BinomialHeapNode(int k) {
			//	public BinomialHeapNode(Integer k) {
			key = k;
			degree = 0;
			parent = null;
			sibling = null;
			child = null;
		}

		public int getKey() { // returns the element in the current node
			return key;
		}

		private void setKey(int value) { // sets the element in the current node
			key = value;
		}

		public int getDegree() { // returns the degree of the current node
			return degree;
		}

		private void setDegree(int deg) { // sets the degree of the current node
			degree = deg;
		}

		public BinomialHeapNode getParent() { // returns the father of the current node
			return parent;
		}

		private void setParent(BinomialHeapNode par) { // sets the father of the current node
			parent = par;
		}

		public BinomialHeapNode getSibling() { // returns the next binomial tree in the list
			return sibling;
		}

		private void setSibling(BinomialHeapNode nextBr) { // sets the next binomial tree in the list
			sibling = nextBr;
		}

		public BinomialHeapNode getChild() { // returns the first child of the current node
			return child;
		}

		private void setChild(BinomialHeapNode firstCh) { // sets the first child of the current node
			child = firstCh;
		}

		public int getSize() {
			return (1 + ((child == null) ? 0 : child.getSize()) + ((sibling == null) ? 0
					: sibling.getSize()));
		}

		private BinomialHeapNode reverse(BinomialHeapNode sibl) {
			BinomialHeapNode ret;
			if (sibling != null)
				ret = sibling.reverse(this);
			else
				ret = this;
			sibling = sibl;
			return ret;
		}

		private BinomialHeapNode findMinNode() {
			BinomialHeapNode x = this, y = this;
			int min = x.key;

			while (x != null) {
				if (x.key < min) {
					y = x;
					min = x.key;
				}
				x = x.sibling;
			}

			return y;
		}

		// Find a node with the given key
		private BinomialHeapNode findANodeWithKey(int value) {
			BinomialHeapNode temp = this, node = null;
			while (temp != null) {
				if (temp.key == value) {
					node = temp;
					break;
				}
				if (temp.child == null)
					temp = temp.sibling;
				else {
					node = temp.child.findANodeWithKey(value);
					if (node == null)
						temp = temp.sibling;
					else
						break;
				}
			}

			return node;
		}

	}

	// end of helper class BinomialHeapNode

	//--------------------------------------------------------------------
	public static void outputTestSequence(int number) {
	}

	public native boolean checkAbstractState(int which);

	public native static int gen_native(int br, BinomialHeapNode n1,
			BinomialHeapNode n2); //SPECIFY

	public static void gen(int br, BinomialHeapNode n1, BinomialHeapNode n2) {//SPECIFY
		int c = gen_native(br, n1, n2);//SPECIFY
		if (c != 0)
			outputTestSequence(c);
	}

	//-------------------------------------------------------------------

	private BinomialHeapNode Nodes;

	private int size;

	public BinomialHeap() {
		Nodes = null;
		size = 0;
	}

	// 2. Find the minimum key
	public int findMinimum() {
		return Nodes.findMinNode().key;
	}

	// 3. Unite two binomial heaps
	// helper procedure
	private void merge(BinomialHeapNode binHeap) {
		BinomialHeapNode temp1 = Nodes, temp2 = binHeap;
		while ((temp1 != null) && (temp2 != null)) {
			if (temp1.degree == temp2.degree) {
				gen(1, temp1, temp2);
				BinomialHeapNode tmp = temp2;
				temp2 = temp2.sibling;
				tmp.sibling = temp1.sibling;
				temp1.sibling = tmp;
				temp1 = tmp.sibling;
			} else {
				if (temp1.degree < temp2.degree) {
					if ((temp1.sibling == null)
							|| (temp1.sibling.degree > temp2.degree)) {
						gen(2, temp1, temp2);
						BinomialHeapNode tmp = temp2;
						temp2 = temp2.sibling;
						tmp.sibling = temp1.sibling;
						temp1.sibling = tmp;
						temp1 = tmp.sibling;
					} else {
						gen(3, temp1, temp2);
						temp1 = temp1.sibling;
					}
				} else {
					BinomialHeapNode tmp = temp1;
					temp1 = temp2;
					temp2 = temp2.sibling;
					temp1.sibling = tmp;
					if (tmp == Nodes) {
						gen(4, temp1, temp2);
						Nodes = temp1;
					} else {
						gen(5, temp1, temp2);
					}
				}
			}
		}

		if (temp1 == null) {
			temp1 = Nodes;
			while (temp1.sibling != null) {
				gen(6, temp1, temp2);
				temp1 = temp1.sibling;
			}
			temp1.sibling = temp2;
		} else {
			gen(7, temp1, temp2);
		}
	}

	// another helper procedure
	private void unionNodes(BinomialHeapNode binHeap) {
		merge(binHeap);

		BinomialHeapNode prevTemp = null, temp = Nodes, nextTemp = Nodes.sibling;

		while (nextTemp != null) {
			if ((temp.degree != nextTemp.degree)
					|| ((nextTemp.sibling != null) && (nextTemp.sibling.degree == temp.degree))) {
				gen(8, temp, nextTemp);
				prevTemp = temp;
				temp = nextTemp;
			} else {
				if (temp.key <= nextTemp.key) {
					gen(9, temp, nextTemp);
					temp.sibling = nextTemp.sibling;
					nextTemp.parent = temp;
					nextTemp.sibling = temp.child;
					temp.child = nextTemp;
					temp.degree++;
				} else {
					if (prevTemp == null) {
						gen(10, temp, nextTemp);
						Nodes = nextTemp;
					} else {
						gen(11, temp, nextTemp);
						prevTemp.sibling = nextTemp;
					}
					temp.parent = nextTemp;
					temp.sibling = nextTemp.child;
					nextTemp.child = temp;
					nextTemp.degree++;
					temp = nextTemp;
				}
			}
			gen(12, temp, nextTemp);

			nextTemp = temp.sibling;
		}
	}

	// 4. Insert a node with a specific value
	public void insert(int value) {
		if (value > 0) {
			BinomialHeapNode temp = new BinomialHeapNode(value);
			if (Nodes == null) {
				Nodes = temp;
				size = 1;
			} else {
				unionNodes(temp);
				size++;
			}
		}
	}

	// 5. Extract the node with the minimum key
	public int extractMin() {
		if (Nodes == null)
			return -1;

		BinomialHeapNode temp = Nodes, prevTemp = null;
		BinomialHeapNode minNode = Nodes.findMinNode();
		while (temp.key != minNode.key) {
			gen(13, temp, prevTemp);
			prevTemp = temp;
			temp = temp.sibling;
		}

		if (prevTemp == null) {
			gen(14, temp, prevTemp);
			Nodes = temp.sibling;
		} else {
			gen(15, temp, prevTemp);
			prevTemp.sibling = temp.sibling;
		}
		temp = temp.child;
		BinomialHeapNode fakeNode = temp;
		while (temp != null) {
			gen(16, temp, prevTemp);
			temp.parent = null;
			temp = temp.sibling;
		}

		if ((Nodes == null) && (fakeNode == null)) {
			gen(17, temp, prevTemp);
			size = 0;
		} else {
			if ((Nodes == null) && (fakeNode != null)) {
				gen(18, Nodes, fakeNode);
				Nodes = fakeNode.reverse(null);
				size = Nodes.getSize();
			} else {
				if ((Nodes != null) && (fakeNode == null)) {
					gen(19, Nodes, fakeNode);
					size = Nodes.getSize();
				} else {
					gen(20, Nodes, fakeNode);
					unionNodes(fakeNode.reverse(null));
					size = Nodes.getSize();
				}
			}
		}

		return minNode.key;
	}

	// 6. Decrease a key value
	public void decreaseKeyValue(int old_value, int new_value) {
		BinomialHeapNode temp = Nodes.findANodeWithKey(old_value);
		if (temp == null)
			return;
		temp.key = new_value;
		BinomialHeapNode tempParent = temp.parent;

		while ((tempParent != null) && (temp.key < tempParent.key)) {
			int z = temp.key;
			gen(21, temp, tempParent);
			temp.key = tempParent.key;
			tempParent.key = z;

			temp = tempParent;
			tempParent = tempParent.parent;
		}
	}

	// 7. Delete a node with a certain key
	public void delete(int value) {
		if ((Nodes != null) && (Nodes.findANodeWithKey(value) != null)) {
			decreaseKeyValue(value, findMinimum() - 1);
			extractMin();
		}
	}

	public static void main(String[] Argv) {
		BinomialHeap b = new BinomialHeap();

		b.insert(3);
		b.insert(5);
		System.out.println("min: " + b.findMinimum());
		System.out.println("size: " + b.size);
		b.extractMin();
		System.out.println("min: " + b.findMinimum());
	}

}
// end of class BinomialHeap
