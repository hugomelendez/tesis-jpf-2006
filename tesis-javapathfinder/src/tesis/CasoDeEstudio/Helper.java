package tesis.CasoDeEstudio;

class Helper {

	// Helper
	public static void msgs(Object o, String msg, String tabifier) {
		System.out.println(tabifier+Thread.currentThread()+" "+ o +" -> "+ msg);
	}

	public static void esperar (int seg) {
		try {
			Thread.sleep(seg*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}