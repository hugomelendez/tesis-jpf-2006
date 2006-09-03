package tesis.Ejemplo_02;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.jvm.JVM;
import gov.nasa.jpf.jvm.bytecode.Instruction;
import gov.nasa.jpf.search.DFSearchTesis;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.TesisListener;

public class ListenerConEstado extends PropertyListenerAdapter implements TesisListener {

	public class Evento{
		String ev; 
		public Evento (Instruction i, int threadNumber){
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
		public boolean equal (Evento e){
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
	public class Automata {
		private int estadoActual;
		private boolean blnFinalizoConError = false;
		
		public Automata (){
			estadoActual = 0;
		}
		public void irAEstado(int est){
			System.out.println(" Aut bktrk estado " + est);
			estadoActual = est;
		}

		public boolean finalizoConError(){
			return blnFinalizoConError;
		}

		public void hlpFwd(int est) {
			System.out.println(" Aut fwd estado " + est);
			
			switch (est) {
				case 10:
				case 11:
				case 12:
				case 13:
				case 14:
				case 15:
					System.out.println(" ++ Aut fwd estado HOJA \r\n\r\n");
					break;
				default: break;
			}
			estadoActual = est;
		}
		
		public void consumir (Evento e){
			
			switch (estadoActual) {
				case 0:
					if (e.esA()) {
						hlpFwd(1);
					} else if (e.esB()) {
						hlpFwd(2);
					} else if (e.esC()) {
						hlpFwd(3);
					}
					break;
				case 1:
					if (e.esA()) {
						hlpFwd(1);
					} else if (e.esB()) {
						hlpFwd(4);
					} else if (e.esC()) {
						hlpFwd(5);
					}
					break;
				case 2:
					if (e.esA()) {
						hlpFwd(6);
					} else if (e.esB()) {
						hlpFwd(2);
					} else if (e.esC()) {
						hlpFwd(7);
					}
					break;
				case 3:
					if (e.esA()) {
						hlpFwd(9);
					} else if (e.esB()) {
						hlpFwd(8);
					} else if (e.esC()) {
						hlpFwd(3);
					}
					break;
				case 4:
					if (e.esA()) {
						hlpFwd(4);
					} else if (e.esB()) {
						hlpFwd(4);
					} else if (e.esC()) {
						hlpFwd(10);
					}
					break;
				case 5:
					if (e.esA()) {
						hlpFwd(5);
					} else if (e.esB()) {
						hlpFwd(11);
					} else if (e.esC()) {
						hlpFwd(5);
					}
					break;
				case 6:
					if (e.esA()) {
						hlpFwd(6);
					} else if (e.esB()) {
						hlpFwd(6);
					} else if (e.esC()) {
						hlpFwd(12);
					}
					break;
				case 7:
					if (e.esA()) {
						hlpFwd(13);
					} else if (e.esB()) {
						hlpFwd(7);
					} else if (e.esC()) {
						hlpFwd(7);
					}
					break;
				case 8:
					if (e.esA()) {
						hlpFwd(14);
					} else if (e.esB()) {
						hlpFwd(8);
					} else if (e.esC()) {
						hlpFwd(8);
					}
					break;
				case 9:
					if (e.esA()) {
						hlpFwd(9);
					} else if (e.esB()) {
						blnFinalizoConError = true;
						hlpFwd(15);
					} else if (e.esC()) {
						hlpFwd(9);
					}
					break;
				default:
					break;
			}
		}

		public void consumirOld (Evento e){
			
			switch (estadoActual) {
				case 0:
					if (e.esA()){
						System.out.println(" Aut fwd estado " + 1);
						estadoActual = 1;
					}
					break;
				case 1:
					if (e.esC()){
						System.out.println(" Aut fwd estado " + 2);
						estadoActual = 2;
					}
					break;
				case 2:
					if (e.esB()){
						System.out.println(" Aut fwd estado " + 3);
						estadoActual = 3;
						blnFinalizoConError = true;
					}
					else if (e.esA()){
						estadoActual = 1;
					}
					break;
	
				default:
					break;
			}
		}

		public int getEstadoActual(){
			return estadoActual;
		}
	}

	private int cont = 0;
	private Automata  aut;

	public ListenerConEstado (){
		aut = new Automata();		
	}
	
	public int getEstadoActual() {
		return aut.getEstadoActual();
	}

	@Override
	public void stateAdvanced(Search search) {
		System.out.println("--------------------------------- STATE-ADVANCED: " + search.getStateNumber() + ";" + aut.getEstadoActual() + "  --------------------------------");
	}

	@Override
	public void stateBacktracked(Search search) {
		System.out.println("--------------------------------- STATE-BACKTRACKED:" + search.getStateNumber()  + ";" + aut.getEstadoActual() + "--------------------------------");
	}

	public void irAEstado(int i) {
		aut.irAEstado(i);		
	}

	@Override
	public boolean check(Search search, JVM vm) {
		//System.out.println(" Aut chk estado " + getEstadoActual());
		return (!aut.finalizoConError());
		//return true;
	}
	
	public void instructionExecuted(JVM vm) {
		cont++;
		Instruction li = vm.getLastInstruction();
		try {
			aut.consumir(new Evento (li, vm.getThreadNumber()));
		}
		catch (NullPointerException e) {
		}
	}

	
	public static void main (String[] args) {
		ListenerConEstado listener = new ListenerConEstado();

	    Config conf = JPF.createConfig(args);
	    System.out.println(args.toString());


	    // usamos nuestra busqueda
	    conf.setProperty("search.class","gov.nasa.jpf.search.DFSearchTesis");

	    JPF jpf = new JPF(conf); 
	    jpf.addSearchListener(listener);
	    jpf.addVMListener(listener);

	    // agregamos nuestro listener de forma especial para la tesis
	    ((DFSearchTesis)jpf.search).addTesisListener(listener);

	    System.out.println("---------------- JPF started");
	    jpf.run();
	    System.out.println("---------------- JPF terminated");

	}
	}


	