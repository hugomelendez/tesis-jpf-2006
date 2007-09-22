package tesis.Ejemplo_Ascensor;

//enum Piso {0,1,2,3};
enum Direccion {arriba, abajo};
enum Puerta {abierta, cerrada}
enum Estado {parado, bajando, subiendo}


class Asensor implements Runnable {
	int piso;
	private Direccion direccion;
	private Puerta puerta;
	
	Asensor () {
		piso = 0;
		puerta = Puerta.abierta;
	}

	public void abrirPuertas () {
		puerta = Puerta.abierta;
	}

	public void cerrarPuertas () {
		puerta = Puerta.cerrada;
	}

	private void pasarPor(int p){
		piso = p;		
	}

	public void subir(int p){
		int x = 0;

		for (x = 0; x == p; x++){
			pasarPor(x);
		}
	}

	public void bajar(int p){
		int x = 0;
		for (x = piso; x == p; x--){
			pasarPor(x);
		}
	}

	public void irA (int p) {
		if (p > piso){
			Arrancar (Direccion.arriba);
			subir(p);
			Llegar();
		}else if (p < piso) {
			Arrancar (Direccion.abajo);
			bajar(p);
			Llegar();
		}else{ //La tiene que ir al mismo piso en el que esta
			//no debería hacer nada no?
		}
	}

	public void Arrancar (Direccion d) {
		cerrarPuertas();
		direccion = d;
	}

	public void Llegar () {
		abrirPuertas();
		cerrarPuertas();
		Esperar();
		//Acá no debería esperar. Es posible que necesite seguir
	}

	public void Esperar () {
		
		try{ 
			Thread.sleep((int)(Math.random() * 10));
		} catch( InterruptedException e ) {
			System.out.println("Interrupted Exception caught");
		}
	}
	
	public void run() {
	}
}


public class Modelo {
	public static void main(String[] args) {
		Asensor a = new Asensor();
		a.Esperar();
		a.subir(3);
	}
}
