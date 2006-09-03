package tesis.ModelosVarios;

public class ModeloDosHilos {
	class Hilo implements Runnable {

	void mA(){
		int a;
	}

	public void run () {
		mA();
	 }
	}

	public static void main (String[] args) {
		Hilo o1 = new Hilo();
		Hilo o2 = new Hilo();

		Thread t1 = new Thread(o1);
		Thread t2 = new Thread(o2);

		t1.start();
		t2.start();
	}
}
