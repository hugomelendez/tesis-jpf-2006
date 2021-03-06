package tesis.Ejemplo_01;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.search.Search;

public class ListenerSinEstado extends ListenerAdapter {
	private int cont = 0;

	@Override
	public void choiceGeneratorAdvanced(JVM vm) {
		//System.out.println("--------------------------------- CHOICEGENERATORADVANCED: " + vm.getThreadNumber() + "  --------------------------------");
	}

	@Override
	public void choiceGeneratorProcessed(JVM vm) {
		//System.out.println("--------------------------------- CHOICEGENERATORPROCESSED: " + vm.getThreadNumber() + "  --------------------------------");
	}

	@Override
	public void choiceGeneratorSet(JVM vm) {
		//System.out.println("--------------------------------- CHOICEGENERATORSET: " + vm.getThreadNumber() + "  --------------------------------");
	}

	@Override
	public void stateProcessed(Search search) {
		System.out.println("--------------------------------- STATE-PROCESSED: " + search.getStateNumber() + "  --------------------------------");
	}

	@Override
	public void stateRestored(Search search) {
		System.out.println("--------------------------------- STATE-RESTORED: " + search.getStateNumber() + "  --------------------------------");
	}

	@Override
	public void stateAdvanced(Search search) {
		System.out.println("--------------------------------- STATE-ADVANCED: " + search.getStateNumber() + "  --------------------------------");
	}

	@Override
	public void stateBacktracked(Search search) {
		System.out.println("--------------------------------- STATE-BACKTRACKED:" + search.getStateNumber() + "--------------------------------");
	}

	@Override
	public void threadStarted(JVM vm) {
		System.out.println("--------------------------------- THREAD-STARTED: " + vm.getThreadNumber() + "  --------------------------------");
	}

	@Override
	public void threadTerminated(JVM vm) {
		System.out.println("--------------------------------- THREAD-TERMINATED: " + vm.getThreadNumber() + " --------------------------------");
	}

	public void instructionExecuted(JVM vm) {
		String s = new String("");
		cont++;
		
		try {
			Instruction lI = vm.getLastInstruction();

			s += "threadNum: " + vm.getThreadNumber() + " - ";
			s += "  li: " + lI + " - ";
			System.out.println(s);
		}
		catch (NullPointerException e) {
		}
	}

}