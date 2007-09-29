package tesis.Ejemplo_Ascensor;

class Persona implements Runnable {
	ControladorAscensor controlA;
	private int pisoActual;
	private int pisoDestino;
	
	Persona (ControladorAscensor c) {
		pisoActual = 0;
		pisoDestino = Ascensor.ALTURA-1;
		controlA = c;
	}

	public void setPiso(int p) {
		pisoActual = p;
	}
	
	public void pisoDestino(int p) {
		pisoDestino = p;
	}
	
	public void run() {
		Ascensor a = controlA.solicitudSubir(pisoActual);

		//Esperamos a q el ascensor llegue
		while (!a.enPiso(pisoActual));
		a.apretarBoton(pisoDestino);
		System.out.println("APRETE BOTON");
	}
}
