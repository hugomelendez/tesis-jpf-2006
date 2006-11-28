package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

// TODO Levantar los datos del XML
public class XMLAFDReader {
	String nmXML;
	Document document;

	public XMLAFDReader(String file) {
		nmXML = file;
	}

	public int estadoInicial() {
		// Buscamos el estado inicial
		Element estado = (Element)document.selectSingleNode("//TypeStateProperty/estados/estado[@inicial]");
		String label = ((Attribute)estado.attribute("label")).getValue();
		Integer i = new Integer(label);
		return i;
	}

	private Evento findEvent(String eventLabel) {
		// buscamos el evento q matchea y lo devolvemos
		Element el = (Element)document.selectSingleNode("//TypeStateProperty/eventos/evento[@label='" + eventLabel + "']");

		String type = ((Attribute)el.attribute("type")).getValue();
		String name = ((Attribute)el.attribute("name")).getValue();
		String label = ((Attribute)el.attribute("label")).getValue();

		Evento e = new Evento(type, name, label);
		System.out.println(e);
		return (e);
	}
	
	public HashSet<Transicion> transiciones() {
		HashSet<Transicion> hs = new HashSet<Transicion>();

		List lt = document.selectNodes("//TypeStateProperty/transiciones/transicion");
		for (Iterator i = lt.iterator(); i.hasNext();) {
			Element foo = (Element) i.next();

			int desde = new Integer(((Attribute)foo.attribute("desde")).getValue());
			int hasta = new Integer(((Attribute)foo.attribute("hasta")).getValue());
			Evento e = findEvent(((Attribute)foo.attribute("evento")).getValue());

			Transicion t = new Transicion(desde, hasta, e);
			System.out.println(t);

			hs.add(t);
		}

		return hs;
	}

	public HashSet<Integer> estadosFinales() {
		HashSet<Integer> hs = new HashSet<Integer>();

		// Buscamos los estados finales
		List l = document.selectNodes("//TypeStateProperty/estados/estado[@final]");
		for (Iterator i = l.iterator(); i.hasNext();) {
			Element estado = (Element) i.next();
			hs.add(new Integer(((Attribute)estado.attribute("label")).getValue()));
		}

		return hs;
	}

	public void openFile() throws DocumentException {
		SAXReader reader = new SAXReader();
        document = reader.read(nmXML);
	}

	public static void main (String[] args) {
		XMLAFDReader x = new XMLAFDReader("/home/hugo/workspace/javapathfinder-trunk/src/tesis/templates/eventos.xml");
		try {
			x.openFile();
			x.estadoInicial();
			x.estadoInicial();
			x.transiciones();
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}
}
