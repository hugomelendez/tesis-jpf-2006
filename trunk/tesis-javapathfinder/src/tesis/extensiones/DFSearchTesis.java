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
package tesis.extensiones;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.util.Debug;

/**
 * standard depth first model checking (but can be bounded by search depth
 * and/or explicit Verify.ignoreIf)
 */
public class DFSearchTesis extends gov.nasa.jpf.search.Search {
	Coordinador coord;

	public DFSearchTesis(Config config, JVM vm) {
		super(config, vm);

		Debug.println(Debug.MESSAGE, "MC Search");
	}

	/**
	 * state model of the search
	 *    next new  -> action
	 *     T    T      forward
	 *     T    F      backtrack, forward
	 *     F    T      backtrack, forward
	 *     F    F      backtrack, forward
	 *
	 * end condition
	 *    backtrack failed (no saved states)
	 *  | property violation (currently only checked in new states)
	 *  | search constraint (depth or memory or time)
	 *
	 * <2do> we could split the properties into forward and backtrack properties,
	 * the latter ones being usable for liveness properties that are basically
	 * condition accumulators for sub-paths of the state space, to be checked when
	 * we backtrack to the state where they were introduced. I think that could be
	 * actually much simpler (to implement) and more powerful than our currently
	 * broken LTL based scheme.
	 * But then again - at some point the properties and the searches will probably
	 * be unified into VM listeners, anyway
	 */
	public void search() {
		int maxDepth = getMaxSearchDepth();

		depth = 0;

		notifySearchStarted();
		while (!done) {
			// Si el par <estado VM, estado Listener> es conocido
			// || estado VM es final
			// || el coordinador lo indica (ContextoBusqueda no se cumplio)
			// --> backtrack
			//if ( !isNewState
			if ( isEndState
					|| coord.backtrackear() ) {

				if (!backtrack()) { // backtrack not possible, done
					break;
				}

				depth--;
				//assert depth == vm.getPath().length();

				coord.stateBacktracked();
				
				notifyStateBacktracked();
			}

			coord.registrarEstadoVistado();
			if (forward()) {
				coord.stateAdvanced();
				
				notifyStateAdvanced();

				if (hasPropertyTermination()) {
					break;
				}

				depth++;
				//assert depth == vm.getPath().length();

				if (isNewState) {
					if (depth >= maxDepth) {
						isEndState = true;
						notifySearchConstraintHit(QUEUE_CONSTRAINT);
					}

					if (!checkStateSpaceLimit()) {
						notifySearchConstraintHit(SIZE_CONSTRAINT);
						// can't go on, we exhausted our memory
						break;
					}
				}
			} else { // state was processed
				notifyStateProcessed();
			}
		}

		notifySearchFinished();
	}

	/**
	 * Helper method para agregar listener que trabajan para nosotros
	 * @param l listener especial para la tesis
	 */
	public void setCoordinador(Coordinador c) {
		coord = c;
		c.setSearch(this);
	}
}
