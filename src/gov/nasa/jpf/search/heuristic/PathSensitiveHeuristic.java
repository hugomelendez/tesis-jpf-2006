package gov.nasa.jpf.search.heuristic;

/**
 * This should encompass all heuristics whose value depends on the
 * path to the state, not just the state itself.  An exception is 
 * a heuristic search in which the "best" path is the one visited
 * first (e.g. BFS).
 * TODO: not yet properly utilized
 * @author peterd
 */
public interface PathSensitiveHeuristic extends Heuristic {

}
