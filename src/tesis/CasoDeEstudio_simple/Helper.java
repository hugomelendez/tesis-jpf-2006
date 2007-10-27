package tesis.CasoDeEstudio_simple;

//enum Direccion {arriba, abajo}; //{arriba 1, abajo 0}
//enum Puerta {abierta, cerrada}; //{abierta 1, cerrada 0}
//enum Estado {detenido, enMovimiento}; //{detenido 0, enMovimiento 1}

/**
 * Helper para implementar funcionalidades comunes
 */
class Helper {
	// Helper
	public static void esperar (int seg) {
		try {
			Thread.sleep(seg*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}