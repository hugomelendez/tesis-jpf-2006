package tesis.extensiones;

public class State {
	private String id;
	
	public State(int i) {
		id = new String((new Integer(i).toString()));
	}

	public State(String str) {
		id = str;
	}

	public boolean equals(State s) {
		return (id.equals(s.id));
	}
}
