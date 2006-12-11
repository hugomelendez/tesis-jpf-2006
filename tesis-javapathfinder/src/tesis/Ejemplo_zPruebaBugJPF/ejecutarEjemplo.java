package tesis.Ejemplo_zPruebaBugJPF;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import tesis.extensiones.*;

public class ejecutarEjemplo {
	public static void main (String[] args) {
		Coordinador c = new Coordinador();

		XMLContextoBusquedaReaderBug xmlpre = new XMLContextoBusquedaReaderBug("pepito.xml");
		ContextoBusqueda pre = new ContextoBusqueda(xmlpre);
		c.setContexto(pre);
		c.setModoContexto();
		
		XMLAFDReaderGLOBAL xmlafd = new XMLAFDReaderGLOBAL("pepito.xml");
		AutomataVerificacion aut = new AutomataVerificacion(xmlafd);
		c.setAfd(aut);

		//Prueba de múltiples AFD para los objetos de tipo Canal
		XMLAFDReaderCANAL xmlafdcanal = new XMLAFDReaderCANAL("pepito.xml");
		c.agregarTipoAFD(xmlafdcanal, "tesis.Ejemplo_zPruebaBugJPF.Canal");
		
		XMLEventBuilderReaderBug xmleb = new XMLEventBuilderReaderBug("pepito.xml"); 
		EventBuilder eb = new EventBuilder(xmleb);
		c.setEvb(eb);

		Listener listener = new Listener(c);
		
		String[] a = new String[1];
		a[0] = "tesis.Ejemplo_zPruebaBugJPF.Modelo";
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