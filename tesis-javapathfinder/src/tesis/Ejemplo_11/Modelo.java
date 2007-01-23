package tesis.Ejemplo_11;

public class Modelo {

	public static void main(String[] args) throws ClassNotFoundException {
		ConcreteChild1 c1 = new ConcreteChild1();
		ConcreteChild2 c2 = new ConcreteChild2();
		ConcreteFather f1 = new ConcreteFather();
		ConcreteFather f2 = new ConcreteChild1();

		System.out.println("c1.add "+ c1.getClass().getName());
		c1.add();
		System.out.println("c2.add "+ c2.getClass().getName());
		c2.add();
		System.out.println("f1.add "+ f1.getClass().getName());
		f1.add();
		System.out.println("f2.add "+ f2.getClass().getName());
		f2.add();
		
		//c1.add();
	}
}
