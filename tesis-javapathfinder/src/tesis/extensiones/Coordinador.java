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
	private Stack<Object> stackCaminoPreambulo = new Stack();
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
	 * Se ejecuta cada vez que el Listener escucha una nueva instrucci�n Avanza
	 * el Pre�mbulo o el AFD
	 */
	public void ocurrioInstruccion(Instruction i) {
		Evento e = evb.eventFrom(i);

		if (!preambulo.violado() && e.esObservable()) {
			if (!preambulo.aceptado())
				preambulo.consumir(e);
			else
				afd.consumir(e);
		}
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
	 * El Search notifica al coordinador que se backtracke� el �rbol. Indica al
	 * AFD que regrese al estado corresp. al estado al que se backtracke�
	 */
	public void stateBacktracked() {
		stackCaminoPreambulo.pop();
		preambulo.irAEstado((Integer) stackCaminoPreambulo.peek());

		stackCaminoAFD.pop();
		afd.irAEstado((Integer) stackCaminoAFD.peek());

		//TODO Ver si esto se configura con un par�metro (property)
		System.out.println("--------------------------------- STATE-BACKTRACKED:" + this.estadoActual() + "--------------------------------");
	}

	/**
	 * El Search notifica al coordinador que se avanz� el �rbol. Agrega a la pila el estado
	 * en el que se encuentra el AFD
	 */
	public void stateAdvanced() {
		stackCaminoPreambulo.push(preambulo.getEstadoActual());
		stackCaminoAFD.push(afd.getEstadoActual());

		//TODO Ver si esto se configura con un par�metro (property)
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

	public boolean backtrackear() {
		return ( preambulo.violado() );
	}
}
