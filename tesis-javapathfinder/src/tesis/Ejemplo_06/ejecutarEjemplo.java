package tesis.Ejemplo_06;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import tesis.extensiones.*;

public class ejecutarEjemplo {
	public static void main (String[] args) {
		Coordinador c = new Coordinador();

		XMLContextoBusquedaReaderEj06 xmlpre = new XMLContextoBusquedaReaderEj06("pepito.xml");
		ContextoBusqueda pre = new ContextoBusqueda(xmlpre);
		c.setContexto(pre);
		c.setModoContexto();
		
		XMLAFDReaderEj06 xmlafd = new XMLAFDReaderEj06("pepito.xml");
		AutomataVerificacion aut = new AutomataVerificacion(xmlafd);
		c.setAfd(aut);

		XMLEventBuilderReader xmleb = new XMLEventBuilderReader("pepito.xml"); 
		EventBuilder eb = new EventBuilder(xmleb);
		c.setEvb(eb);

		Listener listener = new Listener(c);
		
		String[] a = new String[1];
		a[0] = "tesis.Ejemplo_06.Modelo";
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