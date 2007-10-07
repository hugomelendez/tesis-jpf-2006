package tesis.CasoDeEstudio;

public class Modelo {
	public static void main(String[] args) {
		Ascensor a1 = new Ascensor("a1");
		a1.setTab("\t");
		Thread t1 = new Thread(a1);
		t1.start();

		Ascensor a2 = new Ascensor("a2");
		a2.setTab("\t\t");
		Thread t2 = new Thread(a2);
		t2.start();

		Ascensor[] ascensores = new Ascensor[2];
		ascensores[0] = a1;
		ascensores[1] = a2;

		ControladorAscensor ca = new ControladorAscensor(ascensores);
		ca.setTab("");
		Thread t3 = new Thread(ca);
		t3.start();

		Persona p = new Persona("p1");
		p.setTab("\t\t\t");
		p.controlador(ca);
		Thread t4 = new Thread(p);
		t4.start();

		Persona p2 = new Persona("p2");
		p2.setTab("\t\t\t\t");
		p2.controlador(ca);
		Thread t5 = new Thread(p2);
		t5.start();
	}
}