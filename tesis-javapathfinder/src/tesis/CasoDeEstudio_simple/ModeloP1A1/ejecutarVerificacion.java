package tesis.CasoDeEstudio_simple.ModeloP1A1;

import tesis.extensiones.*;

public class ejecutarVerificacion {
	public static void main (String[] args) throws XMLException {
		//Propiedad 1
//		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad1/", "tesis.CasoDeEstudio_simple.ModeloP1A1.Modelo", true);

		//Propiedad 2
//		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad2/", "tesis.CasoDeEstudio_simple.ModeloP1A1.Modelo", true);

		//Propiedad 3
//		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad3/", "tesis.CasoDeEstudio_simple.ModeloP1A1.Modelo", true);

		//Propiedad 4
//		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad4/", "tesis.CasoDeEstudio_simple.ModeloP1A1.ModeloPropiedad4", true);

		//Propiedad 5
		VerificationLauncher.execute("./src/tesis/CasoDeEstudio_simple/propiedad5/", "tesis.CasoDeEstudio_simple.ModeloP1A1.ModeloPropiedad5", true);

		//Verificacion solo JPF
//		VerificationLauncher.execute("tesis.CasoDeEstudio_simple.ModeloP1A1.Modelo");
	}
}