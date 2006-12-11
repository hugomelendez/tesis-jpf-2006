package tesis.Ejemplo_07;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

import tesis.extensiones.*;

// TODO Levantar los datos del XML
public class XMLEventBuilderReaderEj07 extends XMLEventBuilderReader {
	final String EVENT_TAG = "//events/event";
	final String EVENT_TAG_TYPE_PROP = "type";
	final String EVENT_TAG_NAME_PROP = "name";
	final String EVENT_TAG_LABEL_PROP = "label";
	
	public XMLEventBuilderReaderEj07(String file) {
		super(file);
	}

	/**
	 * Levanta del XML los eventos y los agrega al conjunto
	 * @return
	 */
	public HashSet<Evento> eventos() {
		HashSet<Evento> hs = new HashSet<Evento>();

		hs.add(new Evento("invoke", "open", "OPEN"));
		hs.add(new Evento("invoke", "close", "CLOSE"));
		hs.add(new Evento("invoke", "write", "WRITE"));

		return hs;
	}
}
