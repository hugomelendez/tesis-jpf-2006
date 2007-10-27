Revisión 356:
  + Al ejecutar la verificación de la propiedad “El ascensor no puede arrancar si no cerró la puerta” hay dos problemas:
      + El primero es que el llamado al métododo “cerrarPuertas” esta dentro del método 
        arrancar (y por ende, siempre arranca antes de cerrar las puertas). 
        Para que ande bien, hay que poner el llamado en el método atender solicitud.
      + Da un deadlock al ejecutar la verificación. No sabemos aún no sabemos bien porque es.
        (da deadlock porque la Persona ejecuta finitos movimientos dentro de edificio, luego de terminar el thread muere y el Ascensor queda en un eterno wait)

