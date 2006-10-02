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
	protected AutomataVerificacion  aut;

	public Listener (AutomataVerificacion afd) {
		aut = afd;		
	}
	
	public final int getEstadoActual() {
		return aut.getEstadoActual();
	}

	public final void irAEstado(int i) {
		aut.irAEstado(i);		
	}

	@Override
	public final boolean check(Search search, JVM vm) {
		//System.out.println(" Aut chk estado " + getEstadoActual());
		return (!aut.estadoFinal());
		//return true;
	}
}

