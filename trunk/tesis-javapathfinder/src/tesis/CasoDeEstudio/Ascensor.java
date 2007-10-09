package tesis.CasoDeEstudio;

import java.util.Vector;

enum Direccion {arriba, abajo};
enum Puerta {abierta, cerrada}
enum Estado {detenido, enMovimiento}

class Ascensor implements Runnable {
	ControladorAscensor controladorAscensor;
	String id;
	private Direccion direccion;
	private Puerta puerta;
	private Estado estado;
	private String tabifier;
	private int piso; 
	private Boolean terminar;
	
	public int piso() {
		return piso;
	}

	Ascensor (String id) {
		terminar = false;
		this.id = id;
		puerta = Puerta.abierta;
		direccion = Direccion.arriba;
		estado = Estado.detenido;
		piso = 0;
	}

//	synchronized
	public void detenerse() {
		msgs("detenerse");
		estado = Estado.detenido;
	}

	synchronized
	public void subir() {
		msgs("subir");
		estado = Estado.enMovimiento;
		direccion = Direccion.arriba;
		notify();
	}

	synchronized
	public void bajar() {
		msgs("bajar");
		estado = Estado.enMovimiento;
		direccion = Direccion.abajo;
		notify();
	}

//	synchronized
	public void abrirPuertas () {
		msgs("abrirPuertas");
		puerta = Puerta.abierta;
	}

//	synchronized
	public void cerrarPuertas () {
		msgs("cerrarPuertas");
		puerta = Puerta.cerrada;
	}

	synchronized public void run() {
		try {
			while (!terminar) {
				msgs("wait()");
				wait();
				if (!terminar)
					moverse();
			}
		} catch (InterruptedException e) {
			msgs("Interrupted");
		}
	}

	private void moverse() {
		while (estado==Estado.enMovimiento) {
			esperar(1);
			piso += (direccion==Direccion.arriba?1:-1);
		
			controladorAscensor.estoyEn(this, piso);
		}
	}
	
//	synchronized
	private void esperar (int seg) {
		try {
			Thread.sleep(seg*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Helper
	private void msgs(String s) {
		System.out.println(tabifier+"Ascensor " + id + " -> " + s);
	}

	public void setTab(String s) {
		tabifier = s;
	}

	public Direccion direccion() {
		return direccion;
	}
	
	public String toString() {
		return "Ascensor " + id;
	}

	public Estado estado() {
		return estado;
	}

	public void controladorAscensor(ControladorAscensor ca) {
		controladorAscensor = ca;
	}

	synchronized 
	public void terminar() {
		terminar = true;
		msgs("terminar");
		this.notify();
	}
}