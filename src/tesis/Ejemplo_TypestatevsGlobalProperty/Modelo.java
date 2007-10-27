package tesis.Ejemplo_TypestatevsGlobalProperty;

class Canal {
	private int opens = 0;
	private int writes = 0;

	public void open() { 
		opens++;
	}
	
	public void close() {
		opens--;
	}

	public void write() {
		writes++;
	}
}

public class Modelo {
	public static void main(String[] args) {
		Canal c1 = new Canal();
		Canal c2 = new Canal();
		
		c1.open();
		c1.write();
		c1.close();
		
		c1.open();
		c1.write();
		
		c2.open();
		c2.write();
		c2.close();
		
		c1.close();
	}
}
