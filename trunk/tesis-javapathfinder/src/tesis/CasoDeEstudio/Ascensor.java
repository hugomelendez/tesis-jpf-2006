package tesis.CasoDeEstudio;

enum Direccion {arriba, abajo};
enum Puerta {abierta, cerrada}
enum Estado {detenido, enMovimiento}

class Ascensor implements Runnable {
	Object monitor;
	ControladorAscensor controladorAscensor;
	private String id;
	private Direccion direccion;
	private Puerta puerta;
	private Estado estado;
	private String tabifier;
	private int piso; 
	private Boolean terminar;
	
	public int piso() {
		return piso;
	}

	Ascensor (String id, Object mon) {
		monitor = mon;
		terminar = false;
		this.id = id;
		puerta = Puerta.abierta;
		direccion = Direccion.arriba;
		estado = Estado.detenido;
		piso = 0;
	}

	public void detenerse() {
		//msgs("detenerse");
		estado = Estado.detenido;
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
			estado = Estado.enMovimiento;
			direccion = Direccion.arriba;
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
			estado = Estado.enMovimiento;
			direccion = Direccion.abajo;
			this.notify();
		}
	}

	public void abrirPuertas () {
		//msgs("abrirPuertas");
		puerta = Puerta.abierta;
	}

	public void cerrarPuertas () {
		//msgs("cerrarPuertas");
		puerta = Puerta.cerrada;
	}

	//synchronized
	public void run() {
//		synchronized (monitor) {
			while (!terminar) {
				//msgs("wait()");
				synchronized (this) {
					try {
						if (!terminar)
							this.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (!terminar)
					moverse();
			}
//		}
	}

	private void moverse() {
		while (estado==Estado.enMovimiento) {
			Helper.esperar(1);
			piso += (direccion==Direccion.arriba?1:-1);
		
			controladorAscensor.estoyEn(this, piso);
		}
	}

	// Helper
	private void msgs(String s) {
		//System.out.println("Thread " + Thread.currentThread() + tabifier + "Ascensor " + id + " -> " + s);
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

//	synchronized 
	public void terminar() {
		terminar = true;
		//msgs("terminar");
		synchronized (this) {
			this.notify();
		}
	}
}