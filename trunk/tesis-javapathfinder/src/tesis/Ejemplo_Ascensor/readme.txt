Revisión 356:
  + Al ejecutar la verificación de la propiedad “El ascensor no puede arrancar si no cerró la puerta” hay dos problemas:
      + El primero es que el llamado al métododo “cerrarPuertas” esta dentro del método 
        arrancar (y por ende, siempre arranca antes de cerrar las puertas). 
        Para que ande bien, hay que poner el llamado en el método atender solicitud.
      + Da un deadlock al ejecutar la verificación. No sabemos aún no sabemos bien porque es.
      