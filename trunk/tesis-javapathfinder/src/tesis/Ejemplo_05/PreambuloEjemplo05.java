package tesis.Ejemplo_05;

import tesis.extensiones.Evento;
import tesis.extensiones.Preambulo;

public class PreambuloEjemplo05 extends Preambulo {

	public boolean cumplido() {
		return true;
	}
	
	@Override
	public void consumir(Evento e) {
	}

	/* (non-Javadoc)
	 * @see tesis.extensiones.Preambulo#aceptado()
	 */
	@Override
	public boolean aceptado() {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see tesis.extensiones.Preambulo#violado()
	 */
	@Override
	public boolean violado() {
		// TODO Auto-generated method stub
		return false;
	}

}
