package tesis.test;

import java.util.List;

import tesis.extensiones.EventBuilder;
import tesis.extensiones.XMLAFDReader;
import tesis.extensiones.XMLEventBuilderReader;
import tesis.extensiones.XMLException;

public class TestXMLAFDReader {

	public static void main(String[] args) throws XMLException {
		// WinXP
//		 String path = ".\\src\\tesis\\Ejemplo_05\\";
		// Linux
		String path = "./src/tesis/Ejemplo_09/";
		EventBuilder eb = new EventBuilder(new XMLEventBuilderReader(path + "Events.xml"));
		XMLAFDReader a = new XMLAFDReader(path + "ProblemProperty.xml", eb);

		if (a.hasGlobalProperties()) {
			System.out.println("Estado inicial: " + a.estadoInicial());
			System.out.println("Estados finales: " + a.estadosFinales());
			System.out.println("Transiciones: " + a.transiciones());
		}
		
		System.out.println("type state: " + a.hasTypeStateProperties());
		System.out.println("global state: " + a.hasGlobalProperties());
	}
}
