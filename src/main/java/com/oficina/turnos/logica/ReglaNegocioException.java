package com.oficina.turnos.logica;

/** Se lanza cuando una operacion viola una validacion o una regla del negocio. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
