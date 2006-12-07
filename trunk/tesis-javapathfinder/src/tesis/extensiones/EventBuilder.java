package tesis.extensiones;

import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.HashSet;
import java.util.Iterator;

import org.dom4j.Attribute;
import org.dom4j.Element;


/**
 * Builder de Eventos
 * 
 * Conoce un conjunto de eventos definidos en un xml y "construye" 
 * eventos a partir de instrucciones  
 */
public class EventBuilder {
	private HashSet<Evento> eventos;

	public EventBuilder (XMLEventBuilderReader xmlEB) {
		eventos = xmlEB.eventos();
	}

	/**
	 * TODO escribir mejor esto
	 * 
	 * Construye Eventos partiendo de Instrucciones
	 * De los eventos definidos, elige el q corresponde segun type, label y name. 
	 * El primer evento q contenga un match es el elegido 
	 * 
	 * @param i Instruccion utilizada para determinar el tipo de evento
	 * @return Evento construido a partir de i
	 */
	public Evento eventFrom(Instruction i) {
		Evento ret = new Evento();
		Evento e;
		Iterator<Evento> it;
		
		it = eventos.iterator();
		
		while (it.hasNext()) {
			e = it.next();
			if ( i.toString().contains(e.type()) && i.toString().contains(e.keyword()) ) {
				ret = e;
				break;
			}
		}

		return ret;
	}

	/**
	 * TODO escribir mejor esto
	 * 
	 * Construye Eventos partiendo de un label
	 * De los eventos definidos, elige el q corresponde segun el label (debe ser unico) 
	 * El primer evento q contenga un match es el elegido 
	 * 
	 * @param eventLabel String utilizada para determinar el evento
	 * @return Evento construido a partir de eventLabel
	 */
	public Evento eventFrom(String eventLabel) {
		Evento ret = new Evento();
		Evento e;
		Iterator<Evento> it;
		
		it = eventos.iterator();
		
		while (it.hasNext()) {
			e = it.next();
			if ( e.label() == eventLabel) {
				ret = e;
				break;
			}
		}

		return ret;
	}
}
