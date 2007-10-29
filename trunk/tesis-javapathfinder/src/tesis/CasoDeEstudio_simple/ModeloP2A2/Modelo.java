package tesis.CasoDeEstudio_simple.ModeloP2A2;

import tesis.CasoDeEstudio_simple.*;

class Modelo extends tesis.CasoDeEstudio_simple.Modelo {
	private final static int CANT_PERSONAS = 2;
	public final static int CANT_ASCENSORES = 2;
	
	public Modelo() {
		super();
	}
	
	public static void main(String[] args) throws InterruptedException {
		Modelo m = new Modelo();

		Ascensor a1 = new Ascensor("a1");
		Thread tA1 = new Thread(a1);

		Ascensor a2 = new Ascensor("a2");
		Thread tA2 = new Thread(a2);

		Ascensor[] ascensores = new Ascensor[CANT_ASCENSORES];
		ascensores[0] = a1;
		ascensores[1] = a2;

		ControladorAscensor ca = new ControladorAscensor(ascensores);
		Thread tCA = new Thread(ca);

		Persona p1 = new Persona("p1");
		p1.controlador(ca);
		Thread tP1 = new Thread(p1);

		Persona p2 = new Persona("p2");
		p2.controlador(ca);
		Thread tP2 = new Thread(p2);

//		Persona pMatrix = new Persona("pMatrix");
//		pMatrix.controlador(ca);
//		Thread tPMatrix = new Thread(pMatrix);

		m.ascensores(ascensores);
		m.controlador(ca);
		p1.modelo(m);
		p2.modelo(m);
//		pMatrix.modelo(m);

		tA1.start();
		tA2.start();
		tCA.start();
		tP1.start();
		tP2.start();
//		tPMatrix.start();
		
		m.coordinarFinEjecución();
	}
}