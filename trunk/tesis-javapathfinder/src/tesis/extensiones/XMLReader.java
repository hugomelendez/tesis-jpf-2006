package tesis.extensiones;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public abstract class XMLReader {
	Document document;

	/**
	 * Common constructor
	 * @param file
	 */
	public XMLReader(String file) {
		try {
			openFile(file);
		} catch (DocumentException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Open XML file
	 *
	 * @param fileName
	 * @throws DocumentException
	 */
	protected void openFile(String fileName) throws DocumentException {
		SAXReader reader = new SAXReader();
        document = reader.read(fileName);
	}

}
