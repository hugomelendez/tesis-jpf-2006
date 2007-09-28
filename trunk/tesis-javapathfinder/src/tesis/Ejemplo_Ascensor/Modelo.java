package tesis.Ejemplo_Ascensor;

import java.util.Vector;

//enum Piso {0,1,2,3};
enum Direccion {arriba, abajo};
enum Puerta {abierta, cerrada}
enum Estado {parado, bajando, subiendo}

class Ascensor implements Runnable {
	//Se considera la PB como piso 0, por ende el piso más alto es ALTURA-1
	private static final int ALTURA = 11;
	int piso;
	private Direccion direccion;
	private Puerta puerta;
	private Estado estado;
	private Vector<Boolean> solicitudes;
	
	Ascensor () {
		piso = 2;
		puerta = Puerta.abierta;
		direccion = Direccion.arriba;
		inicializarSolicitudes();
	}

	private void inicializarSolicitudes() {
		solicitudes = new Vector<Boolean>(ALTURA);
		for (int i=0; i<ALTURA; i++)
			//OJO, acá no se puede llamar a limpiarSolicitudEn
			solicitudes.add(i, false);
	}
	
	private void limpiarSolicitudEn(int p) {
		 solicitudes.set(p, false);
	}

	private void asignarSolicitudEn(int p) {
		 solicitudes.set(p, true);
	}

	private Boolean haySolicitudEn(int p) {
		 return (solicitudes.get(p));
	}

	public synchronized void run() {
		while (true) {
			try {
				if (!solicitudesPendientes()) 
					wait();
				
				int i = proximaSolicitud();
				irA(i);
				
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
	}

	private boolean solicitudesPendientes() {
		Boolean ret = false;
		for (int i = 0; i<ALTURA && !ret; i++) {
			ret = haySolicitudEn(i);
		}
		return ret;
	}

	// Devuelve la proxima solicitud a atender
	// La precondición es que haya una solicitud pendiente
	private int proximaSolicitud() {
		int ret;

		if (direccion == Direccion.arriba) {
			for (ret = piso; ret<ALTURA && !haySolicitudEn(ret); ret++);

			//Si no encontró una próxima solicitud para arriba, busca para abajo
			if (ret >= ALTURA) {
				for (ret = piso; ret>=0 && !haySolicitudEn(ret); ret--);
			}
		} else {
			for (ret = piso; ret>=0 && !haySolicitudEn(ret); ret--);
			
			//Si no encontró una próxima solicitud para abajo, busca para arriba
			if (ret < 0) {
				for (ret = piso; ret<ALTURA && !haySolicitudEn(ret); ret++);
			}
		}

		//Por la precondición
		assert(ret>=0 && ret<ALTURA);
		
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
		System.out.println("Pasa por " + p);
		esperar(1);
		piso = p;		
	}

	private void subir(int p){
		for (int i = piso; i <= p; i++){
			pasarPor(i);
			if (haySolicitudEn(i)) {
				atenderSolicitud(i);
			}
		}
	}

	private void bajar(int p){
		for (int i = piso; i >= p; i--){
			pasarPor(i);
			if (haySolicitudEn(i)) {
				atenderSolicitud(i);
			}
		}
	}

	public synchronized void solicitudA (int p) {
		System.out.println("Solicitud NUEVA, piso " + p);
		asignarSolicitudEn(p);
		this.notify();
	}
	
	private void irA (int p) {
		if (p > piso){
			arrancar (Direccion.arriba);
			subir(p);
		} else if (p < piso) {
			arrancar (Direccion.abajo);
			bajar(p);
		}else{
			atenderSolicitud(p);
		}
	}

	private void arrancar (Direccion d) {
		if (direccion != d) {
			direccion = d;
			System.out.println("Cambio direccion "+direccion);
		}

		cerrarPuertas();
		if (direccion == Direccion.arriba){
			estado = Estado.subiendo;
		} else {
			estado = Estado.bajando;
		}
	}

	private void atenderSolicitud (int p) {
		estado = Estado.parado;
		abrirPuertas();
		esperar(1);
		limpiarSolicitudEn(p);
	}

	private void esperar (int seg) {
		try {
			Thread.sleep(seg*1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
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

		ControladorAscensor ca = new ControladorAscensor(a);
		Thread t2 = new Thread(ca);
		
		Persona p = new Persona(ca);
		Thread t3 = new Thread(p); 

		t1.start();
		t2.start();
		t3.start();
	}
}