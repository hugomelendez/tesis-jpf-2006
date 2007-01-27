package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class XMLContextoBusquedaReader extends XMLReader {
	private static final String SEARCH_CONTEXT_TAG = "//SearchContext";
	private static final String SEARCHCONTEXT_MODE_ATT = "mode";
	public static final String SEARCHCONTEXT_MODE_CONTEXT = "contexto";
	public static final String SEARCHCONTEXT_MODE_PREAMBLE = "preambulo";

	private static final String STATES_TAG = SEARCH_CONTEXT_TAG + "/states";
	private static final String STATE_TAG = STATES_TAG + "/state";
	private static final String STATE_TAG_LABEL_ATT = "label";
	private static final String STATE_TAG_START_ATT = STATE_TAG + "[@start]";
	private static final String STATE_TAG_FINAL_ATT = STATE_TAG + "[@final]";
	
	private static final String TRANSITIONS_TAG = SEARCH_CONTEXT_TAG + "/transitions";
	private static final String TRANSITION_TAG = TRANSITIONS_TAG + "/transition";
	private static final String TRANSITION_TAG_FROM_ATT = "from";
	private static final String TRANSITION_TAG_TO_ATT = "to";
	private static final String TRANSITION_TAG_LABEL_ATT = "labelEvent";

	EventBuilder eventBuilder;

	public XMLContextoBusquedaReader(String file, EventBuilder eb) {
		super(file);
		eventBuilder = eb;
	}

	public XMLContextoBusquedaReader(String file) {
		super(file);
	}

	public State estadoInicial() {
		// Buscamos el estado inicial
		Element estado = (Element)document.selectSingleNode(STATE_TAG_START_ATT);
		return stateFromElem(estado, STATE_TAG_LABEL_ATT);
	}

	public HashSet<Transicion> transiciones() {
		HashSet<Transicion> hs = new HashSet<Transicion>();

		List lt = document.selectNodes(TRANSITION_TAG);
		for (Iterator i = lt.iterator(); i.hasNext();) {
			Element trans = (Element) i.next();

			State desde = stateFromElem(trans, TRANSITION_TAG_FROM_ATT);
			State hasta = stateFromElem(trans, TRANSITION_TAG_TO_ATT);
			Evento e = eventBuilder.eventFrom(attFromElem(trans, TRANSITION_TAG_LABEL_ATT));

			Transicion t = new Transicion(desde, hasta, e);

			hs.add(t);
		}

		return hs;
	}

	/**
	 * Indica el unico estado final del automata, en caso de haber varios retorna el primero
	 * @return estado final del automata
	 */
	public State estadoFinal() {
		// Buscamos los estados finales
		Element estado = (Element)document.selectSingleNode(STATE_TAG_FINAL_ATT);
		return stateFromElem(estado, STATE_TAG_LABEL_ATT);
	}

	/**
	 * Determina si el SearchContext se usara en modo Contexto o Preambulo
	 * Default: Contexto
	 * @return
	 */
	public String modoContexto() {
		Element estado = (Element)document.selectSingleNode(SEARCH_CONTEXT_TAG);
		Attribute att = (Attribute) estado.attribute(SEARCHCONTEXT_MODE_ATT);
		if (att != null)
			if (att.getValue() == SEARCHCONTEXT_MODE_PREAMBLE)
				return SEARCHCONTEXT_MODE_PREAMBLE;

		return SEARCHCONTEXT_MODE_CONTEXT;
	}
}
