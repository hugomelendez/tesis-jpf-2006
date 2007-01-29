package tesis.Ejemplo_10_Lider;

import java.util.Stack;

class Mensaje {
	public static final int MSG_ONE = 1;
	public static final int MSG_WINNER = 2;

	int tipo;
	String contenido;

	public Mensaje(int t, String c) {
		tipo = t;
		contenido = c;
	}

	public String getContenido() {
		return contenido;
	}

	public void setContenido(String contenido) {
		this.contenido = contenido;
	}

	public int getTipo() {
		return tipo;
	}

	public void setTipo(int tipo) {
		this.tipo = tipo;
	}
	
}

class Canal {
	// cantidad de mensajes q soporta el canal
	int buffer = 10;
	Stack<Mensaje> mensajes;

	Canal () {
		mensajes = new Stack<Mensaje>();
	}

	public void send (Mensaje m) {
		mensajes.push(m);
		// TODO 
		// NOTA1: Ver esto, deberiamos levantar el bloqueo del receive pero esto usa native
		// NOTA2: probar un lock exclusivo mas casero opero q funque en concurrencia
		//this.notify();
	}

	public Mensaje receive () {
		while (mensajes.empty()) {
/*			try {
				// TODO ver NOTA1 y NOTA2 anteriores 
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}*/
		}

		return mensajes.pop();
	}

}

class Nodo implements Runnable {
	int id;
	Canal chan_in, chan_out;

	boolean Active = true;
	boolean know_winner = false;

	public Nodo (int nr, Canal in, Canal out) {
		id = nr;
		chan_in = in;
		chan_out = out;

		// Inicio la ronda de msgs
		chan_out.send(new Mensaje(Mensaje.MSG_ONE, Integer.toString(this.id)));
	}

	// algoritmo
	private void searchLider() throws InterruptedException {

		while (true) {
			Mensaje m = chan_in.receive();
			String nr = m.getContenido();

			if (m.getTipo() == Mensaje.MSG_WINNER) {

				chan_out.send(new Mensaje(Mensaje.MSG_WINNER, nr));
				break;

			} else if (m.getTipo() == Mensaje.MSG_ONE) {

				if (Active) {
					int nrAsInt = Integer.parseInt(nr);

					if (nrAsInt > this.id) {

						System.out.println("Nodo " + this.id + ": Desactivado");
						Active = false;

						// Si no sirvo mas soy un dummy node y fwd lo q vino
						chan_out.send(m);

					} else if (nrAsInt == this.id) {
						winner();
						break;

					} else if (nrAsInt < this.id) {
						chan_out.send(new Mensaje(Mensaje.MSG_ONE, Integer.toString(this.id)));
					}
				} else {
					// Sino estoy Activo es un dummy node y fwd lo q viene
					chan_out.send(m);
				}
			}
		}
	}
	
	private void winner() {
		chan_out.send(new Mensaje(Mensaje.MSG_WINNER, Integer.toString(this.id)));
		System.out.println("Nodo " + this.id + ": yo soy el LEADER(1)!!!");
	}

	public void run() {
		// aca va el algoritmo
		try {
			searchLider();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

public class Modelo implements Runnable {
	private static final int CANT_NODOS = 3;

	private void init() {
		System.out.println("init!");
	}
	private void end() {
		System.out.println("end!");
	}

	public static void main(String[] args) {
		Modelo m = new Modelo();
		Thread t = new Thread(m);
		t.start();
	}

	public void run() {
		Nodo[] nodos  = new Nodo[CANT_NODOS];
		Canal[] canales = new Canal[CANT_NODOS];
		Thread[] threads = new Thread[CANT_NODOS];;

		for (int i=0; i<CANT_NODOS; i++) {
			canales[i] = new Canal();
		}

		// marcamos el inicio de la busqueda
		init();

		for (int i=0; i<CANT_NODOS; i++) {
			nodos[i] = new Nodo(i, canales[i % CANT_NODOS], canales[(i+1) % CANT_NODOS]);
			threads[i] = new Thread(nodos[i]);
			threads[i].start();
		}

		// Esto no funciona, verificar otras formas ?
		// wait manual para impedir que el thread cero (Modelo.main) sea el primero en terminar
//		while (true) {
//			boolean allDone=true;
//			for (Thread t : threads) {
//				if (t.isAlive()) {
//					allDone=false;
////					System.out.println(t + " " + t.isAlive());
//				}
//			}
//			if (allDone)
//				break;
//		}

		// marcamos el fin de la busqueda
		end();
	}

}
