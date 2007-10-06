package tesis.CasoDeEstudio;

import tesis.extensiones.*;

public class ejecutarEjemplo {
	public static void main (String[] args) throws XMLException {
		VerificationLauncher.execute("./src/tesis/CasoDeEstudio/", "tesis.CasoDeEstudio.Modelo", false);
	}
}