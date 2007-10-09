package tesis.CasoDeEstudio;

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

	public void modelo(ModeloV2 modelo) {
		this.modelo = modelo;
	}

	public synchronized void run() {
		Ascensor[] as = controlador.ascensores();

		if (id == "p1") {
			controlador.solicitudAscensor(as[0], 3);
			while (as[0].piso()!=3) {
				esperar(1);
			}
/*			esperar(4);
			controlador.solicitudAscensor(as[0], 2);
			esperar(3);
			controlador.solicitudAscensor(as[0], 6);*/
		}

		if (id == "p2") {
			controlador.solicitudAscensor(as[1], 1);
			while (as[1].piso()!=1) {
				esperar(1);
			}
/*			esperar(8);
			controlador.solicitudAscensor(as[1], 3);
			esperar(1);
			controlador.solicitudAscensor(as[1], 9);*/
		}
		if (id == "pMatrix") {
/*			controlador.solicitudPisoAbajo(10);
			esperar(2);
			controlador.solicitudPisoArriba(3);
			esperar(2);
			controlador.solicitudPisoArriba(0);*/
		}

		synchronized(modelo) {
			msgs("modelo.notify()");
			modelo.notify();
		}
	}

	// Helper
	private void msgs(String s) {
		System.out.println(tabifier+"Persona "+id+" -> " + s);
	}

	public void setTab(String s) {
		tabifier = s;
	}

	private void esperar (int seg) {
		try {
			Thread.sleep(seg*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}