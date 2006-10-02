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
package gov.nasa.jpf.symbolic.integer;

import gov.nasa.jpf.jvm.Verify;


/**
 * @author Sarfraz Khurshid (khurshid@lcs.mit.edu)
 *
 */
public class SymbolicInteger extends LinearExpression {
  int        _min = Integer.MIN_VALUE;
  int        _max = Integer.MAX_VALUE;
  public int solution = 0; // C
  public String name;

  public SymbolicInteger () {
    super();
    PathCondition.symbolicVarCount++;
  }

  public SymbolicInteger (String s) {
		super();
		PathCondition.symbolicVarCount++;
		name = s;
		//System.out.println(name);
  }


  public SymbolicInteger (int l, int u) {
    _min = l - 1;
    _max = u + 1;
    PathCondition.symbolicVarCount++;
  }

  public String toString () {
    if (!PathCondition.flagSolved) {
    	return (name != null) ? name : "INT_" + hashCode();
	  	
    } else {
			return (name != null) ? name + "[" + solution + "]" : 
                              "INT_" + hashCode() + "[" + solution + "]";
    }
  }

  public boolean updateMax (int i) {
    if (i < _min) {
      //              System.out.println("BACKTRACKING!!!");
      Verify.ignoreIf(true);
    }

    if (_max <= i) {
      return false;
    }

    _max = i;

    //          System.out.println("Setting _max to " + _max);
    return true;
  }

  public boolean updateMin (int i) {
    if (i > _max) {
      //              System.out.println("BACKTRACKING!!!");
      Verify.ignoreIf(true);
    }

    if (_min >= i) {
      return false;
    }

    _min = i;

    //          System.out.println("Setting _min to " + _min);
    return true;
  }
  
  public int solution() {
  		return solution;
  }
}
