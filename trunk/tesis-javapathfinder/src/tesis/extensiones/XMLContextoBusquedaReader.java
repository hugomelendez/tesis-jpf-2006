package tesis.extensiones;

import java.util.HashSet;

// TODO Levantar los datos del XML
public class XMLContextoBusquedaReader extends XMLReader {
	EventBuilder eventBuilder;

	public XMLContextoBusquedaReader(String file, EventBuilder eb) {
		super(file);
		eventBuilder = eb;
	}

	public XMLContextoBusquedaReader(String file) {
		super(file);
	}

	public int estadoInicial() {
		return 0;
	}

	public HashSet<Transicion> transiciones() {
		HashSet<Transicion> hs = new HashSet<Transicion>();

		//Cuando es en MODO Preambulo
		//hs.add(new Transicion(0, 1, new Evento("OPEN")));
		//hs.add(new Transicion(1, 2, new Evento("OPEN")));
		
		//Cuando es en MODO Contexto
		hs.add(new Transicion(0, 0, new Evento("OPEN")));

		return hs;
	}

	public Integer estadoFinal() {
		return (new Integer(2));
		//return (new Integer(0));
	}

}
