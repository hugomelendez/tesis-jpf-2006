package tesis.pruebaVerify;

import gov.nasa.jpf.jvm.Verify;


public class PruebaVerify1 {

	public static int Evento1 () {
		System.out.println("11111111111111111111");
		return 1;
	}

	public static int Evento2 () {
		System.out.println("22222222222222222222");
		return 2;
	}

	public static void main (String[] args){ 
		int a;
		boolean cond = Verify.getBoolean();

		if (cond){
			a = Evento1();
			a = Evento2();
		}
		else{
			a = Evento2();
			a = Evento1();
		}
	    
	}
}
