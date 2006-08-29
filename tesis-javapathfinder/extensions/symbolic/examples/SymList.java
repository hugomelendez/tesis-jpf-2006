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
import gov.nasa.jpf.symbolic.integer.Comparator;
import java.util.HashSet;

/*

class List {
    Node header;
    static class Node {
      int elem;
      Node next;
    }
    
    public void deleteFirst(int n) {
      if (header == null) 
      	return;
      if (header.elem < n)	
	    header = header.next;		
    }
}

*/



class sList {
	Node header;

	boolean _symbolic_header = true;

	Node _get_header() {
		if (_symbolic_header) {
			_symbolic_header = false;
			header = Node._get_Node();
		}
		return header;
	}

	public void printList() {
		HashSet hs = new HashSet();
		System.out.println("---------------");
		System.out.println(Expression.pc);
		for (Node t = header; t != null; t = t.next) {
			if (hs.contains(t))
				break;
			hs.add(t);
			System.out.println("node" + t.hashCode() + " elem = "
					+ ((t.sym_elem == null) ? "*" : "" + t.sym_elem)
					+ " next = "
					+ ((t.next != null) ? "node" + t.next.hashCode() : "null"));
		}
		System.out.println("---------------");

	}

	static class Node {
		int elem;

		boolean _symbolic_elem = true;

		Expression sym_elem;

		Node next;

		boolean _symbolic_next = true;

		static java.util.Vector _v = new java.util.Vector();
		static {
			_v.add(null);
		}

		public static Node _get_Node() {
			int i = Verify.random(_v.size());
			if (i < _v.size())
				return (Node) _v.elementAt(i);
			Node t = Node._new_Node();
			_v.add(t);
			return t;
		}

		static Node _new_Node() {
			Node t = new Node();
			t._symbolic_elem = true;
			t._symbolic_next = true;
			return t;
		}

		public Node _get_next() {
			if (_symbolic_next) {
				_symbolic_next = false;
				next = Node._get_Node();
			}
			return next;
		}

		public final Expression _elem() {
			if (_symbolic_elem) {
				_symbolic_elem = false;
				sym_elem = Expression._Expression();
			}
			return sym_elem;
		}

		public final void _elem(Object _value) {
			sym_elem = (Expression) _value;
			_symbolic_elem = false;
		}

	}

	public void deleteFirst() {
		if (header == null)
			return;
		if (header.elem < 100)
			header = header.next;
	}

	public void deleteFirstSymbolicIfLT100() {
		if (_get_header() == null)
			return;
		if (_get_header()._elem()._LT(new IntegerConstant(100))) {
			Node temp = _get_header()._get_next();
			printList();
			header = temp;
		}
	}

	public void deleteFirstSymbolicIfLTn(Expression n) {
		if (_get_header() == null)
			return;
		if (_get_header()._elem()._LT(n)) {
			Node temp = _get_header()._get_next();
			printList();
			header = temp;
		}
	}

}

public class SymList {

	public static void main(String[] args) {
		sList l = new sList();
		//	  l.deleteFirstSymbolicIfLT100();
		l.deleteFirstSymbolicIfLTn(new SymbolicInteger("n"));

	}
}
