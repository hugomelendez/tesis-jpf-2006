En esta version intentamos escribir otro TypeStateProp que verificara que, sobre
ningun iterador, se realizara un next() antes de un hasNext()

El principal inconveniente con esto es que en JPF no podemos observar propiedades sobre
clases abstractas o interfaces (como es Iterator). Por lo tanto esta propiedad la debemos
escribir asociandola a la clase concreta que implenta el tipo (en este caso, LinkedList$ListItr)

El sig. inconveniente es que aun cuando observamos la ejecucion sobre estas clases concretas,
el JPF no esta detectando explicitamente la invocacion de next y hasNext (solo observamos
los (a|i)return )

Mas datos, el add de la Collection tampoco es observable, pq esta es Abstracta, 
sin embargo se puede ver el ireturn del LinkedList.add, con lo q podemos conjeturar q la invocacion no es capturable y el return del codigo concreto si.

Al cambiar el add utilizado (Collection.add) por un LinkedList.add observamos que nuestra conejtura es cierta.

Por ahora no podemos trabajar con next y hasNext por ser de una clase privada a LinkedList (LinkedList$ListItr)

