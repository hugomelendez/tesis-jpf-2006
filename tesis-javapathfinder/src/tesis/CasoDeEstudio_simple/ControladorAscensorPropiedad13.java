package tesis.CasoDeEstudio_simple;

/**
 * Modelo de Controlador de ascensores espcifico para la propiedad 13
 *  
 * @author Roberto
 */
class ControladorAscensorPropiedad13 extends ControladorAscensor {
	public ControladorAscensorPropiedad13(Ascensor[] a) {
		super(a);
	}

	/**
	 * Recibe una solicitud para ir al piso 1
	 * 
	 * @param a {@link Ascensor} 
	 * @param pisoDestino
	 */
	private void solicitudAscensorPiso1() {
//		msgs("solicitudAscensorPiso1");
	}
	 
	@Override
	public void solicitudAscensor(Ascensor a, int pisoDestino) {
		switch (pisoDestino) {
			case 1: solicitudAscensorPiso1(); break;
		}
		super.solicitudAscensor(a, pisoDestino);
	}

	/**
	 * el ascensor esta en piso 1
	 */
	private void estoyEnPiso1() {
//		msgs("estoyEnPiso1");
	}

	/**
	 * Override del metodo original para trapear el llamado a piso 1
	 * 
	 * @param a {@link Ascensor}
	 * @param piso
	 */
	@Override
	public void estoyEn(Ascensor a, int piso) {
		if (piso==1) {
			estoyEnPiso1();
			super.estoyEn(a, piso);
			termineEstadiaPiso1();
		}
		else {
			super.estoyEn(a, piso);
		}
	}

	/**
	 * Método sólo para detectar, en la GlobalProperty si al terminar la estadía en el piso 1
	 * no se atendió el pedido solicitado para ese piso
	 */
	private void termineEstadiaPiso1() {
	}
}