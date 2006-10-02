package tesis.Ejemplo_05;
import tesis.extensiones.AutomataVerificacion;
import tesis.extensiones.Evento;

public class Automata extends AutomataVerificacion {
	public Automata (){
		super();
	}
	
	public void consumir (Evento e) {
		EventoOpenClose eventoOC = (EventoOpenClose) e;
		
		switch (estadoActual) {
			case 0:
				if (eventoOC.esOPEN()) {
					estadoActual = 1;
				} else if (eventoOC.esCLOSE()) {
					estadoActual = 999;
					blnEstadoFinal = true;
				}
				break;
			case 1:
				if (eventoOC.esOPEN()) {
					estadoActual = 2;
				} else if (eventoOC.esCLOSE()) {
					estadoActual = 0;
				}
				break;
			case 2:
				if (eventoOC.esOPEN()) {
					estadoActual = 3;
					//OJO, esto es para probar que se cuelgue con 3 threads!!!
					blnEstadoFinal = true;
				} else if (eventoOC.esCLOSE()) {
					estadoActual = 1;
				}
				break;
			case 3:
				if (eventoOC.esOPEN()) {
					estadoActual = 999;
					blnEstadoFinal = true;
				} else if (eventoOC.esCLOSE()) {
					estadoActual = 2;
				}
				break;
			default:
				break;
		}
	}
}
