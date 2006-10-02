package tesis.Ejemplo_03;
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
					estadoActual = 2;
					blnEstadoFinal = true;
				}
				break;
			case 1:
				if (eventoOC.esOPEN()) {
					estadoActual = 3;
					blnEstadoFinal = true;
				} else if (eventoOC.esCLOSE()) {
					estadoActual = 0;
				}
				break;
			default:
				break;
		}
	}
}
