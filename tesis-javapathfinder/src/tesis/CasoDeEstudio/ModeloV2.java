package tesis.CasoDeEstudio;

class ModeloV2 {
	Ascensor a1;
	Ascensor a2;
	ControladorAscensor ca;

	public ModeloV2() {
	}
	
	synchronized
	public void coordinarFinEjecución() {
		//En teoria, debería esperar a que c/persona haga notify cuando termina
		for (int cantPersonas=1;cantPersonas<=3;cantPersonas++) {
			try {
				System.out.println("ModeloV2.wait()");
				this.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		a1.terminar();
		a2.terminar();
		ca.terminar();
	}
	
	private void controlador(ControladorAscensor ca) {
		this.ca = ca;
	}

	private void ascensores(Ascensor a1, Ascensor a2) {
		this.a1 = a1;
		this.a2 = a2;
	}

	public static void main(String[] args) {
		Ascensor a1 = new Ascensor("a1");
		a1.setTab("\t");
		Thread t1 = new Thread(a1);

		Ascensor a2 = new Ascensor("a2");
		a2.setTab("\t\t");
		Thread t2 = new Thread(a2);

		Ascensor[] ascensores = new Ascensor[2];
		ascensores[0] = a1;
		ascensores[1] = a2;

		ControladorAscensor ca = new ControladorAscensor(ascensores);
		ca.setTab("");
		Thread t3 = new Thread(ca);

		Persona p1 = new Persona("p1");
		p1.setTab("\t\t\t");
		p1.controlador(ca);
		Thread t4 = new Thread(p1);

		Persona p2 = new Persona("p2");
		p2.setTab("\t\t\t\t");
		p2.controlador(ca);
		Thread t5 = new Thread(p2);

		Persona pMatrix = new Persona("pMatrix");
		pMatrix.setTab("\t\t\t\t\t");
		pMatrix.controlador(ca);
		Thread tMatrix = new Thread(pMatrix);

		ModeloV2 m = new ModeloV2();
		m.ascensores(a1, a2);
		m.controlador(ca);
		p1.modelo(m);
		p2.modelo(m);
		pMatrix.modelo(m);

		
		t1.start();
		t2.start();
		t3.start();
		t4.start();
		t5.start();
		tMatrix.start();
		
		
		m.coordinarFinEjecución();
	}
}