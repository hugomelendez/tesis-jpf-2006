package tesis.extensiones;

import java.util.List;
import java.util.HashSet;
import java.util.Iterator;

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
		return 0;
		//return 2;
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

	public void openFile() throws DocumentException {
		SAXReader reader = new SAXReader();
        document = reader.read(nmXML);
	}

	public void bar() throws DocumentException {

        Element root = document.getRootElement();
        
        System.out.println(root.getName());

        // iterate through child elements of root
//		for (Iterator i = root.elementIterator(); i.hasNext();) {
//			Element element = (Element) i.next();
//			System.out.println(element.getName());
//		}

		// iterate through child elements of root with element name "foo"
//		for (Iterator i = root.elementIterator("event*"); i.hasNext();) {
//			Element foo = (Element) i.next();
//			System.out.println(foo);
//		}

		List l = document.selectNodes("//evento");
		for (Iterator i = l.iterator(); i.hasNext();) {
			Element foo = (Element) i.next();
			System.out.println(foo);
		}
		
//		// iterate through attributes of root 
//		for (Iterator i = root.attributeIterator(); i.hasNext();) {
//			Attribute attribute = (Attribute) i.next();
//			System.out.println(attribute);
//		}
	}

	public static void main (String[] args) {
		XMLAFDReader x = new XMLAFDReader("/home/hugo/workspace-java/tesis-javapathfinder/src/tesis/templates/eventos.xml");

		try {
			x.openFile();
			x.bar();
		} catch (DocumentException e) {
			e.printStackTrace();
		}

	}
}
