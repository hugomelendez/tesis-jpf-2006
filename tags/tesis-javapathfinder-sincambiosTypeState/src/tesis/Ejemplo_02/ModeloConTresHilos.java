package tesis.Ejemplo_02;

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

class HiloA implements Runnable {
	/**
	 * El nombre del Evento
	 */
	String nombreEvento;

	/**
	 * Referencia al otro Thread
	 */
	Hilo other;

	public HiloA(String name) {
		this.nombreEvento = name;
	}

	public void run() {
		imprimirA();
	}
	
	private void imprimirA() {
		System.out.println("EVENTO " + nombreEvento + this.getClass().getName());		
	}
}

class HiloB implements Runnable {
	/**
	 * El nombre del Evento
	 */
	String nombreEvento;

	/**
	 * Referencia al otro Thread
	 */
	Hilo other;

	public HiloB(String name) {
		this.nombreEvento = name;
	}

	public void run() {
		imprimirB();
	}
	
	private void imprimirB() {
		System.out.println("EVENTO " + nombreEvento + this.getClass().getName());		
	}
}

class HiloC implements Runnable {
	/**
	 * El nombre del Evento
	 */
	String nombreEvento;

	public HiloC(String name) {
		this.nombreEvento = name;
	}

	public void run() {
		imprimirC();
	}
	
	private void imprimirC() {
		System.out.println("EVENTO " + nombreEvento + this.getClass().getName());		
	}
}

public class ModeloConTresHilos {

	public static void main(String[] args) {
		String s = new String("Hilo MAGIA");
		
		HiloA o1 = new HiloA(s);
		HiloB o2 = new HiloB(s);
		HiloC o3 = new HiloC(s);

		Thread t1 = new Thread(o1);
		Thread t2 = new Thread(o2);
		Thread t3 = new Thread(o3);

		t1.start();
		t2.start();
		t3.start();
	}
}
