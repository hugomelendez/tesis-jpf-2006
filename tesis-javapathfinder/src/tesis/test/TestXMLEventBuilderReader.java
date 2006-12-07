package tesis.test;

import tesis.extensiones.XMLEventBuilderReader;

// TODO Levantar los datos del XML
public class TestXMLEventBuilderReader {

	public TestXMLEventBuilderReader(String file) {
	}

	public static void main(String[] args) {
		XMLEventBuilderReader e = new XMLEventBuilderReader("C:\\Documents and Settings\\hmelendez\\tesis\\tesis-javapathfinder\\src\\tesis\\Ejemplo_05\\Events.xml");
		System.out.println(e.eventos());
	}
}
