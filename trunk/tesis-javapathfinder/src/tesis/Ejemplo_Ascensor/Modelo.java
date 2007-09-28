package tesis.Ejemplo_Ascensor;

import java.util.Vector;

//enum Piso {0,1,2,3};
enum Direccion {arriba, abajo};
enum Puerta {abierta, cerrada}
enum Estado {parado, bajando, subiendo}

class Ascensor implements Runnable {
	private static final int ALTURA = 10;
	int piso;
	private Direccion direccion;
	private Puerta puerta;
	private Estado estado;
	private Vector<Boolean> solicitudes;
	
	Ascensor () {
		piso = 2;
		puerta = Puerta.abierta;
		direccion = Direccion.arriba;
		solicitudes = new Vector<Boolean>(ALTURA+1);
	}

	public synchronized void run() {
		while (true) {
			try {
				if (!solicitudesPendientes()) 
					wait();
				
				int i = proximaSolicitud();
				solicitudes.add(i, false);
				irA(i);
				
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}

	private boolean solicitudesPendientes() {
		Boolean ret = false;
		for (int i = 0; i<=ALTURA && !ret; i++) {
			ret = solicitudes.elementAt(i);
		}
		return ret;
	}

	// devuelve la proxima solicitud a atender
	private int proximaSolicitud() {
		int ret;
		if (direccion == Direccion.arriba) {
			for (ret = piso; ret<=ALTURA && !solicitudes.elementAt(ret); ret++);
		} else {
			for (ret = piso; ret>=0 && !solicitudes.elementAt(ret); ret--);
		}
		return ret;
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
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Pasa por " + p);
		piso = p;		
	}

	private void subir(int p){
		int x = 0;

		for (x = piso; x <= p; x++){
			pasarPor(x);
		}
	}

	private void bajar(int p){
		int x = 0;
		for (x = piso; x >= p; x--){
			pasarPor(x);
		}
	}

	public synchronized void solicitudA (int p) {
		System.out.println("Solicitud NUEVA, piso " + p);
		solicitudes.add(p, true);
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

		if (direccion != d) {
			direccion = d;
			System.out.println("Cambio direccion "+direccion);
		}
	}

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
	}

	public boolean enPiso(int p) {
		return (piso==p);
	}
}

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

class Persona implements Runnable {
	ControladorAscensor controlA;

	Persona (ControladorAscensor c) {
		controlA = c;
	}

	public void run() {
		Ascensor a = controlA.solicitudSubir(0);

		//Esperamos a q el ascensor llegue
		while (!a.enPiso(0));
		a.apretarBoton(10);
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
		Thread t3 = new Thread(p); 
		t3.start();
	}
}