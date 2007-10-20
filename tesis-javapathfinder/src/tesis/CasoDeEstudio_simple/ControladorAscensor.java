package tesis.CasoDeEstudio_simple;

import java.util.Hashtable;

/**
 * Modelo de Controlador de ascensores
 * Contiene una coleccion de {@link Ascensor}es a los cuales controla
 * Recibe pedidos desde los distintos pisos hechos por {@link Persona}s
 * Recibe pedidos desde dentro de los ascensores hechos por {@link Persona}s
 *  
 * @author Roberto
 */
class ControladorAscensor implements Runnable {
	private Object miMonitor;
	
	// Altura del "edificio"
	private final static int ALTURA = 4;
	private Ascensor[] ascensores;

	// Solicitudes de un ascensor
	private Hashtable<Ascensor, Boolean[]> solicitudesPorAscensor;

	private String tabifier;
	private boolean terminar;

	public ControladorAscensor(Ascensor[] a){
		miMonitor = new Object();
		
		terminar = false;
		ascensores = a;
		
		solicitudesPorAscensor = new Hashtable<Ascensor, Boolean[]>();
		for (int i=0;i<ascensores.length;i++) {
			Boolean[] b = new Boolean[ALTURA+1];
			for (int j=0; j<b.length; j++) {
				b[j] = false;
			}
			solicitudesPorAscensor.put(ascensores[i], b);
			ascensores[i].controladorAscensor(this);
		}
	}

	public void run() {
		try {
			while (!terminar) {
				//msgs("wait()");
				/* FIXME: 
				 * Se sincroniza el acceso al monitor local para hacer el wait
				 * Normalmente, este synchronize deberia ir fuera del while
				 * En este momento sacar el sync fuera del while da un deadlock
				 * Se decide dejar aca pq no nos interesa que la semantica del Modelo sea perfecta sino simplemente que funcione
				 */
				synchronized (miMonitor) {
					if (!terminar)
						miMonitor.wait();
				}
				if (!terminar)
					atenderSolicitudes();
			}
		} catch (InterruptedException e) {
			//msgs("Interrupted");
		}
	}

	/**
	 * Finalizacion del trabajo del ascensor
	 */
	public void terminar() {
		//msgs("terminar");
		/*
		 * Sincroniza el acceso pq es usado por
		 * a. Thread del Controlador en el run
		 * b. Thread del modelo para finalizar la ejecucion
		 */
		synchronized (miMonitor) {
			terminar = true;
			miMonitor.notify();
		}
	}

	/**
	 * Recibe una solicitud desde un piso para ir hacia arriba 
	 * @param pisoDesde donde llega la solicitud 
	 */
	public void solicitudPisoArriba(int pisoDesde) {
		//msgs("solicitudPisoArriba desde piso " + pisoDesde);
		
		Ascensor ascensorDesignado = null;
		for (int i=0;i<ascensores.length && ascensorDesignado==null;i++) {
			Ascensor a = ascensores[i];

			// TODO: verificar si sincronizar el acceso al ascensor genera menos estados
			if (a.piso() <= pisoDesde && a.estaSubiendo()) {
				ascensorDesignado = a;
			}
		}

		if (ascensorDesignado==null) {
			ascensorDesignado = ascensores[0];
		}

		//msgs("solicitudPisoArriba ASIGNADO " + ascensorDesignado + " a piso " + pisoDesde);
		solicitudAscensor(ascensorDesignado, pisoDesde);
	}
	
	/**
	 * Recibe una solicitud desde un piso para ir hacia abajo 
	 * @param pisoDesde donde llega la solicitud 
	 */
	public void solicitudPisoAbajo(int pisoDesde) {
		//msgs("solicitudPisoAbajo desde piso " + pisoDesde);

		Ascensor ascensorDesignado = null;
		for (int i=0;i<ascensores.length && ascensorDesignado==null;i++) {
			Ascensor a = ascensores[i];
			
			// TODO: verificar si sincronizar el acceso al ascensor genera menos estados
			if (a.piso() >= pisoDesde && a.estaBajando()) {
				ascensorDesignado = a;
			}
		}
		
		if (ascensorDesignado==null) {
			ascensorDesignado = ascensores[0];
		}
		
		//msgs("solicitudPisoAbajo ASIGNADO " + ascensorDesignado + " a piso " + pisoDesde);
		solicitudAscensor(ascensorDesignado, pisoDesde);
	}

	/**
	 * Recibe una solicitud desde dentro de un ascensor para ir a un piso determinado
	 * 
	 * @param a {@link Ascensor} 
	 * @param pisoDestino
	 */
	public void solicitudAscensor(Ascensor a, int pisoDestino) {
		//msgs("solicitudAscensor " + a + " a piso " + pisoDestino);

		setSolicitud(a, pisoDestino, true);
		//msgs("notify@solicitudAscensor");

		/*
		 * Luego de recibir la solicitud, avisamo al thread del Controlador para que atienda el pedido
		 */
		synchronized (miMonitor) {
			miMonitor.notify();
		}
	}
	
	/**
	 * Revisa las colas de pedidos de cada ascensor y:
	 *  si existe alguno Y si el ascensor est� detenido, lo arranca en la direcci�n correspondiente 
	 */
	private void atenderSolicitudes() {
		for (int i=0;i<ascensores.length;i++) {
			Ascensor a = ascensores[i];
			/*
			 * Lo correcto es hacer mover al ascensor cuando no tiene nada para hacer
			 * 
			 * Si esta parado, lo puede haber parado el controlador en medio de una subida/bajada,
			 * con el fin de atender una solicitud
			 * 
			 * TODO: encontrar una forma de expresar que el ascensor esta idle y no simplemente parado
			 */
			if (a.estaParado()) {
				if (haySolicitudArriba(a, a.piso()))
					a.subir();
				else if (haySolicitudAbajo(a, a.piso()))
					a.bajar();
			}
		}
	}

