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
import gov.nasa.jpf.symbolic.integer.*;

public class SwapValuesSym {

	public static void concrete(int x, int y) {
		int old_x = x;
		int old_y = y;
		if (x > y) {
			x = x + y;
			y = x - y;
			x = x - y;
			if (x > y)
				assert false;
			else {
				System.out.println("x = " + x + " y = " + y);
				System.out.println("old x = " + old_x + " old y = " + old_y);
			}
		}
	}
	
	public static void symbolic (Expression x, Expression y) {
		Expression old_x = x; 
		Expression old_y = y;
		
		//Expression.pc._addDet(Comparator.GE,x,0);
		//Expression.pc._addDet(Comparator.GE,y,0);
		//Expression.pc._addDet(Comparator.LT,x,10);
		
		if (x._GT(y)) {
			System.out.println("PC branch 1: " + Expression.pc);
			x = x._plus(y);
			y = x._minus(y);
			x = x._minus(y);
			if (x._GT(y)) 
				assert false;
			else {
				System.out.println("PC Branch 2: " + Expression.pc);				
				System.out.println("x = " + x + " y = " + y);
        Expression.pc.solve();
				System.out.println("old x = " + old_x.solution() + 
						              " old y = " + old_y.solution()); 
				concrete(old_x.solution(),old_y.solution());
			}
		}
  		
	}
	
  public static void main(String[] args) {
  	Expression x = new SymbolicInteger("x");
  	Expression y = new SymbolicInteger("y");
		symbolic(x,y);
  }
  
}
