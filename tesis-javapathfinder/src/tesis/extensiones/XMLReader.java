package tesis.extensiones;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
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

	/**
	 * Devuelve el valor del attributo del elemento
	 * @param elem
	 * @param attTag
	 * @return
	 */
	protected String attFromElem(Element elem, String attTag) {
		return ((Attribute)elem.attribute(attTag)).getValue();
	}

	/**
	 * Devuelve el valor Integer del attributo del elemento
	 * @param elem
	 * @param attTag
	 * @return
	 */
	protected Integer intAttFromElem(Element elem, String attTag) {
		return new Integer(attFromElem(elem, attTag));
	}

}
