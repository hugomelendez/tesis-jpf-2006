package tesis.test;

import tesis.extensiones.XMLEventBuilderReader;

public class TestXMLEventBuilderReader {

	public static void main(String[] args) {
		// WinXP
		// String path = ".\\src\\tesis\\Ejemplo_05\\";
		// Linux
		String path = "./src/tesis/Ejemplo_05/";
		XMLEventBuilderReader e = new XMLEventBuilderReader(path + "Events.xml");
		System.out.println(e.eventos());
	}
}
