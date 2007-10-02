package tesis.Ejemplo_Ascensor;

import java.util.Random;

class Persona implements Runnable {
	ControladorAscensor controlA;
	Integer id;
	private int pisoActual;
	private int pisoDestino;
	private String tabifier;
	
	Persona (Integer i, ControladorAscensor c) {
		pisoActual = 0;
		pisoDestino = pisoActual;
		controlA = c;
		id = i;
	}

	public void pisoActual(int p) {
		pisoActual = p;
	}
	
	public void pisoDestino(int p) {
		pisoDestino = p;
	}
	
	public void moverse() {
		Ascensor a;

		if (pisoActual > pisoDestino) {
			a = controlA.solicitudBajar(pisoActual);
		} else {
			a = controlA.solicitudSubir(pisoActual);
		}

		//Esperamos a q el ascensor llegue
		while (!a.enPiso(pisoActual));

//		msgs("Desde p: " + pisoActual + " aprete boton p: " + pisoDestino);
		a.apretarBoton(pisoDestino);

		// cuando llegamos al destino nos cambiamos de piso
		while (!a.enPiso(pisoDestino));
		pisoActual = pisoDestino;
//		msgs("Llegue al piso: " + pisoActual);
	}

	/**
	 * Elegimos un nuevo piso al azar para visitar
	 */
	private void nuevoPiso() {
//		Random r = new Random();
//		pisoDestino(r.nextInt(Ascensor.ALTURA-1));
//		if (pisoActual == pisoDestino) {
//			pisoDestino = (pisoDestino+1) % Ascensor.ALTURA-1;
//		}

		//pisoDestino = (pisoDestino*2+1) % (Ascensor.ALTURA-1);
		pisoDestino = (pisoDestino+1) % (Ascensor.ALTURA);
//		msgs("Nuevo destino: "+ pisoDestino);
	}

	public synchronized void run() {
		// lo hacemos cambiar 3 veces de piso
//		for (int i=0; i++ < 4; ) {
//		int cont = 0;
		
		while (true) {
//			System.out.println("************************************ CONT " + (cont++));
			nuevoPiso();
			moverse();
		}
	}

	// Helper
	private void msgs(String s) {
//		System.out.println(tabifier+"Persona "+id+" -> " + s);
	}

	public void setTab(String s) {
		tabifier = s;
	}
}