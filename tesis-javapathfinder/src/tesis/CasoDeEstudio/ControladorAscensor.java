package tesis.CasoDeEstudio;

import java.util.Hashtable;

class ControladorAscensor {
	private final static int ALTURA = 10;
	private Ascensor[] ascensores;
	
	//Cada posicion corresponde a un piso
	private Boolean[] solicitudesSubir;
	private Boolean[] solicitudesBajar;
	private Hashtable<Ascensor, Boolean[]> solicitudesPorAscensor;

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
		}
	}

	private void solicitudPisoArriba(int pisoDesde) {
		solicitudesSubir[pisoDesde] = true;
	}
	
	private void solicitudPisoAbajo(int pisoDesde) {
		solicitudesBajar[pisoDesde] = true;
	}
	
	private void solicitudAscensor(Ascensor a, int pisoDestino) {
		setSolicitud(a, pisoDestino, true);
	}
	
	private void atenderSolicitudPiso(Ascensor a, int piso) {
		a.detenerse();
		a.abrirPuertas();
		esperar(2);
		setSolicitud(a, piso, false);
		a.cerrarPuertas();
	}
	
	public void estoyEn(Ascensor a, int piso) {
		if (haySolicitudEn(a, piso)) {
			atenderSolicitudPiso(a, piso);

			if (a.getDireccion()==Direccion.arriba) {
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
			if (haySolicitudesArriba(a, piso))
				a.subir();
			else
				a.detenerse();
		}
		else if (piso==ALTURA) {
			if (haySolicitudesAbajo(a, piso))
				a.bajar();
			else
				a.detenerse();
		}
	}
	
	private boolean haySolicitudAbajo(Ascensor a, int piso) {
		Boolean[] s = solicitudesPorAscensor.get(a);
		Boolean ret = false;
		for (int i=piso; i>=0 && !ret;i++) {
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
	}
	
	private Boolean haySolicitudEn(Ascensor a, int piso) {
		return (solicitudesPorAscensor.get(a))[piso];
	}
	
	/**
	 * La persona aprieta el boton de bajar desde el piso p
	 * 
	 * Helper para poder capturar el evento
	 */
	public Ascensor solicitudBajar (int p){
//		msgs("Solicitud bajar desde: " + p);
		return solicitud(p);
	}

	/**
	 * La persona aprieta el boton de subir desde el piso p
	 * 
	 * Helper para poder capturar el evento
	 */
	public Ascensor solicitudSubir(int p){
//		msgs("Solicitud subir desde: " + p);
		return solicitud(p);
	}

	/**
	 * Presiona el boton dentro del ascensor para ir al piso 
	 * @param p piso
	 */
	public void apretarBoton(int p) {
		msgs("Boton presionado: "+ p);
//		solicitudA(p);
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
//		System.out.println(tabifier+"Controlador -> " + s);
	}

	// Helper
	public void setTab(String s) {
		tabifier = s;
	}
}