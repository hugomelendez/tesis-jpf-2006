package tesis.extensiones;

import java.util.HashSet;

/**
 * Clase generica que mantiene los estados y transiciones obtenidos del XML de propiedades 
 */
public class PropertyTemplate {
	protected  HashSet<Transicion> transiciones;
	protected HashSet<State> estadosFinales;
	protected State estadoInicial;
	
	public PropertyTemplate () {
	}

	public PropertyTemplate (XMLAFDReader xmlafd) throws XMLException {
		estadoInicial = xmlafd.estadoInicial();
		transiciones = xmlafd.transiciones();
		estadosFinales = xmlafd.estadosFinales();
	}
	
	public HashSet<State> estadosFinales() {
		return estadosFinales;
	}
	public State estadoInicial() {
		return estadoInicial;
	}
	public HashSet<Transicion> transiciones() {
		return transiciones;
	}
}
