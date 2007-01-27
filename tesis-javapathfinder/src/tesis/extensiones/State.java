package tesis.extensiones;

import java.util.Hashtable;

public class State {
	private String id;
	
	public State(int i) {
		id = new String((new Integer(i).toString()));
	}

	public State(String str) {
		id = str;
	}
	
	public String getId(){
		return id;
	}

	@Override
	public boolean equals(Object anObject) {
		if (this == anObject) return true;
		if (anObject instanceof State) {
			return (id.equals(((State)anObject).getId()));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return (id.hashCode());
	}
	
	@Override
	public String toString() {
		return id;
	}

/*	public static void main(String[] args) {
		Hashtable<State, Integer> ht = new Hashtable<State, Integer>();
		State st1 = new State("HOLA");
		State st2 = new State("HOLA");
		ht.put(st1, 0);
		
		if (ht.containsKey(st2)) System.out.println("SI!");
		if (st2.equals(st1)) System.out.println("s2 equals s1!");
		if (st1.equals(st2)) System.out.println("s1 equals s2!");
		if (st1.hashCode() == st2.hashCode()) System.out.println("hashcode igual!");

		System.out.println(st1.hashCode());
		System.out.println(st2.hashCode());


		Hashtable<String, Integer> h = new Hashtable<String, Integer>();
		String s1 = new String("HOLA");
		String s2 = new String("HOLA");
		h.put(s1, 0);
		
		if (h.containsKey(s2)) System.out.println("SI!");

		System.out.println(s1.hashCode());
		System.out.println(s2.hashCode());
	}
*/
}
