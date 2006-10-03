package tesis.extensiones;

import gov.nasa.jpf.jvm.bytecode.Instruction;

/**
 * Coordinador entre todos los objetos
 * 
 * ConcreteMediator
 * Implements cooperative behavior by coordinating
 * Colleague objects. Knows and maintains its colleagues.
 */

public class Coordinador implements Mediator
{
	private Listener lsn;
	private AutomataVerificacion afd;
	private DFSearchTesis search;
	private Preambulo preambulo;
	private EventBuilder evb;
	
	/**
	 * Verifica si el automata llego a un estado final,
	 * por lo tanto se cumple la antipropiedad 
	 * @return true si el afd esta en un estado final 
	 */
	public boolean propiedadViolada() {
		return afd.estadoFinal();
	}

	/**
	 *  
	 */
	public void ocurrioInstruccion(Instruction i) {
		Evento e = evb.eventoFrom(i);
		if ( !preambulo.cumplido() )
			preambulo.consumir(e);
		else
			afd.consumir(e);
	}

	/**
	 * La idea es que devuelve una representacion del
	 * estado combinado del Search y el AFD
	 * @return
	 */
	public String estadoActual() {
		return search.getVM().getStateId() + ";" + afd.getEstadoActual();
	}
}