package tesis.Ejemplo_02;

import gov.nasa.jpf.jvm.bytecode.Instruction;
import tesis.extensiones.Evento;

public class EventoDeEjemplo2 implements Evento {
	String ev;
	public EventoDeEjemplo2 (Instruction i, int threadNumber){
		//System.out.println("++" + i + "++");
		if (i.toString().equals("invokevirtual java.io.PrintStream.println(Ljava/lang/String;)V")) {
			switch (threadNumber){
			case 1: ev = "A"; break;
			case 2: ev = "B"; break;
			case 3: ev = "C"; break;
			default: ev = "ERROR"; 
			}
		}
	}
	public boolean equal (EventoDeEjemplo2 e){
		return (ev.equals((e.ev)));
	}
	public boolean esA(){
		return (ev.equals("A"));
	}
	public boolean esB(){
		return (ev.equals("B"));
	}
	public boolean esC(){
		return (ev.equals("C"));
	}
}
