Revisi�n 356:
  + Al ejecutar la verificaci�n de la propiedad �El ascensor no puede arrancar si no cerr� la puerta� hay dos problemas:
      + El primero es que el llamado al m�tododo �cerrarPuertas� esta dentro del m�todo 
        arrancar (y por ende, siempre arranca antes de cerrar las puertas). 
        Para que ande bien, hay que poner el llamado en el m�todo atender solicitud.
      + Da un deadlock al ejecutar la verificaci�n. No sabemos a�n no sabemos bien porque es.
        (da deadlock porque la Persona ejecuta finitos movimientos dentro de edificio, luego de terminar el thread muere y el Ascensor queda en un eterno wait)

