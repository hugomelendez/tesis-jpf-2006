package tesis.Ejemplo_03;

import gov.nasa.jpf.jvm.bytecode.Instruction;
import tesis.extensiones.Evento;

public class EventoOpenClose implements Evento {
	String ev;
	
	public EventoOpenClose (Instruction i, int threadNumber) {
		//System.out.println("++" + i + "++");
		if (i.toString().contains("tesis.Ejemplo_03")) {
			if (i.toString().contains("invokestatic tesis.Ejemplo_03.ModeloOpenClose.open()V") )
				ev = "OPEN";
			else if (i.toString().contains("invokestatic tesis.Ejemplo_03.ModeloOpenClose.close()V") )
				ev = "CLOSE";
		}
		
	}
	public boolean equals (EventoOpenClose e) {
		return (ev.equals((e.ev)));
	}
	
	public boolean esOPEN() {
		return (ev.equals("OPEN"));
	}
	
	public boolean esCLOSE() {
		return (ev.equals("CLOSE"));
	}
}
