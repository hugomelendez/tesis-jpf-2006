package tesis.Ejemplo_07;

import java.util.HashSet;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import tesis.extensiones.*;

// TODO Levantar los datos del XML
public class XMLContextoBusquedaReaderEj07 extends XMLContextoBusquedaReader {
	String nmXML;
	Document document;
	
	public XMLContextoBusquedaReaderEj07(String file) {
		super(file);
	}

	public int estadoInicial() {
		return 0;
	}

	public HashSet<Transicion> transiciones() {
		HashSet<Transicion> hs = new HashSet<Transicion>();

		hs.add(new Transicion(0, 1, new Evento("OPEN")));
		
		hs.add(new Transicion(1, 2, new Evento("OPEN")));
		hs.add(new Transicion(1, 0, new Evento("CLOSE")));

		hs.add(new Transicion(2, 1, new Evento("CLOSE")));

		//No me interesa el WRITE (rulo en c/estado)
		hs.add(new Transicion(0, 0, new Evento("WRITE")));
		hs.add(new Transicion(1, 1, new Evento("WRITE")));
		hs.add(new Transicion(2, 2, new Evento("WRITE")));

		return hs;
	}

	public Integer estadoFinal() {
		return (new Integer(2));
		//return (new Integer(0));
	}

}
