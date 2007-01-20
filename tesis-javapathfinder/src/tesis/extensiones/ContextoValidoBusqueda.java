package tesis.extensiones;

import java.util.HashSet;

/**
 * Clase generica que implementa la logica de los Contextos de Busqueda vacios o siempre validos
 */
public class ContextoValidoBusqueda extends ContextoBusqueda {
	
	public ContextoValidoBusqueda () {
	}
	
	@Override
	public boolean invalido() {
		return (false);
	}

	@Override
	public boolean cumplido() {
		return (true); 
	}

	@Override
	public void consumir(Evento e) {
	}

	@Override
	public void irAEstado(Integer est) {
	}

	@Override
	public int getEstadoActual() {
		return 0;
	}

}
