package tesis.Ejemplo_05;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import tesis.extensiones.AutomataVerificacion;
import tesis.extensiones.ContextoBusqueda;
import tesis.extensiones.Coordinador;
import tesis.extensiones.DFSearchTesis;
import tesis.extensiones.EventBuilder;
import tesis.extensiones.Listener;
import tesis.extensiones.XMLAFDReader;
import tesis.extensiones.XMLContextoBusquedaReader;
import tesis.extensiones.XMLEventBuilderReader;

public class ejecutarEjemplo {
	public static void main (String[] args) {
		// Path para acceder a este ejemplo, es el prefijo usado para leer los xmls
		// WinXP
		// String path = ".\\src\\tesis\\Ejemplo_05\\";
		// Linux
		String path = "./src/tesis/Ejemplo_05/";

		Coordinador c = new Coordinador();
		
		EventBuilder eb = new EventBuilder(new XMLEventBuilderReader(path + "Events.xml"));
		c.setEvb(eb);

		c.setContexto(new ContextoBusqueda(new XMLContextoBusquedaReader(path + "ProblemContext.xml", eb)));
		c.setModoContexto();

		c.setAfd(new AutomataVerificacion(new XMLAFDReader(path + "ProblemProperty.xml", eb)));

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