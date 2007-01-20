package tesis.extensiones;

import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.jvm.bytecode.Instruction;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.lang.Class;

/**
 * Coordinador entre todos los objetos
 * 
 * ConcreteMediator Implements cooperative behavior by coordinating Colleague
 * objects. Knows and maintains its colleagues.
 */
public class Coordinador implements Mediator {
	//Contiene los estados visitados hasta el momento
	//(cada estado es una composici�n de los estados de la VM y los AFDs)
	private Hashtable<String, Integer> htEstadosVisitados = new Hashtable<String, Integer>();

	private Stack<Object> stackCaminoPreambulo = new Stack<Object>();
	private Stack<Object> stackCaminoAFD = new Stack<Object>();

	//Por ahora no es necesario que conozca al Listener
	//private Listener lsn;

	//Contiene las asociaciones de Clase con TypeStatePropertyTemplate
	private Hashtable<String, TypeStatePropertyTemplate> htClaseAFD = new Hashtable<String, TypeStatePropertyTemplate>();
	//Contiene las asociaciones de OID con AFD
	private Hashtable<Integer, AutomataVerificacion> htOIDAFD = new Hashtable<Integer, AutomataVerificacion>();
	//Contiene la colecci�n de caminos (stack) de cada AFD de instancia
	//(para soportar los backtracks de la JVM)
	private Hashtable<Integer, Stack<Object>> htOIDStack = new Hashtable<Integer, Stack<Object>>();

	//Contiene los AFDs que est�n VIOLADOS
	private Hashtable<String, AutomataVerificacion> htAFDViolados = new Hashtable<String, AutomataVerificacion>();

