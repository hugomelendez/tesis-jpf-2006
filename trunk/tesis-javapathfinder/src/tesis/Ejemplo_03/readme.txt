Ejemplo para usar el Verify
La idea es hacer un ejemplo que verifique, usando un Listener con Automata, 
que no se den:
 - 2 opens seguidos, 
 - un close antes de un open
 - 2 close seguidos

* Modelo original
while true
	open
	close

* Modelo decorado con Verify
while true
	if RandomBool
		open
	else
		close

* AFD
0 INIT
	open -> OPENED
	close -> FIN 1
1 OPENED
	close -> INIT
	open -> FIN 2
2 FIN 1 error, close antes de open o 2 close seguidos
3 FIN 2 error, 2 open seguidos

	
Nos planteamos generar el ejemplo para un buffer, es decir,
permitir varios OPENs (sin superar una cantidad) y varios CLOSEs.
Con este ejemplo, deberíamos estar chequeando con un Autómata de pila para el never claim.
Llegamos a la conclusión de que este tipo de casos, no se puede representar en VTS, por lo tanto queda fuera del alcance.