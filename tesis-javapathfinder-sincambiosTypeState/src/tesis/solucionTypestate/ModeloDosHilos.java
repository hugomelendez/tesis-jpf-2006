package tesis.solucionTypestate;

class Hilo2 implements Runnable {

	void mA(){
		int a;
//		for (int i=0; i<3; ++i) {
			Hilo2 b1 = new Hilo2();
			b1.mB();
//		}
	}

	void mB() {
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
		Hilo2 o3 = new Hilo2();

		Thread t1 = new Thread(o1);
		Thread t2 = new Thread(o2);
		Thread t3 = new Thread(o3);

		t1.start();
		t2.start();
		t3.start();
	}
}
