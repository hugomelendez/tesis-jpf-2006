package tesis.CasoDeEstudio_simple;

/**
 * Modelo de Ascensor
 * Recibe mensajes de un {@link ControladorAscensor} que le indica subir/bajar 
 * o detenerse para atender solicitudes
 * 
 * Implementa {@link Runnable} para poder representar distintos ascensores que puedan existir en un modelo
 * 
 * @author Roberto
 */
class Ascensor implements Runnable {
	ControladorAscensor controladorAscensor;
	private String id;
	private int direccion;
	private int puerta;
	private int estado;
	private String tabifier;
	private int piso; 
	private Boolean terminar;
	
	Ascensor (String id) {
		terminar = false;
		this.id = id;
		puerta = 1;
		direccion = 1;
		estado = 0;
		piso = 0;
	}

	public void run() {
		try {
			/*
			 * Se sincroniza esta seccion pq
			 * a. el thread del Modelo setea terminar para finalizar la ejecucion
			 * b. el thread del ascensor usa terminar para saber cuando dejar de "dormir" el thread y salir
			 */
			synchronized (this) {
				while (!terminar) {
					//msgs("wait()");
					this.wait();

					/*
					 * Cuando salimos del wait puede ser por:
					 * a. el controlador le indico subir/bajar para atender solicitudes
					 * b. el modelo le indica al ascensor que termine
					 */
					if (!terminar)
						moverse();
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ciclo de movimiento del {@link Ascensor}
	 * Aumenta/disminuye el piso y le comunica al {@link ControladorAscensor} en que piso esta
	 */
	private void moverse() {
		while (estaEnMovimiento()) {
			Helper.esperar(1);
			synchronized (this) {
				piso += (estaSubiendo()?1:-1);
				controladorAscensor.estoyEn(this, piso);
			}
		}
	}

	// Setters
	/**
	 * Enviado por el {@link ControladorAscensor} para parar el movimiento del {@link Ascensor} 
	 */
	public void detenerse() {
		//msgs("detenerse");
		synchronized (this) {
			estado = 0;
		}
	}

	/**
	 * Se indica al {@link Ascensor} que suba
	 */
	public void subir() {
		//msgs("subir");

		/*
		 * Se sincroniza esta seccion pq
		 * a. el thread del Controlador utiliza este metodo para arrancar el ascensor
		 * b. el thread del ascensor consulta el estado y la direccion
		 * 
		 * Notifica al thread del ascensor para indicarle que tiene algo para hacer
		 */
		synchronized (this) {
			estado = 1;
			direccion = 1;
			this.notify();
		}
	}

	/**
	 * idem subir
	 */
	public void bajar() {
		//msgs("bajar");
		/*
		 * Se sincroniza esta seccion pq
		 * a. el thread del Controlador utiliza este metodo para arrancar el ascensor
		 * b. el thread del ascensor consulta el estado y la direccion
		 * 
		 * Notifica al thread del ascensor para indicarle que tiene algo para hacer
		 */
		synchronized (this) {
			estado = 1;
			direccion = 0;
			this.notify();
		}
	}

	/**
	 * Utilizado cuando el {@link Ascensor} esta viajando y es frenado en un piso para atender una solicitud
	 */
	public void continuarSubir() {
		//msgs("continuarSubir");
		estado = 1;
		direccion = 1;
	}

	/**
	 * idem continuarSubir
	 */
	public void continuarBajar() {
		//msgs("continuarBajar");
		estado = 1;
		direccion = 0;
	}

	public void abrirPuertas () {
		//msgs("abrirPuertas");
		synchronized (this) {
			puerta = 1;
		}
	}

	public void cerrarPuertas () {
		//msgs("cerrarPuertas");
		synchronized (this) {
			puerta = 0;
		}
	}

	/**
	 * Finalizacion del trabajo del ascensor
	 */
	public void terminar() {
		//msgs("terminar");

		/*
		 * Sincroniza el acceso pq es usado por
		 * a. Thread del Ascensor en el run
		 * b. Thread del modelo para finalizar la ejecucion
		 * 
		 * Notifica al thread del ascensor para indicarle que se despierte y finalice la ejecucion
		 */
		synchronized (this) {
			terminar = true;
			this.notify();
		}
	}
	// END Setters
	// Getters
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
	
	public int piso() {
		synchronized (this) {
			return piso;
		}
	}

	public void controladorAscensor(ControladorAscensor ca) {
		synchronized (this) {
			controladorAscensor = ca;
		}
	}
	// End Getters
	
	// Helpers
	/**
	 * Impresion de mensajes internos
	 * Nota: comentar las llamadas en verificacion pq genera muchos estados
	 */
	private void msgs(String s) {
		System.out.println("Thread " + Thread.currentThread() + tabifier + "Ascensor " + id + " -> " + s);
	}

	/**
	 * Tabulador para impresion de mensajes
	 * @param s
	 */
	public void setTab(String s) {
		tabifier = s;
	}

	public String toString() {
		return "Ascensor " + id;
	}
	// End Helpers
}