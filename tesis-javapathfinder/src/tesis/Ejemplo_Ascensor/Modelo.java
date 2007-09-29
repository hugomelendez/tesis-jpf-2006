package tesis.Ejemplo_Ascensor;

public class Modelo {
	public static void main(String[] args) {
		Ascensor a = new Ascensor();
		a.setTab("");
		Thread t1 = new Thread(a);

		ControladorAscensor ca = new ControladorAscensor(a);
		ca.setTab("\t\t\t");
		Thread t2 = new Thread(ca);

		Persona p = new Persona(1, ca);
		p.setTab("\t");
		Thread t3 = new Thread(p);
		Persona p2 = new Persona(2, ca);
		p2.setTab("\t\t");
		Thread t4 = new Thread(p2);

		t1.start();
		t2.start();
		t3.start();
		t4.start();
	}
}