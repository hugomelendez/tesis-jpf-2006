package tesis.extensiones;

import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.search.Search;

/**
 * Clase abstracta para extender en los Listeners de Verificación
 * 
 */
public abstract class Listener extends PropertyListenerAdapter implements JPFListener {
	protected Coordinador coord;

	public Listener (Coordinador c) {
		coord = c;
	}

	@Override
	public void instructionExecuted(JVM vm) {
		coord.ocurrioInstruccion(vm.getLastInstruction());
	}

	@Override
	public final boolean check(Search search, JVM vm) {
		return (!coord.propiedadViolada());
	}
}