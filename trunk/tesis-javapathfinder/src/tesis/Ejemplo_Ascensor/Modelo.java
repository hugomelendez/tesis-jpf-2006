package tesis.Ejemplo_Ascensor;

public class Modelo {
	public static void main(String[] args) {
		Ascensor a = new Ascensor();
		Thread t1 = new Thread(a);

		ControladorAscensor ca = new ControladorAscensor(a);
		Thread t2 = new Thread(ca);

		Persona p = new Persona(ca);
		Thread t3 = new Thread(p);

		t1.start();
		t2.start();
		t3.start();
	}
}