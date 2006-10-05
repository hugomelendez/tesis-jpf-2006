package tesis.Ejemplo_03;

import tesis.extensiones.EventBuilder;
import tesis.extensiones.Evento;
import gov.nasa.jpf.jvm.bytecode.Instruction;


/** 
 */
public class EventBuilderEjemplo03 extends EventBuilder {
	public Evento eventoFrom(Instruction i) {
		String strNom = "";
		if (i.toString().contains("tesis.Ejemplo_05")) {
			if (i.toString().contains("invoke") && i.toString().contains("Canal.open") )
				strNom = "OPEN";
			else if (i.toString().contains("invoke") && i.toString().contains("Canal.close") )
				strNom = "CLOSE";
		}
		
		return new Evento(strNom);
	}
}