	/**
	 * El {@link ControladorAscensor} resuelve que un {@link Ascensor} debe parar para atender una solicitud
	 * La solicitud se considera atendida una vez que se cierran las puertas
	 * 
	 * @param a
	 * @param piso
	 */
	private void atenderSolicitudPiso(Ascensor a, int piso) {
		//msgs("atenderSolicitudPiso " + a + " en piso " + piso);
		a.detenerse();
		a.abrirPuertas();
		Helper.esperar(2);
		setSolicitud(a, piso, false);
		a.cerrarPuertas();
	}

	// Helper
	public Ascensor[] ascensores() {
		/*
		 * No es necesario sincronizar este acceso puesto q el unico
		 * acceso (luego del constructor), es de lectura desde Persona
		 */
		return ascensores;
	}

	/**
	 * Desde el Thread de {@link Ascensor} se le avisa al {@link ControladorAscensor} en que piso esta
	 * 
	 * @param a {@link Ascensor}
	 * @param piso
	 */
	public void estoyEn(Ascensor a, int piso) {
		//msgs(a + " estoyEn " + piso);
		if (haySolicitudEn(a, piso)) {
			atenderSolicitudPiso(a, piso);

			/**
			 * Finalizado el atender solicitudes, se revisa si el ascensor debe continuar su viaje
			 * Si no existen solicitudes en la direccion que tenia o la contraria, 
			 * no es necesario <code>detener</code> al Ascensor pq ya esta parado
			 */
			if (a.estaSubiendo()) {
				if (haySolicitudArriba(a, piso)) {
					a.continuarSubir();
				} else if (haySolicitudAbajo(a, piso)) {
					a.continuarBajar();
				}
			} else {
				if (haySolicitudAbajo(a, piso)) {
					a.continuarBajar();
				} else if (haySolicitudArriba(a, piso)) {
					a.continuarSubir();
				}
			}

		}

		if (piso==0) {
			if (haySolicitudArriba(a, piso))
				a.continuarSubir();
			else if (!a.estaParado())
				a.detenerse();
		}
		else if (piso==ALTURA) {
			if (haySolicitudAbajo(a, piso))
				a.continuarBajar();
			else if (!a.estaParado())
				a.detenerse();
		}
	}

	/**
	 * Verifica si el {@link Ascensor} tiene alguna solicitud para ir hacia abajo
	 * @param a {@link Ascensor}
	 * @param piso desde donde empezamos a revisar
	 * @return
	 */
	private boolean haySolicitudAbajo(Ascensor a, int piso) {
		Boolean[] s = solicitudesPorAscensor(a);
		Boolean ret = false;
		for (int i=piso; i>=0 && !ret;i--) {
			ret = s[i];
		}
		return ret;
	}

	/**
	 * Verifica si el {@link Ascensor} tiene alguna solicitud para ir hacia arriba
	 * @param a {@link Ascensor}
	 * @param piso desde donde empezamos a revisar
	 * @return
	 */
	private boolean haySolicitudArriba(Ascensor a, int piso) {
		Boolean[] s = solicitudesPorAscensor(a);
		Boolean ret = false;
		for (int i=piso; i<=ALTURA && !ret;i++) {
			ret = s[i];
		}
		return ret;
	}
	private Boolean haySolicitudEn(Ascensor a, int piso) {
		return (solicitudesPorAscensor(a))[piso];
	}

	/**
	 * Verifica si hay solcitudes que atender en algun {@link Ascensor}
	 * @return
	 */
	public Boolean haySolicitudes() {
		Boolean ret = false;
	
		for (int i=0;i<ascensores.length && !ret;i++) {
			ret = haySolicitudes(ascensores[i]);
		}
		return ret;
	}

	/**
	 * Verifica si hay solicitudes para un {@link Ascensor}
	 * @return
	 */
	private Boolean haySolicitudes(Ascensor a) {
		return (haySolicitudArriba(a, 0));
	}

	/**
	 * Setters de las solicitudes para un {@link Ascensor}
	 * 
	 * @param a
	 * @param piso
	 * @param b
	 */
	private void setSolicitud(Ascensor a, int piso, Boolean b) {
		//Esto es para que 2 solicitudes que llegan en el mismo instante
		//sincronicen sus modificaciones al array individualmente
		synchronized (miMonitor) {
			Boolean[] s = solicitudesPorAscensor(a);
			s[piso] = b;
			//msgs("solicitudes de " +a+ ": "+ printSolicitudes(s));
		}
	}

	// Helper
	/**
	 * Impresion de mensajes internos
	 * Nota: comentar las llamadas en verificacion pq genera muchos estados
	 */
	private void msgs(String s) {
		System.out.println("Thread " + Thread.currentThread() /*+ tabifier*/+"Controlador -> " + s);
	}

	// Helper
	public void setTab(String s) {
		tabifier = s;
	}

	//Helper
	private Boolean[] solicitudesPorAscensor (Ascensor a) {
		Boolean[] s;
		/*
		 * El acceso sincronizado se debe a que Personas setean las solicitudes y 
		 * Controlador debe poder tener toda la informacion antes de decidir
		 */
		synchronized (miMonitor) {
			s = solicitudesPorAscensor.get(a);
		}
		return s;
	}
	
	//Helper
	/**
	 * Imprime las solicitudes de un ascensor
	 */
	private String printSolicitudes(Boolean[] s) {
		String ret = "[";
		for (int i=0; i<=ALTURA;i++) {
			ret += (s[i] ? i : "") + ",";
		}
		return ret+"]";
	}
}