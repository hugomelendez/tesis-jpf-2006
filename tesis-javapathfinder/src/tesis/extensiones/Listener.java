package tesis.extensiones;

import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.jvm.bytecode.InvokeInstruction;
import gov.nasa.jpf.search.Search;

/**
 * Clase Listener de Verificaci�n
 * Se encarga de lo b�sico: cada vez que ocurre instrucci�n, notifica al Coordinador
 * Podr�a llegar a extenderse, habr�a que ver si el m�todo instructionExecuted ser�a final o no
 */
public class Listener extends PropertyListenerAdapter implements JPFListener {
	protected Coordinador coord;

	public Listener (Coordinador c) {
		coord = c;
	}

//	@Override
//	public void executeInstruction(JVM vm) {
//		Instruction li = vm.getLastInstruction();
//		try {
//			if (li instanceof INVOKEVIRTUAL) {
//				INVOKEVIRTUAL i = (INVOKEVIRTUAL) li;
//				System.out.println("li = ");
//				System.out.println(i.toString());
//			}
//			if (li.toString().contains("open") && li.toString().contains("Ejemplo")) {
//				System.out.println("PRE-OBJECTID = " + vm.getLastThreadInfo().getCalleeThis(li.getMethod()));
//			}
//		} catch (Exception e) {
//			System.out.println("ERROR en executeInstruction");
//			if (li != null)
//				System.out.println("li es " + li.getClass().toString());
//			else
//				System.out.println("li es null");
//			
//		}
//	}

	/**
	 * Le avisa al Coordinar que acaba de ocurrir la Instruccion
	 */
	@Override
	public final void instructionExecuted(JVM vm) {
		coord.ocurrioInstruccion(vm.getLastInstruction());
	}
	
	/**
	 * Utilizamos este metodo (se invoca ANTES de que se ejecute la Instruction) en vez de instructionExecuted
	 * para poder determinar el OID y la CLASE del objeto sobre el cual se invocara el metodo
	 */
	public final void executeInstruction(JVM vm) {
		coord.ocurriraInstruccion(vm);
	}

	@Override
	public void objectCreated(JVM vm) {
		if (vm.getLastElementInfo().getClassInfo().getName().contains("Ejemplo")) {
			System.out.println("OBJETO CREADO DE TIPO " + vm.getLastElementInfo().getClassInfo().getName());
			System.out.println("OID=" + vm.getLastElementInfo().getIndex());
		}
	}

	@Override
	public void objectReleased(JVM vm) {
		if (vm.getLastElementInfo().getClassInfo().getName().contains("Ejemplo")) {
			System.out.println("OBJETO LIBERADO DE TIPO " + vm.getLastElementInfo().getClassInfo().getName());
			System.out.println("OID=" + vm.getLastElementInfo().getIndex());
		}
	}

	@Override
	public final boolean check(Search search, JVM vm) {
		return (!coord.propiedadViolada());
	}
}