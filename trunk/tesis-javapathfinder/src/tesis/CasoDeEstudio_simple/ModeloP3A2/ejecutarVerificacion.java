package tesis.CasoDeEstudio_simple.ModeloP3A2;

import tesis.extensiones.*;

public class ejecutarVerificacion {
	public static void main (String[] args) throws XMLException {
		//Propiedad 1
//		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad1/", "tesis.CasoDeEstudio_simple.ModeloP3A2.Modelo", true);

		//Propiedad 2
//		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad2/", "tesis.CasoDeEstudio_simple.ModeloP3A2.Modelo", true);

		//Propiedad 3
//		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad3/", "tesis.CasoDeEstudio_simple.ModeloP3A2.Modelo", true);

		//Propiedad 13
		//Esta propiedad sólo funciona para 1 ASCENSOR!

		// Verificacion solo JPF
		VerificationLauncher.execute("tesis.CasoDeEstudio_simple.ModeloP3A2.Modelo");
	}
}