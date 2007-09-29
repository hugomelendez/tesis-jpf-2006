package tesis.Ejemplo_Ascensor;


class ControladorAscensor implements Runnable{
	Ascensor ascensor;

	public ControladorAscensor(Ascensor a){
		ascensor = a;
	}

	//La persona aprieta el boton de bajar desde el piso p
	public Ascensor solicitudBajar (int p){
		// El controlador debe decidir que ascensor envia para el pedido
		ascensor.solicitudA(p);
		return ascensor;
	}

	//La persona aprieta el boton de subir desde el piso p
	public Ascensor solicitudSubir(int p){
		// El controlador debe decidir que ascensor envia para el pedido
		ascensor.solicitudA(p);
		return ascensor;
	}

	public void run() {
	}
}
