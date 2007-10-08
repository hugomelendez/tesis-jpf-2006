package tesis.CasoDeEstudio;

class Modelo implements Runnable {

	private ControladorAscensor ca;
	private Thread tca;
	private Thread tp1;
	private Thread tp2;
	private Thread tp3;
	private Thread tA1;
	private Thread tA2;

	public void run() {

		//Espera que terminen de ejecutar las personas
		while (tp1.isAlive() && tp2.isAlive() && tp3.isAlive()) {
		}

		//Espera a que el Controlador ya no tenga solicitudes pendientes
		//(ya que terminaron las personas, no van a existir más pedidos.
		while (ca.haySolicitudes()){
		}

		tA1.interrupt();
		tA2.interrupt();
		tca.interrupt();

	
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void controlador(ControladorAscensor ca) {
		this.ca = ca;
	}
	private void tControlador(Thread t) {
		this.tca = t;
	}

	private void tPersonas(Thread t4, Thread t5, Thread matrix) {
		this.tp1 = t4;
		this.tp2 = t5;
		this.tp3 = matrix;
	}

	private void tAscensores(Thread t1, Thread t2) {
		this.tA1 = t1;
		this.tA2 = t2;
	}

	public static void main(String[] args) {
		Ascensor a1 = new Ascensor("a1");
		a1.setTab("\t");
		Thread t1 = new Thread(a1);
		t1.start();

		Ascensor a2 = new Ascensor("a2");
		a2.setTab("\t\t");
		Thread t2 = new Thread(a2);
		t2.start();

		Ascensor[] ascensores = new Ascensor[2];
		ascensores[0] = a1;
		ascensores[1] = a2;

		ControladorAscensor ca = new ControladorAscensor(ascensores);
		ca.setTab("");
		Thread t3 = new Thread(ca);
		t3.start();

		Persona p = new Persona("p1");
		p.setTab("\t\t\t");
		p.controlador(ca);
		Thread t4 = new Thread(p);
		t4.start();

		Persona p2 = new Persona("p2");
		p2.setTab("\t\t\t\t");
		p2.controlador(ca);
		Thread t5 = new Thread(p2);
		t5.start();

		Persona pMatrix = new Persona("pMatrix");
		pMatrix.setTab("\t\t\t\t\t");
		pMatrix.controlador(ca);
		Thread tMatrix = new Thread(pMatrix);
		tMatrix.start();
	
		
		Modelo m = new Modelo();
		m.tAscensores(t1, t2);
		m.tPersonas(t4, t5, tMatrix);
		m.controlador(ca);
		m.tControlador(t3);
		Thread t = new Thread(m);
		t.start();
	}
}