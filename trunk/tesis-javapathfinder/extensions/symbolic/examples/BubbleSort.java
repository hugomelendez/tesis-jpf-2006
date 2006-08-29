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
import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.symbolic.array.*;
public class BubbleSort 
{
	
	public static boolean sorted(ArrayIntStructure a) {
		Expression j = new IntegerConstant(0);
		while (j._LT(a.length)) {
			if (!(a._get(j)._LE(a._get(j._plus(1)))))
				return false;
			j = j._plus(1);
		}
		return true;
	}
	
	public static void main(String[] args) 
	{
		ArrayIntStructure b = new ArrayIntStructure("b");
		b.length = new IntegerConstant(3);
		//int b[] ={8, 2, 1, 7, 6, 5, 3, 4, 0, 10, 9};
		new BBS().Sort(b);
		//new BBS().Sort(b);
		System.out.println("PC: " + Expression.pc);
		assert sorted(b);
		// print the sorted array
		//for (int k = 0; k < b.length; k++){
		//	System.out.println("b[" + k + "]: " + b[k]);
		//}
	}
}

class BBS{
	
	public void Sort(int a[]){
		for (int i = a.length; --i >= 0; ) 
		{
			for (int j = 0; j < i; j++) 
			{
				if (a[j] > a[j+1]) 
				{
					int temp = a[j];
					a[j] = a[j + 1];
					a[j + 1] = temp;
				}
			}
		}
	}
	
	public void Sort(ArrayIntStructure a){
		IntegerConstant zero = new IntegerConstant(0);
		Expression i = a.length;
		i = i._minus(1);
		while (i._GE(zero)) {
			Expression j = zero;
			while (j._LT(i)) {
				Expression j_plus_1 = j._plus(1);
				if (a._get(j)._GT(a._get(j_plus_1))) {
					Expression temp = a._get(j);
					a._set(j,a._get(j_plus_1));
					a._set(j_plus_1,temp);
				}
				j = j_plus_1;
			}
			i = i._minus(1);
		}
	}
}
