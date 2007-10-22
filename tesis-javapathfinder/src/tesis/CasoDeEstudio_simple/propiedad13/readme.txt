Llega una solicitud a piso 1 para el unico ascensor
El contexto debe ser que exista una "solicitud a piso 1"

La propiedad esta representada por el sgte AFD
 init -> estoyEnPiso1
 estoyEnPiso1 -> {* - atendersolicitud} -> ERR
 estoyEnPiso1 -> atendersolicitud -> init

coloquialmente, la propiedad se invalida si luego de un evento estoyEnPiso1 no se atiende la solicitud (viene cualquier evento que no sea atenderSolicitud)
