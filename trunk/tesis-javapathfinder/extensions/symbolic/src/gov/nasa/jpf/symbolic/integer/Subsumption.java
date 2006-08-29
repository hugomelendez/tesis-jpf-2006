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
import java.util.*;
import omega.*;

//Author: Corina Pasareanu
@SuppressWarnings("unchecked")
public class Subsumption {

	// check r1 => r2
	// check r1 && !r2 is unsatisfiable
	// first r1 and r2 should be simplified -- eliminate existential quantifier

	static Relation r;

	static F_And root;

	public static boolean check(Relation r1, Relation r2,
			boolean debugFlag, int problemSize) {
		
		if (debugFlag) {
			System.out.println("check r1 => r2");
			System.out.println("r1");
			r1.prefixPrint();
			System.out.println("r2");
			r2.prefixPrint();
		}
		//check if there are conjuncts in r1
		Vector<Conjunct> conjuncts_r1 = r1.getRelBody().simplifiedDNF().getConjList();
		if (conjuncts_r1.size() != 1) {
			// check if r1 is empty then r1 => r2
			if (conjuncts_r1.size() == 0)
				return true;
			System.out.println("trouble: 1 we should always get only one conjunct");
			assert (false);
		}
		
		// iterate over r2
		Vector<Conjunct> conjuncts_r2 = r2.getRelBody().simplifiedDNF().getConjList();
		if (conjuncts_r2.size() != 1) {
			if (conjuncts_r2.size() == 0)
				return false;
			System.out.println("trouble: 2 we should always get only one conjunct");
			assert (false);
		}

		Conjunct conj_r2 = conjuncts_r2.elementAt(0);

		// for each EQ constraint do something  
		Equation[] eqs_r2 = conj_r2.getEQs();
		int neqs_r2 = conj_r2.getNumEQs();

		for (int i = 0; i < neqs_r2; i++) {

			create_copy(r1, problemSize, debugFlag); // result is in r and root

			// add to r the negation of current r2 constraint; for EEQ we consider LT and GT 
			// LT
			ConstraintHandle handle = root.addGEQ(false);
			Vector<VarDecl> vars = conj_r2.variables();
			for (int ix = 0; ix < vars.size(); ix++) {
				//int index_r2 = conj_r2.findColumn((VarDecl) vars.elementAt(ix));
				int index_r2 = vars.elementAt(ix).getPosition();
				VarDecl var_r = r.setVar(index_r2);
				//handle.updateCoefficient(var_r, -eqs_r2[i]
				//		.getCoefficient(index_r2));
				handle.updateCoefficient(var_r, -eqs_r2[i]
							.getCoefficient(ix+1));
			}

			handle.updateConstant(-eqs_r2[i].getConstant() - 1);

			if (debugFlag) {
				System.out.println("Copy of r1 and negation of constraint");
				r.prefixPrint();
			}
			boolean check_result = false;
			try {
				check_result = r.isSatisfiable();
			} catch (Error e) {
				r.prefixPrint();
			}
			if (check_result) {
				if (debugFlag)
					System.out.println("r1 => r2 is false");
				return false;
			}

			create_copy(r1, problemSize, debugFlag); // result is in r and root
			// add to r the negation of current r2 constraint 
			// GT
			handle = root.addGEQ(false);
			vars = conj_r2.variables();
			for (int ix = 0; ix < vars.size(); ix++) {
				//int index_r2 = conj_r2.findColumn((VarDecl) vars.elementAt(ix));
				int index_r2 = vars.elementAt(ix).getPosition();
				VarDecl var_r = r.setVar(index_r2);
				//handle.updateCoefficient(var_r, eqs_r2[i]
				//		.getCoefficient(index_r2));
				handle.updateCoefficient(var_r, eqs_r2[i]
							.getCoefficient(ix+1));
			}

			handle.updateConstant(eqs_r2[i].getConstant() - 1);

			if (debugFlag) {
				System.out.println("Copy of r1 and negation of constraint");
				r.prefixPrint();
			}
			try {
				check_result = r.isSatisfiable();
			} catch (Error e) {
				r.prefixPrint();
			}
			if (check_result) {
				if (debugFlag)
					System.out.println("r1 => r2 is false");
				return false;
			}
		}

		// for each GEQ constraint do something
		Equation[] geqs_r2 = conj_r2.getGEQs();
		int ngeqs_r2 = conj_r2.getNumGEQs();

		for (int i = 0; i < ngeqs_r2; i++) {

			create_copy(r1, problemSize, debugFlag); // result is in r and root

			// add to r the negation of current r2 constraint 
			ConstraintHandle handle = root.addGEQ(false);
			Vector<VarDecl> vars = conj_r2.variables();
			for (int ix = 0; ix < vars.size(); ix++) {
				//int index_r2 = conj_r2.findColumn((VarDecl) vars.elementAt(ix));
				int index_r2 = vars.elementAt(ix).getPosition();
				VarDecl var_r = r.setVar(index_r2);
				//handle.updateCoefficient(var_r, -geqs_r2[i]
				//		.getCoefficient(index_r2));
				handle.updateCoefficient(var_r, -geqs_r2[i]
						.getCoefficient(ix+1));

			}

			handle.updateConstant(-geqs_r2[i].getConstant() - 1);

			if (debugFlag) {
				System.out.println("Copy of r1 and negation of constraint");
				r.prefixPrint();
			}
			boolean check_result = false;
			try {
				//if (vars.size() >= 7) {
				//	r.prefixPrint();
				//}
				r.simplify();
				check_result = r.isSatisfiable();
			} catch (Error e) {
				System.out.println("i = " + i);
				r.prefixPrint();
				r1.prefixPrint();
				r2.prefixPrint();
			}
			if (check_result) {
				if (debugFlag)
					System.out.println("r1 => r2 is false");
				return false;
			}
		}

		if (debugFlag)
			System.out.println("r1 => r2 is true");
		return true;
	}

