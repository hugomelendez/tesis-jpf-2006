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
package issta2006;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.Path;
import gov.nasa.jpf.jvm.Step;
import gov.nasa.jpf.jvm.ThreadInfo;
import gov.nasa.jpf.jvm.Transition;
import gov.nasa.jpf.jvm.bytecode.INVOKESTATIC;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.search.Search;

import java.util.Hashtable;
import java.util.Vector;

public class TestListener extends ListenerAdapter {

	int state_id = 0;

	Hashtable<String,String> done = new Hashtable<String,String>();

	int done_native = 0, test_length_total = 0;
	
	//this will hold all the labels (in order) for one transition 
	Vector<String> testLabels = new Vector<String>(); 

	/******************************************* SearchListener interface *****/
	public void searchStarted(Search search) {
	}
	
	private void addLabelsToTransitionComment(Transition t) {
		String result = "";
		for (int i = 0; i < testLabels.size(); i++) {
			result += testLabels.get(i);
		}
		//((Step)t.getTransitionStep(0)).addComment(result);
		t.getStep(0).setComment(result);
		// clear all the labels 
		testLabels = new Vector<String>();
	}
	
	public void stateAdvanced(Search search) {
		state_id = search.getStateNumber();
		addLabelsToTransitionComment(search.getTransition());
	}

	public void stateProcessed(Search search) {
	}

	public void stateBacktracked(Search search) {
		state_id = search.getStateNumber();
	}

	public void searchFinished(Search search) {
		/* This needs fixing
		System.err.println("----------------------------");
		System.err.println("Tests info:");
		if (done_native > 0) {
			System.err.println("Tests covered: " + done_native);
			System.err.println("Avg. test length: "
					+ (double) test_length_total / done_native);
		} else {
			System.err.println("Tests covered: " + done.size());
			System.err.println("Avg. test length: "
					+ (double) test_length_total / done.size());
		}
		System.out.println("----------------------------");
		*/
	}

	public void searchConstraintHit(Search search) {
		// TODO
	}

	public void stateRestored(Search search) {
		state_id = search.getStateNumber();
		// TODO
	}

	public void propertyViolated(Search search) {
		// TODO
	}

	/******************************************* VMListener interface *********/
	
	private void newPrintSequence(JVM jvm) {
		//test_length_total = 0;
		Path path = jvm.getPath();
		for(int i = 0; i < path.length(); i++) {
			Transition p = path.get(i);
			Step stp = p.getStep(0);
			//String cmt = stp.getFirstComment();
			String cmt = stp.getComment();
			if ((cmt!=null)&& !cmt.equals("")) {
				System.out.print(cmt + ";");
				test_length_total++;
			}
		}
		// if the branch was covered before the end of the transition
		// also print what were covered so far
		for (int i = 0; i < testLabels.size(); i++) {
			System.out.print(testLabels.get(i)+ ";");
			test_length_total++;
		}
		System.out.println();
	}

	private void registerTest(String s, JVM jvm) {
		if (done.get(s) == null) {
			done.put(s, "1");
			System.out.println("Test case number " + done.size() + " for '" + s
					+ "': ");
			newPrintSequence(jvm);
		}
	}

	//--- the ones we are interested in
	public void instructionExecuted(JVM jvm) {
		Instruction insn = jvm.getLastInstruction();
		ThreadInfo ti = jvm.getLastThreadInfo();
/*
		if ((insn instanceof INVOKESTATIC))
			System.out.println("instr " + ((INVOKESTATIC) insn).mname);
		
		if ((insn instanceof INVOKEVIRTUAL))
			System.out.println("instr " + ((INVOKEVIRTUAL) insn).mname);
		
*/		
		if ((insn instanceof INVOKESTATIC)
				&& (((INVOKESTATIC) insn).mname
						.equals("logEvent(Ljava/lang/String;)V"))) {
			String info = ti.getStringLocal("event");		
			//System.out.println("add label " + info);
			testLabels.add(info);
			//newPrintSequence(jvm);
		} else if ((insn instanceof INVOKESTATIC)
				&& (((INVOKESTATIC) insn).mname.equals("testCase(I)V"))) {
			int c = ti.getIntLocal("branch");
			registerTest(c + ";",jvm);
		} else if ((insn instanceof INVOKESTATIC)
				&& (((INVOKESTATIC) insn).mname
						.equals("testCase(Ljava/lang/String;)V"))) {
			registerTest(ti.getStringLocal("branch"),jvm);
		} else if ((insn instanceof INVOKESTATIC)
				&& (((INVOKESTATIC) insn).mname.equals("clearLog()V"))) {
			testLabels = new Vector<String>();
		} else if ((insn instanceof INVOKESTATIC)
				&& (((INVOKESTATIC) insn).mname.equals("outputTestSequence(I)V"))) {
			done_native = ti.getIntLocal("number");
			newPrintSequence(jvm);
		} 
	}

}

