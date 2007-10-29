package tesis.CasoDeEstudio_simple.ModeloP3A2;

import tesis.CasoDeEstudio_simple.*;

class ModeloPropiedad13 extends Modelo {

	public static void main(String[] args) {
		ModeloPropiedad13 m = new ModeloPropiedad13();

		Ascensor a1 = new Ascensor("a1");
		Thread tA1 = new Thread(a1);

		Ascensor a2 = new Ascensor("a2");
		Thread tA2 = new Thread(a2);

		Ascensor[] ascensores = new Ascensor[Modelo.CANT_ASCENSORES];
		ascensores[0] = a1;
		ascensores[1] = a2;

		ControladorAscensorPropiedad13 ca = new ControladorAscensorPropiedad13(ascensores);
		Thread tCA = new Thread(ca);

		Persona p1 = new Persona("p1");
		p1.controlador(ca);
		Thread tP1 = new Thread(p1);

		Persona p2 = new Persona("p2");
		p2.controlador(ca);
		Thread tP2 = new Thread(p2);

		Persona pMatrix = new Persona("pMatrix");
		pMatrix.controlador(ca);
		Thread tPMatrix = new Thread(pMatrix);

		m.ascensores(ascensores);
		m.controlador(ca);
		p1.modelo(m);
		p2.modelo(m);
		pMatrix.modelo(m);

		tA1.start();
		tA2.start();
		tCA.start();
		tP1.start();
		tP2.start();
		tPMatrix.start();
		
		m.coordinarFinEjecución();
	}
}