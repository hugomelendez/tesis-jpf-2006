package tesis.CasoDeEstudio_simple;

//enum Direccion {arriba, abajo}; //{arriba 1, abajo 0}
//enum Puerta {abierta, cerrada}; //{abierta 1, cerrada 0}
//enum Estado {detenido, enMovimiento}; //{detenido 0, enMovimiento 1}

class Ascensor implements Runnable {
	Object monitor;
//	Object miMonitor;
	ControladorAscensor controladorAscensor;
	private String id;
//	private Direccion direccion;
//	private Puerta puerta;
//	private Estado estado;
	private int direccion;
	private int puerta;
	private int estado;
//	private String tabifier;
	private int piso; 
	private Boolean terminar;
	
	public int piso() {
		return piso;
	}

	Ascensor (String id, Object mon) {
		monitor = mon;
		terminar = false;
		this.id = id;
//		puerta = Puerta.abierta;
//		direccion = Direccion.arriba;
//		estado = Estado.detenido;
		puerta = 1;
		direccion = 1;
		estado = 0;
		piso = 0;
	}

	public void detenerse() {
		//msgs("detenerse");
//		estado = Estado.detenido;
		synchronized (this) {
			estado = 0;
		}
	}

	/**
	 * No nos interesa sincronizar los accesos a estado y direccion, porque en nuestro modelo
	 * existe un unico thread que ejecuta estos metodos, ya que existe un unico controlador que
	 * comanda todos los ascensores
	 * 
	 * Por ahora, volvemos a poner synch en todo el metodo
	 */
//	synchronized
	public void subir() {
		//msgs("subir");
		synchronized(this) {
//			estado = Estado.enMovimiento;
//			direccion = Direccion.arriba;
			estado = 1;
			direccion = 1;
			this.notify();
		}
	}

	/**
	 * idem subir
	 */
//	synchronized
	public void bajar() {
		//msgs("bajar");
		synchronized(this) {
//			estado = Estado.enMovimiento;
//			direccion = Direccion.abajo;
			estado = 1;
			direccion = 0;
			this.notify();
		}
	}

	public void abrirPuertas () {
		//msgs("abrirPuertas");
		synchronized (this) {
//			puerta = Puerta.abierta;
			puerta = 1;
		}
	}

	public void cerrarPuertas () {
		//msgs("cerrarPuertas");
		synchronized (this) {
//			puerta = Puerta.cerrada;
			puerta = 0;
		}
	}

	//synchronized
	public void run() {
		synchronized (this) {
			while (!terminar) {
				//msgs("wait()");
				try {
					if (!terminar)
						this.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (!terminar)
					moverse();
			}
		}
	}

	private void moverse() {
//			while (estado==Estado.enMovimiento) {
			while (estaEnMovimiento()) {
				Helper.esperar(1);
//				piso += (direccion==Direccion.arriba?1:-1);
				synchronized (this) {
					piso += (estaSubiendo()?1:-1);
					controladorAscensor.estoyEn(this, piso);
				}
			}
	}

	// Helper
//	private void msgs(String s) {
//		//System.out.println("Thread " + Thread.currentThread() + tabifier + "Ascensor " + id + " -> " + s);
//	}
//
//	public void setTab(String s) {
//		tabifier = s;
//	}

	/**
	 * Debe ser sincronizado para que no haya conflictos con el moverse (si hay más de 1 a un ascensor)
	 */
//	public Direccion direccion() {
//		synchronized (this) {
//			return direccion;
//		}
//	}
//	public Estado estado() {
//		synchronized (this) {
//			return estado;
//		}
//	}
	private int direccion() {
		synchronized (this) {
			return direccion;
		}
	}
	private int estado() {
		synchronized (this) {
			return estado;
		}
	}

	public boolean estaSubiendo() {
		return direccion()==1;
	}
	public boolean estaBajando() {
		return direccion()==0;
	}
	
	public boolean estaParado() {
		return estado()==0;
	}
	public boolean estaEnMovimiento() {
		return estado()==1;
	}
	
	public String toString() {
		return "Ascensor " + id;
	}

	public void controladorAscensor(ControladorAscensor ca) {
		synchronized (this) {
			controladorAscensor = ca;
		}
	}

//	synchronized 
	public void terminar() {
		//msgs("terminar");
		synchronized (this) {
			terminar = true;
			this.notify();
		}
	}
}