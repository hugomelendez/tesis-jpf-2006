package tesis.test;

import tesis.extensiones.EventBuilder;
import tesis.extensiones.XMLContextoBusquedaReader;
import tesis.extensiones.XMLEventBuilderReader;

public class TestXMLContextoBusquedaReader {

	public static void main(String[] args) {
		// WinXP
		 String path = ".\\src\\tesis\\Ejemplo_05\\";
		// Linux
//		String path = "./src/tesis/Ejemplo_05/";
		EventBuilder eb = new EventBuilder(new XMLEventBuilderReader(path + "Events.xml"));
		XMLContextoBusquedaReader a = new XMLContextoBusquedaReader(path + "ProblemContext.xml", eb);

		System.out.println("Estado inicial: " + a.estadoInicial());
		System.out.println("Estados finales: " + a.estadoFinal());
		System.out.println("Transiciones: " + a.transiciones());
		
	}
}
