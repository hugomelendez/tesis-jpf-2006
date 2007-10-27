Ejemplo para verificar el funcionamiento de los TypeStateProperty en una pseudo
aplicacion que utiliza Sockets e Iteradores.

El mismo fallo debido a que el modelo generado utiliza InetAddress el cual contiene metodos nativos.
Los metodos nativos (http://java.sun.com/j2se/1.5.0/docs/guide/jni/spec/intro.html) son metodos escritos en un lenguaje diferente a java y que se llaman desde el java
Esta clase de metodos no esta soportado por JPF.
