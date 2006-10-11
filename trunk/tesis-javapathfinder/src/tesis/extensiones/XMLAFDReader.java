package tesis.extensiones;

import java.util.HashSet;

// TODO Levantar los datos del XML
public class XMLAFDReader {
	String nmXML;
	
	public XMLAFDReader(String file) {
		nmXML = file;
	}
	
	public int estadoInicial() {
		return 0;
	}

	public HashSet<Transicion> transiciones() {
		HashSet<Transicion> hs = new HashSet<Transicion>();

		hs.add(new Transicion(0, 1, new Evento("OPEN")));
		hs.add(new Transicion(0, 999, new Evento("CLOSE")));
		
		hs.add(new Transicion(1, 2, new Evento("OPEN")));
		hs.add(new Transicion(1, 0, new Evento("CLOSE")));
		
		hs.add(new Transicion(2, 3, new Evento("OPEN")));
		hs.add(new Transicion(2, 1, new Evento("CLOSE")));
		
		hs.add(new Transicion(3, 999, new Evento("OPEN")));
		hs.add(new Transicion(3, 2, new Evento("CLOSE")));
		
		return hs;
	}

	public HashSet<Integer> estadosFinales() {
		HashSet<Integer> hs = new HashSet<Integer>();

		hs.add(new Integer(999));
		//Esto es para que el ejemplo 05 pinche por 3 OPENs consecutivos
		hs.add(new Integer(3));
		
		return hs;
	}
}
