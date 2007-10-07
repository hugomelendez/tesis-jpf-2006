package tesis.CasoDeEstudio;

public class Modelo {
	public static void main(String[] args) {
		Ascensor a1 = new Ascensor("a1");
		a1.setTab("");
		Thread t1 = new Thread(a1);
		t1.start();

		Ascensor[] ascensores = new Ascensor[1];
		ascensores[0] = a1;

		ControladorAscensor ca = new ControladorAscensor(ascensores);
		ca.setTab("\t\t");
		Thread t2 = new Thread(ca);
		t2.start();

		Persona p = new Persona("p1");
		p.setTab("\t");
		p.controlador(ca);
		Thread t3 = new Thread(p);
		t3.start();
//		Persona p2 = new Persona("p2");
//		p2.setTab("\t");
//		p2.controlador(ca);
//		Thread t4 = new Thread(p2);
//		t4.start();
	}
}