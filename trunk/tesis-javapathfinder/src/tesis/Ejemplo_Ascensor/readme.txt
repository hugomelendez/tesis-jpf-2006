RS: Modelo utilizado para ejemplificar en la monografía, la aplicación de Escenarios de Control

Propiedades que vamos a chequear

1) El asensor no puede arrancar si no cerro la puerta

2) "Salir de piso", "abrir puerta" es una combinación que no se puede dar.

3) Siempre que llega tiene que abrir la puerta y despues cerrarla

4) El asensor no puede estar en dos pisos al mismo tiempo

5) Siempre que va de n a m, tiene que pasar por los pisos del medio en orden (en principio no)

6) Si del ante ultimo piso va hacia arriba, entonces tiene que ir al último

7) Las puertas del piso x se abren solo cuando el elevador esta en ese piso

8) Si una persona va del primero al quinto, tiene que haber 4 eventos "Pasa Por", un evento abrir y un evento cerrar

9) Si hay una persona en el piso x y el asensor esta en el piso x, entonces, la persona llama y el siguiente evento es "se abren las puertas"

10) Todo pedido tiene que tener una respuesta (llegar a piso). Necesitamos un modelo finito para poder tener un evento termino.

11) Un pedido en un piso, se considera satisfecho si el asensor esta parado en el piso y las puertas estan abiertas

12) No puede haber dos pedidos pendientes en una misma direccion en un mismo piso al mismo tiempo
