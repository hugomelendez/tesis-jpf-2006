package tesis.Doc_Ejemplo_01;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

public class ejecutarEjemplo {

	public static void main (String[] args) {
		ListenerSinEstado listener = new ListenerSinEstado();

		String[] a = new String[1];
		a[0] = "tesis.Doc_Ejemplo_01.ModeloVacio";
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