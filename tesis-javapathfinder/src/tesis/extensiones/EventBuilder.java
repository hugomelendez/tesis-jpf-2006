package tesis.extensiones;

import gov.nasa.jpf.jvm.bytecode.Instruction;


/** 
 * TODO Esta clase la definimos como abstracta por ahora (la implementa cada ejemplo)
 * Cuando implementemos la parte de properties/XMLs, sera
 * concreta y tendra toda la logica
 * La idea es que el usuario no tenga que escribir el codigo, sino los archs de conf.
 */
public abstract class EventBuilder {
	public abstract Evento eventoFrom(Instruction i);
}
