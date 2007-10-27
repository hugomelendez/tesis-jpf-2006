package tesis.CasoDeEstudio;

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