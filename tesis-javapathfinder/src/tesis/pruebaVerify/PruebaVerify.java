package tesis.pruebaVerify;

import gov.nasa.jpf.jvm.Verify;

public class PruebaVerify {
	private static Boolean randomVar = false;

	private static Boolean rndBool() {
//		return (randomVar = !randomVar);
		return Verify.getBoolean();
	}
	
	public static void main (String[] args) {
		Boolean cond = rndBool();
		String b = "";

		b += " " + cond;

		cond = rndBool();

		b += " " + cond;

		System.out.println(b);
	}
}