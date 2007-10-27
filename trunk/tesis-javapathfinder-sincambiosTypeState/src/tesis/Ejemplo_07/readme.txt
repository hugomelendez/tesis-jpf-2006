Ejemplo que verifica, usando un AFD y un ContextoBusqueda

* Modelo:
	Construye 3 Threads que, dentro de un while true, hace un OPEN, WRITE y CLOSE de un Canal compartido

* AFD:
	Verifica que no se realice un WRITE inmediatamente luego de un CLOSE (indepte de la cant. de OPENs anteriores)

* Contexto:
	Verifica que no se hagan mas de 2 opens (porque para verificar que se viola la propiedad
	deberia alcanzar solo con 2 opens)


* Resultados:
- Se comprueba que hay que se encuentra un camino en el que se produce un WRITE luego de un CLOSE