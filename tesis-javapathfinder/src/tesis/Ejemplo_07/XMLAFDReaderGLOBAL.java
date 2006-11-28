package tesis.Ejemplo_07;

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
public class XMLAFDReaderGLOBAL extends XMLAFDReader {

	public XMLAFDReaderGLOBAL(String file) {
		super(file);
	}

	public int estadoInicial() {
		return 0;
		//return 2;
	}

	public HashSet<Transicion> transiciones() {
		HashSet<Transicion> hs = new HashSet<Transicion>();

		//NO HACE NADA ES PARA VERIFICAR QUE SE USEN SOLO LOS DE INSTANCIA!!!
/*		hs.add(new Transicion(0, 1, new Evento("CLOSE")));
		
		hs.add(new Transicion(1, 0, new Evento("OPEN")));
		hs.add(new Transicion(1, 1, new Evento("CLOSE")));
		hs.add(new Transicion(1, 999, new Evento("WRITE")));
*/
		return hs;
	}

	public HashSet<Integer> estadosFinales() {
		HashSet<Integer> hs = new HashSet<Integer>();

		hs.add(new Integer(999));
		
		return hs;
	}
}
