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
package gov.nasa.jpf.symbolic.array;
import java.util.Vector;

import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.symbolic.integer.*;

class Heap {

  Vector<HeapCell> _v;
  String name;

  public Heap(String name) {
    this.name = name;
    _v = new Vector<HeapCell>();
  }

  static class HeapCell {
    Expression index;
    Expression elem;
    Expression original_elem;

    public HeapCell(Expression idx, String name) {
      index = idx;
      Expression.pc._addDet(Comparator.GE, index, 0);
      elem = new SymbolicInteger(name + "[" + idx + "]");
      original_elem = elem;
    }
  }

  HeapCell _new_get_HeapCell(Expression index) {
    for (int i = 0; i < _v.size(); i++) {
      HeapCell cell = _v.elementAt(i);
      if (Expression.pc._add(Comparator.EQ, cell.index, index))
        return cell;
    }
    HeapCell t = new HeapCell(index, name);
    _v.add(t);
    return t;
  }

  public Expression _get(Expression index) { 
      //assert(Expression.pc._add(Comparator.GE, index, 0); // no null deref
    HeapCell cell = _new_get_HeapCell(index);
    return cell.elem;
  }

	HeapCell _check_get_HeapCell(Expression index) {
	  HeapCell cell = _v.elementAt(Verify.random(_v.size()-1));
		return cell;			
	}

/*
 * When checking whether a predicate over an array holds and the 
 * array is either null or the element is not in the array 
 * then we return "undefined" (null) 
 */


	public Expression _check_get(Expression index) {
		if ((_v != null) && (_v.size() > 0)) {
			HeapCell cell = _check_get_HeapCell(index);
			if (Expression.pc._check(Comparator.EQ,cell.index,index))
				return cell.elem;
			else {
				return null;
			}
		}
		else { 
		  return null;
		}	
	}


  public void _set(Expression index, Expression value) { 
      //assert(Expression.pc._add(Comparator.GE, index, 0); // no null deref
    HeapCell cell = _new_get_HeapCell(index);
    cell.elem = value;
  }

  public void printHeap() {
    System.out.println("Heap " + name + " with size " + _v.size());
    for (int i = 0; i < _v.size(); i++) {
      HeapCell cell = _v.elementAt(i);
      System.out.println(cell.original_elem);
    }
  }

}
