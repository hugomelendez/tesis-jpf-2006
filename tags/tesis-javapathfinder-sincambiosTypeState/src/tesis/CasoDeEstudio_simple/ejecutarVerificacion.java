package tesis.CasoDeEstudio_simple;

import tesis.extensiones.*;

public class ejecutarVerificacion {
	public static void main (String[] args) throws XMLException {
		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/", "tesis.CasoDeEstudio_simple.ModeloV2", true);
	}
}