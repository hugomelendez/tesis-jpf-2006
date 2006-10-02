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
 
public class ArrayIntStructure {

	Vector<ArrayCell> _v;
	public Expression length;
	String name;
	
	public ArrayIntStructure(String name) {
		this.name = name;
		_v = new Vector<ArrayCell>();
		length = new SymbolicInteger(name + ".length");
		Expression.pc._addDet(Comparator.GT,length,0);
	}

	static class ArrayCell {
		Expression index;
		Expression elem;
		Expression original_elem;
	
		public ArrayCell(Expression length, Expression idx, String name) {
			index = idx;
			Expression.pc._addDet(Comparator.GE,index,0);
			Expression.pc._addDet(Comparator.LT,index,length);		
			elem = new SymbolicInteger(name + "[" + idx + "]");
			original_elem = elem;
		}
	}
	
	
	ArrayCell _new_get_ArrayCell(Expression index) {
		for(int i = 0; i < _v.size(); i++) {
			ArrayCell cell = _v.elementAt(i);
			if (Expression.pc._add(Comparator.EQ,cell.index,index))
				return cell;
		}
		ArrayCell t = new ArrayCell(length, index, name);
		t.index = index;
		_v.add(t);
		return t;
	}
	
	public Expression _get(Expression index) {
		Verify.ignoreIf (!(Expression.pc._add(Comparator.GE,index,0) &&
						Expression.pc._add(Comparator.LT,index,length)));
		ArrayCell cell = _new_get_ArrayCell(index); 
		//Expression.pc._addDet(Comparator.EQ,cell.index,index);						
		return cell.elem;
	}

	public void _set(Expression index, Expression value) {
		Verify.ignoreIf (!(Expression.pc._add(Comparator.GE,index,0) &&
						Expression.pc._add(Comparator.LT,index,length)));
		ArrayCell cell = _new_get_ArrayCell(index); 
		//Expression.pc._addDet(Comparator.EQ,cell.index,index);
		//Expression.pc._addDet(Comparator.EQ,cell.elem,value);
		cell.elem = value;												
	}
	
	public void printArray() {
		System.out.println("Array " + name + " with length " + _v.size());
		for(int i = 0; i < _v.size(); i++) {
			ArrayCell cell = _v.elementAt(i);
			System.out.println(cell.original_elem);
		}
	}

}
