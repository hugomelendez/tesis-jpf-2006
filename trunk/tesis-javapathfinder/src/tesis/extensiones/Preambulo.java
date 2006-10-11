package tesis.extensiones;

// TODO Idem EventBuilder 
public abstract class Preambulo {

	public abstract boolean violado();

	public abstract boolean cumplido();

	public abstract void consumir(Evento e);

	public abstract boolean aceptado();

}
