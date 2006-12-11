package tesis.Ejemplo_zPruebaBugJPF;

import java.util.List;
import java.util.HashSet;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import tesis.extensiones.*;

// TODO Levantar los datos del XML
public class XMLAFDReaderCANAL extends XMLAFDReader {

	public XMLAFDReaderCANAL(String file) {
		super(file);
	}

	public int estadoInicial() {
		return 0;
		//return 2;
	}

	public HashSet<Transicion> transiciones() {
		HashSet<Transicion> hs = new HashSet<Transicion>();

		hs.add(new Transicion(0, 1, new Evento("CLOSE")));
		
		hs.add(new Transicion(1, 0, new Evento("OPEN")));
		hs.add(new Transicion(1, 1, new Evento("CLOSE")));
		hs.add(new Transicion(1, 999, new Evento("WRITE")));
		
		/*hs.add(new Transicion(0, 1, new Evento("OPEN")));
		hs.add(new Transicion(1, 999, new Evento("WRITE")));*/
		return hs;
	}

	public HashSet<Integer> estadosFinales() {
		HashSet<Integer> hs = new HashSet<Integer>();

		hs.add(new Integer(999));
		
		return hs;
	}
}
