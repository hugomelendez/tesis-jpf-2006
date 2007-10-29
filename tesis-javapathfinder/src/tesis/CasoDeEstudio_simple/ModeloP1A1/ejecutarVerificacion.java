package tesis.CasoDeEstudio_simple.ModeloP1A1;

import tesis.extensiones.*;

public class ejecutarVerificacion {
	public static void main (String[] args) throws XMLException {
		//Propiedad 1
//		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad1/", "tesis.CasoDeEstudio_simple.ModeloP1A1.Modelo", true);

		//Propiedad 2
		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad2/", "tesis.CasoDeEstudio_simple.ModeloP1A1.Modelo", true);

		//Propiedad 3
//		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad3/", "tesis.CasoDeEstudio_simple.ModeloP1A1.Modelo", true);

		//Propiedad 13
//		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad13/", "tesis.CasoDeEstudio_simple.ModeloP1A1.ModeloPropiedad13", true);
	}
}