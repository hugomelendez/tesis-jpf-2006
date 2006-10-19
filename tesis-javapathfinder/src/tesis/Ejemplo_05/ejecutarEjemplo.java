package tesis.Ejemplo_05;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import tesis.extensiones.*;

public class ejecutarEjemplo {
	public static void main (String[] args) {
		Coordinador c = new Coordinador();

		PreambuloEjemplo05 pre = new PreambuloEjemplo05();
		c.setPreambulo(pre);

		XMLAFDReader xmlafd = new XMLAFDReader("pepito.xml");
		AutomataVerificacion aut = new AutomataVerificacion(xmlafd);
		c.setAfd(aut);

		XMLEventBuilderReader xmleb = new XMLEventBuilderReader("pepito.xml"); 
		EventBuilder eb = new EventBuilder(xmleb);
		c.setEvb(eb);

		Listener listener = new Listener(c);
		
		String[] a = new String[1];
		a[0] = "tesis.Ejemplo_05.Modelo";
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