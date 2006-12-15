package tesis.extensiones;


/**
 * Clase para mantener un template de la propiedad de un tipo (clase) determinado
 */
public class TypeStatePropertyTemplate extends PropertyTemplate {
	/**
	 * Tipo asociado a la propiedad
	 */
	String type;

	public TypeStatePropertyTemplate (String type, XMLAFDReader xmlafd) {
		this.type = type;
		estadoInicial = xmlafd.estadoInicial(type);
		transiciones = xmlafd.transiciones(type);
		estadosFinales = xmlafd.estadosFinales(type);
	}
}
