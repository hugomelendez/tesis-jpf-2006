package tesis.extensiones;


/**
 * Clase generica que implementa la logica de los Contextos de Busqueda vacios o siempre validos
 */
public class ContextoValidoBusqueda extends ContextoBusqueda {
	
	public ContextoValidoBusqueda () {
		contexto = XMLContextoBusquedaReader.SEARCHCONTEXT_MODE_CONTEXT;
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
	public void irAEstado(State est) {
	}

	@Override
	public State getEstadoActual() {
		return new State(0);
	}
}
