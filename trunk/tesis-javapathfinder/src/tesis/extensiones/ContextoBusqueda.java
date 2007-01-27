package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Clase generica que implementa la logica de los Contextos de Busqueda
 * (toma los datos de un XML)
 * 
 * El Contexto de Busq. genera una condicion de ramas validas, para revisar en
 * todo momento si es aceptable continuar con la exploracion actual o es necesario 
 * backtrackear
 * 
 */
public class ContextoBusqueda {
	private static final int ESTADO_INVALIDO = -9999;
	protected int estadoAnterior;
	private int estadoActual;
	private int estadoFinal;
	protected String contexto;
	private HashSet<Transicion> setTransiciones;
	
	/**
	 * Constructor default
	 * Sirve para la subclase ContextoValidoBusqueda
	 *
	 */
	public ContextoBusqueda () {
	}

	public ContextoBusqueda (XMLContextoBusquedaReader xml) {
		setEstadoActual(xml.estadoInicial());
		estadoFinal = xml.estadoFinal();
		setTransiciones = xml.transiciones();
		contexto = xml.modoContexto();
	}
	
	public boolean invalido() {
		return (estadoActual==ESTADO_INVALIDO);
	}

	/**
	 * Se utiliza para el modo Prefijo/Preambulo, en el que deben cumplirse una
	 * secuencia de Eventos antes de comenzar a verificar la propiedad en el AFD
	 * @return
	 */
	public boolean cumplido() {
		return (estadoActual == estadoFinal); 
	}

	public void consumir(Evento e) {
 		//Avanza/Viola el ContextoBusqueda solo si es un evento observable
		if ( e.esObservable() ) {
			Transicion tran;
			Iterator<Transicion> it;
			boolean transicionValida = false;

			it = setTransiciones.iterator();
			while (it.hasNext() && !transicionValida) {
				tran = it.next();
	
				if (tran.estadoDesde() == estadoActual && tran.evento().equals(e)) {
					setEstadoActual(tran.estadoHacia());
					transicionValida = true;
				}
			}

			if (!transicionValida) {
				setEstadoActual(ESTADO_INVALIDO);
			}
		}
	}

	public void irAEstado(Integer est) {
		setEstadoActual(est);
	}

	public int getEstadoActual() {
		return estadoActual;
	}

	public int getEstadoAnterior() {
		return estadoAnterior;
	}

	private void setEstadoActual(int estado) {
		estadoAnterior = estadoActual;
		estadoActual = estado;
	}

	public boolean modoContexto() {
		return contexto == XMLContextoBusquedaReader.SEARCHCONTEXT_MODE_CONTEXT;
	}
	public boolean modoPreambulo() {
		return contexto == XMLContextoBusquedaReader.SEARCHCONTEXT_MODE_PREAMBLE;
	}
}
