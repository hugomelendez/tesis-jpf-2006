package tesis.Ejemplo_05;

import gov.nasa.jpf.jvm.bytecode.Instruction;
import tesis.extensiones.Evento;

public class EventoOpenClose implements Evento {
	String ev;
	
	public EventoOpenClose (Instruction i, int threadNumber) {
//		System.out.println("++" + i + "++");
		if (i.toString().contains("tesis.Ejemplo_05")) {
			if (i.toString().contains("invoke") && i.toString().contains("Canal.open") )
				ev = "OPEN";
			else if (i.toString().contains("invoke") && i.toString().contains("Canal.close") )
				ev = "CLOSE";
		}
		
	}
	public boolean equals (EventoOpenClose e) {
		return (ev.equals(e.ev));
	}
	
	public boolean esOPEN() {
		return (ev.equals("OPEN"));
	}
	
	public boolean esCLOSE() {
		return (ev.equals("CLOSE"));
	}
}
