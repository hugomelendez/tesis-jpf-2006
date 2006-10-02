package tesis.Ejemplo_04;

import gov.nasa.jpf.jvm.Verify;

public class PruebaVerify {

	private static void testBool() {
		System.out.println("Bool test !");

		String b = "";

		b += " " + Verify.getBoolean();
		b += " " + Verify.getBoolean();

		System.out.println(b);
	}
	
	private static void testInt() {
		System.out.println("Int test !");

		String b = "";

		b += Verify.getInt(0,9) + " " + Verify.getInt(0,9);
		System.out.println(b);
	}
	
	public static void main (String[] args) {
		testBool();
		System.out.println("");
		testInt();
	}
}
