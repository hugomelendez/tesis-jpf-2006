package tesis.zPruebas;

import java.util.Collection;
import java.util.LinkedList;

public class pruebas {
	@SuppressWarnings("finally")
	private static Collection<String> padresDeClase(String clase) {
		Collection<String> list = new LinkedList<String>();
		
		try {
			Class cl = Class.forName(clase);
			while (cl != null) {
				list.add(cl.getName());
				Class[] ints = cl.getInterfaces();
				for (int i = 0; i < ints.length; i++) {
					list.add(ints[i].getName());
				}
	
				cl = cl.getSuperclass();
			}
		} catch (ClassNotFoundException e) {
		}
		finally {
			return list;
		}
	}
	
	public static void main (String[] args) {
		System.out.println(padresDeClase("java.util.LinkedList"));
	}
}