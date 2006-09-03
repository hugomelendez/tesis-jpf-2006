package tesis.ModelosVarios;

public class ModeloConTresHilos {
	class Hilo implements Runnable {
		/**
		 * El nombre del Evento
		 */
		String nombreEvento;

		/**
		 * Referencia al otro Thread
		 */
		Hilo other;

		public Hilo(String name) {
			this.nombreEvento = name;
		}

		public void run() {
			System.out.println("EVENTO " + nombreEvento);
		}
	}


	public static void main(String[] args) {
		Hilo o1 = new Hilo("A");
		Hilo o2 = new Hilo("B");
		Hilo o3 = new Hilo("C");

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
