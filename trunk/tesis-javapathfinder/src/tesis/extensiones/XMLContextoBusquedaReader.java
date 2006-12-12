package tesis.extensiones;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Attribute;
import org.dom4j.Element;

public class XMLContextoBusquedaReader extends XMLReader {
	final String SEARCH_CONTEXT_TAG = "//SearchContext";

	final String STATES_TAG = SEARCH_CONTEXT_TAG + "/states";
	final String STATE_TAG = STATES_TAG + "/state";
	final String STATE_TAG_LABEL_ATT = "label";
	final String STATE_TAG_START_ATT = "start";
	final String STATE_TAG_FINAL_ATT = "final";
	
	final String TRANSITIONS_TAG = SEARCH_CONTEXT_TAG + "/transitions";
	final String TRANSITION_TAG = TRANSITIONS_TAG + "/transition";
	final String TRANSITION_TAG_FROM_ATT = "from";
	final String TRANSITION_TAG_TO_ATT = "to";
	final String TRANSITION_TAG_LABEL_ATT = "labelEvent";

	EventBuilder eventBuilder;

	public XMLContextoBusquedaReader(String file, EventBuilder eb) {
		super(file);
		eventBuilder = eb;
	}

	public XMLContextoBusquedaReader(String file) {
		super(file);
	}

	public int estadoInicial() {
		// Buscamos el estado inicial
		Element estado = (Element)document.selectSingleNode(STATE_TAG + "[@"+ STATE_TAG_START_ATT +"]");
		String label = ((Attribute)estado.attribute(STATE_TAG_LABEL_ATT)).getValue();
		Integer i = new Integer(label);
		return i;
	}

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
	 * Indica el unico estado final del automata, en caso de haber varios retorna el primero
	 * @return estado final del automata
	 */
	public Integer estadoFinal() {
		// Buscamos los estados finales
		Element estado = (Element)document.selectSingleNode(STATE_TAG + "[@"+ STATE_TAG_FINAL_ATT +"]");
		String label = ((Attribute)estado.attribute(STATE_TAG_LABEL_ATT)).getValue();
		Integer i = new Integer(label);
		return i;
	}

}
