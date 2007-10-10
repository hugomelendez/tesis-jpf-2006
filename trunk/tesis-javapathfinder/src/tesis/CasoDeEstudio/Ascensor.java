package tesis.CasoDeEstudio;

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

	public void detenerse() {
		msgs("detenerse");
		estado = Estado.detenido;
	}

	
	/**
	 * No nos interesa sincronizar los accesos a estado y direccion, porque en nuestro modelo
	 * existe un unico thread que ejecuta estos metodos, ya que existe un unico controlador que
	 * comanda todos los ascensores
	 */
	public void subir() {
		msgs("subir");
		estado = Estado.enMovimiento;
		direccion = Direccion.arriba;
		synchronized(this) {
			notify();
		}
	}

	/**
	 * idem subir
	 */
	public void bajar() {
		msgs("bajar");
		estado = Estado.enMovimiento;
		direccion = Direccion.abajo;
		synchronized(this) {
			notify();
		}
	}

	public void abrirPuertas () {
		msgs("abrirPuertas");
		puerta = Puerta.abierta;
	}

	public void cerrarPuertas () {
		msgs("cerrarPuertas");
		puerta = Puerta.cerrada;
	}

	public void run() {
		try {
			while (!terminar) {
				msgs("wait()");
				synchronized(this) {
					wait();
				}
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
	
	private void esperar (int seg) {
		try {
			Thread.sleep(seg*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Helper
	private void msgs(String s) {
		System.out.println("Thread " + Thread.currentThread() + tabifier+"Ascensor " + id + " -> " + s);
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