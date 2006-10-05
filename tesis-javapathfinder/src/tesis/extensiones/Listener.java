package tesis.extensiones;

import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.search.Search;

/**
 * Clase Listener de Verificación
 * Se encarga de lo básico: cada vez que ocurre instrucción, notifica al Coordinador
 * Podría llegar a extenderse, habría que ver si el método instructionExecuted sería final o no
 */
public class Listener extends PropertyListenerAdapter implements JPFListener {
	protected Coordinador coord;

	public Listener (Coordinador c) {
		coord = c;
	}

	@Override
	public final void instructionExecuted(JVM vm) {
		coord.ocurrioInstruccion(vm.getLastInstruction());
	}

	@Override
	public final boolean check(Search search, JVM vm) {
		return (!coord.propiedadViolada());
	}
}