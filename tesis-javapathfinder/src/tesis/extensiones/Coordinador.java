package tesis.extensiones;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Stack;

import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.INVOKEVIRTUAL;
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

	//Contiene las asociaciones de Clase con XMLAFDReader
	private Hashtable htClaseAFD = new Hashtable();

	//Contiene las asociaciones de OID con AFD
	private Hashtable htOIDAFD = new Hashtable();
	//Es el OID de la �ltima ejecuci�n de un VirtualInvocation de m�todo
	private int iOIDUltimaEjecucion = -1;

	private AutomataVerificacion afd;
	private DFSearchTesis search;
	private ContextoBusqueda contexto;
	private EventBuilder evb;

	private static final int MODO_PREAMBULO = 0; 
	private static final int MODO_CONTEXTO = 1;
	private int modo = MODO_PREAMBULO;
	
	/**
	 * Verifica si el automata GLOBAL o los AFDs x Instancia
	 * llegaron a un estado final, por lo tanto se cumple
	 * la antipropiedad
	 * 	
	 * @return true si el afd esta en un estado final
	 */
	public boolean propiedadViolada() {
		boolean res = false;
		
		if (htOIDAFD.size() > 0) {
			AutomataVerificacion afdOID;
			Iterator<AutomataVerificacion> it = htOIDAFD.values().iterator();
			while (it.hasNext() && !res) {
				afdOID = it.next();
				res = afdOID.estadoFinal();
			}
		}
		
		return afd.estadoFinal() || res;
	}

	/**
	 * Se ejecuta cada vez que el Listener escucha una nueva instruccion
	 * Avanza el ContextoBusqueda y el/los AFD/s
	 */
	public void ocurrioInstruccion(Instruction i) {
		Evento e = evb.eventFrom(i);

		//DEBUG
		if (e.esObservable()) {
			System.out.println("EVENTO: " + e.label());
		}
		
		if (!contexto.invalido() && e.esObservable()) {
			if (modo == MODO_PREAMBULO) {
				//MODO Preambulo
				//Antes de avanzar el AFD, verifica que se haya cumplido el Contexto (Preambulo)
				if (!contexto.cumplido())
					contexto.consumir(e);
				else {
					//AFD GLOBALES
					afd.consumir(e);
					
					//AFD de INSTANCIA
					if (iOIDUltimaEjecucion != -1) {
						if (htOIDAFD.containsKey(iOIDUltimaEjecucion)) {
							AutomataVerificacion afdOID = (AutomataVerificacion) htOIDAFD.get(iOIDUltimaEjecucion);
							afdOID.consumir(e);
						}
						iOIDUltimaEjecucion = -1;
					}
				}
			}
			else if (modo == MODO_CONTEXTO) {
				//MODO Contexto Busqueda
				//Avanza el Contexto y el AFD en paralelo
				contexto.consumir(e);

				//AFD GLOBALES
				afd.consumir(e);

				//AFD de INSTANCIA
				if (iOIDUltimaEjecucion != -1) {
					if (htOIDAFD.containsKey(iOIDUltimaEjecucion)) {
						AutomataVerificacion afdOID = (AutomataVerificacion) htOIDAFD.get(iOIDUltimaEjecucion);
						afdOID.consumir(e);
					}
					iOIDUltimaEjecucion = -1;
				}
			}
		}
	}

	/**
	 * Devuelve una representacion del estado combinado del Search y el AFD
	 * 
	 * @return String
	 */
	public String estadoActual() {
		//TODO Hay que ver c�mo se resuelve el tema del estado con los AFDs de Instancia!!!
		return search.getVM().getStateId() + ";" + afd.getEstadoActual();
	}

	/**
	 * El Search notifica al coordinador que se backtracke� el �rbol. Indica al
	 * AFD que regrese al estado corresp. al estado al que se backtracke�
	 */
	public void stateBacktracked() {
		//TODO Hay que ver de backtrackear tambi�n los AFDs de Instancia!!!
		stackCaminoPreambulo.pop();
		contexto.irAEstado((Integer) stackCaminoPreambulo.peek());

		stackCaminoAFD.pop();
		afd.irAEstado((Integer) stackCaminoAFD.peek());

		//TODO Ver si esto se configura con un par�metro (property)
		System.out.println("--------------------------------- STATE-BACKTRACKED (CTX;JVM;AFD) " + contexto.getEstadoActual() +  ";" + this.estadoActual() + "--------------------------------");
	}

	/**
	 * El Search notifica al coordinador que se avanz� el �rbol. Agrega a la pila el estado
	 * en el que se encuentra el AFD
	 */
	public void stateAdvanced() {
		//TODO Hay que ver de mantener los STACKS de los AFDs de Instancia!!!
		stackCaminoPreambulo.push(contexto.getEstadoActual());
		stackCaminoAFD.push(afd.getEstadoActual());

		//TODO Ver si esto se configura con un par�metro (property)
		System.out.println("--------------------------------- STATE-ADVANCED (CTX;JVM;AFD) " + contexto.getEstadoActual() +  ";" + this.estadoActual() + "--------------------------------");
	}

	public void setAfd(AutomataVerificacion afd) {
		this.afd = afd;
	}

	/**
	 * Guarda una asociaci�n de xmlAFDReader con clase
	 * La utilizar� para crear los AFDs para cada instancia de dicha clase
	 */
	public void agregarTipoAFD(XMLAFDReader xmlAFD, String clase) {
		htClaseAFD.put(clase, xmlAFD); 
	}

	/**
	 * Determina si debe crear un AFD asociado al nuevo objeto
	 */
	public void objetoCreado(JVM vm) {
		String strClase = vm.getLastElementInfo().getClassInfo().getName();

		if (htClaseAFD.containsKey(strClase)) {
			XMLAFDReader xmlAFD = (XMLAFDReader) htClaseAFD.get(strClase);
			AutomataVerificacion afd = new AutomataVerificacion(xmlAFD);
			htOIDAFD.put(vm.getLastElementInfo().getIndex(), afd);
		}
	}

	/**
	 * Determina si debe destruir un AFD asociado al objeto
	 */
	public void objetoLiberado(JVM vm) {
		int iOID = vm.getLastElementInfo().getIndex();

		if (htOIDAFD.containsKey(iOID)) {
			htOIDAFD.remove(iOID);
		}
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

	public void ocurriraInstruccion(JVM vm) {
		//TODO Adaptar esto
		/**
		 * Esto es para determinar, en caso de una Virtual Invocation el OID y la clase del objeto asociado al metodo
		 */
		try {
			if (vm.getLastInstruction().toString().contains("open") && vm.getLastInstruction().toString().contains("Ejemplo")) {
				INVOKEVIRTUAL li = (INVOKEVIRTUAL) vm.getLastInstruction();
				int oid = li.getCalleeThis(vm.getLastThreadInfo());
				System.out.println("OID invocado = " + oid);
				System.out.println("CLASE = " + li.getCalleeClassInfo(vm.getKernelState(), oid).getName());
				
				iOIDUltimaEjecucion = oid; 
			}
		} catch (Exception ex) {
		}

	}
}
