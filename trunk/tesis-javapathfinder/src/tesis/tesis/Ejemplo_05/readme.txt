La idea es hacer un ejemplo que verifique, usando un Listener con Automata, 
que no cumpla:
 - cantidad de opens consecutivos (sin close intermedio) MAYOR a la cant. de threads
 - cantidad de close consecutivos mayor a la cantidad de open

* Modelo:
Construye 3 Threads que, dentro de un while true, hace un OPEN y CLOSE de un Canal compartido

* AFD:
0
 open -> 1
 close -> 999
1
 open -> 2
 close -> 0
2
 open -> 3
 close -> 1
3
 open -> 999
 close -> 2
999 error (estado final)


* Resultados:
- Se comprueba que, dado que un thread aislado no ejecuta 2 OPEN sin haber hecho un CLOSE intermedio,
la aplicación no viola la propiedad del AFD.
- Se realizó una modificación al AFD para permitir sólo una cant. OPEN=2. Se encuentra el PATH del error.
