package tesis.Ejemplo_04;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.search.Search;

public class ListenerSinEstado extends ListenerAdapter {
	private int cont = 0;

	@Override
	public void stateProcessed(Search search) {
		System.out.println("       STATE-PROCESSED: " + search.getStateNumber() );
	}

	@Override
	public void stateRestored(Search search) {
		System.out.println("       STATE-RESTORED: " + search.getStateNumber() );
	}

	@Override
	public void stateAdvanced(Search search) {
		System.out.println("       STATE-ADVANCED: " + search.getStateNumber() );
	}

	@Override
	public void stateBacktracked(Search search) {
		System.out.println("       STATE-BACKTRACKED:" + search.getStateNumber());
	}

	@Override
	public void instructionExecuted(JVM vm) {
		String s = new String("");
		cont++;
		
		try {
			if (vm.getLastInstruction().toString().contains("Verify")) {			
				s += "  li: " + vm.getLastInstruction() + " - ";
				System.out.println(s);
			}
		}
		catch (NullPointerException e) {
		}
	}

}