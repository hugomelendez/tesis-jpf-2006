package tesis.Ejemplo_05;
import tesis.extensiones.AutomataVerificacion;
import tesis.extensiones.Evento;

public class Automata extends AutomataVerificacion {
	final String OPEN = "OPEN";
	final String CLOSE = "CLOSE";
	
	public Automata (){
		super();
	}
	
	//TODO: Falta definir los nombres de los Strings desde el XML, ahora están hardcoded
	public void consumir (Evento e) {
		switch (estadoActual) {
			case 0:
				if (e.sos(OPEN)) {
					estadoActual = 1;
				} else if (e.sos(CLOSE)) {
					estadoActual = 999;
					blnEstadoFinal = true;
				}
				break;
			case 1:
				if (e.sos(OPEN)) {
					estadoActual = 2;
				} else if (e.sos(CLOSE)) {
					estadoActual = 0;
				}
				break;
			case 2:
				if (e.sos(OPEN)) {
					estadoActual = 3;
					//OJO, esto es para probar que se cuelgue con 3 threads!!!
					blnEstadoFinal = true;
				} else if (e.sos(CLOSE)) {
					estadoActual = 1;
				}
				break;
			case 3:
				if (e.sos(OPEN)) {
					estadoActual = 999;
					blnEstadoFinal = true;
				} else if (e.sos(CLOSE)) {
					estadoActual = 2;
				}
				break;
			default:
				break;
		}
	}
}
