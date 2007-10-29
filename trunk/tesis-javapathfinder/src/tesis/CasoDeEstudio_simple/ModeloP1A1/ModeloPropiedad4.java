package tesis.CasoDeEstudio_simple.ModeloP1A1;

import tesis.CasoDeEstudio_simple.*;

class ModeloPropiedad4 extends Modelo {

	public static void main(String[] args) {
		ModeloPropiedad4 m = new ModeloPropiedad4();

		Ascensor a1 = new Ascensor("a1");
		Thread tA1 = new Thread(a1);

		Ascensor[] ascensores = new Ascensor[Modelo.CANT_ASCENSORES];
		ascensores[0] = a1;

		ControladorAscensorPropiedad4 ca = new ControladorAscensorPropiedad4(ascensores);
		Thread tCA = new Thread(ca);

		Persona p1 = new Persona("p1");
		p1.controlador(ca);
		Thread tP1 = new Thread(p1);

		m.ascensores(ascensores);
		m.controlador(ca);
		p1.modelo(m);

		tA1.start();
		tCA.start();
		tP1.start();

		m.coordinarFinEjecución();
	}
}