package tesis.Ejemplo_03;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import tesis.extensiones.*;

public class ejecutarEjemplo {
	public static void main (String[] args) {
		Coordinador c = new Coordinador();

		PreambuloEjemplo03 pre = new PreambuloEjemplo03();
		c.setPreambulo(pre);
		
		Automata aut = new Automata();
		c.setAfd(aut);

		EventBuilderEjemplo03 eb = new EventBuilderEjemplo03();
		c.setEvb(eb);

		Listener listener = new Listener(c);
		
		String[] a = new String[1];
		a[0] = "tesis.Ejemplo_03.ModeloOpenClose";
	    Config conf = JPF.createConfig(a);

	    // usamos nuestra busqueda
	    conf.setProperty("search.class","tesis.extensiones.DFSearchTesis");

	    JPF jpf = new JPF(conf);
	    jpf.addSearchListener(listener);
	    jpf.addVMListener(listener);
	    
	    ((DFSearchTesis)jpf.search).setCoordinador(c);
	    
	    System.out.println("---------------- JPF started");
	    jpf.run();
	    System.out.println("---------------- JPF terminated");
	}
}