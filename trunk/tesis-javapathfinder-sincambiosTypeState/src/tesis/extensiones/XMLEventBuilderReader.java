package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

// TODO Levantar los datos del XML
public class XMLEventBuilderReader extends XMLReader {
	final String EVENT_TAG = "//events/event";
	final String EVENT_TAG_TYPE_PROP = "type";
	final String EVENT_TAG_NAME_PROP = "name";
	final String EVENT_TAG_LABEL_PROP = "label";
	
	public XMLEventBuilderReader(String file) {
		super(file);
	}

	/**
	 * Levanta del XML los eventos y los agrega al conjunto
	 * @return
	 */
	public HashSet<Evento> eventos() {
		HashSet<Evento> hs = new HashSet<Evento>();

		List lt = document.selectNodes(EVENT_TAG);
		for (Iterator i = lt.iterator(); i.hasNext();) {
			Element el = (Element) i.next();

			String type = ((Attribute)el.attribute(EVENT_TAG_TYPE_PROP)).getValue();
			String name = ((Attribute)el.attribute(EVENT_TAG_NAME_PROP)).getValue();
			String label = ((Attribute)el.attribute(EVENT_TAG_LABEL_PROP)).getValue();

			Evento e = new Evento(type, name, label);

			hs.add(e);
		}
		
		return hs;
	}
}
