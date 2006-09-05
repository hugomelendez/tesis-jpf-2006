package tesis.Ejemplo_02;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import tesis.extensiones.DFSearchTesis;

public class ejecutarEjemplo {
	public static void main (String[] args) {
		Automata aut = new Automata();
		ListenerConEstado listener = new ListenerConEstado(aut);

		String[] a = new String[1];
		a[0] = "tesis.ModelosVarios.ModeloConTresHilos";
	    Config conf = JPF.createConfig(a);

	    // usamos nuestra busqueda
	    conf.setProperty("search.class","tesis.extensiones.DFSearchTesis");

	    JPF jpf = new JPF(conf);
	    jpf.addSearchListener(listener);
	    jpf.addVMListener(listener);
	    
	    // agregamos nuestro listener de forma especial para la tesis
	    ((DFSearchTesis)jpf.search).addTesisListener(listener);

	    System.out.println("---------------- JPF started");
	    jpf.run();
	    System.out.println("---------------- JPF terminated");
	}
}