  static void create_copy(Relation r1, int problemSize, boolean debugFlag) {
		// create new relation that copies r1
		// can I do it simpler?

		r = new Relation(problemSize);
		root = r.addAnd();

		Vector<Conjunct> conjuncts_r1 = r1.getRelBody().simplifiedDNF().getConjList();
		if (conjuncts_r1.size() != 1) {
			System.out.println("trouble: 3 we should always get only one conjunct");
			assert (false);
		}
		Conjunct conj_r1 = conjuncts_r1.elementAt(0);

		// for each EQ constraint do something
		Equation[] eqs_r1 = conj_r1.getEQs();
		int neqs_r1 = conj_r1.getNumEQs();

		for (int j = 0; j < neqs_r1; j++) {
			ConstraintHandle handle = root.addEQ(false);
			Vector<VarDecl> vars = conj_r1.variables();
			for (int ix = 0; ix < vars.size(); ix++) {
				//int index_r1 = conj_r1.findColumn((VarDecl) vars.elementAt(ix));
				int index_r1 = vars.elementAt(ix).getPosition();
				VarDecl var_r = r.setVar(index_r1);
				//handle.updateCoefficient(var_r, eqs_r1[j]
				//		.getCoefficient(index_r1));
				handle.updateCoefficient(var_r, eqs_r1[j]
						.getCoefficient(ix+1));

			}

			handle.updateConstant(eqs_r1[j].getConstant());
		}

		// for each GEQ constraint do something
		Equation[] geqs_r1 = conj_r1.getGEQs();
		int ngeqs_r1 = conj_r1.getNumGEQs();

		for (int j = 0; j < ngeqs_r1; j++) {
			ConstraintHandle handle = root.addGEQ(false);
			Vector<VarDecl> vars = conj_r1.variables();
			for (int ix = 0; ix < vars.size(); ix++) {
				//int index_r1 = conj_r1.findColumn((VarDecl) vars.elementAt(ix));
				int index_r1 = vars.elementAt(ix).getPosition();
				VarDecl var_r = r.setVar(index_r1);
				//handle.updateCoefficient(var_r, geqs_r1[j]
				//		.getCoefficient(index_r1));
				handle.updateCoefficient(var_r, geqs_r1[j]
						.getCoefficient(ix+1));

			}

			handle.updateConstant(geqs_r1[j].getConstant());
		}

		if (debugFlag) {
			System.out.println("Copy of r1");
			r.prefixPrint();
		}

		// end create copy 
	}

}
