Ejemplo para verificar si existe secuencia de:
A ---C---> B

-> Utiliza un Listener con Estado (contiene Automata)
-> EventoDeEjemplo2: se utiliza para filtrar los casos en que se hace el println(X)
-> Las insn de println determinan un evento segun el Thread que la ejecuta (Th 1 -> A, Th 2 -> B, Th 3 -> C)
