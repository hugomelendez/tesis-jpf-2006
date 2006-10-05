package tesis.extensiones;

import java.util.Stack;

import gov.nasa.jpf.jvm.bytecode.Instruction;

/**
 * Coordinador entre todos los objetos
 * 
 * ConcreteMediator Implements cooperative behavior by coordinating Colleague
 * objects. Knows and maintains its colleagues.
 */
public class Coordinador implements Mediator {
	private Stack<Object> stackCaminoAFD = new Stack();

	//Por ahora no es necesario que conozca al Listener
	//private Listener lsn;

	private AutomataVerificacion afd;
	private DFSearchTesis search;
	private Preambulo preambulo;
	private EventBuilder evb;

	/**
	 * Verifica si el automata llego a un estado final, por lo tanto se cumple
	 * la antipropiedad
	 * 
	 * @return true si el afd esta en un estado final
	 */
	public boolean propiedadViolada() {
		return afd.estadoFinal();
	}

	/**
	 * Se ejecuta cada vez que el Listener escucha una nueva instrucción Avanza
	 * el Preámbulo o el AFD
	 */
	public void ocurrioInstruccion(Instruction i) {
		Evento e = evb.eventoFrom(i);
		if (!preambulo.cumplido())
			preambulo.consumir(e);
		else
			afd.consumir(e);
	}

	/**
	 * Devuelve una representacion del estado combinado del Search y el AFD
	 * 
	 * @return String
	 */
	public String estadoActual() {
		return search.getVM().getStateId() + ";" + afd.getEstadoActual();
	}

	/**
	 * El Search notifica al coordinador que se backtrackeó el árbol. Indica al
	 * AFD que regrese al estado corresp. al estado al que se backtrackeó
	 */
	public void stateBacktracked() {
		stackCaminoAFD.pop();
		afd.irAEstado((Integer) stackCaminoAFD.peek());

		//TODO Ver si esto se configura con un parámetro (property)
		System.out.println("--------------------------------- STATE-BACKTRACKED:" + this.estadoActual() + "--------------------------------");
	}

	/**
	 * El Search notifica al coordinador que se avanzó el árbol. Agrega a la pila el estado
	 * en el que se encuentra el AFD
	 */
	public void stateAdvanced() {
		stackCaminoAFD.push(afd.getEstadoActual());

		//TODO Ver si esto se configura con un parámetro (property)
		System.out.println("--------------------------------- STATE-ADVANCED: " + this.estadoActual() + "  --------------------------------");
	}

	public void setAfd(AutomataVerificacion afd) {
		this.afd = afd;
	}

	public void setEvb(EventBuilder evb) {
		this.evb = evb;
	}

	public void setPreambulo(Preambulo preambulo) {
		this.preambulo = preambulo;
	}

	public void setSearch(DFSearchTesis search) {
		this.search = search;
	}

}
