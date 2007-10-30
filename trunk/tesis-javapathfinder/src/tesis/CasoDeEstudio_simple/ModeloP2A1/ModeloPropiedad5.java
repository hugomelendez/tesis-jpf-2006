package tesis.CasoDeEstudio_simple.ModeloP2A1;

import tesis.CasoDeEstudio_simple.*;

class ModeloPropiedad5 extends Modelo {

	public static void main(String[] args) {
		ModeloPropiedad5 m = new ModeloPropiedad5();

		Ascensor a1 = new Ascensor("a1");
		Thread tA1 = new Thread(a1);

		Ascensor[] ascensores = new Ascensor[Modelo.CANT_ASCENSORES];
		ascensores[0] = a1;

		ControladorAscensorPropiedad5 ca = new ControladorAscensorPropiedad5(ascensores);
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