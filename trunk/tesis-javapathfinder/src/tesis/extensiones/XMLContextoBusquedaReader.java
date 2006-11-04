package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

// TODO Levantar los datos del XML
public class XMLContextoBusquedaReader {
	String nmXML;
	Document document;
	
	public XMLContextoBusquedaReader(String file) {
		nmXML = file;
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

	public void openFile() throws DocumentException {
		SAXReader reader = new SAXReader();
        document = reader.read(nmXML);
	}

}
