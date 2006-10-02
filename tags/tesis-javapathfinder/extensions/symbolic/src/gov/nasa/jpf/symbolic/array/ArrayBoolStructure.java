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
import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.symbolic.integer.*;
import gov.nasa.jpf.symbolic.integer.Comparator;
import java.util.Vector;

public class ArrayBoolStructure {

	Vector<ArrayCell> _v;
	Expression length;
	String name;
	
	public ArrayBoolStructure(String name) {
		this.name = name;
		_v = new Vector<ArrayCell>();
		length = new SymbolicInteger(name + ".length");
		Expression.pc._addDet(Comparator.GT,length,0);
	}

	static class ArrayCell {
		Expression index;
		boolean elem;
		boolean original_elem;
	
		public ArrayCell(Expression length, Expression idx) {			
			index = idx;
			Expression.pc._addDet(Comparator.GE,index,0);
			Expression.pc._addDet(Comparator.LT,index,length);		
			elem = Verify.randomBool();
			original_elem = elem;
		}
	}
	
	ArrayCell _new_get_ArrayCell(Expression index) {
		for(int i = 0; i < _v.size(); i++) {
			ArrayCell cell = _v.elementAt(i);
			if (Expression.pc._add(Comparator.EQ,cell.index,index))
				return cell;
		}			
		ArrayCell t = new ArrayCell(length, index);
		//t.index = index;
		_v.add(t);
		return t;
	}
	
	public boolean _get(Expression index) {
		Verify.ignoreIf (!(Expression.pc._add(Comparator.GE,index,0) &&
						Expression.pc._add(Comparator.LT,index,length)));
		ArrayCell cell = _new_get_ArrayCell(index); 
		//Expression.pc._addDet(Comparator.EQ,cell.index,index);						
		return cell.elem;
	}

	ArrayCell _check_get_ArrayCell(Expression index) {
	  ArrayCell cell = _v.elementAt(Verify.random(_v.size()-1));
		return cell;			
	}

/*
 * When checking whether a predicate over an array holds and the 
 * array is either null or the element is not in the array then
 * we return "undefined" (-1) and otherwise (0 = false) or (1 = true)
 */


	public int _check_get(Expression index) {
		if ((_v != null) && (_v.size() > 0)) {
			ArrayCell cell = _check_get_ArrayCell(index);
			if (Expression.pc._check(Comparator.EQ,cell.index,index))
				return (cell.elem == true ? 1 : 0);
			else {
		  	return -1;
			}
		}
		else { 
		  return -1;
		}	
	}


	public void _set(Expression index, boolean value) {
		assert (Expression.pc._add(Comparator.GE,index,0) &&
						Expression.pc._add(Comparator.LT,index,length));
		ArrayCell cell = _new_get_ArrayCell(index); 
		//Expression.pc._addDet(Comparator.EQ,cell.index,index);
		//Expression.pc._addDet(Comparator.EQ,cell.elem,value);
		cell.elem = value;												
	}
	
	public void printArray() {
		System.out.println("Array " + name + " with length " + _v.size());
		for(int i = 0; i < _v.size(); i++) {
			ArrayCell cell = _v.elementAt(i);
			System.out.println(name+"["+cell.index+"] == " + cell.original_elem);
		}
	}

}
