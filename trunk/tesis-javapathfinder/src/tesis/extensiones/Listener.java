package tesis.extensiones;

import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.jvm.bytecode.InvokeInstruction;
import gov.nasa.jpf.search.Search;

/**
 * Clase Listener de Verificaci�n
 * Se encarga de lo b�sico: cada vez que ocurre instrucci�n, notifica al Coordinador
 * Podr�a llegar a extenderse, habr�a que ver si el m�todo instructionExecuted ser�a final o no
 */
public class Listener extends PropertyListenerAdapter implements JPFListener {
	protected Coordinador coord;

	public Listener (Coordinador c) {
		coord = c;
	}

	// TODO Revisar
	// TODO Revisar
	// TODO Revisar
	// TODO Revisar
	// TODO Revisar
	@Override
	public void executeInstruction(JVM vm) {
		Instruction li = vm.getNextInstruction();
		try {
			if (li instanceof InvokeInstruction) {
				InvokeInstruction i = (InvokeInstruction) li;
				System.out.println(i.toString());
			}
			if (li.toString().contains("open") && li.toString().contains("Ejemplo")) {
				System.out.println("PRE-OBJECTID = " + vm.getLastThreadInfo().getCalleeThis(li.getMethod()));
			}
		} catch (Exception e) {
		}
	}
	// TODO Revisar
	// TODO Revisar
	// TODO Revisar
	// TODO Revisar



	@Override
	public final void instructionExecuted(JVM vm) {
		//System.out.println(vm.getLastInstruction());

		try {
			if (vm.getLastInstruction().toString().contains("open") && vm.getLastInstruction().toString().contains("Ejemplo")) {
				System.out.println("OBJECTID = " + vm.getLastThreadInfo().getCalleeThis(vm.getLastInstruction().getMethod()));
			}
		} catch (Exception e) {
		}
		coord.ocurrioInstruccion(vm.getLastInstruction());
	}

	@Override
	public final boolean check(Search search, JVM vm) {
		return (!coord.propiedadViolada());
	}
}