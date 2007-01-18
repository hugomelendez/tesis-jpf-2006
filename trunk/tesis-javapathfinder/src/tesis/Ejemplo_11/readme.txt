En esta version intentamos escribir otro TypeStateProp que se aplique tanto 
a una clase concreta (ConcreteFather) como a sus subclases (ConcreteChild{1,2})

Verificamos que todos los metodos que son visibles al JPF se realizan sobre las objetos
de las clases instanciadas (sobre las que se hizo "new xxx"), entonces

		ConcreteChild1 c1 = new ConcreteChild1();
		ConcreteChild2 c2 = new ConcreteChild2();
		ConcreteFather f1 = new ConcreteFather();
		ConcreteFather f2 = new ConcreteChild1();

		c1.add();	// instruccion observada -> ConcreteChild1.add
		c2.add();	// instruccion observada -> ConcreteChild2.add
		f1.add();	// instruccion observada -> ConcreteFather.add
		f2.add();	// instruccion observada -> ConcreteChild1.add

Se puede implementar en la extension de nuestra tesis, que busque en la jerarquia de los objetos
la pertenencia a las superclasses o interfaces que nos interesan (ver java.lang.Class::getClasses())

TODO: ver de juntar con el ej9