package tesis.Ejemplo_PC;

class Productor implements Runnable {
	Consumidor con;
	Productor (Consumidor c) {
		con = c;
	}

	public void run() {
		while (true) {
			con.poner();
		}
	}

}