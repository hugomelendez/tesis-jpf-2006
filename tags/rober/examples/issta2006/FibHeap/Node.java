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
public class Node {

    public Node parent, left, right, child;

    public boolean mark=false;
    public int cost, degree=0;

    public Node(int c) {cost = c; right=this; left=this;}

    public String toString(int k, boolean flag) {
        String res="{"+mark+" ";
        if (flag) res+= cost+" ";
        if (k!=0) {
            if (left == null || left == this)
                res += "null";
            else 
                res+= left.toString(k-1, flag);
            if (right == null || right ==this)
                res += "null";
            else 
                res+= right.toString(k-1, flag);
            if (child == null)
                res += "null";
	}
	return res;
    }
    
    public String toString() {
    	return toString(0,true);
    }
}
