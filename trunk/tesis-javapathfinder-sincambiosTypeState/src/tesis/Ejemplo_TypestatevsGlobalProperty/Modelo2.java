package tesis.Ejemplo_TypestatevsGlobalProperty;

public class Modelo2 {
	public static void main(String[] args) {
		Canal c1 = new Canal();
		Canal c2 = new Canal();
		
		c1.open();
		c2.write();
		c2.close();
	}
}
