package tesis.extensiones;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

public class VerificationLauncher {

	/**
	 * 
	 * @param path: ruta donde se encuentran los xmls 
	 * @param modelo: clase principal de la aplicación a verificar
	 * @throws XMLException
	 */
	public static void execute(String pathXML, String modelo) throws XMLException {
		Coordinador c = new Coordinador();
		c.loadConfiguration(pathXML + "Events.xml", pathXML + "ProblemProperty.xml", pathXML + "ProblemContext.xml");

		Listener listener = new Listener(c);
		
		String[] a = new String[1];
		a[0] = new String(modelo);
	    Config conf = JPF.createConfig(a);

	    // Búsqueda custom del FWK
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