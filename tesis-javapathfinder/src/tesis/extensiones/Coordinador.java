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
	private Stack<Object> stackCaminoPreambulo = new Stack<Object>();
	private Stack<Object> stackCaminoAFD = new Stack<Object>();

	//Por ahora no es necesario que conozca al Listener
	//private Listener lsn;

	private AutomataVerificacion afd;
	private DFSearchTesis search;
	private ContextoBusqueda contexto;
	private EventBuilder evb;

	private static final int MODO_PREAMBULO = 0; 
	private static final int MODO_CONTEXTO = 1;
	private int modo = MODO_PREAMBULO;
	
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
	 * Se ejecuta cada vez que el Listener escucha una nueva instruccion
	 * Avanza el ContextoBusqueda y el AFD
	 */
	public void ocurrioInstruccion(Instruction i) {
		Evento e = evb.eventFrom(i);

		//Solo para DEBUG
		if (e.esObservable()) {
			System.out.println("EVENTO: " + e.label());
		}
		
		if (!contexto.invalido() && e.esObservable()) {
			if (modo == MODO_PREAMBULO) {
				//MODO Preambulo
				//Antes de avanzar el AFD, verifica que se haya cumplido el Contexto (Preambulo)
				if (!contexto.cumplido())
					contexto.consumir(e);
				else
					afd.consumir(e);
			}
			else if (modo == MODO_CONTEXTO) {
				//MODO Contexto Busqueda
				//Avanza el Contexto y el AFD en paralelo
				contexto.consumir(e);
				afd.consumir(e);
			}
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
		contexto.irAEstado((Integer) stackCaminoPreambulo.peek());

		stackCaminoAFD.pop();
		afd.irAEstado((Integer) stackCaminoAFD.peek());

		//TODO Ver si esto se configura con un par�metro (property)
		System.out.println("--------------------------------- STATE-BACKTRACKED (PRE=" + contexto.getEstadoActual() +  "): " + this.estadoActual() + "--------------------------------");
	}

	/**
	 * El Search notifica al coordinador que se avanz� el �rbol. Agrega a la pila el estado
	 * en el que se encuentra el AFD
	 */
	public void stateAdvanced() {
		stackCaminoPreambulo.push(contexto.getEstadoActual());
		stackCaminoAFD.push(afd.getEstadoActual());

		//TODO Ver si esto se configura con un par�metro (property)
		System.out.println("--------------------------------- STATE-ADVANCED (PRE=" + contexto.getEstadoActual() +  "): " + this.estadoActual() + "  --------------------------------");
	}

	public void setAfd(AutomataVerificacion afd) {
		this.afd = afd;
	}

	public void setEvb(EventBuilder evb) {
		this.evb = evb;
	}

	public void setContexto(ContextoBusqueda preambulo) {
		this.contexto = preambulo;
	}

	public void setSearch(DFSearchTesis search) {
		this.search = search;
	}

	public boolean backtrackear() {
		return ( contexto.invalido() );
	}
	
	public void setModoPreambulo() {
		modo = MODO_PREAMBULO;
	}

	public void setModoContexto() {
		modo = MODO_CONTEXTO;
	}
}
