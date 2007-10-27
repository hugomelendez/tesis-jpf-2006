package tesis.solucionTypestate;

import java.util.Iterator;
import java.util.LinkedList;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.search.Search;

class RamaObjeto {
	Integer estadoDesde;
	Integer estadoHacia;
	Object objeto;
	
	public RamaObjeto (int eDesde, Object o) {
		objeto = o;
		estadoDesde = eDesde;
	}
	
	public void estadoHacia(int eHacia) {
		estadoHacia = eHacia;
	}
	
	public String toString() {
		return ("Desde: " + estadoDesde + " Hacia: " + estadoHacia + " OID=" + objeto);
	}
}

public class ListenerSinEstado extends ListenerAdapter {
	private int cont = 0;
	private LinkedList<RamaObjeto> listaObjetos = new LinkedList<RamaObjeto>();
	private int estadoAnterior;

	@Override
	public void stateProcessed(Search search) {
		System.out.println("--------------------------------- STATE-PROCESSED: " + search.getStateNumber() + "  --------------------------------");
	}

	@Override
	public void stateRestored(Search search) {
		System.out.println("--------------------------------- STATE-RESTORED: " + search.getStateNumber() + "  --------------------------------");
	}

	@Override
	public void stateAdvanced(Search search) {
		System.out.println("--------------------------------- STATE-ADVANCED: " + search.getStateNumber() + "  --------------------------------");
		System.out.println("********************************* RAMA: " + estadoAnterior + " -> " + search.getStateNumber() + "  --------------------------------");
		for (Iterator iter = listaObjetos.iterator(); iter.hasNext();) {
			RamaObjeto element = (RamaObjeto) iter.next();
			if (element.estadoDesde == estadoAnterior && element.estadoHacia==null) {
				element.estadoHacia = search.getStateNumber();
			}
		}
		estadoAnterior = search.getStateNumber();
	}

	@Override
	public void stateBacktracked(Search search) {
		System.out.println("--------------------------------- STATE-BACKTRACKED:" + search.getStateNumber() + "--------------------------------");
		System.out.println("********************************* RAMA: " + search.getStateNumber() + " -> " + estadoAnterior + "  --------------------------------");
		estadoAnterior = search.getStateNumber();
	}

	@Override
	public void threadStarted(JVM vm) {
//		System.out.println("--------------------------------- THREAD-STARTED: " + vm.getThreadNumber() + "  --------------------------------");
	}

	@Override
	public void threadTerminated(JVM vm) {
//		System.out.println("--------------------------------- THREAD-TERMINATED: " + vm.getThreadNumber() + " --------------------------------");
	}

	public void instructionExecuted(JVM vm) {
		String s = new String("");
		cont++;
		
		try {
			Instruction lI = vm.getLastInstruction();

			s += "threadNum: " + vm.getThreadNumber() + " - ";
			s += "  li: " + lI + " - ";
			System.out.println(s);
		}
		catch (NullPointerException e) {
		}
	}

	@Override
	public void objectCreated(JVM vm) {
		if (vm.getLastElementInfo().getClassInfo().getName().startsWith("tesis.Ejemplo")) {
			System.out.println("--------------------------------- objectCreated oid: "+vm.getLastElementInfo().getIndex()+" - clase:"+ vm.getLastElementInfo().getClassInfo().getName()+ " - estado:" + vm.getStateId() +"--------------------------------");
			listaObjetos.add(new RamaObjeto(vm.getStateId(), vm.getLastElementInfo().getIndex()));
		}
	}

	@Override
	public void objectReleased(JVM vm) {
		if (vm.getLastElementInfo().getClassInfo().getName().startsWith("tesis.Ejemplo")) {
			System.out.println("--------------------------------- objectReleased - clase:"+ vm.getLastElementInfo().getClassInfo().getName()+ " - estado:" + vm.getStateId() +"--------------------------------");
		}
	}

	@Override
	public void searchStarted(Search search) {
		estadoAnterior = -1;
		super.searchStarted(search);
	}

	public void searchFinished(Search search) {
		System.out.println("--------------------------------- FIN  --------------------------------");
		for (Iterator iter = listaObjetos.iterator(); iter.hasNext();) {
			RamaObjeto element = (RamaObjeto) iter.next();
			System.out.println(element);			
		}
	}

}