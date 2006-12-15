package tesis.test;

import tesis.extensiones.EventBuilder;
import tesis.extensiones.XMLAFDReader;
import tesis.extensiones.XMLEventBuilderReader;

public class TestXMLAFDReader {

	public static void main(String[] args) {
		// WinXP
//		 String path = ".\\src\\tesis\\Ejemplo_05\\";
		// Linux
		String path = "./src/tesis/Ejemplo_05/";
		EventBuilder eb = new EventBuilder(new XMLEventBuilderReader(path + "Events.xml"));
		XMLAFDReader a = new XMLAFDReader(path + "ProblemProperty.xml", eb);

		System.out.println("Estado inicial: " + a.estadoInicial());
		System.out.println("Estados finales: " + a.estadosFinales());
		System.out.println("Transiciones: " + a.transiciones());
		
	}
}
