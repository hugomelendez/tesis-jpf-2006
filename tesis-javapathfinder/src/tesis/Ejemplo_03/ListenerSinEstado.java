package tesis.Ejemplo_03;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;


public class ListenerSinEstado extends ListenerAdapter {
	private int cont = 0;

	
	public void instructionExecuted(JVM vm) {
		String s = new String("");
		cont++;
		
		try {
			Instruction lI = vm.getLastInstruction();
			Instruction nI = vm.getNextInstruction();
			MethodInfo limi = lI.getMethod();
			MethodInfo nimi = nI.getMethod();

			if (vm.getLastInstruction().getSourceLocation().contains("Modelo.java")) {
				s += "instr: " + cont + "\r\n";
				s += "  limi: " + limi + "\r\n";
				s += "  li: " + vm.getLastInstruction().getSourceLocation() + "\r\n";
				System.out.println(s);
			}
		}
		catch (NullPointerException e) {
		}
	}

	  /* (non-Javadoc)
	 * @see gov.nasa.jpf.ListenerAdapter#objectCreated(gov.nasa.jpf.jvm.JVM)
	@Override
	 */
	public void objectCreated2(JVM vm) {
		String s = new String("");

		try {
			s += "  oC: " + vm.getLastElementInfo().toString() + "\r\n";
			
			System.out.println(s);
		}
		catch (NullPointerException e) {
		}
	}



	public static void main (String[] args) {
			ListenerSinEstado listener = new ListenerSinEstado();

		    Config conf = JPF.createConfig(args);
		    System.out.println(args.toString());

		    // add your own args here..
		    //conf.setProperty("jpf.print_exception_stack","true");

		    // NO FUNCIONO !
		    //conf.setProperty("vm.classpath","build/jpf/tesis");
		    conf.setProperty("vm.por.exclude_fields","java.lang.");
		    //vm.por.include_fields=


		    JPF jpf = new JPF(conf); 
		    jpf.addSearchListener(listener);
		    jpf.addVMListener(listener);
		    
		    System.out.println("---------------- JPF started");
		    jpf.run();
		    System.out.println("---------------- JPF terminated");
	  }
}