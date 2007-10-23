package tesis.CasoDeEstudio_simple;

class Persona implements Runnable {
	private String id;
	private String tabifier;
	private ControladorAscensor controlador;
	private ModeloV2 modelo;
	
	Persona (String id) {
		this.id = id;
	}

	public void controlador(ControladorAscensor c) {
		controlador = c;
	}

	/**
	 * Vinculamos el monitor a la persona para sincronizar la finalizacion de la ejecucion
	 * @param modelo
	 */
	public void modelo(ModeloV2 modelo) {
		this.modelo = modelo;
	}

	public void run() {
		runPersona();
	}

	private void runPersona() {
		Ascensor[] as = controlador.ascensores();

		if (id == "p1") {
			controlador.solicitudAscensor(as[0], 1);
			Helper.esperar(2);
			controlador.solicitudAscensor(as[0], 4);
			Helper.esperar(4);
			controlador.solicitudAscensor(as[0], 2);
		}
		if (id == "p2") {
			controlador.solicitudAscensor(as[1], 1);
			while (as[1].piso()!=1) {
				Helper.esperar(1);
			}
			Helper.esperar(8);
			controlador.solicitudAscensor(as[1], 3);
			Helper.esperar(1);
			controlador.solicitudAscensor(as[1], 9);
		}
		if (id == "pMatrix") {
			controlador.solicitudPisoAbajo(10);
			Helper.esperar(2);
			controlador.solicitudPisoArriba(3);
			Helper.esperar(2);
			controlador.solicitudPisoArriba(0);
		}
		
		//msgs("modelo.terminoPersona()");

		// Indicamos al modelo la finalizacion de los requests
		modelo.terminoPersona();
	}

	// Helper
	/**
	 * Impresion de mensajes internos
	 * Nota: comentar las llamadas en verificacion pq genera muchos estados
	 */
	private void msgs(String s) {
		System.out.println("Thread " + Thread.currentThread() + tabifier+"Persona "+id+" -> " + s);
	}

	/**
	 * Tabulador para impresion de mensajes
	 * @param s
	 */
	public void setTab(String s) {
		tabifier = s;
	}
}