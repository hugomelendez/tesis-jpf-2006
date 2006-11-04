package tesis.extensiones;

import java.util.HashSet;

// TODO Levantar los datos del XML
public class XMLEventBuilderReader {
	String nmXML;
	
	public XMLEventBuilderReader(String file) {
		nmXML = file;
	}
	
	public HashSet<Evento> eventos() {
		HashSet<Evento> hs = new HashSet<Evento>();

		hs.add(new Evento("invoke", "open", "OPEN"));
		hs.add(new Evento("invoke", "close", "CLOSE"));
		hs.add(new Evento("invoke", "write", "WRITE"));

		return hs;
	}

}
