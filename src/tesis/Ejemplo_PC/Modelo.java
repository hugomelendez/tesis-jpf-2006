package tesis.Ejemplo_PC;

public class Modelo {
	Consumidor consumidor;
	private boolean hayProduccion;
	
	public Modelo(Consumidor c) {
		consumidor = c;
		hayProduccion = true;
	}
	
	synchronized
	public void coordinarFinEjecución() {
		while (hayProduccion) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		consumidor.terminar();
	}
	
	synchronized
	public void terminoProduccion() {
		hayProduccion = false;
		this.notify();
	}

	public static void main(String[] args) {
		Consumidor c = new Consumidor();
		Modelo m = new Modelo(c);
		
		Productor p = new Productor(c, m);
		Thread t1 = new Thread(c);
		Thread t2 = new Thread(p);

		t1.start();
		t2.start();
		
		m.coordinarFinEjecución();
	}
}