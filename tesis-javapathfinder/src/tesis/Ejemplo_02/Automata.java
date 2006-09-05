package tesis.Ejemplo_02;

import tesis.extensiones.AutomataVerificacion;
import tesis.extensiones.Evento;

public class Automata implements AutomataVerificacion {
	private int estadoActual;
	private boolean blnEstadoFinal = false;
	
	public Automata (){
		estadoActual = 0;
	}
	
	public void irAEstado(int est){
		System.out.println(" Aut bktrk estado " + est);
		estadoActual = est;
	}

	public boolean estadoFinal(){
		return blnEstadoFinal;
	}

	public void hlpFwd(int est) {
		System.out.println(" Aut fwd estado " + est);
		
		switch (est) {
			case 10:
			case 11:
			case 12:
			case 13:
			case 14:
			case 15:
				System.out.println(" ++ Aut fwd estado HOJA \r\n\r\n");
				break;
			default: break;
		}
		estadoActual = est;
	}
	
	public void consumir (Evento e) {
		EventoDeEjemplo2 eventoEj2 = (EventoDeEjemplo2) e;
		
		switch (estadoActual) {
			case 0:
				if (eventoEj2.esA()) {
					hlpFwd(1);
				} else if (eventoEj2.esB()) {
					hlpFwd(2);
				} else if (eventoEj2.esC()) {
					hlpFwd(3);
				}
				break;
			case 1:
				if (eventoEj2.esA()) {
					hlpFwd(1);
				} else if (eventoEj2.esB()) {
					hlpFwd(4);
				} else if (eventoEj2.esC()) {
					hlpFwd(5);
				}
				break;
			case 2:
				if (eventoEj2.esA()) {
					hlpFwd(6);
				} else if (eventoEj2.esB()) {
					hlpFwd(2);
				} else if (eventoEj2.esC()) {
					hlpFwd(7);
				}
				break;
			case 3:
				if (eventoEj2.esA()) {
					hlpFwd(9);
				} else if (eventoEj2.esB()) {
					hlpFwd(8);
				} else if (eventoEj2.esC()) {
					hlpFwd(3);
				}
				break;
			case 4:
				if (eventoEj2.esA()) {
					hlpFwd(4);
				} else if (eventoEj2.esB()) {
					hlpFwd(4);
				} else if (eventoEj2.esC()) {
					hlpFwd(10);
				}
				break;
			case 5:
				if (eventoEj2.esA()) {
					hlpFwd(5);
				} else if (eventoEj2.esB()) {
					hlpFwd(11);
				} else if (eventoEj2.esC()) {
					hlpFwd(5);
				}
				break;
			case 6:
				if (eventoEj2.esA()) {
					hlpFwd(6);
				} else if (eventoEj2.esB()) {
					hlpFwd(6);
				} else if (eventoEj2.esC()) {
					hlpFwd(12);
				}
				break;
			case 7:
				if (eventoEj2.esA()) {
					hlpFwd(13);
				} else if (eventoEj2.esB()) {
					hlpFwd(7);
				} else if (eventoEj2.esC()) {
					hlpFwd(7);
				}
				break;
			case 8:
				if (eventoEj2.esA()) {
					hlpFwd(14);
				} else if (eventoEj2.esB()) {
					hlpFwd(8);
				} else if (eventoEj2.esC()) {
					hlpFwd(8);
				}
				break;
			case 9:
				if (eventoEj2.esA()) {
					hlpFwd(9);
				} else if (eventoEj2.esB()) {
					blnEstadoFinal = true;
					hlpFwd(15);
				} else if (eventoEj2.esC()) {
					hlpFwd(9);
				}
				break;
			default:
				break;
		}
	}

	public int getEstadoActual(){
		return estadoActual;
	}
}
