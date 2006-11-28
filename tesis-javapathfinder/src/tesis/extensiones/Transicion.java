package tesis.extensiones;

public class Transicion {
	int estadoDesde;
	int estadoHacia;
	Evento evento;
	
	public Transicion (int estD, int estH, Evento evt) {
		estadoDesde = estD;
		estadoHacia = estH;
		evento = evt;
	}

	/**
	 * @return the estadoDesde
	 */
	public int estadoDesde() {
		return estadoDesde;
	}

	/**
	 * @return the estadoHacia
	 */
	public int estadoHacia() {
		return estadoHacia;
	}

	/**
	 * @return the evento
	 */
	public Evento evento() {
		return evento;
	}

	@Override
	public String toString() {
		return "Trns: " + estadoDesde + " " + estadoHacia + " " + evento;
	}

	
}
