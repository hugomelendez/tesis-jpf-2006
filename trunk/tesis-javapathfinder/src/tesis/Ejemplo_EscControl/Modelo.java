package tesis.Ejemplo_EscControl;

class Canal {
	private int opens = 0;
	private int writes = 0;

	public void open() { 
		opens++;
	}
	
	public void close() {
		opens--;
	}

	public void write() {
		writes++;
	}
}

class Hilo implements Runnable {
	private Canal c;
	
	Hilo (Canal canal) {
		c = canal;
	}

	public void run() {
		c.open();
		c.write();
		c.close();
	}
}

public class Modelo {
	public static void main(String[] args) {
		Canal c = new Canal();
		
		Hilo o;
		Thread t;
		
		for (int i=0;i<3;i++) {
			o = new Hilo(c);
			t = new Thread(o);
			t.start();
		}
	}
}
