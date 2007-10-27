package tesis.Ejemplo_PC;

class Productor implements Runnable {
	Consumidor con;
	Modelo modelo;
	Productor (Consumidor c, Modelo m) {
		con = c;
		modelo = m;
	}

	public void run() {
		for (int i = 0; i < 1; i++) {
			con.poner();
			esperar(2);
		}
		modelo.terminoProduccion();
	}

	private static void esperar (int seg) {
		try {
			Thread.sleep(seg*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}