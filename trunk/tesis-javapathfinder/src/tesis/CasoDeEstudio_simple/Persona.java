package tesis.CasoDeEstudio_simple;

class Persona implements Runnable {
	private String id;
//	private String tabifier;
	private ControladorAscensor controlador;
	private ModeloV2 modelo;
	
	Persona (String id) {
		this.id = id;
	}

	public void controlador(ControladorAscensor c) {
		controlador = c;
	}

	public void modelo(ModeloV2 modelo) {
		this.modelo = modelo;
	}

	public void run() {
		Ascensor[] as = controlador.ascensores();

		if (id == "p1") {
			controlador.solicitudAscensor(as[0], 1);
//
//			Helper.esperar(1);
//			controlador.solicitudAscensor(as[0], 4);
//
//			Helper.esperar(2);
//			controlador.solicitudAscensor(as[0], 2);
		}

		if (id == "p2") {
/*			controlador.solicitudAscensor(as[1], 1);
			while (as[1].piso()!=1) {
				esperar(1);
			}
			Helper.esperar(8);
			controlador.solicitudAscensor(as[1], 3);
			Helper.esperar(1);
			controlador.solicitudAscensor(as[1], 9);*/
		}
		if (id == "pMatrix") {
/*			controlador.solicitudPisoAbajo(10);
			Helper.esperar(2);
			controlador.solicitudPisoArriba(3);
			Helper.esperar(2);
			controlador.solicitudPisoArriba(0);*/
		}

		//msgs("modelo.terminoPersona()");
		modelo.terminoPersona();
	}

	// Helper
//	private void msgs(String s) {
//		//System.out.println("Thread " + Thread.currentThread() + tabifier+"Persona "+id+" -> " + s);
//	}
//
//	public void setTab(String s) {
//		tabifier = s;
//	}
}