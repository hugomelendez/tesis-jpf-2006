package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Clase generica que implementa la logica de los Preambulos
 * (toma los datos de un XML)
 * 
 */
public class Preambulo {
	private XMLPreambuloReader xml;
	private static final int ESTADO_VIOLADO = 999;
	protected int estadoActual;
	private int estadoFinal;
	private HashSet<Transicion> setTransiciones;
	
	public Preambulo (XMLPreambuloReader xml) {
		this.xml = xml;
		estadoActual = xml.estadoInicial();
		estadoFinal = xml.estadoFinal();
		setTransiciones = xml.transiciones();
	}
	
	public final boolean violado() {
		return (estadoActual==ESTADO_VIOLADO);
	}

	public final boolean cumplido() {
		return (estadoActual == estadoFinal); 
	}

	public final void consumir(Evento e) {
		Transicion tran;
		Iterator<Transicion> it;
		
		it = setTransiciones.iterator();
		
		//System.out.println("EVENTO: " + e.label());
		
 		//Avanza/Viola el Preambulo solo si es un evento observable
		if ( e.esObservable() ) {
			while (it.hasNext()) {
				tran = it.next();
	
				if (tran.estadoDesde() == estadoActual && tran.evento().equals(e)) {
					estadoActual = tran.estadoHacia();
					break;
				}
				else if (tran.estadoDesde() == estadoActual && !tran.evento().equals(e)) {
					estadoActual = ESTADO_VIOLADO;
					break;
				}
			}
		}
	}

	public final void irAEstado(Integer est) {
		System.out.println("Preambulo BACKTRACK al estado " + est);
		estadoActual = est;
	}

	public final int getEstadoActual() {
		return estadoActual;
	}

}
