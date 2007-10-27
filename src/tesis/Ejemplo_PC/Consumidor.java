package tesis.Ejemplo_PC;

class Consumidor implements Runnable {
	int piso;
	private boolean terminar;

	Consumidor () {
		piso = 0;
		terminar = false;
	}

	public synchronized void run() {
		while (!terminar) {
			try {
				//if (!hay())
					wait();

				sacar();
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}

	private boolean hay() {
		return (piso!=0);
	}

	public synchronized void poner() {
		//piso = 1;
		notify();
	}

	private synchronized void sacar() {
		//piso = 0;
	}

	synchronized
	public void terminar() {
		terminar = true;
		this.notify();
	}
}
