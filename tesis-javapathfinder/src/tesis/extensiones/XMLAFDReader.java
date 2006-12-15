package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

//TODO: reusar los metodos con y sin type
public class XMLAFDReader extends XMLReader {
	final String TYPE_STATE_PROP_TAG = "//TypeStateProperty";
	final String TYPE_STATE_PROP_TAG_CLASS_ATT = "class";

	final String STATES_LABEL = "/states";
	final String STATES_TAG = TYPE_STATE_PROP_TAG + STATES_LABEL;
	final String STATE_LABEL = "/state";
	final String STATE_TAG = STATES_TAG + STATE_LABEL;
	final String STATE_TAG_LABEL_ATT = "label";
	final String STATE_TAG_START_ATT = "start";
	final String STATE_TAG_FINAL_ATT = "final";
	
	final String TRANSITIONS_LABEL = "/transitions";
	final String TRANSITIONS_TAG = TYPE_STATE_PROP_TAG + TRANSITIONS_LABEL;
	final String TRANSITION_LABEL = "/transition";
	final String TRANSITION_TAG = TRANSITIONS_TAG + TRANSITION_LABEL;
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

	/**
	 * Obtiene todas las clases definidas en TypeStateProperties
	 * @return
	 */
	public HashSet<String> getClases() {
		HashSet<String> hs = new HashSet<String>();

		List lt = document.selectNodes(TYPE_STATE_PROP_TAG);
		for (Iterator i = lt.iterator(); i.hasNext();) {
			Element foo = (Element) i.next();
			String type = ((Attribute)foo.attribute(TYPE_STATE_PROP_TAG_CLASS_ATT)).getValue();
			hs.add(type);
		}

		return hs;
	}

	public int estadoInicial() {
		// Buscamos el estado inicial
		Element estado = (Element)document.selectSingleNode(STATE_TAG + "[@"+ STATE_TAG_START_ATT +"]");
		String label = ((Attribute)estado.attribute(STATE_TAG_LABEL_ATT)).getValue();
		Integer i = new Integer(label);
		return i;
	}
	
	public int estadoInicial(String type) {
		// Buscamos el estado inicial de un tipo particular
		String typeState = TYPE_STATE_PROP_TAG + "[@"+ TYPE_STATE_PROP_TAG_CLASS_ATT +"='"+type+"']";
		Element estado = (Element)document.selectSingleNode(typeState + STATES_LABEL + STATE_LABEL + "[@"+ STATE_TAG_START_ATT +"]");
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
	 * Construye el conjunto de estados finales del automata
	 * @return Conjunto de estados finales del automata
	 */
	public HashSet<Integer> estadosFinales(String type) {
		HashSet<Integer> hs = new HashSet<Integer>();
		String typeState = TYPE_STATE_PROP_TAG + "[@"+ TYPE_STATE_PROP_TAG_CLASS_ATT +"='"+type+"']";

		// Buscamos los estados finales
		List l = document.selectNodes(typeState + STATES_LABEL + STATE_LABEL + "[@"+ STATE_TAG_FINAL_ATT +"]");
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

	/**
	 * Construye el conjunto de transiciones del automata
	 * @return Conjunto de transiciones del automata
	 */
	public HashSet<Transicion> transiciones(String type) {
		HashSet<Transicion> hs = new HashSet<Transicion>();
		String typeState = TYPE_STATE_PROP_TAG + "[@"+ TYPE_STATE_PROP_TAG_CLASS_ATT +"='"+type+"']";

		List lt = document.selectNodes(typeState + TRANSITIONS_LABEL + TRANSITION_LABEL);
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
