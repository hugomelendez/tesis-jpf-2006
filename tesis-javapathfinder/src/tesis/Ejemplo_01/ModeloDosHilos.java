package tesis.Ejemplo_01;

class Hilo2 implements Runnable {

	void mA(){
		int a;
	}

	public void run () {
		mA();
	}
}

public class ModeloDosHilos {

	public static void main (String[] args) {
		Hilo2 o1 = new Hilo2();
		Hilo2 o2 = new Hilo2();

		Thread t1 = new Thread(o1);
		Thread t2 = new Thread(o2);

		t1.start();
		t2.start();
	}
}
