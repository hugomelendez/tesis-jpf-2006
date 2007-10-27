package tesis.CasoDeEstudio;

import tesis.extensiones.*;

public class ejecutarVerificacion {
	public static void main (String[] args) throws XMLException {
		VerificationLauncher.execute("./src/tesis/CasoDeEstudio/", "tesis.CasoDeEstudio.ModeloV2", false);
	}
}