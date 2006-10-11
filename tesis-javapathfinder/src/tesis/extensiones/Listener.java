package tesis.extensiones;

import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
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

	@Override
	public final void instructionExecuted(JVM vm) {
		//System.out.println(vm.getLastInstruction());
		coord.ocurrioInstruccion(vm.getLastInstruction());
	}

	@Override
	public final boolean check(Search search, JVM vm) {
		return (!coord.propiedadViolada());
	}
}