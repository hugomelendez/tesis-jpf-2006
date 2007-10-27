package tesis.CasoDeEstudio_simple;

class ModeloPropiedad13 {

	public static void main(String[] args) {
		ModeloV2 m = new ModeloV2();

		Ascensor a1 = new Ascensor("a1");
		Thread tA1 = new Thread(a1);

		Ascensor[] ascensores = new Ascensor[ModeloV2.CANT_ASCENSORES];
		ascensores[0] = a1;

		ControladorAscensorPropiedad13 ca = new ControladorAscensorPropiedad13(ascensores);
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