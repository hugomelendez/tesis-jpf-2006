package tesis.Ejemplo_05;

import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.search.Search;
import tesis.extensiones.AutomataVerificacion;
import tesis.extensiones.Listener;

public class ListenerConEstado extends Listener {
	private int cont = 0;

	public ListenerConEstado (AutomataVerificacion afd) {
		super(afd);
	}
	
	@Override
	public void stateAdvanced(Search search) {
		System.out.println("--------------------------------- STATE-ADVANCED: " + search.getStateNumber() + ";" + aut.getEstadoActual() + "  --------------------------------");
	}

	@Override
	public void stateBacktracked(Search search) {
		System.out.println("--------------------------------- STATE-BACKTRACKED:" + search.getStateNumber()  + ";" + aut.getEstadoActual() + "--------------------------------");
	}

	public void instructionExecuted(JVM vm) {
		cont++;
		Instruction li = vm.getLastInstruction();
		try {
			aut.consumir(new EventoOpenClose (li, vm.getThreadNumber()));
		}
		catch (NullPointerException e) {
		}
	}
}
