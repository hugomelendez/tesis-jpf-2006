package tesis.CasoDeEstudio;

import java.util.Hashtable;

class ControladorAscensor implements Runnable {
	private final static int ALTURA = 3;
	private Ascensor[] ascensores;
	
	//Botones dentro de un ascensor
	private Hashtable<Ascensor, Boolean[]> solicitudesPorAscensor;

	private String tabifier;
	private boolean terminar;

	public ControladorAscensor(Ascensor[] a){
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

	public void solicitudPisoArriba(int pisoDesde) {
		msgs("solicitudPisoArriba desde piso " + pisoDesde);
		
		Ascensor ascensorDesignado = null;
		for (int i=0;i<ascensores.length && ascensorDesignado==null;i++) {
			Ascensor a = ascensores[i];
			
			if (a.piso() <= pisoDesde && a.direccion()==Direccion.arriba) {
				ascensorDesignado = a;
			}
		}

		if (ascensorDesignado==null) {
			ascensorDesignado = ascensores[0];
		}

		msgs("solicitudPisoArriba ASIGNADO " + ascensorDesignado + " a piso " + pisoDesde);
		solicitudAscensor(ascensorDesignado, pisoDesde);
	}
	
	public void solicitudPisoAbajo(int pisoDesde) {
		msgs("solicitudPisoAbajo desde piso " + pisoDesde);

		Ascensor ascensorDesignado = null;
		for (int i=0;i<ascensores.length && ascensorDesignado==null;i++) {
			Ascensor a = ascensores[i];
			
			if (a.piso() >= pisoDesde && a.direccion()==Direccion.abajo) {
				ascensorDesignado = a;
			}
		}
		
		if (ascensorDesignado==null) {
			ascensorDesignado = ascensores[0];
		}
		
		msgs("solicitudPisoAbajo ASIGNADO " + ascensorDesignado + " a piso " + pisoDesde);
		solicitudAscensor(ascensorDesignado, pisoDesde);
	}
	
	public void solicitudAscensor(Ascensor a, int pisoDestino) {
		msgs("solicitudAscensor " + a + " a piso " + pisoDestino);

		//Es probable que, al no estar synchronized el notificar
		//2 threads lean la version incorrecta de notificar
		//y generen cant. innecesaria de this.notify() 	
		Boolean notificar = (!haySolicitudes(a));
		setSolicitud(a, pisoDestino, true);

		msgs("notify@solicitudAscensor");
		
		if (notificar) {
			synchronized(this) {
				this.notify();
			}
		}
	}
	
	private void atenderSolicitudPiso(Ascensor a, int piso) {
		msgs("atenderSolicitudPiso " + a + " en piso " + piso);
		a.detenerse();
		a.abrirPuertas();
		esperar(2);
		setSolicitud(a, piso, false);
		a.cerrarPuertas();
	}
	
	public void estoyEn(Ascensor a, int piso) {
		msgs(a + " estoyEn " + piso);
		if (haySolicitudEn(a, piso)) {
			atenderSolicitudPiso(a, piso);

			if (a.direccion()==Direccion.arriba) {
				if (haySolicitudArriba(a, piso)) {
					a.subir();
				} else if (haySolicitudAbajo(a, piso)) { 
					a.bajar();
//				} else {
//					a.detenerse();
				}
			} else {
				if (haySolicitudAbajo(a, piso)) { 
					a.bajar();
				} else if (haySolicitudArriba(a, piso)) {
					a.subir();
//				} else {
//					a.detenerse();
				}
			}
		}

		if (piso==0) {
			if (haySolicitudArriba(a, piso))
				a.subir();
			else if (a.estado()!=Estado.detenido)
				a.detenerse();
		}
		else if (piso==ALTURA) {
			if (haySolicitudAbajo(a, piso))
				a.bajar();
			else if (a.estado()!=Estado.detenido)
				a.detenerse();
		}
	}
	
	private boolean haySolicitudAbajo(Ascensor a, int piso) {
		Boolean[] s = solicitudesPorAscensor.get(a);
		Boolean ret = false;
		for (int i=piso; i>=0 && !ret;i--) {
			ret = s[i];
		}
		return ret;
	}

	private boolean haySolicitudArriba(Ascensor a, int piso) {
		Boolean[] s = solicitudesPorAscensor.get(a);
		Boolean ret = false;
		for (int i=piso; i<=ALTURA && !ret;i++) {
			ret = s[i];
		}
		return ret;
	}

	private void setSolicitud(Ascensor a, int piso, Boolean b) {
		//Esto es para que 2 solicitudes que llegan en el mismo instante
		//sincronicen sus modificaciones al array individualmente
		synchronized(this) {
			Boolean[] s = solicitudesPorAscensor.get(a);
			s[piso] = b;
			msgs("solicitudes de " +a+ ": "+ printSolicitudes(s));
		}
	}
	
	private Boolean haySolicitudEn(Ascensor a, int piso) {
		return (solicitudesPorAscensor.get(a))[piso];
	}

	private void esperar (int seg) {
		try {
			Thread.sleep(seg*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {
			while (!terminar) {
				msgs("wait()");
				synchronized (this){
					wait();
				}
				if (!terminar)
					atenderSolicitudes();
			}
		} catch (InterruptedException e) {
			msgs("Interrupted");
		}
	}
	
	public Boolean haySolicitudes() {
		Boolean ret = false;
	
		for (int i=0;i<ascensores.length && !ret;i++) {
			ret = haySolicitudArriba(ascensores[i], 0);
		}
		return ret;
	}
	
	private Boolean haySolicitudes(Ascensor a) {
		return (haySolicitudArriba(a, 0));
	}

	/**
	 * Revisa las colas de pedidos de cada ascensor y:
	 *  si existe alguno Y si el ascensor est� detenido, lo arranca en la direcci�n correspondiente 
	 */
	private void atenderSolicitudes() {
		for (int i=0;i<ascensores.length;i++) {
			Ascensor a = ascensores[i];
			if (a.estado()==Estado.detenido) {
				if (haySolicitudArriba(a, a.piso()))
					a.subir();
				else if (haySolicitudAbajo(a, a.piso()))
					a.bajar();
			}
		}
	}

	// Helper
	private void msgs(String s) {
		System.out.println("Thread " + Thread.currentThread() + tabifier+"Controlador -> " + s);
	}

	// Helper
	public void setTab(String s) {
		tabifier = s;
	}

	// Helper
	public Ascensor[] ascensores() {
		return ascensores;
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

	synchronized 
	public void terminar() {
		terminar = true;
		msgs("terminar");
		this.notify();
	}
}