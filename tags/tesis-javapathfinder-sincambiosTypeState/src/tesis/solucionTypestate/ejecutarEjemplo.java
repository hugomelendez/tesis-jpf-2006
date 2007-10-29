package tesis.solucionTypestate;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

public class ejecutarEjemplo {

	public static void main (String[] args) {
		ListenerSinEstado listener = new ListenerSinEstado();

		String[] a = new String[1];
		a[0] = "tesis.Ejemplo_01.ModeloDosHilos";
	    Config conf = JPF.createConfig(a);

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