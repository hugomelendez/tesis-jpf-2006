Ejemplo para usar el Verify
La idea es hacer un ejemplo que verifique, usando un Listener con Automata, y la busqueda desarrollada por nosotros,
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


Si agregamos 2 opens antes de entrar en el loop, deberiamos estar modelando la falla correctamente. 
Pero la limitacion que tiene JPF, es que verifica la propiedad (check) en la transicion entre estados. 
En este ejemplo en particular las transiciones estan dadas en la linea del Verify.getBoolean. 
Por esto, al activar los opens comentados veremos que el camino a la falla muestra la ejecucion de 2 opens 
y al ejecutar la linea del Verify, y generar la transicion entre estados, el check falla.
Si generamos un modelo que no contenga Verify, la falla se encontraria al final del programa.


Nos planteamos generar el ejemplo para un buffer, es decir,
permitir varios OPENs (sin superar una cantidad) y varios CLOSEs.
Con este ejemplo, deberíamos estar chequeando con un Autómata de pila para el never claim.
Llegamos a la conclusión de que este tipo de casos, no se puede representar en VTS, por lo tanto queda fuera del alcance.