	//OID de la �ltima ejecuci�n de un VirtualInvocation de m�todo
	//TODO Tesis: Mejorar el manejo de este id especial 
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
					//DEBUG
					if (afd.estadoFinal()) {
						System.out.println("Propiedad violada en AFD GLOBAL");
					}

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
				//DEBUG
				if (afd.estadoFinal()) {
					System.out.println("Propiedad violada en AFD GLOBAL");
				}

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
	 * Devuelve una representacion del estado combinado de JVM;AFD(global);AFDs(instancia)
	 * 
	 * @return String
	 */
	public String estadoActual() {
		String strRes = new String();
		
		strRes = search.getVM().getStateId() + ";";
		strRes += afd.getEstadoActual();
		
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
	 * El Search notifica al coordinador que se backtrackeo el arbol.
	 * Backtrackea todos los AFDs (global y los de instancia) para que regresen al estado corresp.
	 * al estado (JVM) al que se backtrackeo
	 */
	public void stateBacktracked() {
		//TODO Hay que ver de backtrackear tambi�n los AFDs de Instancia!!!
		stackCaminoPreambulo.pop();
		contexto.irAEstado((Integer) stackCaminoPreambulo.peek());

		stackCaminoAFD.pop();
		afd.irAEstado((Integer) stackCaminoAFD.peek());

		//Se backtrackea desde el stack correspondiente, el estado de c/AFD de instancia 
		if (htOIDAFD.size() > 0) {
			AutomataVerificacion afdOID;

			Enumeration<Integer> enume = htOIDAFD.keys();
			while (enume.hasMoreElements()) {
				int oid = enume.nextElement();

				afdOID = htOIDAFD.get(oid);
				Stack<Object> stkAfdOid = htOIDStack.get(oid);
				stkAfdOid.pop();
				afdOID.irAEstado((Integer) stkAfdOid.peek());
			}
		}
		
		//TODO Ver si esto se configura con un parametro (property)
		System.out.println("--------------------------------- STATE-BACKTRACKED (CTX;JVM;AFDs) " + contexto.getEstadoActual() +  ";" + this.estadoActual() + "--------------------------------");
	}

	/**
	 * El Search notifica al coordinador que avanzo el arbol. Agrega a la pila el estado
	 * en el que se encuentra el AFD
	 */
	public void stateAdvanced() {
		stackCaminoPreambulo.push(contexto.getEstadoActual());
		stackCaminoAFD.push(afd.getEstadoActual());

		//Se registran en el stack correspondiente, el estado de c/AFD de instancia 
		if (htOIDAFD.size() > 0) {
			AutomataVerificacion afdOID;

			Enumeration<Integer> enume = htOIDAFD.keys();
			while (enume.hasMoreElements()) {
				int oid = enume.nextElement();

				afdOID = htOIDAFD.get(oid);
				Stack<Object> stkAfdOid = htOIDStack.get(oid);
				stkAfdOid.push(afdOID.getEstadoActual());
			}
		}
		
		//TODO Ver si esto se configura con un parametro (property)
		System.out.println("--------------------------------- STATE-ADVANCED (CTX;JVM;AFDs) " + contexto.getEstadoActual() +  ";" + this.estadoActual() + "--------------------------------");
	}

	public void setAfd(AutomataVerificacion afd) {
		this.afd = afd;
	}

	/**
	 * Guarda una asociaci�n de TypeStatePropertyTemplate con clase
	 * La utilizar� para crear los AFDs para cada instancia de dicha clase
	 */
	public void agregarTipoAFD(TypeStatePropertyTemplate tpl, String clase) {
		htClaseAFD.put(clase, tpl); 
	}

	/**
	 * Determina si debe crear un AFD asociado al nuevo objeto
	 */
	public void objetoCreado(JVM vm) {
		String strClase = vm.getLastElementInfo().getClassInfo().getName();
		Collection padres = padresDeClase(strClase);
		
		for (Iterator iter = padres.iterator(); iter.hasNext();) {
			String element = (String) iter.next();
			
			strClase = element;
			if (htClaseAFD.containsKey(strClase)) {
				TypeStatePropertyTemplate tpl = (TypeStatePropertyTemplate) htClaseAFD.get(strClase);
				//Se crea el AFD correspondiente
				AutomataVerificacion afd = new AutomataVerificacion(tpl);
				htOIDAFD.put(vm.getLastElementInfo().getIndex(), afd);
				
				//Se crea su stack de estados (para backtrack) asociados
				Stack<Object> stkAfdOid = new Stack<Object>();
				htOIDStack.put(vm.getLastElementInfo().getIndex(), stkAfdOid);
			}
		}
	}

	private Collection<String> padresDeClase(String clase) {
		Collection<String> list = new LinkedList<String>();
//		try {
//			Class cl = Class.forName(clase);
//			while (cl != null) {
//				list.add(cl.getName());
//				Class[] ints = cl.getInterfaces();
//				for (int i = 0; i < ints.length; i++) {
//					list.add(ints[i].getName());
//				}
//
//				cl = cl.getSuperclass();
//			}
//			return list;
//		} catch (ClassNotFoundException e) {
//			return list;
//		}
		return list;
	}
	
	/**
	 * Determina si debe destruir un AFD asociado al objeto
	 */
	public void objetoLiberado(JVM vm) {
		int iOID = vm.getLastElementInfo().getIndex();

		if (htOIDAFD.containsKey(iOID)) {
			//Se elimina el AFD de la colecci�n y su stack de estados asociado
			htOIDAFD.remove(iOID);
			htOIDStack.remove(iOID);
		}
	}

	public void setEvb(EventBuilder evb) {
		this.evb = evb;
	}

	public void setContexto(ContextoBusqueda contexto) {
		this.contexto = contexto;
	}

	public void setSearch(DFSearchTesis search) {
		this.search = search;
	}

	/**
	 * Decide si hay que backtrackear la rama actual de la b�squeda
	 * en funci�n de si se invalid� el contexto o si es estado actual ya es conocido  
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
			if (vm.getLastInstruction() instanceof INVOKEVIRTUAL) {
				INVOKEVIRTUAL li = (INVOKEVIRTUAL) vm.getLastInstruction();
				//TODO: RS, ver si se puede investigar directamente por el objeto mname 
				//li.mname.toString()
				int oid = li.getCalleeThis(vm.getLastThreadInfo());
				System.out.println("OID invocado = " + oid + " de TIPO = " + li.getCalleeClassInfo(vm.getKernelState(), oid).getName());

				iOIDUltimaEjecucion = oid; 
			}
			else iOIDUltimaEjecucion = -1;
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public void registrarEstadoVistado() {
		htEstadosVisitados.put(estadoActual(), 0);
	}

	/**
	 * Carga los 3 XML de configuracion
	 * 
	 * @param eventsFile
	 * @param propertiesFile
	 * @param searchContextFile
	 * @throws XMLException 
	 */
	public void loadConfiguration(String eventsFile, String propertiesFile, String searchContextFile) throws XMLException {
		loadConfiguration(eventsFile, propertiesFile);

		this.setContexto(new ContextoBusqueda(new XMLContextoBusquedaReader(searchContextFile, this.evb)));
		// TODO: esto hay q setearlo en el XML para q sepa si es Preambulo o Contexto
		this.setModoContexto();
	}

	/**
	 * Carga los 2 XML de configuracion
	 * 
	 * @param eventsFile
	 * @param propertiesFile
	 * @throws XMLException 
	 */
	public void loadConfiguration(String eventsFile, String propertiesFile) throws XMLException {
		EventBuilder eb = new EventBuilder(new XMLEventBuilderReader(eventsFile));
		this.setEvb(eb);

		// Al no tener un contexto de busqueda debemos crear uno siempre valido
		this.setContexto(new ContextoValidoBusqueda());
		this.setModoContexto();

		this.setProperties(new XMLAFDReader(propertiesFile, eb));
	}

	/**
	 * Carga todas las propiedades TypeState y las Globals
	 * @param reader
	 * @throws XMLException 
	 */
	private void setProperties(XMLAFDReader reader) throws XMLException {
		String type;
		Iterator<String> it;

		// TypeStateProperties
		if (reader.hasTypeStateProperties()) {
			it = reader.getClases().iterator();
			while (it.hasNext()) {
				type = it.next();
				TypeStatePropertyTemplate tpl = new TypeStatePropertyTemplate(type, reader);
				this.agregarTipoAFD(tpl, type);
			}
		}

		// GlobalProperties
		if (reader.hasGlobalProperties()) {
			this.setAfd(new AutomataVerificacion(reader));
		} else {
			this.setAfd(new AutomataVerificacionVacio());
		}
	}
}
