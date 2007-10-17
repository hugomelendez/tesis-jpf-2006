package tesis.CasoDeEstudio;

class ModeloV2 {
	private final static int CANT_PERSONAS = 1;
	private final static int CANT_ASCENSORES = 1;
	
	Ascensor[] ascensores;
	ControladorAscensor ca;
	int cantPersonasVivas;
	
	public ModeloV2() {
		cantPersonasVivas = CANT_PERSONAS;
	}
	
	synchronized
	public void coordinarFinEjecución() {
		if (cantPersonasVivas > 0) {
			try {
//				System.out.println("ModeloV2.wait() IN");
				this.wait();
//				System.out.println("ModeloV2.wait() OUT");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for (int i = 0; i < ascensores.length; i++) {
			ascensores[i].terminar();
			
		}
		ca.terminar();
	}
	
	synchronized
	public void terminoPersona() {
		cantPersonasVivas--;
		if (cantPersonasVivas==0) {
			this.notify();
		}
	}

	private void controlador(ControladorAscensor ca) {
		this.ca = ca;
	}

	private void ascensores(Ascensor[] a) {
		ascensores = a;
	}

	public static void main(String[] args) {
		Ascensor a1 = new Ascensor("a1");
		a1.setTab("\t");
		Thread tA1 = new Thread(a1);

//		Ascensor a2 = new Ascensor("a2");
//		a2.setTab("\t\t");
//		Thread tA2 = new Thread(a2);

		Ascensor[] ascensores = new Ascensor[CANT_ASCENSORES];
		ascensores[0] = a1;
//		ascensores[1] = a2;

		ControladorAscensor ca = new ControladorAscensor(ascensores);
		ca.setTab("");
		Thread tCA = new Thread(ca);

		Persona p1 = new Persona("p1");
		p1.setTab("\t\t\t");
		p1.controlador(ca);
		Thread tP1 = new Thread(p1);

//		Persona p2 = new Persona("p2");
//		p2.setTab("\t\t\t\t");
//		p2.controlador(ca);
//		Thread tP2 = new Thread(p2);
//
//		Persona pMatrix = new Persona("pMatrix");
//		pMatrix.setTab("\t\t\t\t\t");
//		pMatrix.controlador(ca);
//		Thread tPMatrix = new Thread(pMatrix);

		ModeloV2 m = new ModeloV2();
		m.ascensores(ascensores);
		m.controlador(ca);
		p1.modelo(m);
//		p2.modelo(m);
//		pMatrix.modelo(m);

		tA1.start();
//		tA2.start();
		tCA.start();
		tP1.start();
//		tP2.start();
//		tPMatrix.start();
		
		m.coordinarFinEjecución();
	}
}