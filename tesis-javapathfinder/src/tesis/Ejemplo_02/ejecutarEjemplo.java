package tesis.Ejemplo_02;

import tesis.extensiones.*;

public class ejecutarEjemplo {
	public static void main (String[] args) throws XMLException {
		VerificationLauncher.execute("./src/tesis/Ejemplo_02/", "tesis.Ejemplo_02.ModeloConTresHilos", false);
//		VerificationLauncher.execute("tesis.Ejemplo_02.ModeloConTresHilos");
	}
}