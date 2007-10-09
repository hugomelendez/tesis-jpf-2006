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
import java.io.FileWriter;
import java.io.IOException;
import java.text.*;

/**
 * Coordinador entre todos los objetos
 * 
 * ConcreteMediator Implements cooperative behavior by coordinating Colleague
 * objects. Knows and maintains its colleagues.
 */
public class Coordinador {
	/**
	 * Contiene los estados visitados hasta el momento
	 * (cada estado es una composiciï¿½n de los estados de la VM y los AFDs)
	 */
	private Hashtable<String, Integer> estadosVisitados = new Hashtable<String, Integer>();

	private Stack<State> stackCaminoPreambulo = new Stack<State>();
	private Stack<State> stackCaminoAFD = new Stack<State>();

	//Por ahora no es necesario que conozca al Listener
	//private Listener lsn;

	/**
	 * Contiene las asociaciones de Clase con TypeStatePropertyTemplate
	 */
	private Hashtable<String, TypeStatePropertyTemplate> htClaseAFD = new Hashtable<String, TypeStatePropertyTemplate>();
	/**
	 * Contiene las asociaciones de OID con AFD
	 */
	private Hashtable<Integer, AutomataVerificacion> htOIDAFD = new Hashtable<Integer, AutomataVerificacion>();
	/**
	 * Contiene la colecciï¿½n de caminos (stack) de cada AFD de instancia
	 * (para soportar los backtracks de la JVM)
	 */
	private Hashtable<Integer, Stack<State>> htOIDStack = new Hashtable<Integer, Stack<State>>();
	/**
	 * Contiene los AFDs que estï¿½n VIOLADOS
	 */
	//private Hashtable<String, AutomataVerificacion> htAFDViolados = new Hashtable<String, AutomataVerificacion>();

	/**
	 * OID de la ï¿½ltima ejecuciï¿½n de un VirtualInvocation de mï¿½todo
	 * TODO Tesis: Mejorar el manejo de este id especial
	 */
	private int iOIDUltimaEjecucion = -1;

	private AutomataVerificacion afd;
	private DFSearchTesis search;
	private ContextoBusqueda contexto;  //  @jve:decl-index=0:
	private EventBuilder evb;  //  @jve:decl-index=0:

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
			Iterator<AutomataVerificacion> it = htOIDAFD.values().iterator();
			while (it.hasNext() && !res) {
				AutomataVerificacion afdOID = it.next();
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

		//Se evalúan los eventos, SI y SÓLO SI no está violada ninguna propiedad
		if (!propiedadViolada()) {
			if (!contexto.invalido() && e.esObservable()) {
				escribirLog(e);
				if (modo == MODO_PREAMBULO) {
					//MODO Preambulo
					//Antes de avanzar el AFD, verifica que se haya cumplido el Contexto (Preambulo)
					if (!contexto.cumplido()) {
						contexto.consumir(e);
						escribirLog(contexto);
					}
					else {
						//Global Property
						afd.consumir(e);
						escribirLog(afd);

						//TypeStateProperty/s
						if (iOIDUltimaEjecucion != -1) {
							if (htOIDAFD.containsKey(iOIDUltimaEjecucion)) {
								AutomataVerificacion afdOID = (AutomataVerificacion) htOIDAFD.get(iOIDUltimaEjecucion);
								afdOID.consumir(e);
								escribirLog(afdOID);
							}
							iOIDUltimaEjecucion = -1;
						}
					}
				}
				else if (modo == MODO_CONTEXTO) {
					//MODO Contexto Busqueda
					//Avanza el Contexto y el AFD en paralelo
					contexto.consumir(e);
					escribirLog(contexto);
	
					//Global Property
					afd.consumir(e);
					escribirLog(afd);
	
					//TypeStateProperty/s
					if (iOIDUltimaEjecucion != -1) {
						if (htOIDAFD.containsKey(iOIDUltimaEjecucion)) {
							AutomataVerificacion afdOID = (AutomataVerificacion) htOIDAFD.get(iOIDUltimaEjecucion);
							afdOID.consumir(e);
							escribirLog(afdOID);
						}
						iOIDUltimaEjecucion = -1;
					}
				}
			}
		}
	}

	/**
	 * Devuelve una representacion del estado combinado de JVM;AFD(global);AFDs(instancia)
	 * 
	 * @return String
	 */
	public String estadoCompuestoAsString() {
		String strRes = new String();
		
		strRes = search.getVM().getStateId() + ";";
		strRes += afd.getEstadoActual();
		
		if (htOIDAFD.size() > 0) {
			Iterator<AutomataVerificacion> it = htOIDAFD.values().iterator();
			while (it.hasNext()) {
				AutomataVerificacion afdOID = it.next();
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
		stackCaminoPreambulo.pop();
		contexto.irAEstado(stackCaminoPreambulo.peek());
 
		stackCaminoAFD.pop();
		afd.irAEstado(stackCaminoAFD.peek());

		//Se backtrackea desde el stack correspondiente, el estado de c/AFD de instancia 
		if (htOIDAFD.size() > 0) {
			AutomataVerificacion afdOID;

			Enumeration<Integer> enume = htOIDAFD.keys();
			while (enume.hasMoreElements()) {
				int oid = enume.nextElement();

				afdOID = htOIDAFD.get(oid);
				Stack<State> stkAfdOid = htOIDStack.get(oid);
				stkAfdOid.pop();
				afdOID.irAEstado(stkAfdOid.peek());
			}
		}
		
		escribirLog("----- STATE-BACKTRACKED (CTX;JVM;AFDs) " + contexto.getEstadoActual() +  ";" + this.estadoCompuestoAsString() + "-----");
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
				Stack<State> stkAfdOid = htOIDStack.get(oid);
				stkAfdOid.push(afdOID.getEstadoActual());
			}
		}
		
		escribirLog("----- STATE-ADVANCED (CTX;JVM;AFDs) " + contexto.getEstadoActual() +  ";" + this.estadoCompuestoAsString() + "-----");
	}

