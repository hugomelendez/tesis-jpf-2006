package tesis.Ejemplo_04;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

public class ejecutarEjemplo {
	public static void main (String[] args) {
		String[] a = new String[1];
		a[0] = "tesis.Ejemplo_04.PruebaVerify";
		Config conf = JPF.createConfig(a);

	    JPF jpf = new JPF(conf);
//		ListenerSinEstado listener = new ListenerSinEstado();
//	    jpf.addSearchListener(listener);
//	    jpf.addVMListener(listener);

	    System.out.println("---------------- JPF started");
	    jpf.run();
	    System.out.println("---------------- JPF terminated");
	}
}