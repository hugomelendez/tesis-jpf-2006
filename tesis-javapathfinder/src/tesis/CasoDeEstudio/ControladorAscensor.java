package tesis.CasoDeEstudio;

import java.util.Hashtable;

class ControladorAscensor implements Runnable {
	private final static int ALTURA = 10;
	private Ascensor[] ascensores;
	
	//Solicitudes de los botones Subir y Bajar en cada piso del edificio
	//Cada posicion corresponde a un piso
	private Boolean[] solicitudesSubir;
	private Boolean[] solicitudesBajar;
	
	//Botones dentro de un ascensor
	private Hashtable<Ascensor, Boolean[]> solicitudesPorAscensor;

	private String tabifier;

	public ControladorAscensor(Ascensor[] a){
		ascensores = a;
		
		solicitudesBajar = new Boolean[ALTURA+1];
		solicitudesSubir = new Boolean[ALTURA+1];
		for (int i=0;i<ALTURA;i++) {
			solicitudesBajar[i] = false;
			solicitudesSubir[i] = false;
		}

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

	synchronized
	private void solicitudPisoArriba(int pisoDesde) {
		msgs("solicitudPisoArriba desde piso " + pisoDesde);
		solicitudesSubir[pisoDesde] = true;
		msgs("notify@solicitudPisoArriba");
		notify();
	}
	
	synchronized
	private void solicitudPisoAbajo(int pisoDesde) {
		msgs("solicitudPisoAbajo desde piso " + pisoDesde);
		solicitudesBajar[pisoDesde] = true;
		msgs("notify@solicitudPisoAbajo");
		notify();
	}
	
	synchronized
	public void solicitudAscensor(Ascensor a, int pisoDestino) {
		msgs("solicitudAscensor " + a + " a piso " + pisoDestino);
		setSolicitud(a, pisoDestino, true);
		a.cerrarPuertas();
		
		msgs("notify@solicitudAscensor");
		notify();
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
				} else {
					a.detenerse();
				}
			} else {
				if (haySolicitudAbajo(a, piso)) { 
					a.bajar();
				} else if (haySolicitudArriba(a, piso)) {
					a.subir();
				} else {
					a.detenerse();
				}
			}
		}

		if (piso==0) {
			if (haySolicitudArriba(a, piso))
				a.subir();
			else
				a.detenerse();
		}
		else if (piso==ALTURA) {
			if (haySolicitudAbajo(a, piso))
				a.bajar();
			else
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
		Boolean[] s = solicitudesPorAscensor.get(a);
		s[piso] = b;
		msgs("solicitudes de " +a+ ": "+ printSolicitudes(s));
	}
	
	private Boolean haySolicitudEn(Ascensor a, int piso) {
		return (solicitudesPorAscensor.get(a))[piso];
	}

//	synchronized
	private void esperar (int seg) {
		try {
			Thread.sleep(seg*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	synchronized
	public void run() {
		while (true) {
//			if (!haySolicitudes()) {
				try {
					msgs("wait()");
					wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//			}
//			else		
				atenderSolicitudes();
		}
	}
	
//	private Boolean haySolicitudes() {
//		Boolean ret = false;
//	
//		for (int i=0;i<ascensores.length && !ret;i++) {
//			ret = haySolicitudArriba(ascensores[i], 0);
//		}
//		return ret;
//	}
	
	/**
	 * Revisa las colas de pedidos de cada ascensor y:
	 *  si existe alguno Y si el ascensor está detenido, lo arranca en la dirección correspondiente 
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
		System.out.println(tabifier+"Controlador -> " + s);
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
}