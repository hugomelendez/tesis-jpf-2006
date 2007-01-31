package tesis.Ejemplo_10_Lider;

public class ModeloRS implements Runnable {
	private void init() {
		System.out.println("init!");
	}
	private void end() {
		System.out.println("end!");
	}

	public static void main(String[] args) {
		ModeloRS m = new ModeloRS();
		Thread t = new Thread(m);
		t.start();
	}

	public void run() {
		init();
		for (int i=0;i<100000;i++);
		end();
	}
}