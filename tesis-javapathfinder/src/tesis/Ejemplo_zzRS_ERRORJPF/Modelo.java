package tesis.Ejemplo_zzRS_ERRORJPF;

class Canal {
	private int opens = 0;

	public void open() { 
		opens++;
		//assert(opens <= 3);
	}
	
	public void close() {
		opens--;
	}

	public void write() {
		//opened = !!opened;
	}
}

class Hilo implements Runnable {
	private Canal c;
	
	Hilo (Canal canal) {
		c = canal;
	}

	public void run() {
		while (true) {
			c.open();
			c.write();
			c.close();
		}
	}
}

public class Modelo {
	public static void main(String[] args) {
		Canal c = new Canal();
		
		Hilo o1 = new Hilo(c);
		Hilo o2 = new Hilo(c);
		Hilo o3 = new Hilo(c);

		Thread t1 = new Thread(o1);
		Thread t2 = new Thread(o2);
		Thread t3 = new Thread(o3);

		t1.start();
		t2.start();
		t3.start();
	}
}
