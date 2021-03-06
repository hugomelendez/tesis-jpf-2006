package tesis.Ejemplo_08;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import tesis.extensiones.*;

public class ejecutarEjemplo {
	public static void main (String[] args) throws XMLException {
		// Path para acceder a este ejemplo, es el prefijo usado para leer los xmls
		String path = "./src/tesis/Ejemplo_08/";

		 Coordinador c = new Coordinador();
		 c.loadConfiguration(path + "Events.xml", path + "ProblemProperty.xml", path + "ProblemContext.xml");

		Listener listener = new Listener(c);
		
		String[] a = new String[1];
		a[0] = "tesis.Ejemplo_08.Modelo";
	    Config conf = JPF.createConfig(a);

//	    UnsatisfiedLinkError;
	    
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