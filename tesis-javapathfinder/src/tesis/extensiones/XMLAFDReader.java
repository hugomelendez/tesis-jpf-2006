package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class XMLAFDReader extends XMLReader {
	final String TYPE_STATE_PROP_TAG = "//TypeStateProperty";
	final String TYPE_STATE_PROP_TAG_CLASS_ATT = "class";

	final String STATES_TAG = TYPE_STATE_PROP_TAG + "/states";
	final String STATE_TAG = STATES_TAG + "/state";
	final String STATE_TAG_LABEL_ATT = "label";
	final String STATE_TAG_START_ATT = "start";
	final String STATE_TAG_FINAL_ATT = "final";
	
	final String TRANSITIONS_TAG = TYPE_STATE_PROP_TAG + "/transitions";
	final String TRANSITION_TAG = TRANSITIONS_TAG + "/transition";
	final String TRANSITION_TAG_FROM_ATT = "from";
	final String TRANSITION_TAG_TO_ATT = "to";
	final String TRANSITION_TAG_LABEL_ATT = "labelEvent";

	final String GLOBAL_PROP_TAG = "//GlobalProperty";

	EventBuilder eventBuilder;

	public XMLAFDReader(String file, EventBuilder eb) {
		super(file);
		eventBuilder = eb;
	}

	public XMLAFDReader(String file) {
		super(file);
	}

	public int estadoInicial() {
		// Buscamos el estado inicial
		Element estado = (Element)document.selectSingleNode(STATE_TAG + "[@"+ STATE_TAG_START_ATT +"]");
		String label = ((Attribute)estado.attribute(STATE_TAG_LABEL_ATT)).getValue();
		Integer i = new Integer(label);
		return i;
	}
	
	/**
	 * Construye el conjunto de estados finales del automata
	 * @return Conjunto de estados finales del automata
	 */
	public HashSet<Integer> estadosFinales() {
		HashSet<Integer> hs = new HashSet<Integer>();

		// Buscamos los estados finales
		List l = document.selectNodes(STATE_TAG + "[@"+ STATE_TAG_FINAL_ATT +"]");
		for (Iterator i = l.iterator(); i.hasNext();) {
			Element estado = (Element) i.next();
			hs.add(new Integer(((Attribute)estado.attribute(STATE_TAG_LABEL_ATT)).getValue()));
		}

		return hs;
	}

	/**
	 * Construye el conjunto de transiciones del automata
	 * @return Conjunto de transiciones del automata
	 */
	public HashSet<Transicion> transiciones() {
		HashSet<Transicion> hs = new HashSet<Transicion>();

		List lt = document.selectNodes(TRANSITION_TAG);
		for (Iterator i = lt.iterator(); i.hasNext();) {
			Element foo = (Element) i.next();

			int desde = new Integer(((Attribute)foo.attribute(TRANSITION_TAG_FROM_ATT)).getValue());
			int hasta = new Integer(((Attribute)foo.attribute(TRANSITION_TAG_TO_ATT)).getValue());
			Evento e = eventBuilder.eventFrom(((Attribute)foo.attribute(TRANSITION_TAG_LABEL_ATT)).getValue());

			Transicion t = new Transicion(desde, hasta, e);

			hs.add(t);
		}

		return hs;
	}

}
