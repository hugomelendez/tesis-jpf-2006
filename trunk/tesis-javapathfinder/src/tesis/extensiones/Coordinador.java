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
	//Contiene los estados visitados hasta el momento
	//(cada estado es una composición de los estados de la VM y los AFDs)
	private Hashtable<String, Integer> htEstadosVisitados = new Hashtable<String, Integer>();

	private Stack<Object> stackCaminoPreambulo = new Stack<Object>();
	private Stack<Object> stackCaminoAFD = new Stack<Object>();

	//Por ahora no es necesario que conozca al Listener
	//private Listener lsn;

	//Contiene las asociaciones de Clase con XMLAFDReader
	private Hashtable<String, XMLAFDReader> htClaseAFD = new Hashtable<String, XMLAFDReader>();

	//Contiene las asociaciones de OID con AFD
	private Hashtable<Integer, AutomataVerificacion> htOIDAFD = new Hashtable<Integer, AutomataVerificacion>();
	//Es el OID de la última ejecución de un VirtualInvocation de método
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
							//DEBUG
							if (afdOID.estadoFinal()) {
								System.out.println("Propiedad violada en AFD de Instancia (OID=" + iOIDUltimaEjecucion + ")");
							}
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
						//DEBUG
						if (afdOID.estadoFinal()) {
							System.out.println("Propiedad violada en AFD de Instancia (OID=" + iOIDUltimaEjecucion + ")");
						}
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
		//TODO Hay que ver cómo se resuelve el tema del estado con los AFDs de Instancia!!!
		String strRes = new String();
		
		strRes = search.getVM().getStateId() + ";" + afd.getEstadoActual();
		
		if (htOIDAFD.size() > 0) {
			AutomataVerificacion afdOID;
			Iterator<AutomataVerificacion> it = htOIDAFD.values().iterator();
			while (it.hasNext()) {
				afdOID = it.next();
				strRes = strRes + ";" + afdOID.getEstadoActual();
			}
		}
		
		return strRes;
	}

	/**
	 * El Search notifica al coordinador que se backtrackeï¿½ el ï¿½rbol. Indica al
	 * AFD que regrese al estado corresp. al estado al que se backtrackeï¿½
	 */
	public void stateBacktracked() {
		//TODO Hay que ver de backtrackear también los AFDs de Instancia!!!
		stackCaminoPreambulo.pop();
		contexto.irAEstado((Integer) stackCaminoPreambulo.peek());

		stackCaminoAFD.pop();
		afd.irAEstado((Integer) stackCaminoAFD.peek());

		//TODO Ver si esto se configura con un parï¿½metro (property)
		System.out.println("--------------------------------- STATE-BACKTRACKED (CTX;JVM;AFDs) " + contexto.getEstadoActual() +  ";" + this.estadoActual() + "--------------------------------");
	}

	/**
	 * El Search notifica al coordinador que se avanzï¿½ el ï¿½rbol. Agrega a la pila el estado
	 * en el que se encuentra el AFD
	 */
	public void stateAdvanced() {
		//TODO Hay que ver de mantener los STACKS de los AFDs de Instancia!!!
		stackCaminoPreambulo.push(contexto.getEstadoActual());
		stackCaminoAFD.push(afd.getEstadoActual());

		//TODO Ver si esto se configura con un parï¿½metro (property)
		System.out.println("--------------------------------- STATE-ADVANCED (CTX;JVM;AFDs) " + contexto.getEstadoActual() +  ";" + this.estadoActual() + "--------------------------------");
	}

	public void setAfd(AutomataVerificacion afd) {
		this.afd = afd;
	}

	/**
	 * Guarda una asociación de xmlAFDReader con clase
	 * La utilizará para crear los AFDs para cada instancia de dicha clase
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

	/**
	 * Decide si hay que backtrackear la rama actual de la búsqueda
	 * en función de si se invalidó el contexto o si es estado actual ya es conocido  
	 * @return
	 */
	public boolean backtrackear() {
		return ( contexto.invalido() || htEstadosVisitados.containsKey(estadoActual()));
	}
	
	public void setModoPreambulo() {
		modo = MODO_PREAMBULO;
	}

	public void setModoContexto() {
		modo = MODO_CONTEXTO;
	}

	public void ocurriraInstruccion(JVM vm) {
		/**
		 * Esto es para determinar, en caso de una Virtual Invocation el OID y la clase del objeto asociado al metodo
		 */
		//DEBUG
		try {
			if (vm.getLastInstruction().toString().contains("tesis") && vm.getLastInstruction() instanceof INVOKEVIRTUAL) {
				INVOKEVIRTUAL li = (INVOKEVIRTUAL) vm.getLastInstruction();
				int oid = li.getCalleeThis(vm.getLastThreadInfo());
				System.out.println("OID invocado = " + oid + " de TIPO = " + li.getCalleeClassInfo(vm.getKernelState(), oid).getName());

				iOIDUltimaEjecucion = oid; 
			}
		} catch (Exception ex) {
			System.out.println(ex);
		}

	}

	public void registrarEstadoVistado() {
		htEstadosVisitados.put(estadoActual(), 0);
	}
}
