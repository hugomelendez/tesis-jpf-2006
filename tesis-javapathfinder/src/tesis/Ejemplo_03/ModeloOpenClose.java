package tesis.Ejemplo_03;

import gov.nasa.jpf.jvm.Verify;

public class ModeloOpenClose {
	static void open() {}
	static void close() {}
	
	public static void main(String[] args) {
		open();
		while (true) {
			boolean bln = Verify.randomBool();
			
			if (bln)
				open();
			else
				close();
		}
	}
}
