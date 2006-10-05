package tesis.Ejemplo_03;
import tesis.extensiones.AutomataVerificacion;
import tesis.extensiones.Evento;

public class Automata extends AutomataVerificacion {
	final String OPEN = "OPEN";
	final String CLOSE = "CLOSE";
	
	public Automata (){
		super();
	}
	
	public void consumir (Evento e) {
		switch (estadoActual) {
			case 0:
				if (e.sos(OPEN)) {
					estadoActual = 1;
				} else if (e.sos(CLOSE)) {
					estadoActual = 2;
					blnEstadoFinal = true;
				}
				break;
			case 1:
				if (e.sos(OPEN)) {
					estadoActual = 3;
					blnEstadoFinal = true;
				} else if (e.sos(CLOSE)) {
					estadoActual = 0;
				}
				break;
			default:
				break;
		}
	}
}