	public void setAfd(AutomataVerificacion afd) {
		this.afd = afd;
	}

	/**
	 * Guarda una asociaciï¿½n de TypeStatePropertyTemplate con clase
	 * La utilizarï¿½ para crear los AFDs para cada instancia de dicha clase
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
		int cant=0;
		
		for (Iterator iter = padres.iterator(); iter.hasNext();) {
			strClase = (String)iter.next();
			
			if (htClaseAFD.containsKey(strClase)) {
				TypeStatePropertyTemplate tpl = (TypeStatePropertyTemplate) htClaseAFD.get(strClase);
				//Se crea el AFD correspondiente
				AutomataVerificacion afd = new AutomataVerificacion(tpl);
				htOIDAFD.put(vm.getLastElementInfo().getIndex(), afd);
				
				//Se crea su stack de estados (para backtrack) asociados
				Stack<State> stkAfdOid = new Stack<State>();
				htOIDStack.put(vm.getLastElementInfo().getIndex(), stkAfdOid);

				if (cant<1) {
					escribirLog("OBJETO CREADO. Type=" + vm.getLastElementInfo().getClassInfo().getName() + ", OID=" + vm.getLastElementInfo().getIndex());
				}
				cant++;
			}
		}
	}

	@SuppressWarnings("finally")
	private Collection<String> padresDeClase(String clase) {
		Collection<String> list = new LinkedList<String>();
		
		try {
			Class cl = Class.forName(clase);
			while (cl != null) {
				list.add(cl.getName());
				
				/**
				 * Open ISSUE: no se incluye el tema de Interfaces
				 * porque no existe el mï¿½todo en la clase Class de JPF 
				 */
				/*Class[] ints = cl.getInterfaces();
				for (int i = 0; i < ints.length; i++) {
					list.add(ints[i].getName());
				}*/

				cl = cl.getSuperclass();
			}
		} catch (ClassNotFoundException e) {
		}
		finally {
			return list;
		}
	}
	
	/**
	 * Determina si debe destruir un AFD asociado al objeto
	 */
	public void objetoLiberado(JVM vm) {
		int iOID = vm.getLastElementInfo().getIndex();

		if (htOIDAFD.containsKey(iOID)) {
			//Se elimina el AFD de la colecciï¿½n y su stack de estados asociado
			htOIDAFD.remove(iOID);
			htOIDStack.remove(iOID);

			escribirLog("OBJETO LIBERADO. OID=" + iOID);
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
	 * Decide si hay que backtrackear la rama actual de la bï¿½squeda
	 * en funciï¿½n de si se invalidï¿½ el contexto o si es estado actual ya es conocido  
	 * @return
	 */
	public boolean backtrackear() {
		return ( contexto.invalido() || estadosVisitados.containsKey(estadoCompuestoAsString()));
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
				//System.out.println("OID invocado = " + oid + " de TIPO = " + li.getCalleeClassInfo(vm.getKernelState(), oid).getName());

				iOIDUltimaEjecucion = oid; 
			}
			else iOIDUltimaEjecucion = -1;
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public void registrarEstadoVistado() {
		estadosVisitados.put(estadoCompuestoAsString(), 0);
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
		if(this.contexto.modoPreambulo()) {
			this.setModoPreambulo();
		} else {
			this.setModoContexto();			
		}
	}

	public void busquedaIniciada() {
		escribirLog("************************************************");
		escribirLog("Inicio Verificaciï¿½n " + now());
	}
	
	public void busquedaFinalizada() {
		escribirLog("Fin Verificaciï¿½n " + now());
		escribirLog("************************************************");
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
		// TypeStateProperties
		if (reader.hasTypeStateProperties()) {
			Iterator<String> it = reader.getClases().iterator();
			while (it.hasNext()) {
				String type = it.next();
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

	public void escribirLog(String msg) {
	    FileWriter aWriter;
		try {
			//TODO Ver si se parametriza
			System.out.println(msg);

			aWriter = new FileWriter(nombreLog(), true);
		    aWriter.write(msg + System.getProperty("line.separator"));
		    aWriter.flush();
		    aWriter.close();
		} catch (IOException e) {
			System.out.println("No se puede escribir en el log");
			e.printStackTrace();
		}

	}

	public void escribirLog(Evento e) {
		escribirLog("EVENTO: " + e.label());
	}

	public void escribirLog(AutomataVerificacion a) {
		escribirLog("\t" + a.getType() + ": " + a.getEstadoAnterior() + " -> " + a.getEstadoActual());
		if (a.estadoFinal()) {
			escribirLog("Propiedad violada en " + a.getType());
		}
	}

	public void escribirLog(ContextoBusqueda c) {
		escribirLog("\tContextSearch: " + c.getEstadoAnterior() + " -> " + c.getEstadoActual());
		if (c.invalido()) {
			escribirLog("Estado invalido invalido en ContextSearch");
		}
	}

	private String nombreLog() {
		return "log.txt";
	}

	private String now() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		java.util.Date date = new java.util.Date();
		return (dateFormat.format(date));
	}

}
