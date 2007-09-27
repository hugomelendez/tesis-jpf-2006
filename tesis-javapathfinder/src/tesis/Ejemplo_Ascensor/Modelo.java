package tesis.Ejemplo_Ascensor;

import sun.misc.Queue;

//enum Piso {0,1,2,3};
enum Direccion {arriba, abajo};
enum Puerta {abierta, cerrada}
enum Estado {parado, bajando, subiendo}

class Ascensor implements Runnable {
	int piso;
	private Direccion direccion;
	private Puerta puerta;
	private Estado estado;
	private Queue qSolicitudes;  
	
	Ascensor () {
		piso = 2;
		puerta = Puerta.abierta;
		qSolicitudes = new Queue();
	}

	public synchronized void run() {
		while (true) {
			try {
				if (qSolicitudes.isEmpty()) 
					wait();
				
				int i = ((Integer)qSolicitudes.dequeue()).intValue();
				irA(i);
				
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}

	public void abrirPuertas () {
		System.out.println("abrirPuertas");
		puerta = Puerta.abierta;
	}

	public void cerrarPuertas () {
		System.out.println("cerrarPuertas");
		puerta = Puerta.cerrada;
	}

	private void pasarPor(int p){
		System.out.println("Pasa por " + ((new Integer(p).toString())));
		piso = p;		
	}

	private void subir(int p){
		int x = 0;

		for (x = piso; x <= p; x++){
			pasarPor(x);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void bajar(int p){
		int x = 0;
		for (x = piso; x >= p; x--){
			pasarPor(x);
		}
	}

	public synchronized void solicitudA (int p) {
		System.out.println("Solicitud NUEVA");
		qSolicitudes.enqueue(p);
		this.notify();
	}
	
	private void irA (int p) {
		if (p > piso){
			arrancar (Direccion.arriba);
			subir(p);
			llegar();
		} else if (p < piso) {
			arrancar (Direccion.abajo);
			bajar(p);
			llegar();
		}else{ //La tiene que ir al mismo piso en el que esta
			//no debería hacer nada no?
		}
	}

	private void arrancar (Direccion d) {
		cerrarPuertas();
		if (direccion == Direccion.arriba){
			estado = Estado.subiendo;
		} else {
			estado = Estado.bajando;
		}
		direccion = d;
	};

	private void llegar () {
		estado = Estado.parado;
		abrirPuertas();
		esperar();
		//Acá no debería esperar. Es posible que necesite seguir
	}

	private void esperar () {
		estado = Estado.parado;
	}

	public void apretarBoton(int i) {
		solicitudA(i);
		
		System.out.println("INICIA apretarBoton");
		for (int x=0; x<=100000;x++){
			x++;
			x--;
		}
		System.out.println("TERMINA apretarBoton");
	}
}

class ControladorAscensor implements Runnable{
	Ascensor ascensor;
	
	public ControladorAscensor(Ascensor a){
		ascensor = a;
	}
	
	//La persona aprieta el boton de subir desde el piso p
	public Ascensor solicitudBajar (int p){
		// El controlador debe decidir que ascensor envia para el pedido
		ascensor.solicitudA(p);
		return ascensor;
	}

	//La persona aprieta el boton de bajar desde el piso p
	public Ascensor solicitudSubir(int p){
		// El controlador debe decidir que ascensor envia para el pedido
		ascensor.solicitudA(p);
		return ascensor;
	}

	public void run() {
	}
}

class Persona implements Runnable {
	ControladorAscensor controlA;

	Persona (ControladorAscensor c) {
		controlA = c;
	}

	public void run() {
		Ascensor a = controlA.solicitudSubir(0);
		
		a.apretarBoton(10);
		//while (a.piso != 10);
		System.out.println("APRETE BOTON");
	}
}

public class Modelo {
	public static void main(String[] args) {
		Ascensor a = new Ascensor();
		Thread t1 = new Thread(a);
		t1.start();

		ControladorAscensor ca = new ControladorAscensor(a);
		Thread t2 = new Thread(ca);
		t2.start();
		
		Persona p = new Persona(ca);
		//p.run();
		Thread t3 = new Thread(p); 
		t3.start();
	}
}
