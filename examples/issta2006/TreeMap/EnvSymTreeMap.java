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
package issta2006.TreeMap;

import gov.nasa.jpf.jvm.Verify;
import gov.nasa.jpf.symbolic.integer.SymbolicInteger;

public class EnvSymTreeMap {
	static int N, A;

	static SymTreeMap act = new SymTreeMap(); // CHANGE CONSTRUCTOR IF NECESSARY

	static void logEvent(String event) {
	}

	static void clearLog() {
	}

	static int ii=0;
	static void step() {
		// SPECIFY THIS FUNCTION
		int i = Verify.random(1);
		SymbolicInteger x = new SymbolicInteger("X"+ii++);
		switch (i) {
		case 0:
			logEvent("put(" + x + ")");
			act.put(x);
			break;
		case 1:
			logEvent("remove(" + x + ")");
			act.remove(x);
			break;
		}
	}

	static void unbounded() {
		while (true) {
			step();
			if (A > 1)
				Verify.ignoreIf(act.checkAbstractState(A,-1,N)); //N not correct here!
		}
	}

	static void bounded() {
		for (int i = 0; i < N; i++) {
			step();
			if (A > 1)
				Verify.ignoreIf(act.checkAbstractState(A,-1,N));
		}
	}

	static void TestConfig(int length, int params, int technique, int symbolic) {
		// dummy method for the listener to pick up the configurations
	}

	/*
	 * Driver for Symbolic Execution 
	 * 
	 * Parameter A describes the type of search
	 * 0 - Random
	 * 1 - Classic Model Checking
	 * 6 - SE with Shape Abstraction
	 * 7 - SE with full subsumption checking
	 * 
	 * option (0) and (1) not used in the ISSTA results
	 */

	
	public static void main(String[] Argv) {

		N = Integer.parseInt(Argv[0], 10);
		A = Integer.parseInt(Argv[1], 10);

		TestConfig(N,-1,A,1);
		
		if (N == 0)
			unbounded();
		else
			bounded();

		if (A == 0)
			clearLog();
	}
}
