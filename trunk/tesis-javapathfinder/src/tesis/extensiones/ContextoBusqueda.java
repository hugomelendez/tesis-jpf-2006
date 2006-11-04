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
 * 
 */
public class ContextoBusqueda {
	private XMLContextoBusquedaReader xml;
	private static final int ESTADO_INVALIDO = 999;
	protected int estadoActual;
	private int estadoFinal;
	private HashSet<Transicion> setTransiciones;
	
	public ContextoBusqueda (XMLContextoBusquedaReader xml) {
		this.xml = xml;
		estadoActual = xml.estadoInicial();
		estadoFinal = xml.estadoFinal();
		setTransiciones = xml.transiciones();
	}
	
	public final boolean invalido() {
		return (estadoActual==ESTADO_INVALIDO);
	}

	/**
	 * Se utiliza para el modo Prefijo/Preambulo, en el que deben cumplirse una
	 * secuencia de Eventos antes de comenzar a verificar la propiedad en el AFD
	 * @return
	 */
	public final boolean cumplido() {
		return (estadoActual == estadoFinal); 
	}

	public final void consumir(Evento e) {
		Transicion tran;
		Iterator<Transicion> it;
		boolean transicionValida = false;
		
		it = setTransiciones.iterator();
		
		//System.out.println("EVENTO: " + e.label());
		
 		//Avanza/Viola el ContextoBusqueda solo si es un evento observable
		if ( e.esObservable() ) {
			while (it.hasNext() && !transicionValida) {
				tran = it.next();
	
				if (tran.estadoDesde() == estadoActual && tran.evento().equals(e)) {
					estadoActual = tran.estadoHacia();
					transicionValida = true;
				}
			}

			if (!transicionValida)
				estadoActual = ESTADO_INVALIDO;
		}
	}

	public final void irAEstado(Integer est) {
		System.out.println("ContextoBusqueda BACKTRACK al estado " + est);
		estadoActual = est;
	}

	public final int getEstadoActual() {
		return estadoActual;
	}

}
