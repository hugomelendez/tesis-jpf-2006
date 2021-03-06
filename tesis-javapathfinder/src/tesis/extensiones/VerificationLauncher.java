package tesis.extensiones;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

public class VerificationLauncher {

	public static void execute(String pathXML, String modelo) throws XMLException {
		execute(pathXML, modelo, true);
	}
	
	/**
	 * 
	 * @param path: ruta donde se encuentran los xmls 
	 * @param modelo: clase principal de la aplicaci�n a verificar
	 * @throws XMLException
	 */
	public static void execute(String pathXML, String modelo, Boolean usarContexto) throws XMLException {
		Coordinador c = new Coordinador();

		if (usarContexto) {
			c.loadConfiguration(pathXML + "Events.xml", pathXML + "ProblemProperty.xml", pathXML + "ProblemContext.xml");
		} else {
			c.loadConfiguration(pathXML + "Events.xml", pathXML + "ProblemProperty.xml");
		}

		Listener listener = new Listener(c);
		
		String[] a = new String[1];
		a[0] = new String(modelo);
	    Config conf = JPF.createConfig(a);

	    // B�squeda custom del FWK
	    conf.setProperty("search.class","tesis.extensiones.DFSearchTesis");

	    JPF jpf = new JPF(conf);
	    jpf.addSearchListener(listener);
	    jpf.addVMListener(listener);
	    
	    ((DFSearchTesis)jpf.search).setCoordinador(c);
	    
	    run(jpf);
	}

	/**
	 * Ejecucion sin framework, solo JPF original
	 * @param modelo: clase principal de la aplicaci�n a verificar
	 * @throws XMLException
	 */
	public static void execute(String modelo) {
		String[] a = new String[1];
		a[0] = new String(modelo);
	    Config conf = JPF.createConfig(a);

	    JPF jpf = new JPF(conf);

	    run(jpf);
	}

	private static void run (JPF jpf) {
	    String startTime = now();
		System.out.println("---------------- JPF started "+startTime);
	    jpf.run();
	    System.out.println("# estados: " + jpf.search.getVM().getStateSet().size());
	    System.out.println("---------------- JPF terminated - start time: "+startTime+" - end time:"+now());
	}
	
	private static String now() {
		DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		java.util.Date date = new java.util.Date();
		return (dateFormat.format(date));
	}
}