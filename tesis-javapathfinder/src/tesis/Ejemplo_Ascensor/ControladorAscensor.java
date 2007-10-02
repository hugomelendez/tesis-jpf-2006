package tesis.Ejemplo_Ascensor;

class ControladorAscensor {
	private Ascensor ascensor;
	private String tabifier;

	public ControladorAscensor(Ascensor a){
		ascensor = a;
	}

	/**
	 * 
	 */
	private Ascensor solicitud(int p) {
		// El controlador debe decidir que ascensor envia para el pedido
		ascensor.solicitudA(p);
		return ascensor;
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

	// Helper
	private void msgs(String s) {
//		System.out.println(tabifier+"Controlador -> " + s);
	}

	// Helper
	public void setTab(String s) {
		tabifier = s;
	}
}