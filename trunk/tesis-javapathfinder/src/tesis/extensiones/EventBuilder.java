package tesis.extensiones;

import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.HashSet;
import java.util.Iterator;


/** 
 * TODO Esta clase la definimos como abstracta por ahora (la implementa cada ejemplo)
 * Cuando implementemos la parte de properties/XMLs, sera
 * concreta y tendra toda la logica
 * La idea es que el usuario no tenga que escribir el codigo, sino los archs de conf.
 */
public class EventBuilder {
	private XMLEventBuilderReader xml;
	private HashSet<Evento> eventos;
	
	public EventBuilder (XMLEventBuilderReader xmlEB) {
		xml = xmlEB;
		eventos = xml.eventos();
	}
	
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
}
