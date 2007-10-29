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
 * Contiene la tupla de objetos necesarios para la adminitración de los AFD TypeState
 * Es decir, para cada AFD se le asocia la RAMA (estadoDesde-estadoHacia) en que se creó
 * y en que se liberó, debido al ciclo de vida de su objeto OID asociado en la verificación
 * 
 * @author Roberto
 */
class AFDTrack {
	private AutomataVerificacion afd;
	private Integer oid;
	private Integer creadoEstadoDesde;
	private Integer creadoEstadoHacia;
	private Integer liberadoEstadoDesde;
	private Integer liberadoEstadoHacia;
	private boolean activo;
	private Stack<State> stackEstados; 
	
	public AFDTrack(AutomataVerificacion a, Integer o, Integer estadoDesde, Stack<State> st) {
		afd = a;
		oid = o;
		creadoEstadoDesde = estadoDesde;
		stackEstados = st;
		activar();
		
		//TODO: mejorar esto
		//parche para que no se rompa el Coordinador en el Advanced al preguntar por esto
		liberadoEstadoDesde = -10; 
	}

	public Integer creadoEstadoHacia() {
		return creadoEstadoHacia;
	}

	public void creadoEstadoHacia(Integer creadoEstadoHacia) {
		this.creadoEstadoHacia = creadoEstadoHacia;
	}

	public Integer liberadoEstadoDesde() {
		return liberadoEstadoDesde;
	}

	public void liberadoEstadoDesde(Integer liberadoEstadoDesde) {
		this.liberadoEstadoDesde = liberadoEstadoDesde;
	}

	public Integer liberadoEstadoHacia() {
		return liberadoEstadoHacia;
	}

	public void liberadoEstadoHacia(Integer liberadoEstadoHacia) {
		this.liberadoEstadoHacia = liberadoEstadoHacia;
	}

	public boolean activo() {
		return activo;
	}

	public void activar() {
		activo = true;
	}
	public void desactivar() {
		activo = false;
	}

	public AutomataVerificacion afd() {
		return afd;
	}

	public Integer oid() {
		return oid;
	}

	public Integer creadoEstadoDesde() {
		return creadoEstadoDesde;
	}

	public Stack<State> stackEstados() {
		return stackEstados;
	}
}

/**
 * Coordinador entre todos los objetos
 * 
 * ConcreteMediator Implements cooperative behavior by coordinating Colleague
 * objects. Knows and maintains its colleagues.
 */
public class Coordinador {
	/**
	 * Contiene los estados visitados hasta el momento
	 * (cada estado es una composición de los estados de la VM y los AFDs)
	 */
	private Hashtable<String, Integer> estadosVisitados;
	private int cantidadRegistrosEstado;
	
	private Stack<State> stackCaminoPreambulo;
	private Stack<State> stackCaminoAFD;

	/**
	 * OID de la última ejecución de un VirtualInvocation de método
	 * TODO Tesis: Mejorar el manejo de este id especial
	 */
	private int iOIDUltimaEjecucion = -1;

	//GlobalProperty
	private AutomataVerificacion afdGlobalProperty;
	/**
	 * Contiene la lista de todos los AFD TypeState con sus datos de tracking
	 */
	private LinkedList<AFDTrack> listaAFDTrack;

	private DFSearchTesis search;
	private ContextoBusqueda contexto;  //  @jve:decl-index=0:
	private EventBuilder evb;  //  @jve:decl-index=0:
	private static final int MODO_PREAMBULO = 0; 
	private static final int MODO_CONTEXTO = 1;
	private int modo = MODO_PREAMBULO;

	private int estadoAnteriorJPF;
	
	/**
	 * Contiene las asociaciones de Clase con TypeStatePropertyTemplate
	 */
	private Hashtable<String, TypeStatePropertyTemplate> htClaseAFD;

