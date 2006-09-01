package tesis.pruebaListeners;

class ModeloThread implements Runnable {
	/**
	 * El nombre del Evento
	 */
	String nombreEvento;

	/**
	 * Referencia al otro Thread
	 */
	ModeloThread other;

	public ModeloThread(String name) {
		this.nombreEvento = name;
	}

	public void run() {
		System.out.println("EVENTO " + nombreEvento);
	}
}

public class ModeloThreadsAB {
	public static void main(String[] args) {
		ModeloThread o1 = new ModeloThread("A");
		ModeloThread o2 = new ModeloThread("B");
		ModeloThread o3 = new ModeloThread("C");

		o1.other = o2;
		o2.other = o1;

		Thread t1 = new Thread(o1);
		Thread t2 = new Thread(o2);
		Thread t3 = new Thread(o3);

		t1.start();
		t2.start();
		t3.start();

	}
}
