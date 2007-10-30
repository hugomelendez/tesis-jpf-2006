package tesis.CasoDeEstudio_simple;

/**
 * Modelo de Controlador de ascensores espcifico para la propiedad 4
 *  
 * @author Roberto
 */
public class ControladorAscensorPropiedad5 extends ControladorAscensor {
	public ControladorAscensorPropiedad5(Ascensor[] a) {
		super(a);
	}

	/**
	 * Recibe una solicitud para ir al piso 4
	 * 
	 * @param a {@link Ascensor} 
	 * @param pisoDestino
	 */
	private void solicitudAscensorPiso4() {
//		msgs("solicitudAscensorPiso4");
	}
	 
	@Override
	public void solicitudAscensor(Ascensor a, int pisoDestino) {
		switch (pisoDestino) {
			case 4: solicitudAscensorPiso4(); break;
		}
		super.solicitudAscensor(a, pisoDestino);
	}

	/**
	 * 
	 */
	protected void atenderSolicitudPiso(Ascensor a, int piso) {
		switch (piso) {
			case 4: atenderSolicitudPiso4(); break;
		}
		super.atenderSolicitudPiso(a, piso);
	}
	
	private void atenderSolicitudPiso4() {
		
	}

	/**
	 * Sobreescribimos el metodo original para introducir un bug, simulando un copy paste mal hecho
	 * 
	 * @param a {@link Ascensor}
	 * @param piso
	 */
	@Override
	public void estoyEn(Ascensor a, int piso) {
		if (haySolicitudEn(a, piso)) {
			atenderSolicitudPiso(a, piso);

			/**
			 * BUG
			 * si esta bajando prioriza el atender solicitudes hacia arriba y viceversa
			 */
			if (a.estaBajando()) {
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

}