	public Coordinador() {
		estadosVisitados = new Hashtable<String, Integer>();
		stackCaminoPreambulo = new Stack<State>();
		stackCaminoAFD = new Stack<State>();
		listaAFDTrack = new LinkedList<AFDTrack>();
		htClaseAFD = new Hashtable<String, TypeStatePropertyTemplate>();
		estadoAnteriorJPF = -1;
		cantidadRegistrosEstado = 0;
	}
	
	
	/**
	 * Verifica si el automata GLOBAL o los AFDs x Instancia
	 * llegaron a un estado final, por lo tanto se cumple
	 * la antipropiedad
	 * 	
	 * @return true si el afd esta en un estado final
	 */
	public boolean propiedadViolada() {
		boolean res = false;
		
		//Se recorren todos los AFDs para ver si hay alguno violado (no importa si está activo o no,
		//porque hay que mostrar la VIOLACION por más que se haya eliminado el objeto asociado)
		for (Iterator<AFDTrack> iterator = listaAFDTrack.iterator(); iterator.hasNext() && !res;) {
			AFDTrack afdTrack = (AFDTrack) iterator.next();
			res = afdTrack.afd().estadoFinal();
		}
		
		return afdGlobalProperty.estadoFinal() || res;
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
//				escribirLog(e);
				if (modo == MODO_PREAMBULO) {
					//MODO Preambulo
					//Antes de avanzar el AFD, verifica que se haya cumplido el Contexto (Preambulo)
					if (!contexto.cumplido()) {
						contexto.consumir(e);
//						escribirLog(contexto);
					}
					else {
						//Global Property
						afdGlobalProperty.consumir(e);
//						escribirLog(afdGlobalProperty);

						//TypeStateProperty/s
						if (iOIDUltimaEjecucion != -1) {
							//Se recorren todos los AFDs para ver si hay alguno activo que aplique al evento
							for (Iterator<AFDTrack> iterator = listaAFDTrack.iterator(); iterator.hasNext();) {
								AFDTrack afdTrack = (AFDTrack) iterator.next();
								if (afdTrack.activo() && afdTrack.oid()==iOIDUltimaEjecucion) {
									afdTrack.afd().consumir(e);
//									escribirLog(afdTrack.afd());
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
//					escribirLog(contexto);
	
					//Global Property
					afdGlobalProperty.consumir(e);
//					escribirLog(afdGlobalProperty);
	
					//TypeStateProperty/s
					if (iOIDUltimaEjecucion != -1) {
						//Se recorren todos los AFDs para ver si hay alguno activo que aplique al evento
						for (Iterator<AFDTrack> iterator = listaAFDTrack.iterator(); iterator.hasNext();) {
							AFDTrack afdTrack = (AFDTrack) iterator.next();
							if (afdTrack.activo() && afdTrack.oid()==iOIDUltimaEjecucion) {
								afdTrack.afd().consumir(e);
//								escribirLog(afdTrack.afd());
							}
						}
						iOIDUltimaEjecucion = -1;
					}
				}
			}
		}
	}

	/**
	 * Devuelve el estado actual JPF (sería el Desde de la rama procesada)
	 * 
	 * @return int
	 */
	private int estadoActualJPF() {
		return search.getStateNumber();
	}

	/**
	 * Devuelve el estado anterior JPF (usado para saber desde dónde se backtrackeó)
	 * 
	 * @return int
	 */
	private int estadoAnteriorJPF() {
		return estadoAnteriorJPF;
	}

	/**
	 * Devuelve una representacion del estado combinado de JVM;AFD(global);AFDs(instancia)
	 * 
	 * @return String
	 */
	public String estadoCompuestoAsString() {
		String strRes = new String();
		
		strRes = estadoActualJPF() + ";";
		strRes += afdGlobalProperty.getEstadoActual();
		
		for (Iterator<AFDTrack> iterator = listaAFDTrack.iterator(); iterator.hasNext();) {
			AFDTrack afdTrack = (AFDTrack) iterator.next();
			strRes = strRes + ";" + afdTrack.afd().getEstadoActual() + "/activo=" + afdTrack.activo();
		}
		
		return strRes;
	}

	/**
	 * El Search notifica al coordinador que se backtrackeo el arbol.
	 * Backtrackea todos los AFDs (global y TypeState) para que regresen al estado corresp.
	 * al estado (JVM) al que se backtrackeo
	 * Además, si haya algún AFD TypeState que se creó en la rama backtrackeada, se elimina físicamente
	 */
	public void stateBacktracked() {
		stackCaminoPreambulo.pop();
		contexto.irAEstado(stackCaminoPreambulo.peek());
 
		stackCaminoAFD.pop();
		afdGlobalProperty.irAEstado(stackCaminoAFD.peek());

		//Se recorren todos los AFDs para ver si hay alguno creado/liberado en la rama anterior
		//y se le asigna tanto los estados Hacia, como el stack correspondiente
		//si está activo, se lo backtrackea
		for (Iterator<AFDTrack> iterator = listaAFDTrack.iterator(); iterator.hasNext();) {
			AFDTrack afdTrack = (AFDTrack) iterator.next();
		
			//Todos los AFDs que se crearon en la rama que se acaba de backtrackear
			//se eliminan físicamente
			if (afdTrack.creadoEstadoDesde()==estadoActualJPF() && afdTrack.creadoEstadoHacia()==estadoAnteriorJPF()) {
				listaAFDTrack.remove(afdTrack);
			}
			//Pongo else por optimización, ya se si se eliminó de la lista ya no importa 
			else if (!afdTrack.activo() &&
					afdTrack.liberadoEstadoDesde()==estadoActualJPF() && afdTrack.liberadoEstadoHacia()==estadoAnteriorJPF()) {
				afdTrack.activar();
			}

			//Si está activo, se backtrackea a su estado correspondiente 
			//Por ahora, no estamos discriminando si están inactivos o no
//			if (afdTrack.activo()) {
				afdTrack.stackEstados().pop();
				afdTrack.afd().irAEstado(afdTrack.stackEstados().peek());
//			}
		}
		
		estadoAnteriorJPF = estadoActualJPF();
//		escribirLog("----- STATE-BACKTRACKED (CTX;JVM;AFDs) " + contexto.getEstadoActual() +  ";" + this.estadoCompuestoAsString() + "-----");
	}

	/**
	 * El Search notifica al coordinador que avanzo el arbol. Agrega a la pila el estado
	 * en el que se encuentra el AFD
	 */
	public void stateAdvanced() {
		stackCaminoPreambulo.push(contexto.getEstadoActual());
		stackCaminoAFD.push(afdGlobalProperty.getEstadoActual());

		//Se recorren todos los AFDs para ver si hay alguno creado/liberado en la rama anterior
		//y se le asigna tanto los estados Hacia, como el stack correspondiente
		for (Iterator<AFDTrack> iterator = listaAFDTrack.iterator(); iterator.hasNext();) {
			AFDTrack afdTrack = (AFDTrack) iterator.next();
			
			//La 2da guarda de estos IFs debería se siempre true, pero se agrega por consistencia
			if (afdTrack.creadoEstadoHacia()==null && afdTrack.creadoEstadoDesde()==estadoAnteriorJPF()) {
				afdTrack.creadoEstadoHacia(estadoActualJPF());
			}
			if (afdTrack.liberadoEstadoHacia()==null && afdTrack.liberadoEstadoDesde()==estadoAnteriorJPF()) {
				afdTrack.liberadoEstadoHacia(estadoActualJPF());
			}

			//Se registran en el stack correspondiente, el estado de c/AFD de instancia
			//Por ahora, no estamos discriminando si están inactivos o no
			Stack<State> stkAfd = afdTrack.stackEstados();
			stkAfd.push(afdTrack.afd().getEstadoActual());
		}
		estadoAnteriorJPF = estadoActualJPF();
		
//		escribirLog("----- STATE-ADVANCED (CTX;JVM;AFDs) " + contexto.getEstadoActual() +  ";" + this.estadoCompuestoAsString() + "-----");
		
		//TODO: ver dónde conviene setear esto, es para ver realmente la cantidad de estados explorados
		cantidadRegistrosEstado++;
	}

	public void setAfd(AutomataVerificacion afd) {
		this.afdGlobalProperty = afd;
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
		Collection<String> padres = padresDeClase(strClase);
		int cant=0;
		
		for (Iterator<String> iter = padres.iterator(); iter.hasNext();) {
			strClase = (String)iter.next();
			
			if (htClaseAFD.containsKey(strClase)) {
				TypeStatePropertyTemplate tpl = (TypeStatePropertyTemplate) htClaseAFD.get(strClase);
				//OID asociado
				Integer oid = vm.getLastElementInfo().getIndex();
				
				//Se crea el AFD correspondiente
				AutomataVerificacion afd = new AutomataVerificacion(tpl);
				//Se crea su stack de estados (para backtrack) asociados
				Stack<State> stkAfd = new Stack<State>();
				
				AFDTrack afdTrack = new AFDTrack(afd, oid, estadoActualJPF(), stkAfd);
				
				listaAFDTrack.add(afdTrack);

				if (cant<1) {
//					escribirLog("OBJETO CREADO. Type=" + vm.getLastElementInfo().getClassInfo().getName() + ", OID=" + oid);
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
	 * Determina si debe desactivar un AFD asociado al objeto
	 */
	public void objetoLiberado(JVM vm) {
		int iOID = vm.getLastElementInfo().getIndex();

		//Se recorre toda la lista de AFDs
		for (Iterator<AFDTrack> iterator = listaAFDTrack.iterator(); iterator.hasNext();) {
			AFDTrack afdTrack = (AFDTrack) iterator.next();

			//Si el AFD está asociado al objeto liberado y además está activo
			//, se desactiva y se le marca el desde de la rama actual de JPF
			if (afdTrack.oid()==iOID && afdTrack.activo()) {
				afdTrack.desactivar();
				afdTrack.liberadoEstadoDesde(estadoActualJPF());
//				escribirLog("OBJETO LIBERADO. OID=" + iOID);
			}
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
	 * Decide si hay que backtrackear la rama actual de la búsqueda
	 * en funciï¿½n de si se invalidó el contexto o si es estado actual ya es conocido  
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
		escribirLog("# estados JVM: " + search.getVM().getStateSet().size());
		escribirLog("# estados explorados (producción JVM*FWK): " + cantidadRegistrosEstado/*estadosVisitados.size()*/);
		escribirLog("Fin Verificacion " + now());
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

	private void escribirFilelog(String msg) {
	    FileWriter aWriter;
		try {
			aWriter = new FileWriter(nombreLog(), true);
		    aWriter.write(msg + System.getProperty("line.separator"));
		    aWriter.flush();
		    aWriter.close();
		} catch (IOException e) {
			System.out.println("No se puede escribir en el log");
			e.printStackTrace();
		}
	}

	public void escribirLog(String msg) {
		//TODO Ver si se parametriza
		System.out.println(msg);
//		escribirFilelog(msg);
	}

	public void escribirLog(Evento e) {
//		escribirLog("EVENTO: " + e.label());
	}

	public void escribirLog(AutomataVerificacion a) {
//		escribirLog("\t" + a.getType() + ": " + a.getEstadoAnterior() + " -> " + a.getEstadoActual());
		if (a.estadoFinal()) {
//			escribirLog("Propiedad violada en " + a.getType());
		}
	}

	public void escribirLog(ContextoBusqueda c) {
//		escribirLog("\tContextSearch: " + c.getEstadoAnterior() + " -> " + c.getEstadoActual());
		if (c.invalido()) {
//			escribirLog("Estado invalido invalido en ContextSearch");
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
