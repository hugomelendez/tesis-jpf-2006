package tesis.Ejemplo_07;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import tesis.extensiones.*;

public class ejecutarEjemplo {
	public static void main (String[] args) {
		Coordinador c = new Coordinador();

		XMLContextoBusquedaReaderEj07 xmlpre = new XMLContextoBusquedaReaderEj07("pepito.xml");
		ContextoBusqueda pre = new ContextoBusqueda(xmlpre);
		c.setContexto(pre);
		c.setModoContexto();
		
		XMLAFDReaderGLOBAL xmlafd = new XMLAFDReaderGLOBAL("pepito.xml");
		AutomataVerificacion aut = new AutomataVerificacion(xmlafd);
		c.setAfd(aut);

		//Prueba de m�ltiples AFD para los objetos de tipo Canal
		XMLAFDReaderCANAL xmlafdcanal = new XMLAFDReaderCANAL("pepito.xml");
		c.agregarTipoAFD(xmlafdcanal, "tesis.Ejemplo_07.Canal");
		
		XMLEventBuilderReader xmleb = new XMLEventBuilderReader("pepito.xml"); 
		EventBuilder eb = new EventBuilder(xmleb);
		c.setEvb(eb);

		Listener listener = new Listener(c);
		
		String[] a = new String[1];
		a[0] = "tesis.Ejemplo_07.Modelo";
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