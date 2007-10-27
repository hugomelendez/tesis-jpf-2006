package tesis.Doc_Ejemplo_01;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.jvm.bytecode.InvokeInstruction;
import gov.nasa.jpf.search.Search;

public class TestProperty extends PropertyListenerAdapter {
	private int count = 0;

	public boolean check(Search search, JVM vm) {
		return (count < 3);
	}

	public void executeInstruction(JVM vm) {
		Instruction li = vm.getLastInstruction();
		if (li instanceof InvokeInstruction) {
			count++;
		}
	}
}