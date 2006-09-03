package tesis.Ejemplo_01;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.MethodInfo;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.search.Search;

public class ListenerSinEstado extends ListenerAdapter {
	private int cont = 0;

	@Override
	public void choiceGeneratorAdvanced(JVM vm) {
		//System.out.println("--------------------------------- CHOICEGENERATORADVANCED: " + vm.getThreadNumber() + "  --------------------------------");
	}

	@Override
	public void choiceGeneratorProcessed(JVM vm) {
		//System.out.println("--------------------------------- CHOICEGENERATORPROCESSED: " + vm.getThreadNumber() + "  --------------------------------");
	}

	@Override
	public void choiceGeneratorSet(JVM vm) {
		//System.out.println("--------------------------------- CHOICEGENERATORSET: " + vm.getThreadNumber() + "  --------------------------------");
	}

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
	}

	@Override
	public void stateBacktracked(Search search) {
		System.out.println("--------------------------------- STATE-BACKTRACKED:" + search.getStateNumber() + "--------------------------------");
	}

	@Override
	public void threadStarted(JVM vm) {
		System.out.println("--------------------------------- THREAD-STARTED: " + vm.getThreadNumber() + "  --------------------------------");
	}

	@Override
	public void threadTerminated(JVM vm) {
		System.out.println("--------------------------------- THREAD-TERMINATED: " + vm.getThreadNumber() + " --------------------------------");
	}

	public void instructionExecuted(JVM vm) {
		String s = new String("");
		cont++;
		
		try {
			Instruction lI = vm.getLastInstruction();
			MethodInfo limi = lI.getMethod();

			//if (vm.getLastInstruction().getSourceLocation().contains("ModeloThreadsSimple.java")) {
				s += "threadNum: " + vm.getThreadNumber() + " - ";
				//s += "  li: " + instEj(lI) + " - ";
				s += "  li: " + lI + " - ";
				//s += "  limi: " + limi.getName() + " - ";
				//s += "  linea: " + lI.getPosition() + " - ";
				//s += "  li: " + vm.getLastInstruction().getSourceLocation();
				//System.out.println(s);
			//}
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

		    JPF jpf = new JPF(conf); 
		    jpf.addSearchListener(listener);
		    jpf.addVMListener(listener);
		    
		    System.out.println("---------------- JPF started");
		    jpf.run();
		    System.out.println("---------------- JPF terminated");
	  }
}