package tesis.Ejemplo_09;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class Modelo {

	public static void main(String[] args) {
		Collection<Integer> canales = new LinkedList<Integer>();
//		LinkedList<Integer> canales = new LinkedList<Integer>();
		canales.add(1);

		Iterator it = canales.iterator();
		// aun cuando la lista no este vacia, el iterator esta mal utilizado
		Integer i = (Integer) it.next();
		System.out.println(i);

		// el correcto uso seria hacer primero un hasNext y luego un next
		it = canales.iterator();
		while (it.hasNext()) {
			Integer j = (Integer) it.next();
			System.out.println(j);
		}

	}
}
