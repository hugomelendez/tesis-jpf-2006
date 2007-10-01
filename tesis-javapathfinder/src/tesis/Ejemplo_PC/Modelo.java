package tesis.Ejemplo_PC;

public class Modelo {
	public static void main(String[] args) {
		Consumidor c = new Consumidor();
		Productor p = new Productor(c);
		Thread t1 = new Thread(c);
		Thread t2 = new Thread(p);

		t1.start();
		t2.start();
	}
}