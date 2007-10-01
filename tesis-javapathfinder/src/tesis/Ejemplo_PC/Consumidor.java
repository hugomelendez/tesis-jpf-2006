package tesis.Ejemplo_PC;

class Consumidor implements Runnable {
	int piso;

	Consumidor () {
		piso = 0;
	}

	public synchronized void run() {
		while (true) {
			try {
				if (!hay())
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
		piso = 1;
		notify();
	}

	private synchronized void sacar() {
		piso = 0;
	}
}
