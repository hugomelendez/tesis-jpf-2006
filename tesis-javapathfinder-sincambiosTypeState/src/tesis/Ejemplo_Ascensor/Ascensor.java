package tesis.Ejemplo_Ascensor;

import java.util.Vector;

enum Piso {pb, uno , dos, tres, cuatro, cinco, seis, siete, ocho, nueve, diez};
enum Direccion {arriba, abajo};
enum Puerta {abierta, cerrada}
enum Estado {parado, bajando, subiendo}

class Ascensor implements Runnable {
	//Se considera la PB como piso 0, por ende el piso m�s alto es ALTURA-1
	public static final int ALTURA = 3;
	private int piso;
	private Direccion direccion;
	private Puerta puerta;
	private Estado estado;
	private Vector<Boolean> solicitudes;
	private String tabifier;

	Ascensor () {
		piso = 2;
		puerta = Puerta.abierta;
		direccion = Direccion.arriba;
		inicializarSolicitudes();
	}

	synchronized
	private void inicializarSolicitudes() {
		solicitudes = new Vector<Boolean>(ALTURA);
		for (int i=0; i<ALTURA; i++) {
			//OJO, ac� no se puede llamar a limpiarSolicitudEn
			solicitudes.add(i, false);
		}
	}

	synchronized 
	private void limpiarSolicitudEn(int p) {
		solicitudes.set(p, false);
	}

	synchronized 
	private void asignarSolicitudEn(int p) {
		solicitudes.set(p, true);
	}

	synchronized 
	private Boolean haySolicitudEn(int p) {
		return (solicitudes.get(p));
	}

	synchronized
	public void run() {
		while (true) {
			try {
				if (!solicitudesPendientes())
					wait();

				int i = proximaSolicitud();
				irA(i);

			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}

	synchronized
	private boolean solicitudesPendientes() {
		Boolean ret = false;
		for (int i = 0; i<ALTURA && !ret; i++) {
			ret = haySolicitudEn(i);
		}
		return ret;
	}

	/**
	 * Busca la proxima solicitud del piso actual hacia arriba, sino hay, retorna ALTURA
	 */
	private int proximaSolicitudArriba() {
		int ret;
		for (ret = piso; ret<ALTURA && !haySolicitudEn(ret); ret++);
		return ret;
	}

	/**
	 * Busca la proxima solicitud del piso actual hacia abajo, sino hay, retorna -1
	 */
	synchronized
	private int proximaSolicitudAbajo() {
		int ret;
		for (ret = piso; ret>=0 && !haySolicitudEn(ret); ret--);
		return ret;
	}

	/**
	 * Devuelve la proxima solicitud a atender
	 * La precondici�n es que haya una solicitud pendiente
	 */ 
	synchronized
	private int proximaSolicitud() {
		int ret;

		if (direccion == Direccion.arriba) {
			ret = proximaSolicitudArriba();

			//Si no encontr� una pr�xima solicitud para arriba, busca para abajo
			if (ret >= ALTURA) {
				ret = proximaSolicitudAbajo();
			}
		} else {
			ret = proximaSolicitudAbajo();

			//Si no encontr� una pr�xima solicitud para abajo, busca para arriba
			if (ret < 0) {
				ret = proximaSolicitudArriba();
			}
		}

		//Por la precondici�n
		assert(ret>=0 && ret<ALTURA);

		return ret;
	}

	synchronized
	private void abrirPuertas () {
//		msgs("abrirPuertas");
		puerta = Puerta.abierta;
	}

	synchronized
	private void cerrarPuertas () {
//		msgs("cerrarPuertas");
		puerta = Puerta.cerrada;
	}

	synchronized
	private void pasarPor(int p){
//		msgs("Pasa por " + p);
		esperar(1);
		piso = p;
	}

	synchronized
	private void subir(int p){
		for (int i = piso+1; i <= p; i++){
			pasarPor(i);
			if (haySolicitudEn(i)) {
				atenderSolicitud(i);
			}
		}
	}

	synchronized
	private void bajar(int p){
		for (int i = piso-1; i >= p; i--){
			pasarPor(i);
			if (haySolicitudEn(i)) {
				atenderSolicitud(i);
			}
		}
	}

	synchronized
	public void solicitudA (int p) {
//		msgs("Solicitud NUEVA, piso " + p);
		asignarSolicitudEn(p);
		notify();
	}

	synchronized
	private void irA (int p) {
		if (p > piso){
			arrancar (Direccion.arriba);
			subir(p);
		} else if (p < piso) {
			arrancar (Direccion.abajo);
			bajar(p);
		} else {
			atenderSolicitud(p);
		}
	}

	synchronized
	private void arrancar (Direccion d) {
		if (direccion != d) {
			direccion = d;
//			msgs("Cambio direccion "+direccion);
		}

		if (direccion == Direccion.arriba){
			subiendo();
		} else {
			bajando();
		}
	}

	synchronized
	private void atenderSolicitud (int p) {
		parado();
		abrirPuertas();
		esperar(1);
		limpiarSolicitudEn(p);

		// este cerrar estaba antes en arrancar lo cual generaba un problema en la propiead "no arranques con las puertas abiertas"
		cerrarPuertas();
	}

	synchronized
	private void esperar (int seg) {
		try {
			Thread.sleep(seg*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	synchronized
	public void apretarBoton(int i) {
		msgs("Boton presionado: "+ i);
		solicitudA(i);
	}

	synchronized
	public boolean enPiso(int p) {
		return (piso==p);
	}

	synchronized
	private void parado() {
		estado = Estado.parado;
	}

	synchronized
	private void subiendo() {
		estado = Estado.subiendo;
	}

	synchronized
	private void bajando() {
		estado = Estado.bajando;
	}

	// Helper
	private void msgs(String s) {
//		System.out.println(tabifier+"Ascensor -> " + s);
	}

	synchronized
	public void setTab(String s) {
		tabifier = s;
	}
}
