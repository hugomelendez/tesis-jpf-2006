package tesis.Ejemplo_10_Lider;

import java.util.Collection;
import java.util.Iterator;
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
		this.notify();
	}

	public Mensaje receive () {
		while (mensajes.empty()) {
			try {
				// TODO ver NOTA1 y NOTA2 anteriores 
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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
		String nr;

		while (true) {
			Mensaje m = chan_in.receive();
			nr = m.getContenido();

			if (m.getTipo() == Mensaje.MSG_WINNER) {

				chan_out.send(new Mensaje(Mensaje.MSG_WINNER, nr));
				break;

			} else if (m.getTipo() == Mensaje.MSG_ONE) {

				if (Active) {
					int nrAsInt = Integer.parseInt(nr);

					if (nrAsInt > this.id) {

						System.out.println(this.id + " LOST");
						Active = false;

						// Si no sirvo mas soy un dummy node y fwd lo q vino
						chan_out.send(m);

					} else if (nrAsInt == this.id) {

						chan_out.send(new Mensaje(Mensaje.MSG_WINNER, Integer.toString(this.id)));
						System.out.println(this.id + " LEADER!!");
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

public class Modelo {

	private static final int CANT_NODOS = 5;

	public static void main(String[] args) {
		Nodo[] nodos  = new Nodo[CANT_NODOS];
		Canal[] canales = new Canal[CANT_NODOS];
		Thread t;
		
		for (int i=0; i<CANT_NODOS; i++) {
			canales[i] = new Canal();
		}

		for (int i=0; i<CANT_NODOS; i++) {
			nodos[i] = new Nodo(i, canales[i % CANT_NODOS], canales[(i+1) % CANT_NODOS]);
			t = new Thread(nodos[i]);
			t.start();
		}

	}
}
