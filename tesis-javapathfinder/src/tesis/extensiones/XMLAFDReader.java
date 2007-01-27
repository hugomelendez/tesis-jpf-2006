package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;

//TODO: reusar los metodos con y sin type
public class XMLAFDReader extends XMLReader {
	private static final String STATES_LABEL = "/states";
	private static final String STATE_LABEL = "/state";
	private static final String STATE_TAG_LABEL_ATT = "label";
	private static final String STATE_TAG_START_ATT = "[@start]";
	private static final String STATE_TAG_FINAL_ATT = "[@final]";

	private static final String TRANSITIONS_LABEL = "/transitions";
	private static final String TRANSITION_LABEL = "/transition";
	private static final String TRANSITION_TAG_FROM_ATT = "from";
	private static final String TRANSITION_TAG_TO_ATT = "to";
	private static final String TRANSITION_TAG_LABEL_ATT = "labelEvent";

	private static final String TYPE_STATE_PROP_TAG = "//TypeStateProperty";
	private static final String TYPE_STATE_PROP_TAG_CLASS_ATT = "class";
	private static final String TP_STATES_TAG = TYPE_STATE_PROP_TAG + STATES_LABEL;
	private static final String TP_STATE_TAG = TP_STATES_TAG + STATE_LABEL;
	private static final String TP_TRANSITIONS_TAG = TYPE_STATE_PROP_TAG + TRANSITIONS_LABEL;
	private static final String TP_TRANSITION_TAG = TP_TRANSITIONS_TAG + TRANSITION_LABEL;

	private static final String GLOBAL_PROP_TAG = "//GlobalProperty";
	private static final String GP_STATES_TAG = GLOBAL_PROP_TAG + STATES_LABEL;
	private static final String GP_STATE_TAG = GP_STATES_TAG + STATE_LABEL;
	private static final String GP_TRANSITIONS_TAG = GLOBAL_PROP_TAG + TRANSITIONS_LABEL;
	private static final String GP_TRANSITION_TAG = GP_TRANSITIONS_TAG + TRANSITION_LABEL;

	EventBuilder eventBuilder;

	public XMLAFDReader(String file, EventBuilder eb) {
		super(file);
		eventBuilder = eb;
	}

	public XMLAFDReader(String file) {
		super(file);
	}

	private String typeTag(String type) {
		return "[@"+ TYPE_STATE_PROP_TAG_CLASS_ATT +"='"+type+"']";
	}

	/**
	 * Obtiene todas las clases definidas en TypeStateProperties
	 * @return
	 * @throws XMLException 
	 */
	public HashSet<String> getClases() throws XMLException {
		if (!hasTypeStateProperties()) {
			throw new XMLException();
		}

		HashSet<String> hs = new HashSet<String>();

		List lt = document.selectNodes(TYPE_STATE_PROP_TAG);
		for (Iterator i = lt.iterator(); i.hasNext();) {
			Element claz = (Element) i.next();
			hs.add(attFromElem(claz, TYPE_STATE_PROP_TAG_CLASS_ATT));
		}

		return hs;
	}

	/**
	 * Helper method
	 * @param xpathExpression
	 * @return
	 */
	private int _estadoInicial(String xpathExpression) {
		Element estado = (Element)document.selectSingleNode(xpathExpression);
		return intAttFromElem(estado, STATE_TAG_LABEL_ATT);
	}
	
	public int estadoInicial() throws XMLException {
		if (!hasGlobalProperties()) {
			throw new XMLException();
		}

		// Buscamos el estado inicial
		return _estadoInicial(GP_STATE_TAG + STATE_TAG_START_ATT);
	}
	
	public int estadoInicial(String type) throws XMLException {
		if (!hasTypeStateProperties()) {
			throw new XMLException();
		}

		// Buscamos el estado inicial de un tipo particular
		return _estadoInicial(TYPE_STATE_PROP_TAG + typeTag(type) + STATES_LABEL + STATE_LABEL + STATE_TAG_START_ATT);
	}
	
	/**
	 * Helper method
	 * @param xpathExpression 
	 * @return Conjunto de estados finales del automata
	 */
	private HashSet<Integer> _estadosFinales(String xpathExpression) {
		HashSet<Integer> hs = new HashSet<Integer>();

		// Buscamos los estados finales
		List l = document.selectNodes(xpathExpression);
		for (Iterator i = l.iterator(); i.hasNext();) {
			Element estado = (Element) i.next();
			hs.add(intAttFromElem(estado, STATE_TAG_LABEL_ATT));
		}

		return hs;
	}

	/**
	 * Construye el conjunto de estados finales del automata
	 * @return Conjunto de estados finales del automata
	 */
	public HashSet<Integer> estadosFinales() throws XMLException {
		if (!hasGlobalProperties()) {
			throw new XMLException();
		}

		return _estadosFinales(GP_STATE_TAG + STATE_TAG_FINAL_ATT);
	}

	/**
	 * Construye el conjunto de estados finales del automata
	 * @return Conjunto de estados finales del automata
	 */
	public HashSet<Integer> estadosFinales(String type) throws XMLException {
		if (!hasTypeStateProperties()) {
			throw new XMLException();
		}

		return _estadosFinales(TYPE_STATE_PROP_TAG + typeTag(type) + STATES_LABEL + STATE_LABEL + STATE_TAG_FINAL_ATT);
	}

	/**
	 * Helper method
	 * @return Conjunto de transiciones del automata
	 */
	public HashSet<Transicion> _transiciones (String xpathExpression) {
		HashSet<Transicion> hs = new HashSet<Transicion>();

		List lt = document.selectNodes(xpathExpression);
		for (Iterator i = lt.iterator(); i.hasNext();) {
			Element trans = (Element) i.next();

			int desde = intAttFromElem(trans, TRANSITION_TAG_FROM_ATT);
			int hasta = intAttFromElem(trans, TRANSITION_TAG_TO_ATT);
			Evento e = eventBuilder.eventFrom(attFromElem(trans, TRANSITION_TAG_LABEL_ATT));

			Transicion t = new Transicion(desde, hasta, e);

			hs.add(t);
		}

		return hs;
	}

	/**
	 * Construye el conjunto de transiciones del automata
	 * @return Conjunto de transiciones del automata
	 */
	public HashSet<Transicion> transiciones() throws XMLException {
		if (!hasGlobalProperties()) {
			throw new XMLException();
		}

		return _transiciones (GP_TRANSITION_TAG);
	}

	/**
	 * Construye el conjunto de transiciones del automata
	 * @return Conjunto de transiciones del automata
	 */
	public HashSet<Transicion> transiciones(String type) throws XMLException {
		if (!hasTypeStateProperties()) {
			throw new XMLException();
		}

		return _transiciones (TYPE_STATE_PROP_TAG + typeTag(type) + TRANSITIONS_LABEL + TRANSITION_LABEL);
	}

	private boolean _hasProperties(String xpathExpression) {
		return (document.selectNodes(xpathExpression).size() > 0);
	}
	public boolean hasTypeStateProperties() {
		return (_hasProperties(TYPE_STATE_PROP_TAG));
	}
	public boolean hasGlobalProperties() {
		return (_hasProperties(GLOBAL_PROP_TAG));
	}
}
